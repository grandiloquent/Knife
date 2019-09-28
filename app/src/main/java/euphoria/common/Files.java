package euphoria.common;

import android.database.Cursor;
import android.os.Build.VERSION_CODES;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import androidx.annotation.RequiresApi;
import euphoria.psycho.common.Log;
import euphoria.psycho.share.util.ThreadUtils;

import static euphoria.common.Strings.substringBeforeLast;

public class Files {

    public static final int DEFAULT_BUFFER_SIZE = 4096;
    public static final String NORMAL_AUDIO_FORMAT = "3gp|aac|flac|gsm|imy|m4a|mid|mkv|mp3|mp4|mxmf|ogg|ota|rtttl|rtx|ts|wav|xmf";
    public static final String SUPPORT_AUDIO_FORMAT = "aac|flac|m4a|mp3|ogg|wav";
    public static final String SUPPORT_VIDEO_FORMAT = "3gp|mkv|mp4|ts|webm";
    private static final char AltDirectorySeparatorChar = '/';
    private static final int BUFFER_SIZE = 8192;
    private static final long COPY_CHECKPOINT_BYTES = 524288;
    private static final char DirectorySeparatorChar = '\\';
    private static final boolean ENABLE_COPY_OPTIMIZATIONS = true;
    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;
    private static final String TAG = "TAG/" + Files.class.getSimpleName();
    private static final char VolumeSeparatorChar = ':';
    private static String[] mSupportVideoExtensions;
    private static String[] mSupportedAudioExtensions;
    private static String[] mSupportedImageExtensions;
    private static String[] mSupportedTextExtensions;
    private static String[] sAudioExtensions;
    private static final
    char[] sInvalidFileNamechars = {'\n', '\"', '<', '>', '|', '\0', (char) 1, (char) 2, (char) 3, (char) 4, (char) 5, (char) 6, (char) 7, (char) 8, (char) 9, (char) 10, (char) 11, (char) 12, (char) 13, (char) 14, (char) 15, (char) 16, (char) 17, (char) 18, (char) 19, (char) 20, (char) 21, (char) 22, (char) 23, (char) 24, (char) 25, (char) 26, (char) 27, (char) 28, (char) 29, (char) 30, (char) 31, ':', '*', '?', '\\', '/'};
    private static String[] sVideoExtensions;

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null)
                closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeSilently(ParcelFileDescriptor fd) {
        try {

            if (fd != null) fd.close();
        } catch (Throwable t) {
            android.util.Log.w(TAG, "fail to close", t);
        }
    }

    public static void closeSilently(Cursor cursor) {
        try {
            if (cursor != null) cursor.close();
        } catch (Throwable t) {
            android.util.Log.w(TAG, "fail to close", t);
        }
    }

    public static void closeSilently(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (IOException t) {
            android.util.Log.w(TAG, "close fail ", t);
        }
    }

    public static String[] collectFileNames(File[] files) {
        if (files == null) return null;
        int length = files.length;
        String[] strings = new String[length];
        for (int i = 0; i < length; i++) {
            strings[i] = files[i].getAbsolutePath();
        }
        Collator collator = Collator.getInstance(Locale.CHINA);

        Arrays.sort(strings, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return collator.compare(o1, o2);
            }
        });
        return strings;
    }

    public static String combine(String... paths) {
        if (paths == null) return null;
        if (paths.length == 1) return paths[0];

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < paths.length - 1; i++) {
            stringBuilder.append(paths[i]).append('/');
        }
        return stringBuilder.append(paths[paths.length - 1]).toString();
    }

    public static void combineHtmls(File dir, File dstDir) throws IOException {
        File[] htmls = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile() && pathname.getName().endsWith(".html")) return true;
                return false;
            }
        });
        if (htmls == null || htmls.length == 0) return;
        Arrays.sort(htmls, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        File outFile = new File(dstDir, dir.getName() + ".html");
        FileOutputStream out = new FileOutputStream(outFile);

        boolean isFirst = true;
        for (File htm : htmls) {
            byte[] buf = readFully(htm);
            String text = new String(buf, "UTF-8");


            if (isFirst) {
                isFirst = false;
                out.write(Strings.substringBefore(text, "<body").getBytes("UTF-8"));
                out.write("<body>".getBytes("UTF-8"));
            }
            text = Strings.substringAfterRegex(text, "<body[^>]*>");
            text = Strings.substringBefore(text, "</body>");
            out.write(text.getBytes("UTF-8"));
        }
        out.write("</body></html>".getBytes("UTF-8"));
        out.close();
    }

    public static long copy(String source, OutputStream out) {

        InputStream in = null;
        try {
            in = new FileInputStream(source);
            return copy(in, out);
        } catch (IOException e) {

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    public static long copy(InputStream source, OutputStream sink)
            throws IOException {
        long nread = 0L;
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = source.read(buf)) > 0) {
            sink.write(buf, 0, n);
            nread += n;
        }
        return nread;
    }

    public static long copyInternalUserspace(InputStream in, OutputStream out,
                                             ProgressListener listener, CancellationSignal signal) throws IOException {
        long progress = 0;
        long checkpoint = 0;
        byte[] buffer = new byte[8192];

        int t;
        while ((t = in.read(buffer)) != -1) {
            out.write(buffer, 0, t);

            progress += t;
            checkpoint += t;

            if (checkpoint >= COPY_CHECKPOINT_BYTES) {
                if (signal != null) {
                    signal.throwIfCanceled();
                }
                if (listener != null) {
                    listener.onProgress(progress);
                }
                checkpoint = 0;
            }
        }
        if (listener != null) {
            listener.onProgress(progress);
        }
        return progress;
    }

    @RequiresApi(api = VERSION_CODES.N)
    public static String countFileNames(File dir) {
        File[] files = dir.listFiles(file -> {
            if (file.isFile()) return true;
            return false;
        });
        List<String> words = new ArrayList<>();

        for (File file : files) {
            words.addAll(Arrays.asList(substringBeforeLast(file.getName(), ".")
                    .split("[\\s_]+")));
        }
        Map<String, Integer> counts = countWord(words);

        List<Entry<String, Integer>> list = new ArrayList<>(counts.entrySet());
        list.sort((t1, t2) -> t1.getValue().compareTo(t2.getValue()) * -1);


        StringBuilder sb = new StringBuilder();
        for (Entry<String, Integer> c : list) {
            if (c.getValue() == 1) continue;

            sb.append(String.format("%d - %s", c.getValue(), c.getKey())).append('\n');
        }


        return sb.toString();
    }

    @RequiresApi(api = VERSION_CODES.N)
    public static Map<String, Integer> countWord(List<String> words) {
        return words.parallelStream()

                .collect(Collectors.toConcurrentMap(
                        w -> w,
                        w -> 1, Integer::sum
                ));


    }

    public static File createDirectoryIfNotExists(String path) {
        File dir = new File(path);
         /*

         Tests whether the file denoted by this abstract pathname is a
         directory.

         Where it is required to distinguish an I/O exception from the case
         that the file is not a directory, or where several attributes of the
         same file are required at the same time, then the java.nio.file.Files#readAttributes(Path,Class,LinkOption[])
         Files.readAttributes method may be used.

         @return true if and only if the file denoted by this
         abstract pathname exists and is a directory;
         false otherwise

         @throws  SecurityException
         If a security manager exists and its java.lang.SecurityManager#checkRead(java.lang.String)
         method denies read access to the file

         */
        if (!dir.isDirectory()) {
         /*

         Creates the directory named by this abstract pathname, including any
         necessary but nonexistent parent directories.  Note that if this
         operation fails it may have succeeded in creating some of the necessary
         parent directories.

         @return  true if and only if the directory was created,
         along with all necessary parent directories; false
         otherwise

         @throws  SecurityException
         If a security manager exists and its java.lang.SecurityManager#checkRead(java.lang.String)
         method does not permit verification of the existence of the
         named directory and all necessary parent directories; or if
         the java.lang.SecurityManager#checkWrite(java.lang.String)
         method does not permit the named directory and all necessary
         parent directories to be created

         */
            dir.mkdirs();
        }
        return dir;
    }

    public static void deleteFiles(String... paths) {
        for (String f : paths) {
            File file = new File(f);
         /*

         Tests whether the file or directory denoted by this abstract pathname
         exists.

         @return  true if and only if the file or directory denoted
         by this abstract pathname exists; false otherwise

         @throws  SecurityException
         If a security manager exists and its java.lang.SecurityManager#checkRead(java.lang.String)
         method denies read access to the file or directory

         */
            if (file.exists()) {
         /*

         Deletes the file or directory denoted by this abstract pathname.  If
         this pathname denotes a directory, then the directory must be empty in
         order to be deleted.

         Note that the java.nio.file.Files class defines the java.nio.file.Files#delete(Path) delete method to throw an IOException
         when a file cannot be deleted. This is useful for error reporting and to
         diagnose why a file cannot be deleted.

         @return  true if and only if the file or directory is
         successfully deleted; false otherwise

         @throws  SecurityException
         If a security manager exists and its java.lang.SecurityManager#checkDelete method denies
         delete access to the file

         */
                file.delete();
            }
        }
    }

    private static String dirpart(String name) {
        int length = name.length();
        for (int i = length; --i >= 0; ) {
            char ch = name.charAt(i);
            if (ch == '/' || ch == '\\') {
                if (i != length - 1)
                    return name.substring(0, i);
                else
                    return null;
            }

        }

        return null;
    }

    private static boolean extensionMatch(String[] extensions, String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) return false;

        // somevideo.mp4 => .mp4
        String extension = fileName.substring(dotIndex).toLowerCase();


        for (String e : extensions) {
            if (e.equals(extension)) return true;
        }

        return false;
    }

    private static void extractFile(ZipInputStream in, File outdir, String name) throws IOException {

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(outdir,
                name.replaceAll("\\\\", "/"))));
        int count;
        while ((count = in.read(buffer)) != -1)
            out.write(buffer, 0, count);
        out.close();
    }

    public static void extractZip(File zipfile, File outdir) {
        try {
            ZipInputStream zin = new ZipInputStream(new FileInputStream(zipfile));
            ZipEntry entry;
            String name, dir;
            while ((entry = zin.getNextEntry()) != null) {
                name = entry.getName();


                if (entry.isDirectory()) {
                    mkdirs(outdir, name);
                    continue;
                }
                /* this part is necessary because file entry can come before
                 * directory entry where is file located
                 * i.e.:
                 *   /foo/foo.txt
                 *   /foo/
                 */
                dir = dirpart(name);
                if (dir != null)
                    mkdirs(outdir, dir);

                extractFile(zin, outdir, name);
            }
            zin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String formatFileSize(long number) {
        float result = number;
        String suffix = "";
        if (result > 900) {
            suffix = " KB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = " MB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = " GB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = " TB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = " PB";
            result = result / 1024;
        }
        String value;
        if (result < 1) {
            value = String.format("%.2f", result);
        } else if (result < 10) {
            value = String.format("%.1f", result);
        } else if (result < 100) {
            value = String.format("%.0f", result);
        } else {
            value = String.format("%.0f", result);
        }
        return value + suffix;
    }

    public static String getDirectoryName(String path) {
        if (path != null) {

            int i = path.length();

            while (i > 1 && path.charAt(--i) != '/') ;

            return path.substring(0, i);
        }
        return null;
    }

    public static String getExtension(String path) {
        if (path == null)
            return null;

        int length = path.length();
        for (int i = length; --i >= 0; ) {
            char ch = path.charAt(i);
            if (ch == '.') {
                if (i != length - 1)
                    return path.substring(i);
                else
                    return "";
            }
            if (ch == DirectorySeparatorChar || ch == AltDirectorySeparatorChar || ch == VolumeSeparatorChar)
                break;
        }
        return "";
    }

    public static String getFileName(String path) {
        if (path != null) {


            int length = path.length();
            for (int i = length; --i >= 0; ) {
                char ch = path.charAt(i);
                if (ch == DirectorySeparatorChar || ch == AltDirectorySeparatorChar || ch == VolumeSeparatorChar)
                    return path.substring(i + 1);

            }
        }
        return path;
    }

    public static String getFileNameWithoutExtension(String path) {
        path = getFileName(path);
        if (path != null) {
            int i;
            if ((i = path.lastIndexOf('.')) == -1)
                return path; // No path extension found
            else
                return path.substring(0, i);
        }
        return null;
    }


    public static List<String> getFilesRecursively(File dir) {
        List<String> ls = new ArrayList<String>();
        for (File fObj : dir.listFiles()) {
            if (fObj.isDirectory()) {
                ls.add(fObj.getAbsolutePath());
                ls.addAll(getFilesRecursively(fObj));
            } else {
                ls.add(fObj.getAbsolutePath());
            }
        }

        return ls;
    }

    public static String getValidFileName(String value, char c) {

        int len = Math.min(125, value.length());

        char[] buffer = new char[len];

        for (int i = 0; i < len; i++) {
            char ch = value.charAt(i);
            for (int j = 0; j < sInvalidFileNamechars.length; j++) {
                if (ch == sInvalidFileNamechars[j]) {
                    ch = c;
                    break;
                }
            }
            buffer[i] = ch;
        }
        return new String(buffer).trim();
    }

    public static boolean isAudio(String extension) {
        if (sAudioExtensions == null) {
            sAudioExtensions = new String[]{
                    "aac",
                    "flac",
                    "imy",
                    "m4a",
                    "mid",
                    "mp3",
                    "mxmf",
                    "ogg",
                    "ota",
                    "rtttl",
                    "rtx",
                    "wav",
                    "xmf",
            };
        }
        return matchExtension(extension, sAudioExtensions);
    }

    public static boolean isSupportedAudio(String fileName) {
        if (mSupportedAudioExtensions == null) {
            mSupportedAudioExtensions = new String[]{
                    ".3gp",
                    ".aac",
                    ".flac",
                    ".imy",
                    ".m4a",
                    ".mid",
                    ".mkv",
                    ".mp3",
                    ".mp4",
                    ".mxmf",
                    ".ogg",
                    ".ota",
                    ".rtttl",
                    ".rtx",
                    ".ts",
                    ".wav",
                    ".xmf",
            };
        }
        return extensionMatch(mSupportedAudioExtensions, fileName);
    }

    public static boolean isSupportedImage(String fileName) {
        if (mSupportedImageExtensions == null) {
            mSupportedImageExtensions = new String[]{
                    ".jpg",
                    ".gif",
                    ".png",
                    ".bmp",
                    ".webp",
                    ".jpeg"
            };
        }
        return extensionMatch(mSupportedImageExtensions, fileName);
    }

    public static boolean isSupportedText(String fileName) {

        if (mSupportedTextExtensions == null) {
            mSupportedTextExtensions = new String[]{"css",
                    "htm",
                    "js",
                    "log",
                    "srt",
                    "txt",
            };
        }
        return extensionMatch(mSupportedTextExtensions, fileName);
    }

    public static boolean isSupportedVideo(String fileName) {

        if (mSupportVideoExtensions == null) {
            mSupportVideoExtensions = new String[]{
                    ".3gp",
                    ".mkv",
                    ".mp4",
                    ".ts",
                    ".webm",
            };
        }
        return extensionMatch(mSupportVideoExtensions, fileName);
    }

    public static boolean isVideo(String extension) {
        if (sVideoExtensions == null) {
            sVideoExtensions = new String[]{"3gp",
                    "mkv",
                    "mp4",
                    "ts",
                    "webm",
                    "vm",
            };
        }
        return matchExtension(extension, sVideoExtensions);
    }

    private static boolean matchExtension(String extension, String[] extensions) {
        for (String e : extensions) {
            if (e.equals(extension)) return true;
        }
        return false;
    }

    private static void mkdirs(File outdir, String path) {
        File d = new File(outdir, path);
        if (!d.exists())
            d.mkdirs();
    }

    public static void moveImages(File htmlFile, File dstDir) {
        Pattern imagePattern = Pattern.compile("(?<=src=\")[^\"]+(?=\")");

        try {
            // " src="../images/00202.jpeg"/></div>

            byte[] buf = readFully(htmlFile);
            Matcher imageMatcher = imagePattern.matcher(new String(buf, "UTF-8"));
            List<String> images = new ArrayList<>();
            while (imageMatcher.find()) {
                images.add(imageMatcher.group());
            }
            File parent = htmlFile.getParentFile();
            for (int i = 0, j = images.size(); i < j; i++) {
                File imageFile = new File(parent, images.get(i)).getCanonicalFile();
                InputStream in = new FileInputStream(imageFile);
                File dst = new File(dstDir, Long.toString(Keys.crc64Long(Keys.getBytes(parent.getAbsolutePath()))) + "_" + imageFile.getName());
                OutputStream out = new FileOutputStream(dst);
                copy(in, out);
                in.close();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] read(InputStream source, int initialSize) throws IOException {
        int capacity = initialSize;
        byte[] buf = new byte[capacity];
        int nread = 0;
        int n;
        for (; ; ) {
            // read to EOF which may read more or less than initialSize (eg: file
            // is truncated while we are reading)
            while ((n = source.read(buf, nread, capacity - nread)) > 0)
                nread += n;

            // if last call to source.read() returned -1, we are done
            // otherwise, try to read one more byte; if that failed we're done too
            if (n < 0 || (n = source.read()) < 0)
                break;

            // one more byte was read; need to allocate a larger buffer
            if (capacity <= MAX_BUFFER_SIZE - capacity) {
                capacity = Math.max(capacity << 1, BUFFER_SIZE);
            } else {
                if (capacity == MAX_BUFFER_SIZE)
                    throw new OutOfMemoryError("Required array size too large");
                capacity = MAX_BUFFER_SIZE;
            }
         /*

         Copies the specified array, truncating or padding with zeros (if necessary)
         so the copy has the specified length.  For all indices that are
         valid in both the original array and the copy, the two arrays will
         contain identical values.  For any indices that are valid in the
         copy but not the original, the copy will contain (byte)0.
         Such indices will exist if and only if the specified length
         is greater than that of the original array.

         @param original the array to be copied
         @param newLength the length of the copy to be returned
         @return a copy of the original array, truncated or padded with zeros
         to obtain the specified length
         @throws NegativeArraySizeException if newLength is negative
         @throws NullPointerException if original is null
         @since 1.6

         */
            buf = Arrays.copyOf(buf, capacity);
            buf[nread++] = (byte) n;
        }
        return (capacity == nread) ? buf : Arrays.copyOf(buf, nread);
    }

    public static byte[] readAllBytes(File path) {
        InputStream in = null;
        try {
            in = new FileInputStream(path);
            if (path.length() > (long) MAX_BUFFER_SIZE)
                throw new OutOfMemoryError("Required array size too large");

            return read(in, (int) path.length());
        } catch (IOException e) {
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    public static List<String> readAllLines(String path, Charset cs) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), cs));
            List<String> result = new ArrayList<>();
            for (; ; ) {
                String line = reader.readLine();
                if (line == null)
                    break;
                result.add(line);
            }
            reader.close();
            return result;
        } catch (IOException ignored) {

        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (Exception ignored) {

            }
        }

        return null;
    }

    public static byte[] readFully(File file) throws IOException {
        FileInputStream stream = new FileInputStream(file);
        try {
            int pos = 0;
            int avail = stream.available();
            byte[] data = new byte[avail];
            while (true) {
                int amt = stream.read(data, pos, data.length - pos);
                //Log.i("foo", "Read " + amt + " bytes at " + pos
                //        + " of avail " + data.length);
                if (amt <= 0) {
                    //Log.i("foo", "**** FINISHED READING: pos=" + pos
                    //        + " len=" + data.length);
                    return data;
                }
                pos += amt;
                avail = stream.available();
                if (avail > data.length - pos) {
                    byte[] newData = new byte[pos + avail];
                    System.arraycopy(data, 0, newData, 0, pos);
                    data = newData;
                }
            }
        } finally {
            stream.close();
        }
    }

    public static byte[] readFully(InputStream stream) throws IOException {
        try {
            int pos = 0;
            int bufferSize = 1024 * 8;
            byte[] data = new byte[bufferSize];
            while (true) {
                int amt = stream.read(data, pos, bufferSize);
                //Log.i("foo", "Read " + amt + " bytes at " + pos
                //        + " of avail " + data.length);
                if (amt <= 0) {
                    //Log.i("foo", "**** FINISHED READING: pos=" + pos
                    //        + " len=" + data.length);
                    return data;
                }
                if (amt < bufferSize) {
                    byte[] buf = new byte[pos + amt];
                    System.arraycopy(data, 0, buf, 0, pos + amt);
                    return buf;
                }
                pos += amt;


                byte[] newData = new byte[pos + bufferSize];
         /*

         Copies an array from the specified source array, beginning at the
         specified position, to the specified position of the destination array.
         A subsequence of array components are copied from the source
         array referenced by src to the destination array
         referenced by dest. The number of components copied is
         equal to the length argument. The components at
         positions srcPos through
         srcPos+length-1 in the source array are copied into
         positions destPos through
         destPos+length-1, respectively, of the destination
         array.

         If the src and dest arguments refer to the
         same array object, then the copying is performed as if the
         components at positions srcPos through
         srcPos+length-1 were first copied to a temporary
         array with length components and then the contents of
         the temporary array were copied into positions
         destPos through destPos+length-1 of the
         destination array.

         If dest is null, then a
         NullPointerException is thrown.

         If src is null, then a
         NullPointerException is thrown and the destination
         array is not modified.

         Otherwise, if any of the following is true, an
         ArrayStoreException is thrown and the destination is
         not modified:

         The src argument refers to an object that is not an
         array.
         The dest argument refers to an object that is not an
         array.
         The src argument and dest argument refer
         to arrays whose component types are different primitive types.
         The src argument refers to an array with a primitive
         component type and the dest argument refers to an array
         with a reference component type.
         The src argument refers to an array with a reference
         component type and the dest argument refers to an array
         with a primitive component type.


         Otherwise, if any of the following is true, an
         IndexOutOfBoundsException is
         thrown and the destination is not modified:

         The srcPos argument is negative.
         The destPos argument is negative.
         The length argument is negative.
         srcPos+length is greater than
         src.length, the length of the source array.
         destPos+length is greater than
         dest.length, the length of the destination array.


         Otherwise, if any actual component of the source array from
         position srcPos through
         srcPos+length-1 cannot be converted to the component
         type of the destination array by assignment conversion, an
         ArrayStoreException is thrown. In this case, let
         k be the smallest nonnegative integer less than
         length such that src[srcPos+k]
         cannot be converted to the component type of the destination
         array; when the exception is thrown, source array components from
         positions srcPos through
         srcPos+k-1
         will already have been copied to destination array positions
         destPos through
         destPos+k-1 and no other
         positions of the destination array will have been modified.
         (Because of the restrictions already itemized, this
         paragraph effectively applies only to the situation where both
         arrays have component types that are reference types.)

         @param      src      the source array.
         @param      srcPos   starting position in the source array.
         @param      dest     the destination array.
         @param      destPos  starting position in the destination data.
         @param      length   the number of array elements to be copied.
         @exception  IndexOutOfBoundsException  if copying would cause
         access of data outside array bounds.
         @exception  ArrayStoreException  if an element in the src
         array could not be stored into the dest array
         because of a type mismatch.
         @exception  NullPointerException if either src or
         dest is null.

         */
                System.arraycopy(data, 0, newData, 0, pos);
                data = newData;

            }
        } finally {
            stream.close();
        }
    }

    public static String readGzipStreamToEnd(InputStream inputStream, String charsetName) {

        GZIPInputStream in = null;
        try {
            in = new GZIPInputStream(inputStream);
            return readToEnd(in, charsetName);
        } catch (IOException e) {

        } finally {
            if (in != null) {
                try {

                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static String readToEnd(InputStream inputStream, String charsetName) {
        try {
         /*

         Reads text from a character-input stream, buffering characters so as to
         provide for the efficient reading of characters, arrays, and lines.

         The buffer size may be specified, or the default size may be used.  The
         default is large enough for most purposes.

         In general, each read request made of a Reader causes a corresponding
         read request to be made of the underlying character or byte stream.  It is
         therefore advisable to wrap a BufferedReader around any Reader whose read()
         operations may be costly, such as FileReaders and InputStreamReaders.  For
         example,


         BufferedReader in
         = new BufferedReader(new FileReader("foo.in"));


         will buffer the input from the specified file.  Without buffering, each
         invocation of read() or readLine() could cause bytes to be read from the
         file, converted into characters, and then returned, which can be very
         inefficient.

         Programs that use DataInputStreams for textual input can be localized by
         replacing each DataInputStream with an appropriate BufferedReader.

         @see FileReader
         @see InputStreamReader
         @see java.nio.file.Files#newBufferedReader

         @author      Mark Reinhold
         @since       JDK1.1

         */
         /*

         An InputStreamReader is a bridge from byte streams to character streams: It
         reads bytes and decodes them into characters using a specified java.nio.charset.Charset charset.  The charset that it uses
         may be specified by name or may be given explicitly, or the platform's
         default charset may be accepted.

         Each invocation of one of an InputStreamReader's read() methods may
         cause one or more bytes to be read from the underlying byte-input stream.
         To enable the efficient conversion of bytes to characters, more bytes may
         be read ahead from the underlying stream than are necessary to satisfy the
         current read operation.

         For top efficiency, consider wrapping an InputStreamReader within a
         BufferedReader.  For example:


         BufferedReader in
         = new BufferedReader(new InputStreamReader(System.in));


         @see BufferedReader
         @see InputStream
         @see java.nio.charset.Charset

         @author      Mark Reinhold
         @since       JDK1.1

         */
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charsetName));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
         /*

         Reads a line of text.  A line is considered to be terminated by any one
         of a line feed ('\n'), a carriage return ('\r'), or a carriage return
         followed immediately by a linefeed.

         @param      ignoreLF  If true, the next '\n' will be skipped

         @return     A String containing the contents of the line, not including
         any line-termination characters, or null if the end of the
         stream has been reached

         @see        java.io.LineNumberReader#readLine()

         @exception  IOException  If an I/O error occurs

         */
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
         /*

         Closes the stream and releases any system resources associated with
         it.  Once the stream has been closed, further read(), ready(),
         mark(), reset(), or skip() invocations will throw an IOException.
         Closing a previously closed stream has no effect.

         @exception  IOException  If an I/O error occurs

         */
            reader.close();
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * Delete the given File and (if it's a directory) everything within it.
     */
    public static void recursivelyDeleteFile(File currentFile) {
        ThreadUtils.assertOnBackgroundThread();
        if (currentFile.isDirectory()) {
            File[] files = currentFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    recursivelyDeleteFile(file);
                }
            }
        }

        if (!currentFile.delete()) Log.e(TAG, "Failed to delete: " + currentFile);
    }

    public static long sizeOfDirectory(File directory) {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        long size = 0;

        File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            return 0L;
        }
        for (int i = 0; i < files.length; i++) {
            File file = files[i];

            if (file.isDirectory()) {
                size += sizeOfDirectory(file);
            } else {
                size += file.length();
            }
        }

        return size;
    }

    public interface ProgressListener {
        public void onProgress(long progress);
    }


}
