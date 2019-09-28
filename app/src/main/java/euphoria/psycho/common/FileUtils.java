package euphoria.psycho.common;

import android.content.Context;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipFile;

import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;
import euphoria.common.Documents;
import euphoria.common.Files;
import euphoria.common.Strings;
import euphoria.psycho.share.util.ContextUtils;
import euphoria.psycho.share.util.MimeUtils;
import euphoria.psycho.share.util.ThreadUtils;

// https://android.googlesource.com/platform/packages/apps/UnifiedEmail/+/kitkat-mr1-release/src/org/apache/commons/io

// https://developer.android.com/guide/topics/providers/document-provider
// content://com.android.externalstorage.documents/tree/19E7-1704%3A

/**
 * Helper methods for dealing with Files.
 */
public class FileUtils {
    public static final char EXTENSION_SEPARATOR = '.';
    private static final String TAG = "FileUtils";
    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';

    private static String sSDCardPath;
    private static Uri sTreeUri;

    /**
     * Delete the given files or directories by calling {@link Files#recursivelyDeleteFile(File)}.
     *
     * @param files The files to delete.
     */
    public static void batchDeleteFiles(List<File> files) {
        ThreadUtils.assertOnBackgroundThread();

        for (File file : files) {
            if (file.exists()) Files.recursivelyDeleteFile(file);
        }
    }

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

    /**
     * Overload of the above function for {@link ZipFile} which implements Closeable only starting
     * from api19.
     *
     * @param zipFile - the ZipFile to be closed.
     */
    public static void closeQuietly(ZipFile zipFile) {
        if (zipFile == null) return;

        try {
            zipFile.close();
        } catch (IOException ex) {
            // Ignore the exception on close.
        }
    }

    public static void closeSilently(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception ignored) {

        }
    }

    public static boolean deleteFile(File file) {
        if (euphoria.psycho.knife.util.FileUtils.hasSDCardPath() && !file.getPath().startsWith(Environment.getExternalStorageDirectory().getAbsolutePath())) {
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

    public static boolean deleteFile(Context context, File file) {

        boolean result = file.delete();
        if (!result) {
            if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
                try {
                    return DocumentsContract.deleteDocument(context.getContentResolver(), Documents.getDocumentUriFromTreeUri(file));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }


    /**
     * Extracts an asset from the app's APK to a file.
     *
     * @param context
     * @param assetName Name of the asset to extract.
     * @param dest      File to extract the asset to.
     * @return true on success.
     */
    public static boolean extractAsset(Context context, String assetName, File dest) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = context.getAssets().open(assetName);
            outputStream = new BufferedOutputStream(new FileOutputStream(dest));
            byte[] buffer = new byte[8192];
            int c;
            while ((c = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, c);
            }
            inputStream.close();
            outputStream.close();
            return true;
        } catch (IOException e) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                }
            }
        }
        return false;
    }

    public static File findFirstImage(File dir) {
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && Files.isSupportedImage(pathname.getName());
            }
        });
        if (files != null && files.length > 0) return files[0];
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

    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfExtension(filename);
        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }

    @RequiresApi(api = VERSION_CODES.KITKAT)
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
            String treeUri = ContextUtils.getAppSharedPreferences().getString(C.KEY_TREE_URI, null);
            if (treeUri != null)
                sTreeUri = Uri.parse(treeUri);
        }
        return sTreeUri;
    }


    public static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        }
        int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
        int lastSeparator = indexOfLastSeparator(filename);
        return (lastSeparator > extensionPos ? -1 : extensionPos);
    }

    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }


    public static boolean moveFile(Context context, File src, File destinationDirectory) {

        String[] srcPieces = src.getAbsolutePath().split("/");
        String[] dstPieces = destinationDirectory.getAbsolutePath().split("/");
        boolean r = false;
        if (srcPieces[1].equals(dstPieces[1])) {
            r = src.renameTo(new File(destinationDirectory, src.getName()));


            if (!r && VERSION.SDK_INT >= VERSION_CODES.N) {
                try {
                    Uri resultURI = DocumentsContract.moveDocument(context.getContentResolver(),
                            Documents.getDocumentUriFromTreeUri(src),
                            Documents.getDocumentUriFromTreeUri(src.getParentFile()),
                            Documents.getDocumentUriFromTreeUri(destinationDirectory));

                    r = resultURI != null;
                } catch (Exception e) {
                    // java.lang.IllegalStateException: Failed to move to /mnt/media_rw/19E7-1704/19E7-1704
                }
            }
        }

        if (!r && VERSION.SDK_INT >= VERSION_CODES.N) {
            if (!src.getAbsolutePath().startsWith(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                Uri srcDocumentUri = Documents.getDocumentUriFromTreeUri(src);


                try {
                    r = DocumentsContract.copyDocument(context.getContentResolver(),
                            srcDocumentUri,
                            Documents.getDocumentUriFromTreeUri(destinationDirectory)) != null;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (r) {
                    try {
                        DocumentsContract.deleteDocument(context.getContentResolver(),
                                srcDocumentUri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    Uri newDocument = DocumentsContract.createDocument(
                            context.getContentResolver(),
                            Documents.getDocumentUriFromTreeUri(destinationDirectory),
                            MimeUtils.guessMimeTypeFromExtension(Strings.substringAfterLast(src.getName(), ".")),
                            src.getName());
                    if (newDocument != null) {
                        OutputStream outputStream = context.getContentResolver().openOutputStream(newDocument);
                        FileInputStream inputStream = new FileInputStream(src);
                        euphoria.psycho.share.util.FileUtils.copy(inputStream, outputStream);
                        closeSilently(inputStream);
                        closeSilently(outputStream);
                        src.delete();
                        r = true;
                    }
                } catch (Exception e) {

                }
            }

        }


        return r;
    }

    public static boolean renameFile(Context context, File src, File dst) {


        boolean result = src.renameTo(dst);

        if (!result && VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {


            Uri srcDocumentUri = Documents.getDocumentUriFromTreeUri(src);

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


}
