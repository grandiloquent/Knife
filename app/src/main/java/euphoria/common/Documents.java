package euphoria.common;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.provider.DocumentsContract;

import java.io.File;
import java.io.FileNotFoundException;

import euphoria.psycho.common.FileUtils;
import euphoria.psycho.knife.util.StorageUtils;

public class Documents {
    @SuppressLint("NewApi")
    public static boolean deleteDocument(ContentResolver contentResolver, File srcFile, String treeUri) {
        try {
            return DocumentsContract.deleteDocument(contentResolver,
                    StorageUtils.getDocumentUri(srcFile, treeUri));
        } catch (FileNotFoundException ignored) {

        }
        return false;
    }

    public static Uri getDocumentUriFromTreeUri(File file) {
        String lastPath = Strings.substringAfterLast(FileUtils.getTreeUri().toString(), "/");

        String baseURI = FileUtils.getTreeUri() + "/document/" + lastPath;
        String splited = Strings.substringBeforeLast(lastPath, "%");


        String subPath = Strings.substringAfter(file.getAbsolutePath(), splited + "/");

        if (subPath != null) {
            subPath = Uri.encode(subPath);
        } else {
            subPath = "";
        }
        return Uri.parse(baseURI + subPath);
    }

    @TargetApi(VERSION_CODES.KITKAT)
    public static void keepPermission(Context context, Intent intent) {

        context.getContentResolver().takePersistableUriPermission(intent.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        context.grantUriPermission(context.getPackageName(), intent.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    public static void requestTreeUri(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.startActivityForResult(intent, requestCode);
    }
}
