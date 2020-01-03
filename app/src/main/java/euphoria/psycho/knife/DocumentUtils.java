package euphoria.psycho.knife;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import euphoria.common.Files;
import euphoria.common.Strings;
import euphoria.psycho.common.C;
import euphoria.psycho.common.base.Job;
import euphoria.psycho.common.base.Job.Listener;
import euphoria.psycho.common.widget.selection.SelectionDelegate;
import euphoria.psycho.knife.helpers.FileHelper;
import euphoria.psycho.knife.video.VideoActivity;
import euphoria.psycho.share.util.ClipboardUtils;
import euphoria.psycho.share.util.CollectionUtils;
import euphoria.psycho.share.util.ContextUtils;
import euphoria.psycho.share.util.DialogUtils;
import euphoria.psycho.share.util.DialogUtils.DialogListener;
import euphoria.psycho.share.util.FileUtils;
import euphoria.psycho.share.util.MimeUtils;
import euphoria.psycho.share.util.ThreadUtils;

import static euphoria.psycho.knife.helpers.FileHelper.getType;


public class DocumentUtils {

    private static String mTreeUri;

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
                .setTitle(R.string.dialog_delete_title)
                .setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                    dialog.dismiss();
                    FileHelper.deleteDocuments(context, documentInfos);
                    callback.accept(true);

                }))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss()).create();

        dlg.show();
    }


    public static void buildNewDirectoryDialog(Context context, DialogListener<CharSequence> listener) {

        EditText editText = new EditText(context);
        editText.setMaxLines(1);
        editText.setHint(context.getString(euphoria.psycho.share.R.string.hint_new_folder_hint));
        editText.requestFocus();

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(euphoria.psycho.share.R.string.dialog_title_new_folder)
                .setView(editText)
                .setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                    dialog.dismiss();
                    if (listener != null) {
                        listener.ok(editText.getText());
                    }
                }))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                    if (listener != null) listener.cancel();
                });
        AlertDialog dialog = builder.create();
        DialogUtils.showKeyboard(dialog);
        dialog.show();


