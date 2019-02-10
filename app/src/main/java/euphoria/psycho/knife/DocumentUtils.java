package euphoria.psycho.knife;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.text.Selection;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.documentfile.provider.DocumentFile;
import euphoria.psycho.common.C;
import euphoria.psycho.common.Log;
import euphoria.psycho.common.StorageUtils;
import euphoria.psycho.common.StringUtils;
import euphoria.psycho.common.ThreadUtils;
import euphoria.psycho.common.base.Job;
import euphoria.psycho.common.base.Job.Listener;
import euphoria.psycho.common.widget.KeyboardVisibilityDelegate;
import euphoria.psycho.common.widget.selection.SelectionDelegate;


public class DocumentUtils {
    private static final String TAG = "TAG/" + DocumentUtils.class.getSimpleName();

    static {
        System.loadLibrary("native-lib");
    }

    public static void buildDeleteDialog(Context context, Consumer<Boolean> callback, DocumentInfo... documentInfos) {


        String description = documentInfos[0].getFileName();

        if (documentInfos.length > 1) {
            description += " 等 " + documentInfos.length + " 个文件";
        }

        AlertDialog dlg = new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(context.getString(R.string.dialog_delete_message, description))
                .setTitle(R.string.dialog_rename_title)
                .setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                    dialog.dismiss();
                    ThreadUtils.postOnBackgroundThread(new DeleteFileJob(context, new Listener() {
                        @Override
                        public void onFinished(Job job) {
                            ThreadUtils.postOnUiThread(() -> callback.accept(true));

                        }

                        @Override
                        public void onStart(Job job) {

                        }
                    }, documentInfos));

                }))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss()).create();

        dlg.show();
    }

    public static void buildRenameDialog(Context context, String originalFileName, Consumer<CharSequence> callback) {
        EditText editText = new EditText(context);
        if (originalFileName != null) {
            editText.setText(originalFileName);
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex != -1) {
                editText.setSelection(0, dotIndex);
            }
        }

        AlertDialog dlg = new AlertDialog.Builder(context)
                .setView(editText)
                .setTitle(R.string.dialog_rename_title)
                .setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                    dialog.dismiss();
                    callback.accept(editText.getText());
                }))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss()).create();
        editText.requestFocus();
        dlg.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dlg.show();
    }

    public static native long calculateDirectory(String dir);

    public static native int deleteDirectories(String[] directories);

    public static List<DocumentInfo> getDocumentInfos(File dir, int sortBy) {

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) return null;

        List<DocumentInfo> infos = new ArrayList<>();
        boolean isAscending = false;

        for (File file : files) {
            DocumentInfo.Builder builder = new DocumentInfo.Builder()
                    .setFileName(file.getName())
                    .setLastModified(file.lastModified())
                    .setPath(file.getAbsolutePath())
                    .setType(getType(file));

            if (file.isDirectory()) {
                File[] fs = file.listFiles();
                builder.setSize(fs == null ? 0 : fs.length);
            } else {
                builder.setSize(file.length());
            }


            infos.add(builder.build());
        }

        switch (sortBy) {
            case C.SORT_BY_NAME:
                Collator collator = Collator.getInstance(Locale.CHINA);
                Collections.sort(infos, (o1, o2) -> {
                    boolean b1 = o1.getType() == C.TYPE_DIRECTORY;
                    boolean b2 = o2.getType() == C.TYPE_DIRECTORY;
                    if (b1 == b2)
                        if (isAscending)
                            return collator.compare(o1.getFileName(), o2.getFileName());
                        else
                            return collator.compare(o1.getFileName(), o2.getFileName()) * -1;
                    else if (b1) {

                        if (isAscending) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else {
                        if (isAscending) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                });
                break;
            case C.SORT_BY_SIZE:
                Collections.sort(infos, (o1, o2) -> {
                    boolean b1 = o1.getType() == C.TYPE_DIRECTORY;
                    boolean b2 = o2.getType() == C.TYPE_DIRECTORY;


                    // o1.getSize() >= o2.getSize() ? -1 : 1
                    if (b1 == b2) {
                        if (isAscending)
                            return o1.getSize() >= o2.getSize() ? 1 : -1;
                        else return o1.getSize() <= o2.getSize() ? 1 : -1;
                    } else if (b1) {
                        if (isAscending) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else {
                        if (isAscending) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                });
                break;
            case C.SORT_BY_DATE_MODIFIED:

                Collections.sort(infos, (o1, o2) -> {
                    boolean b1 = o1.getType() == C.TYPE_DIRECTORY;
                    boolean b2 = o2.getType() == C.TYPE_DIRECTORY;

                    if (b1 == b2) {
                        if (isAscending)
                            return o1.getLastModified() >= o2.getLastModified() ? 1 : -1;
                        else
                            return o1.getLastModified() <= o2.getLastModified() ? 1 : -1;

                    } else if (b1) {
                        if (isAscending) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else {
                        if (isAscending) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                });
                break;
        }

        return infos;
    }

    public static ColorStateList getIconForegroundColorList(Context context) {
        return AppCompatResources.getColorStateList(context, R.color.white_mode_tint);
    }

    public static int getType(File file) {
        if (file.isDirectory()) return C.TYPE_DIRECTORY;
        String ext = StringUtils.substringAfterLast(file.getName(), ".");
        if (ext == null) return C.TYPE_OTHER;
        ext = ext.toLowerCase();
        switch (ext) {
            case "mp3":
                return C.TYPE_AUDIO;
            case "mp4":
                return C.TYPE_VIDEO;
            case "txt":
            case "css":
            case "log":
            case "js":
            case "htm":
            case "srt":
                return C.TYPE_TEXT;
            case "pdf":
                return C.TYPE_PDF;
            case "apk":
                return C.TYPE_APK;
            case "zip":
            case "rar":
            case "gz":
                return C.TYPE_ZIP;
            default:
                return C.TYPE_OTHER;
        }
    }

    static void selectSameTypes(SelectionDelegate<DocumentInfo> delegate, DocumentsAdapter adapter) {
        

        List<DocumentInfo> infos = delegate.getSelectedItemsAsList();
        if (infos.size() < 1) return;
        String extension = StringUtils.substringAfterLast(infos.get(0).getFileName(), ".");
        if (extension == null) return;
        List<DocumentInfo> infoList = adapter.getInfos();

        Set<DocumentInfo> documentInfoSet = new HashSet<>();
        for (DocumentInfo info : infoList) {
            if (info.getFileName().endsWith(extension)) {
                documentInfoSet.add(info);


            }
        }
        delegate.setSelectedItems(documentInfoSet);
    }

    interface Consumer<T> {
        void accept(T t);
    }
}
