package euphoria.psycho.common;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

/*
android.os.FileUtils
 */
public class StorageUtils {


    // https://developer.android.com/guide/topics/providers/document-provider
    // content://com.android.externalstorage.documents/tree/19E7-1704%3A

    private static final String TAG = "TAG/" + StorageUtils.class.getSimpleName();
    private static String sSDCardPath;
    private static Uri sTreeUri;

    private static File buildFile(File parent, String name, String ext) {
        if (TextUtils.isEmpty(ext)) {
            return new File(parent, name);
        } else {
            return new File(parent, name + "." + ext);
        }
    }

    private static File buildUniqueFileWithExtension(File parent, String name, String ext)
            throws FileNotFoundException {
        File file = buildFile(parent, name, ext);

        // If conflicting file, try adding counter suffix
        int n = 0;
        while (file.exists()) {
            if (n++ >= 32) {
                throw new FileNotFoundException("Failed to create unique file");
            }
            file = buildFile(parent, name + " (" + n + ")", ext);
        }

        return file;
    }

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

    public static boolean deleteFile(Context context, File file, String treeUri) {

        boolean result = file.delete();
        if (!result) {
            DocumentFile documentFile = StorageUtils.getDocumentFileFromTreeUri(context, treeUri, file);
            result = documentFile.delete();
        }
        return result;
    }

    public static boolean deleteFile(Context context, File file) {

        boolean result = file.delete();
        if (!result) {
            DocumentFile documentFile = StorageUtils.getDocumentFileFromTreeUri(context, getTreeUri().toString(), file);
            result = documentFile.delete();
        }
        return result;
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

    public static DocumentFile getDocumentFileFromTreeUri(Context context, String treeUri, File file) {
        String lastPath = StringUtils.substringAfterLast(treeUri, "/");
        String baseURI = treeUri + "/document/" + lastPath;
        String splited = StringUtils.substringBeforeLast(lastPath, "%");

        return DocumentFile.fromSingleUri(context, Uri.parse(baseURI + Uri.encode(StringUtils.substringAfter(file.getAbsolutePath(), splited + "/"))));
    }

    public static DocumentFile getDocumentFileFromTreeUri(File file) {

        return getDocumentFileFromTreeUri(ContextUtils.getApplicationContext(), getTreeUri().toString(), file);
    }

    public static String getDocumentUriFromTreeUri(Context context, String treeUri, File file) {
        String lastPath = StringUtils.substringAfterLast(treeUri, "/");
        String baseURI = treeUri + "/document/" + lastPath;
        String splited = StringUtils.substringBeforeLast(lastPath, "%");

        return baseURI + Uri.encode(StringUtils.substringAfter(file.getAbsolutePath(), splited + "/"));
    }

    public static Uri getDocumentUriFromTreeUri(File file) {
        String lastPath = StringUtils.substringAfterLast(getTreeUri().toString(), "/");
        String baseURI = getTreeUri() + "/document/" + lastPath;
        String splited = StringUtils.substringBeforeLast(lastPath, "%");

        return Uri.parse(baseURI + Uri.encode(StringUtils.substringAfter(file.getAbsolutePath(), splited + "/")));
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

    public static Uri getTreeUri() {
        if (sTreeUri == null) {
            sTreeUri = Uri.parse(ContextUtils.getAppSharedPreferences().getString(C.KEY_TREE_URI, null));
        }
        return sTreeUri;
    }

    private static boolean isValidFatFilenameChar(char c) {
        if ((0x00 <= c && c <= 0x1f)) {
            return false;
        }
        switch (c) {
            case '"':
            case '*':
            case '/':
            case ':':
            case '<':
            case '>':
            case '?':
            case '\\':
            case '|':
            case 0x7F:
                return false;
            default:
                return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void keepPermission(Context context, Intent intent) {

        context.getContentResolver().takePersistableUriPermission(intent.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        context.grantUriPermission(context.getPackageName(), intent.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    public static boolean moveFile(Context context, File src, File destinationDirectory) {

        boolean r = src.renameTo(new File(destinationDirectory, src.getName()));

        if (!r) {
            if (VERSION.SDK_INT >= VERSION_CODES.N) {
                try {
                    Uri resultURI = DocumentsContract.moveDocument(context.getContentResolver(),
                            getDocumentUriFromTreeUri(src),
                            getDocumentUriFromTreeUri(src.getParentFile()),
                            getDocumentUriFromTreeUri(destinationDirectory));

                    r = resultURI != null;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return r;
    }

    public static boolean renameFile(Context context, File src, File dst) {


        boolean result = src.renameTo(dst);

        if (!result && VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {


            Uri srcDocumentUri = Uri.parse(getDocumentUriFromTreeUri(context, getTreeUri().toString(), src));

//            Log.e("TAG/", "renameFile: " +
//                    "\n" + srcDocumentUri +
//                    "\n" + dst);
            if (src.getParent().equals(dst.getParent())) {
                try {


                    result = DocumentsContract.renameDocument(
                            context.getContentResolver(),
                            srcDocumentUri,
                            dst.getName()) != null;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            } else if (VERSION.SDK_INT >= VERSION_CODES.N) {

            }

        }
        return result;
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
