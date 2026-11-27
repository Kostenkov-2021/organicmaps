package app.organicmaps.bookmarks;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import app.organicmaps.R;
import app.organicmaps.sdk.bookmarks.data.BookmarkCategory;
import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.shadows.ShadowLooper;

@RunWith(RobolectricTestRunner.class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application.class, sdk = 35, qualifiers = "w600dp-h1000dp-mdpi")
public class BookmarkDescriptionAccessibilityTest
{
  private FrameLayout mHost;
  private View mRoot;
  private TextView mText;
  private View mMore;
  private Holders.BaseBookmarkHolder mHolder;
  private BookmarkCategory mCategory;
  private BookmarkListAdapter.SectionsDataSource mSections;

  @Before
  public void setUp() throws Exception
  {
    Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
    activity.setTheme(R.style.MwmTheme);
    var manager = (AccessibilityManager) activity.getSystemService(Context.ACCESSIBILITY_SERVICE);
    Shadows.shadowOf(manager).setEnabled(true);
    Shadows.shadowOf(manager).setTouchExplorationEnabled(true);
    mHost = new FrameLayout(activity);
    activity.setContentView(mHost);
    var adapter = mock(BookmarkListAdapter.class, CALLS_REAL_METHODS);
    mSections = mock(BookmarkListAdapter.SectionsDataSource.class);
    mCategory = mock(BookmarkCategory.class);
    when(mSections.getCategory()).thenReturn(mCategory);
    Field field = BookmarkListAdapter.class.getDeclaredField("mSectionsDataSource");
    field.setAccessible(true);
    field.set(adapter, mSections);
    mHolder = adapter.onCreateViewHolder(mHost, BookmarkListAdapter.TYPE_DESC);
    mRoot = mHolder.itemView;
    mText = mRoot.findViewById(R.id.text);
    mMore = mRoot.findViewById(R.id.more_btn);
    mHost.addView(mRoot);
  }

  private void bind(String title, String description, int width)
  {
    when(mCategory.getName()).thenReturn(title);
    when(mCategory.getAnnotation()).thenReturn("");
    when(mCategory.getDescription()).thenReturn(description);
    mHolder.bind(mock(SectionPosition.class), mSections);
    layout(width);
  }

  private void layout(int width)
  {
    mHost.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                  View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY));
    mHost.layout(0, 0, width, 1000);
    ShadowLooper.idleMainLooper();
    mHost.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                  View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY));
    mHost.layout(0, 0, width, 1000);
  }

  private AccessibilityNodeInfoCompat node()
  {
    return AccessibilityNodeInfoCompat.wrap(mRoot.createAccessibilityNodeInfo());
  }

  private boolean has(int action)
  {
    return node().getActionList().stream().anyMatch(item -> item.getId() == action);
  }

  @Test
  public void expansionKeepsTheFocusedNodeAndUsesOnlyStandardSpokenState()
  {
    bind("My list", "First paragraph\nSecond paragraph\nThird paragraph\nFourth paragraph", 300);
    assertEquals(Button.class.getName(), node().getClassName().toString());
    assertTrue(has(AccessibilityNodeInfoCompat.ACTION_EXPAND));
    assertNull(node().getStateDescription());
    assertTrue(mRoot.performAccessibilityAction(AccessibilityNodeInfoCompat.ACTION_ACCESSIBILITY_FOCUS, null));
    assertTrue(mRoot.performAccessibilityAction(AccessibilityNodeInfoCompat.ACTION_EXPAND, null));
    layout(300);
    assertEquals(View.GONE, mMore.getVisibility());
    assertTrue(mRoot.isAccessibilityFocused());
    assertTrue(has(AccessibilityNodeInfoCompat.ACTION_COLLAPSE));
    assertNull(node().getStateDescription());
    assertTrue(node().getContentDescription().toString().contains("Fourth paragraph"));
    assertFalse(mRoot.performAccessibilityAction(AccessibilityNodeInfoCompat.ACTION_EXPAND, null));
    assertTrue(mRoot.performAccessibilityAction(AccessibilityNodeInfoCompat.ACTION_COLLAPSE, null));
    layout(300);
    assertTrue(mRoot.isAccessibilityFocused());
    assertTrue(has(AccessibilityNodeInfoCompat.ACTION_EXPAND));
    for (int id : new int[] {R.id.title, R.id.text, R.id.more_btn})
    {
      View child = mRoot.findViewById(id);
      assertEquals(View.IMPORTANT_FOR_ACCESSIBILITY_NO, child.getImportantForAccessibility());
      assertFalse(child.isFocusable());
    }
  }

  @Test
  public void rebindClearsStaleContentAndExpansion()
  {
    bind("Old list", "Line one\nLine two\nLine three\nLine four", 300);
    assertTrue(mRoot.performAccessibilityAction(AccessibilityNodeInfoCompat.ACTION_EXPAND, null));
    bind("New list", "Short", 600);
    assertEquals(BookmarkListAdapter.MAX_VISIBLE_LINES, mText.getMaxLines());
    assertFalse(has(AccessibilityNodeInfoCompat.ACTION_EXPAND));
    assertFalse(has(AccessibilityNodeInfoCompat.ACTION_COLLAPSE));
    assertFalse(node().getContentDescription().toString().contains("Old"));
    assertTrue(node().getContentDescription().toString().contains("New list"));
    assertFalse(mRoot.performAccessibilityAction(AccessibilityNodeInfoCompat.ACTION_CLICK, null));
    ViewCompat.setAccessibilityDelegate(mRoot, null);
    bind("Long again", "Line one\nLine two\nLine three\nLine four", 300);
    assertEquals(Button.class.getName(), node().getClassName().toString());
    assertTrue(has(AccessibilityNodeInfoCompat.ACTION_EXPAND));
  }

  @Test
  public void anEmptyDescriptionStillNamesTheListWithoutAnAction()
  {
    bind("Empty list", "", 300);
    assertEquals("Empty list", node().getContentDescription().toString());
    assertFalse(has(AccessibilityNodeInfoCompat.ACTION_EXPAND));
    assertEquals(View.GONE, mText.getVisibility());
    assertEquals(View.GONE, mMore.getVisibility());
  }

  @Test
  public void resizingReevaluatesOverflowAndDisabledActionsAreRejected()
  {
    bind("List", "An ordinary description with several words on a single line.", 160);
    assertTrue(has(AccessibilityNodeInfoCompat.ACTION_EXPAND));
    mRoot.setEnabled(false);
    assertFalse(mRoot.performAccessibilityAction(AccessibilityNodeInfoCompat.ACTION_EXPAND, null));
    assertFalse(has(AccessibilityNodeInfoCompat.ACTION_EXPAND));
    mRoot.setEnabled(true);
    assertTrue(mMore.performClick());
    assertEquals(Integer.MAX_VALUE, mText.getMaxLines());
    layout(600);
    assertFalse(has(AccessibilityNodeInfoCompat.ACTION_COLLAPSE));
    assertFalse(mRoot.isClickable());
  }
}