//        EditText editText = new EditText(context);
//
//        AlertDialog dialog = new AlertDialog.Builder(context)
//                .setView(editText)
//                .setPositiveButton(android.R.string.ok, ((dialog1, which) -> {
//                    dialog1.dismiss();
//                    CharSequence newName = editText.getText();
//                    if (newName == null) return;
//
//                    String fileName = FileUtils.getValidFilName(newName.toString(), ' ');
//                    operationCallback.accept(FileUtils.createDirectory(context, parentFile, fileName));
//                }))
//                .setNegativeButton(android.R.string.cancel, (dialog1, which) -> dialog1.dismiss())
//                .create();
//
//        dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//        dialog.show();
    }

    static void buildRenameDialog(Context context, String originalFileName, Consumer<CharSequence> callback) {
        EditText editText = new EditText(context);
        if (originalFileName != null) {
            editText.setText(originalFileName);
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex != -1) {
                editText.setSelection(0, dotIndex);
            } else {
                editText.setSelection(0, editText.getText().length());
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

    public static native void formatEpubFileName(String path);

    public static native long calculateDirectory(String dir);

    public static native void createZipFromDirectory(String dir, String filename);

    public static native int deleteDirectories(String[] directories);

    public static native void deleteLessFiles(String fileName);

    public static native void extractToDirectory(String filename, String directory);

    static ColorStateList getIconForegroundColorList(Context context) {
        return AppCompatResources.getColorStateList(context, R.color.white_mode_tint);
    }

    public static String getTreeUri() {
        if (mTreeUri == null) {
            mTreeUri = ContextUtils.getAppSharedPreferences().getString(C.KEY_TREE_URI, null);
        }
        return mTreeUri;
    }

    public static native void moveFilesByExtension(String dirPath, String destDirName);

    public static void openContent(Context context, String path, int backWhere) {
        openContent(context, new DocumentInfo.Builder()
                .setPath(path)
                .setType(getType(Paths.get(path)))
                .build(), backWhere);
    }

    public static void openContent(Context context, DocumentInfo documentInfo, int backWhere) {
        if (documentInfo.getType() == C.TYPE_VIDEO) {


            Intent videoIntent = new Intent(context, VideoActivity.class);
            videoIntent.setData(Uri.fromFile(new File(documentInfo.getPath())));
            context.startActivity(videoIntent);

            return;
        } else if (documentInfo.getType() == C.TYPE_TEXT) {
            if (documentInfo.getPath().endsWith(".md")) {
                Intent m = new Intent(context, MarkdownActivity.class);
                m.putExtra("file_path", documentInfo.getPath());
                context.startActivity(m);
                return;
            }
            // 用 Chrome 浏览器打开，更好的阅读体验
            Intent textIntent = new Intent();
            textIntent.setAction(Intent.ACTION_VIEW);
            textIntent.setDataAndType(Uri.fromFile(new File(documentInfo.getPath())), "multipart/related");
            context.startActivity(textIntent);
            return;
        } else if (documentInfo.getFileName().toLowerCase().endsWith(".epub")) {
            //if (IntentUtils.isPackageInstalled(context, "")) {
            try {
                Intent epubIntent = new Intent();
                epubIntent.setAction(Intent.ACTION_VIEW);
                epubIntent.setDataAndType(Uri.fromFile(new File(documentInfo.getPath())), "application/epub+zip");
                epubIntent.setClassName("com.duokan.reader", "com.duokan.reader.DkReaderActivity");
                context.startActivity(epubIntent);
                return;
            } catch (Exception ignored) {

            }
            //}
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(documentInfo.getPath())),
                MimeUtils.guessMimeTypeFromExtension(Strings.substringAfterLast(documentInfo.getFileName(), ".")));

        if (documentInfo.getFileName().toLowerCase().endsWith(".apk")) {
            context.startActivity(Intent.createChooser(intent, "打开"));
            return;
        }
        ComponentName foundActivity = intent.resolveActivity(context.getPackageManager());
        if (foundActivity != null) {
            context.startActivity(intent);
        } else {
            context.startActivity(Intent.createChooser(intent, "打开"));
        }
    }

    public static native void padFileNames(String dir, int paddingLeftLength);

    public static native String renderMarkdown(String text);

    public static void selectAll(SelectionDelegate<DocumentInfo> delegate, DocumentsAdapter adapter) {

        delegate.setSelectedItems(CollectionUtils.toHashSet(adapter.getInfos()));
    }

    public static void selectSameTypes(SelectionDelegate<DocumentInfo> delegate, DocumentsAdapter adapter) {


        List<DocumentInfo> infos = delegate.getSelectedItemsAsList();
        if (infos.size() < 1) return;
        String extension = Strings.substringAfterLast(infos.get(0).getFileName(), ".");
        if (extension == null) return;


        delegate.setSelectedItems(adapter.getInfos()
                .stream()
                .filter(i -> i.getFileName().
                        endsWith(extension))
                .collect(Collectors.toSet()));
    }

    public static void shareDocument(Context context, DocumentInfo documentInfo) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        String extension = FileUtils.getExtension(documentInfo.getFileName());
        shareIntent.setType(MimeUtils.guessMimeTypeFromExtension(extension));
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(documentInfo.getPath())));
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_link_title)));
    }

    static void showDocumentProperties(Context context,
                                       DocumentInfo info) {
        ThreadUtils.postOnBackgroundThread(() -> {
            final List<String> properties = new ArrayList<>();

            properties.add(context.getString(R.string.dialog_properties_path));
            properties.add(info.getPath());
            properties.add(context.getString(R.string.dialog_properties_size));
            if (info.getType() == C.TYPE_DIRECTORY) {
                properties.add(android.text.format.Formatter.formatFileSize(context, calculateDirectory(info.getPath())));
            } else {
                properties.add(android.text.format.Formatter.formatFileSize(context, info.getSize()));
            }

            ThreadUtils.postOnUiThread(() -> {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.dialog_properties_title)
                        .setAdapter(new ArrayAdapter<>(context, R.layout.dialog_properties, R.id.line1, properties),
                                (dialog, which) -> {
                                    ClipboardUtils.writeToClipboard(context, properties.get(which));
                                }).show();

            });
        });


    }

    public interface Consumer<T> {
        void accept(T t);
    }
}
