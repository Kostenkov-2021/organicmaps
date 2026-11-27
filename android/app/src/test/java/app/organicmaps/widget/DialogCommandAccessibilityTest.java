package app.organicmaps.widget;

import static org.junit.Assert.*;

import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import app.organicmaps.R;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class, sdk = {23, 35}, qualifiers = "ru-w600dp-h1000dp-mdpi")
public class DialogCommandAccessibilityTest
{
  @Test
  public void eachVisibleChoiceIsAButtonAndRetainsItsCallback()
  {
    Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
    activity.setTheme(R.style.MwmTheme);
    int[] ids = {R.id.btn__positive, R.id.btn__neutral, R.id.btn__negative};
    int[] expected = {DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEUTRAL, DialogInterface.BUTTON_NEGATIVE};
    for (int i = 0; i < ids.length; ++i)
    {
      AtomicInteger clicked = new AtomicInteger();
      DialogInterface.OnClickListener listener = (dialog, which) -> clicked.set(which);
      var dialog = new StackedButtonsDialog.Builder(activity)
                       .setPositiveButton(R.string.mobile_data_option_always, listener)
                       .setNeutralButton(R.string.mobile_data_option_today, listener)
                       .setNegativeButton(R.string.mobile_data_option_not_today, listener)
                       .build();
      dialog.show();
      ShadowLooper.idleMainLooper();
      View decor = dialog.getWindow().getDecorView();
      decor.measure(View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST));
      decor.layout(0, 0, 600, decor.getMeasuredHeight());
      View button = dialog.findViewById(ids[i]);
      assertEquals(Button.class.getName(), button.createAccessibilityNodeInfo().getClassName().toString());
      assertTrue(button.isFocusable());
      assertTrue(
          button.performAccessibilityAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK, null));
      assertEquals(expected[i], clicked.get());
      assertFalse(dialog.isShowing());
    }
  }

  @Test
  public void anAbsentNeutralChoiceStaysHidden()
  {
    Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
    activity.setTheme(R.style.MwmTheme);
    var dialog = new StackedButtonsDialog.Builder(activity).build();
    dialog.show();
    assertEquals(View.GONE, dialog.findViewById(R.id.btn__neutral).getVisibility());
    dialog.dismiss();
  }
}
