package app.organicmaps.bookmarks;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

/** Keeps the description's accessibility target present while the visual More control disappears. */
final class BookmarkDescriptionAccessibility extends AccessibilityDelegateCompat
{
  private final View mRoot;
  private final TextView mTitle;
  private final TextView mText;
  private final View mMore;
  private boolean mCanExpand;
  private boolean mExpanded;

  BookmarkDescriptionAccessibility(View root, TextView title, TextView text, View more)
  {
    mRoot = root;
    mTitle = title;
    mText = text;
    mMore = more;
    ViewCompat.setAccessibilityDelegate(root, this);
    ViewCompat.setScreenReaderFocusable(root, true);
    root.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    for (View child : new View[] {title, text, more})
    {
      child.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
      child.setFocusable(false);
    }
    root.setOnClickListener(v -> toggle());
    more.setOnClickListener(v -> root.performClick());
    text.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> refresh());
    updatePresentation();
  }

  void bind()
  {
    // Reinstall after RecyclerView restores the delegate recorded before the row was pooled.
    ViewCompat.setAccessibilityDelegate(mRoot, this);
    mExpanded = false;
    mCanExpand = false;
    CharSequence title = mTitle.getText();
    CharSequence text = mText.getText();
    mRoot.setContentDescription(TextUtils.isEmpty(text) ? title : TextUtils.concat(title, "\n", text));
    updatePresentation();
    mRoot.post(this::refresh);
  }

  private void refresh()
  {
    var layout = mText.getLayout();
    boolean canExpand = mText.getVisibility() == View.VISIBLE && layout != null && layout.getLineCount() > 0
                     && (layout.getLineCount() > BookmarkListAdapter.MAX_VISIBLE_LINES
                         || layout.getEllipsisCount(layout.getLineCount() - 1) > 0);
    boolean changed = canExpand != mCanExpand;
    mCanExpand = canExpand;
    if (!canExpand)
    {
      changed |= mExpanded;
      mExpanded = false;
    }
    updatePresentation();
    if (changed)
      mRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
  }

  private void toggle()
  {
    if (!mRoot.isEnabled() || !mCanExpand)
      return;
    mExpanded = !mExpanded;
    updatePresentation();
    // Expansion actions supply the spoken state; a state description would duplicate it.
    mRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
  }

  private void updatePresentation()
  {
    int lines = mExpanded ? Integer.MAX_VALUE : BookmarkListAdapter.MAX_VISIBLE_LINES;
    if (mText.getMaxLines() != lines)
      mText.setMaxLines(lines);
    mMore.setVisibility(mCanExpand && !mExpanded ? View.VISIBLE : View.GONE);
    mRoot.setClickable(mCanExpand);
    mRoot.setFocusable(mCanExpand);
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info)
  {
    super.onInitializeAccessibilityNodeInfo(host, info);
    if (!mCanExpand)
      return;
    info.setClassName(Button.class.getName());
    if (host.isEnabled())
      info.addAction(mExpanded ? AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_COLLAPSE
                               : AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_EXPAND);
  }

  @Override
  public boolean performAccessibilityAction(View host, int action, Bundle arguments)
  {
    if (action != AccessibilityNodeInfoCompat.ACTION_EXPAND && action != AccessibilityNodeInfoCompat.ACTION_COLLAPSE
        && action != AccessibilityNodeInfoCompat.ACTION_CLICK)
      return super.performAccessibilityAction(host, action, arguments);
    if (!host.isEnabled() || !mCanExpand)
      return false;
    if (action != AccessibilityNodeInfoCompat.ACTION_CLICK
        && (action == AccessibilityNodeInfoCompat.ACTION_EXPAND) == mExpanded)
      return false;
    return host.performClick();
  }
}
