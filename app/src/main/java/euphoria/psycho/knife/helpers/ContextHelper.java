package euphoria.psycho.knife.helpers;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import euphoria.psycho.common.C;
import euphoria.psycho.common.widget.ListMenuButton.Item;
import euphoria.psycho.knife.DocumentInfo;
import euphoria.psycho.knife.R;

public class ContextHelper {
    public static Item[] generateListMenu(Context context, DocumentInfo documentInfo) {
        List<Item> items = new ArrayList<>();

        items.add(new Item(context, R.string.rename, true));
        items.add(new Item(context, R.string.delete, true));
        items.add(new Item(context, R.string.share, true));
        items.add(new Item(context, R.string.properties, true));
        items.add(new Item(context, R.string.copy_file_name, true));
        items.add(new Item(context, R.string.add_to_archive, true));

        switch (documentInfo.getType()) {
            case C.TYPE_APK:
            case C.TYPE_OTHER:
            case C.TYPE_WORD:
            case C.TYPE_AUDIO:
            case C.TYPE_EXCEL:
            case C.TYPE_PDF: {
                break;
            }
            case C.TYPE_DIRECTORY: { // 文件夹

                items.add(new Item(context, R.string.add_bookmark, true));
                break;
            }
            case C.TYPE_TEXT: {
                items.add(new Item(context, R.string.copy_content, true));
                break;
            }
            case C.TYPE_VIDEO: {// 视频
                items.add(new Item(context, R.string.trim_video, true));
                break;
            }
            case C.TYPE_ZIP: {
                items.add(new Item(context, R.string.extract, true));
                break;
            }
        }

        return items.toArray(new Item[0]);
    }
}
