package euphoria.psycho.knife.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import euphoria.common.Strings;
import euphoria.psycho.common.C;
import euphoria.psycho.share.util.FileUtils;

public class StorageUtils {

    private static String mSDCard;
    private static String mStorage;


    public static boolean copyFile(Context context, File srcFile, File targetDirectory, String treeUri) {
        boolean success = false;

        File targetFile = new File(targetDirectory, srcFile.getName());
        // 如果目标文件已存在，终止
        if (targetFile.exists()) return success;
        FileInputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(srcFile);

        } catch (Exception ignored) {
            return success;
        }

        try {
            outputStream = new FileOutputStream(targetFile);
        } catch (Exception ignored) {
            // 如果未获取外部储存卡权限，或者系统版本不支持 SAF 框架，终止
            if (treeUri == null || VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) return success;
            try {


                Uri newDocumentUri = DocumentsContract.createDocument(
                        context.getContentResolver(),
                        getDocumentUri(targetDirectory, treeUri),
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(Strings.substringAfterLast(srcFile.getName(), ".")),
                        srcFile.getName()
                );
                if (newDocumentUri == null) return success;
                outputStream = context.getContentResolver().openOutputStream(newDocumentUri);

            } catch (Exception i) {
                return success;
            }
        }

        byte[] buffer = new byte[4096];
        int len;
        try {
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            success = true;
        } catch (Exception e) {
            return success;
        }

        try {
            inputStream.close();
        } catch (IOException ignored) {
        }

        if (outputStream != null) {
            try {

                outputStream.close();
            } catch (IOException ignored) {
            }
        }

        return success;
    }

    public static OutputStream getDocumentOutputStream(Context context, File dst, String treeUri) throws FileNotFoundException {

        Uri uri = getDocumentUri(dst, treeUri);
        return context.getContentResolver().openOutputStream(uri);
    }


    public static boolean sdCardDocumentToStorageFile(ContentResolver contentResolver,
                                                      File srcFile,
                                                      File dstFile,
                                                      String treeUri,
                                                      boolean overwrite) {

        if (dstFile.exists()) {
            if (!overwrite) {
                return false;
            }
            if (!dstFile.delete()) {
                return false;
            }
        }
        InputStream is;
        OutputStream os;
        try {
            os = new FileOutputStream(dstFile);
        } catch (FileNotFoundException e) {
            return false;
        }

        try {

            is = contentResolver.openInputStream(StorageUtils.getDocumentUri(srcFile, treeUri));
        } catch (FileNotFoundException e) {
            euphoria.psycho.share.util.FileUtils.closeQuietly(os);
            return false;
        }

        try {
            euphoria.psycho.share.util.FileUtils.copy(is, os);
            return true;
        } catch (IOException ignored) {

        } finally {
            euphoria.psycho.share.util.FileUtils.closeQuietly(is);
            FileUtils.closeQuietly(os);
        }
        return false;
    }

    public static boolean deleteFile(ContentResolver contentResolver, File srcFile, String treeUri) {
        if (srcFile.delete()) {
            return true;
        }
        if (contentResolver != null && treeUri != null) {
            return deleteDocument(contentResolver, srcFile, treeUri);
        }
        return false;
    }



    public static String getTreeUri(Context context) {

        return PreferenceManager.getDefaultSharedPreferences(context).getString(C.KEY_TREE_URI, null);

    }


    @SuppressLint("NewApi")
    public static boolean deleteDocument(ContentResolver contentResolver, File srcFile, String treeUri) {
        try {
            return DocumentsContract.deleteDocument(contentResolver,
                    StorageUtils.getDocumentUri(srcFile, treeUri));
        } catch (FileNotFoundException ignored) {

        }
        return false;
    }

    public static boolean createDirectory(Context context, File parentFile, String directoryName, String treeUri) {

        File dir = new File(parentFile, directoryName);
        if (dir.isDirectory()) return true;
        boolean b = dir.mkdirs();
        if (!b) {


            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                Uri parentUri = getDocumentUri(parentFile, treeUri);


                try {
                    Uri result = DocumentsContract.createDocument(
                            context.getContentResolver(),
                            parentUri,
                            Document.MIME_TYPE_DIR,
                            directoryName
                    );
                    return result != null;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return b;
    }

    public static Uri getDocumentUri(File file, String treeUri) {
        String lastPath = Strings.substringAfterLast(treeUri, "/");

        String baseURI = treeUri + "/document/" + lastPath;
        String splited = Strings.substringBeforeLast(lastPath, "%");


        String subPath = Strings.substringAfter(file.getAbsolutePath(), splited + "/");

        if (subPath != null) {
            subPath = Uri.encode(subPath);
        } else {
            subPath = "";
        }
        return Uri.parse(baseURI + subPath);
    }

    public static String getExternalStoragePath() {
        if (mStorage == null) {
            mStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return mStorage;
    }

    public static String getSDCardPath() {
        if (mSDCard == null) {
            File[] directories = new File("/storage").listFiles();
            if (directories != null && directories.length > 2) {
                for (File dir : directories) {
                    if (!dir.getAbsolutePath().equals("/storage/emulated")
                            && !dir.getAbsolutePath().equals("/storage/self")) {
                        mSDCard = dir.getAbsolutePath();
                        break;
                    }
                }
            }
        }
        return mSDCard;
    }

    public static boolean isSDCardFile(File file) {
        if (getSDCardPath() == null) {
            return false;
        }
        return file.getAbsolutePath().startsWith(getSDCardPath());
    }


}
