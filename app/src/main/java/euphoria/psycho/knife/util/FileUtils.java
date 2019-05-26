package euphoria.psycho.knife.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FileUtils {
    private static final int BUFFER_SIZE = 8192;
    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

    public static byte[] compressGzip(final String str) throws IOException {
        if ((str == null) || (str.length() == 0)) {
            return null;
        }
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(str.getBytes("UTF-8"));
        gzip.flush();
        gzip.close();
        return obj.toByteArray();
    }

    private static final char DirectorySeparatorChar = '\\';
    private static final   char AltDirectorySeparatorChar = '/';
    private static final  char VolumeSeparatorChar = ':';
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

    public static String decompressGzip(final byte[] compressed) throws IOException {
        final StringBuilder outStr = new StringBuilder();
        if ((compressed == null) || (compressed.length == 0)) {
            return "";
        }
        if (isCompressed(compressed)) {
            final GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gis, "UTF-8"));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                outStr.append(line).append('\n');
            }
        } else {
            outStr.append(compressed);
        }
        return outStr.toString();
    }

    public static boolean hasSDCardPath() {
        File[] directories = new File("/storage").listFiles();
        if (directories.length < 3) return false;
        return true;
    }

    public static void writeAllText(File file, String content) {
        try {
            FileOutputStream out = new FileOutputStream(file);
            byte[] bytes = content.getBytes("utf-8");
            out.write(bytes, 0, bytes.length);

            out.close();
        } catch (Exception e) {

        }
    }


    public static boolean isCompressed(final byte[] compressed) {
        return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }

    private static byte[] read(InputStream source, int initialSize) throws IOException {
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
            buf = Arrays.copyOf(buf, capacity);
            buf[nread++] = (byte) n;
        }
        return (capacity == nread) ? buf : Arrays.copyOf(buf, nread);
    }

    public static byte[] readAllBytes(File file) {
        try {
            InputStream in = new FileInputStream(file);
            byte[] bytes = read(in, (int) file.length());
            in.close();
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
