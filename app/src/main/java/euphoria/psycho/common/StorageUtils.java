package euphoria.psycho.common;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;

import java.io.File;
import java.io.IOException;

import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

public class StorageUtils {


    // https://developer.android.com/guide/topics/providers/document-provider
    // content://com.android.externalstorage.documents/tree/19E7-1704%3A

    private static final String TAG = "TAG/" + StorageUtils.class.getSimpleName();
    private static String sSDCardPath;
    private static Uri sTreeUri;

    public static boolean deleteFile(File file) {
        if (file.getPath().startsWith(getSDCardPath())) {
            if (sTreeUri == null) {
                sTreeUri = Uri.parse(ContextUtils.getAppSharedPreferences().getString(C.KEY_TREE_URI, null));
            }
            DocumentFile documentFile = getDocumentFile(file, false, ContextUtils.getApplicationContext(), sTreeUri);
            if (documentFile != null) return documentFile.delete();
            return false;

        } else {
            return file.delete();
        }
    }

    public static DocumentFile getDocumentFileFromTreeUri(Context context, String treeUri,File file) {
        String lastPath = StringUtils.substringAfterLast(treeUri, "/");
        String baseURI = treeUri + "/document/" + lastPath;
        String splited = StringUtils.substringBeforeLast(lastPath, "%");

        return DocumentFile.fromSingleUri(context, Uri.parse(baseURI + Uri.encode(StringUtils.substringAfter(file.getAbsolutePath(), splited + "/"))));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static DocumentFile getDocument(Context context, Uri treeUri,
                                           String mimeType,
                                           String displayName) {
        DocumentFile documentFile = DocumentFile.fromTreeUri(context, treeUri);
        DocumentFile[] files = documentFile.listFiles();


        return null;
    }

    public static DocumentFile getDocumentFile(final File file, final boolean isDirectory, Context context, Uri treeUri) {
        String baseFolder = getSDCardPath();
        boolean originalDirectory = false;
        if (baseFolder == null) {
            return null;
        }

        String relativePath = null;
        try {
            String fullPath = file.getCanonicalPath();
            if (!baseFolder.equals(fullPath))
                relativePath = fullPath.substring(baseFolder.length() + 1);
            else originalDirectory = true;
        } catch (IOException e) {
            return null;
        } catch (Exception f) {
            originalDirectory = true;
            //continue
        }

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);
        if (originalDirectory) return document;
        String[] parts = relativePath.split("\\/");
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);
            if (nextDocument == null) {
                if ((i < parts.length - 1) || isDirectory) {
                    if (document.createDirectory(parts[i]) == null) {
                        return null;
                    }
                    nextDocument = document.createDirectory(parts[i]);
                } else {
                    nextDocument = document.createFile("image", parts[i]);
                }
            }
            document = nextDocument;
        }

        return document;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getRootDocumentId(Context context, Uri treeUri) {
        if (treeUri != null) {
            DocumentFile documentFile = DocumentFile.fromTreeUri(context, treeUri);
            return DocumentsContract.getDocumentId(documentFile.getUri());
        }
        return null;
    }

    public static String getSDCardPath() {
        if (sSDCardPath == null) {
            sSDCardPath = getSDCardPathInternal();
        }
        return sSDCardPath;
    }

    private static String getSDCardPathInternal() {
        File[] sdcardPaths = new File("/storage").listFiles();
        if (sdcardPaths != null && sdcardPaths.length >= 2) {
            String internal = Environment.getExternalStorageDirectory().getAbsolutePath();
            for (File file : sdcardPaths) {
                if (!file.getAbsolutePath().startsWith(internal)) return file.getAbsolutePath();
            }
        }
        return null;
//        String cmd = "cat /proc/mounts";
//        Runtime run = Runtime.getRuntime();
//        try {
//            Process p = run.exec(cmd);// 启动另一个进程来执行命令
//            BufferedInputStream in = new BufferedInputStream(p.getInputStream());
//            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
//            String lineStr;
//            while ((lineStr = inBr.readLine()) != null) {
//                Log.e(TAG, "getSDCardPathInternal: " + lineStr);
//
//                if (lineStr.contains("sdcard") && lineStr.contains(".android_secure")) {
//
//
//                    String[] strArray = lineStr.split(" ");
//                    if (strArray.length >= 5) {
//                        return strArray[1].replace("/.android_secure", "");
//                    }
//                }
//                if (p.waitFor() != 0 && p.exitValue() == 1) {
//                    Log.e(TAG, "命令执行失败!");
//                }
//            }
//            inBr.close();
//            in.close();
//        } catch (Exception e) {
//            Log.e(TAG, e.toString());
//            return Environment.getExternalStorageDirectory().getPath();
//        }
//        return Environment.getExternalStorageDirectory().getPath();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void keepPermission(Context context, Intent intent) {

        context.getContentResolver().takePersistableUriPermission(intent.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        context.grantUriPermission(context.getPackageName(), intent.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    public static Uri getTreeUri() {
        if (sTreeUri == null) {
            sTreeUri = Uri.parse(ContextUtils.getAppSharedPreferences().getString(C.KEY_TREE_URI, null));
        }
        return sTreeUri;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void requestTreeUri(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.startActivityForResult(intent, requestCode);
    }


}
