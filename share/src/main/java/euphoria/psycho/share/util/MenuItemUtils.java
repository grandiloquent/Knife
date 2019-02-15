package euphoria.psycho.share.util;

import android.view.Menu;
import android.view.MenuItem;

import euphoria.psycho.share.R;


public class MenuItemUtils {

    public static final int MENU_SHARE = 0;
    public static final int MENU_DELETE = 1;

    public static MenuItem addShareMenuItem(Menu menu) {
        MenuItem menuItem = menu.add(0, MENU_SHARE, 0, R.string.share);
        menuItem.setIcon(R.drawable.ic_share_white_24px);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return menuItem;
    }

    public static MenuItem addDeleteMenuItem(Menu menu) {
        MenuItem menuItem = menu.add(0, MENU_DELETE, 0, R.string.delete);
        menuItem.setIcon(R.drawable.ic_delete_forever_white_24px);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return menuItem;
    }
}
