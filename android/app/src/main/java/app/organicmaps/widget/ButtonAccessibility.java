package app.organicmaps.widget;

import android.view.View;
import android.widget.Button;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

/** Gives text-based commands native button semantics without changing their appearance. */
public final class ButtonAccessibility
{
  private static final AccessibilityDelegateCompat BUTTON = new AccessibilityDelegateCompat() {
    @Override
    public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info)
    {
      super.onInitializeAccessibilityNodeInfo(host, info);
      info.setClassName(Button.class.getName());
    }
  };

  public static void attach(View view)
  {
    view.setFocusable(true);
    ViewCompat.setAccessibilityDelegate(view, BUTTON);
  }

  private ButtonAccessibility() {}
}
