package euphoria.psycho.knife;

import android.content.Context;
import android.util.AttributeSet;

import java.util.List;

import euphoria.psycho.common.widget.selection.SelectableListToolbar;

public class DirectoryToolbar extends SelectableListToolbar<DocumentInfo> {

    public DirectoryToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflateMenu(R.menu.options_directory);
    }

    @Override
    public void onSelectionStateChange(List<DocumentInfo> selectedItems) {
        super.onSelectionStateChange(selectedItems);
    }

}
