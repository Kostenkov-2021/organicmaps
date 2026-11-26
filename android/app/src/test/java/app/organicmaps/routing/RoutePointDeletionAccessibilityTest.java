package app.organicmaps.routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.widget.ImageButton;
import androidx.recyclerview.widget.RecyclerView;
import app.organicmaps.R;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class, sdk = {23, 35})
public class RoutePointDeletionAccessibilityTest
{
  @Test
  public void labelTracksMovesRenamesAndUnnamedRowsWithoutChangingTheClickHandler()
  {
    Activity activity = Robolectric.buildActivity(Activity.class).setup().visible().get();
    Resources original = activity.getResources();
    Resources resources =
        new Resources(original.getAssets(), original.getDisplayMetrics(), original.getConfiguration()) {
          @Override
          public String getString(int id, Object... arguments)
          {
            if (id == R.string.route_point_delete)
              return String.format(Locale.US, "Удалить точку маршрута %d", arguments);
            if (id == R.string.route_point_delete_named)
              return String.format(Locale.US, "Удалить точку маршрута %1$d: %2$s", arguments);
            return super.getString(id, arguments);
          }
        };
    Context context = new ContextWrapper(activity) {
      @Override
      public Resources getResources()
      {
        return resources;
      }
    };
    ImageButton button = new ImageButton(context);
    activity.setContentView(button);
    button.setContentDescription("Удалить");
    AtomicInteger position = new AtomicInteger(0);
    AtomicReference<CharSequence> title = new AtomicReference<>("Невский проспект");
    AtomicInteger clicks = new AtomicInteger();
    button.setOnClickListener(v -> clicks.incrementAndGet());
    RoutePointDeletionAccessibility.attach(button, position::get, title::get);
    assertEquals("Удалить точку маршрута 1: Невский проспект",
                 button.createAccessibilityNodeInfo().getContentDescription());
    position.set(3);
    assertEquals("Удалить точку маршрута 4: Невский проспект",
                 button.createAccessibilityNodeInfo().getContentDescription());
    title.set("");
    assertEquals("Удалить точку маршрута 4", button.createAccessibilityNodeInfo().getContentDescription());
    position.set(RecyclerView.NO_POSITION);
    assertEquals("Удалить", button.createAccessibilityNodeInfo().getContentDescription());
    assertTrue(button.performClick());
    assertEquals(1, clicks.get());
  }
}
