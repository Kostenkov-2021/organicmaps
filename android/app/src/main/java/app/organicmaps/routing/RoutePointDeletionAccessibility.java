package app.organicmaps.routing;

import android.text.TextUtils;
import android.view.View;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.recyclerview.widget.RecyclerView;
import app.organicmaps.R;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

final class RoutePointDeletionAccessibility
{
  static void attach(View view, IntSupplier position, Supplier<CharSequence> title)
  {
    ViewCompat.setAccessibilityDelegate(view, new AccessibilityDelegateCompat() {
      @Override
      public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info)
      {
        super.onInitializeAccessibilityNodeInfo(host, info);
        // A move updates adapter positions without necessarily rebinding every affected holder.
        int index = position.getAsInt();
        if (index == RecyclerView.NO_POSITION)
          return;
        CharSequence name = title.get();
        info.setContentDescription(
            TextUtils.isEmpty(name) ? host.getContext().getString(R.string.route_point_delete, index + 1)
                                    : host.getContext().getString(R.string.route_point_delete_named, index + 1, name));
      }
    });
  }

  private RoutePointDeletionAccessibility() {}
}
