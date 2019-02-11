package euphoria.psycho.knife.download;

import android.content.Context;
import android.util.AttributeSet;

import euphoria.psycho.common.widget.selection.SelectableListToolbar;
import euphoria.psycho.knife.R;

public class DownloadToolbar extends SelectableListToolbar {

    public DownloadToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflateMenu(R.menu.options_download);
    }
}
