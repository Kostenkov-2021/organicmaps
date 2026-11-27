package app.organicmaps.bookmarks;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.app.Activity;
import android.app.Application;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;
import app.organicmaps.R;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(application = Application.class, sdk = {23, 35}, qualifiers = "ru")
public class BookmarkHeaderCommandAccessibilityTest
{
  @Test
  public void reusedHeaderKeepsTheCurrentLabelAndAction()
  {
    Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
    activity.setTheme(R.style.MwmTheme);
    FrameLayout host = new FrameLayout(activity);
    activity.setContentView(host);
    var row = LayoutInflater.from(activity).inflate(R.layout.item_bookmark_group_list_header, host, false);
    host.addView(row);
    var holder = new Holders.HeaderViewHolder(row);
    var action = mock(Holders.HeaderViewHolder.HeaderAction.class);
    for (boolean show : new boolean[] {true, false})
    {
      holder.setAction(action, show);
      var button = holder.getButton();
      assertEquals(Button.class.getName(), button.createAccessibilityNodeInfo().getClassName().toString());
      assertEquals(activity.getString(show ? R.string.bookmark_lists_show_all : R.string.bookmark_lists_hide_all),
                   button.getText().toString());
      assertTrue(button.isFocusable());
      assertTrue(button.performClick());
    }
    verify(action).onShowAll();
    verify(action).onHideAll();
  }
}
