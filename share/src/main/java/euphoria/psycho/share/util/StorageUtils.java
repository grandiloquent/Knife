package euphoria.psycho.share.util;

import android.content.Context;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.DocumentsContract;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class StorageUtils {

    public static Uri getDocumentUri(File file, String treeUri) {
        String lastPath = StringUtils.substringAfterLast(treeUri, "/");

        String baseURI = treeUri + "/document/" + lastPath;
        String splited = StringUtils.substringBeforeLast(lastPath, "%");


        String subPath = StringUtils.substringAfter(file.getAbsolutePath(), splited + "/");

        if (subPath != null) {
            subPath = Uri.encode(subPath);
        } else {
            subPath = "";
        }
        return Uri.parse(baseURI + subPath);
    }

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
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(StringUtils.substringAfterLast(srcFile.getName(), ".")),
                        srcFile.getName()
                );
                if (newDocumentUri == null) return success;
                outputStream = context.getContentResolver().openOutputStream(newDocumentUri);

            } catch (Exception i) {
                return success;
            }
        }

        byte[] buffer = new byte[4096];
        try {
            while (inputStream.read(buffer) != -1) {
                outputStream.write(buffer);
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
}
