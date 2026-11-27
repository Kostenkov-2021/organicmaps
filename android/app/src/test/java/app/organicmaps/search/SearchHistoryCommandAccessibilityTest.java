package app.organicmaps.search;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.app.Activity;
import android.app.Application;
import android.widget.Button;
import android.widget.FrameLayout;
import androidx.core.view.ViewCompat;
import app.organicmaps.R;
import app.organicmaps.sdk.search.SearchRecents;
import app.organicmaps.widget.SearchToolbarController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class, sdk = {23, 35})
public class SearchHistoryCommandAccessibilityTest
{
  @Test
  public void clearIsAButtonWhileQueriesRemainOrdinaryRows()
  {
    Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
    activity.setTheme(R.style.MwmTheme);
    FrameLayout host = new FrameLayout(activity);
    activity.setContentView(host);
    try (var recents = mockStatic(SearchRecents.class))
    {
      var adapter = new SearchHistoryAdapter(mock(SearchToolbarController.class));
      var clearHolder = adapter.onCreateViewHolder(host, 1);
      var clear = clearHolder.itemView;
      host.addView(clear);
      adapter.onBindViewHolder(clearHolder, 0);
      assertEquals(Button.class.getName(), clear.createAccessibilityNodeInfo().getClassName().toString());
      assertTrue(clear.isFocusable());
      // RecyclerView may discard the delegate when a row is pooled with accessibility off.
      ViewCompat.setAccessibilityDelegate(clear, null);
      adapter.onBindViewHolder(clearHolder, 0);
      assertEquals(Button.class.getName(), clear.createAccessibilityNodeInfo().getClassName().toString());
      assertTrue(clear.performClick());
      recents.verify(SearchRecents::clear);
      var query = adapter.onCreateViewHolder(host, 0).itemView;
      host.addView(query);
      assertNotEquals(Button.class.getName(), query.createAccessibilityNodeInfo().getClassName().toString());
    }
  }
}
