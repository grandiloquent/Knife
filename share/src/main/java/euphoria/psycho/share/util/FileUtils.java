package euphoria.psycho.share.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Locale;

public class FileUtils {
    public static final int DEFAULT_BUFFER_SIZE = 4096;
    public static final int EOF = -1;
    private static final int SKIP_BUFFER_SIZE = 2048;
    private static byte[] SKIP_BYTE_BUFFER;
    private static char[] SKIP_CHAR_BUFFER;
    public static String changeExtension(String path, String extension) {
        if (path != null) {
            String s = path;
            int length = path.length();
            for (int i = length; --i >= 0; ) {
                char ch = path.charAt(i);
                if (ch == '.') {
                    s = path.substring(0, i);
                    break;
                }
                if (ch == File.separatorChar)
                    break;
            }
            if (extension != null && path.length() != 0) {
                if (extension.length() == 0 || extension.charAt(0) != '.') {
                    s = s + ".";
                }
                s = s + extension;
            }
            return s;
        }
        return null;
    }

    /**
     * Handle closing a {@link Closeable} via {@link Closeable#close()} and catch
     * the potentially thrown {@link IOException}.
     *
     * @param closeable The Closeable to be closed.
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable == null) return;

        try {
            closeable.close();
        } catch (IOException ex) {
            // Ignore the exception on close.
        }
    }

    public static long copy(final InputStream input, final OutputStream output, final int bufferSize)
            throws IOException {
        return copyLarge(input, output, new byte[bufferSize]);
    }

    public static int copy(final InputStream input, final OutputStream output) throws IOException {
        final long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    /**
     * Atomically copies the data from an input stream into an output file.
     *
     * @param is      Input file stream to read data from.
     * @param outFile Output file path.
     * @param buffer  Caller-provided buffer. Provided to avoid allocating the same
     *                buffer on each call when copying several files in sequence.
     * @throws IOException in case of I/O error.
     */
    public static void copyFileStreamAtomicWithBuffer(InputStream is, File outFile, byte[] buffer)
            throws IOException {
        File tmpOutputFile = new File(outFile.getPath() + ".tmp");
        try (OutputStream os = new FileOutputStream(tmpOutputFile)) {

            int count = 0;
            while ((count = is.read(buffer, 0, buffer.length)) != -1) {
                os.write(buffer, 0, count);
            }
        }
        if (!tmpOutputFile.renameTo(outFile)) {
            throw new IOException();
        }
    }

    public static long copyLarge(final InputStream input, final OutputStream output, final byte[] buffer)
            throws IOException {
        long count = 0;
        int n;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static long copyLarge(final InputStream input, final OutputStream output)
            throws IOException {
        return copy(input, output, DEFAULT_BUFFER_SIZE);
    }

    public static long copyLarge(final Reader input, final Writer output, final long inputOffset, final long length,
                                 final char[] buffer)
            throws IOException {
        if (inputOffset > 0) {
            skipFully(input, inputOffset);
        }
        if (length == 0) {
            return 0;
        }
        int bytesToRead = buffer.length;
        if (length > 0 && length < buffer.length) {
            bytesToRead = (int) length;
        }
        int read;
        long totalRead = 0;
        while (bytesToRead > 0 && EOF != (read = input.read(buffer, 0, bytesToRead))) {
            output.write(buffer, 0, read);
            totalRead += read;
            if (length > 0) { // only adjust length if not reading to the end
                // Note the cast must work because buffer.length is an integer
                bytesToRead = (int) Math.min(length - totalRead, buffer.length);
            }
        }
        return totalRead;
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

    public static String getExtension(String path) {
        if (path == null)
            return null;

        int length = path.length();
        for (int i = length; --i >= 0; ) {
            char ch = path.charAt(i);
            if (ch == '.') {
                if (i != length - 1)
                    return path.substring(i + 1);
                else
                    return "";
            }
            if (ch == File.separatorChar)
                break;
        }
        return "";
    }

    public static Collection<File> getFiles(File directory, final FileFilter filter, boolean includeSubDirectories) {
        final Collection<File> files = new LinkedList<>();
        innerListFiles(files, directory,
                filter, includeSubDirectories);
        return files;
    }

    public static File getUniqueFile(File src) {
        if (!src.isFile()) return src;
        int dotIndex = src.getName().lastIndexOf('.');
        String name = "";
        String ext = "";
        int count = 1;
        if (dotIndex != -1) {
            ext = src.getName().substring(dotIndex);
            name = src.getName().substring(0, dotIndex);
        } else {
            name = src.getName();
        }
        File parentFile = src.getParentFile();
        File dstFile = new File(parentFile, name + " (" + count + ")" + ext);
        while (dstFile.isFile()) {
            if (++count > 32) {
                throw new IllegalStateException();
            }
            dstFile = new File(parentFile, name + " (" + count + ")" + ext);

        }
        return dstFile;
    }

    public static String getValidFilName(String fileName, char replaceChar) {
        StringBuilder stringBuilder = new StringBuilder(fileName.length());
        for (int i = 0; i < fileName.length(); i++) {
            if (isValidFatFilenameChar(fileName.charAt(i))) {
                stringBuilder.append(fileName.charAt(i));

            } else {
                stringBuilder.append(replaceChar);
            }
        }
        return stringBuilder.toString();
    }

    private static void innerListFiles(final Collection<File> files, final File directory,
                                       final FileFilter filter, final boolean includeSubDirectories) {
        final File[] found = directory.listFiles(filter);
        if (found != null) {
            for (final File file : found) {
                if (file.isDirectory()) {
                    if (includeSubDirectories) {
                        files.add(file);
                    }
                    innerListFiles(files, file, filter, includeSubDirectories);
                } else {
                    files.add(file);
                }
            }
        }
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

    public static long skip(final InputStream input, final long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
        }
        /*
         * N.B. no need to synchronize this because: - we don't care if the buffer is created multiple times (the data
         * is ignored) - we always use the same size buffer, so if it it is recreated it will still be OK (if the buffer
         * size were variable, we would need to synch. to ensure some other thread did not create a smaller one)
         */
        if (SKIP_BYTE_BUFFER == null) {
            SKIP_BYTE_BUFFER = new byte[SKIP_BUFFER_SIZE];
        }
        long remain = toSkip;
        while (remain > 0) {
            // See https://issues.apache.org/jira/browse/IO-203 for why we use read() rather than delegating to skip()
            final long n = input.read(SKIP_BYTE_BUFFER, 0, (int) Math.min(remain, SKIP_BUFFER_SIZE));
            if (n < 0) { // EOF
                break;
            }
            remain -= n;
        }
        return toSkip - remain;
    }

    public static long skip(final Reader input, final long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
        }
        /*
         * N.B. no need to synchronize this because: - we don't care if the buffer is created multiple times (the data
         * is ignored) - we always use the same size buffer, so if it it is recreated it will still be OK (if the buffer
         * size were variable, we would need to synch. to ensure some other thread did not create a smaller one)
         */
        if (SKIP_CHAR_BUFFER == null) {
            SKIP_CHAR_BUFFER = new char[SKIP_BUFFER_SIZE];
        }
        long remain = toSkip;
        while (remain > 0) {
            // See https://issues.apache.org/jira/browse/IO-203 for why we use read() rather than delegating to skip()
            final long n = input.read(SKIP_CHAR_BUFFER, 0, (int) Math.min(remain, SKIP_BUFFER_SIZE));
            if (n < 0) { // EOF
                break;
            }
            remain -= n;
        }
        return toSkip - remain;
    }

    public static void skipFully(final Reader input, final long toSkip) throws IOException {
        final long skipped = skip(input, toSkip);
        if (skipped != toSkip) {
            throw new EOFException("Chars to skip: " + toSkip + " actual: " + skipped);
        }
    }

    public static void sortByName(File[] files, boolean isAscending) {

        Collator collator = Collator.getInstance(Locale.CHINA);
        Arrays.sort(files, (o1, o2) -> {
            boolean b1 = o1.isDirectory();
            boolean b2 = o2.isDirectory();
            if (b1 == b2) {
                return collator.compare(o1.getName(), o2.getName());
            } else if (b1) {
                return -1;
            } else {
                return 1;
            }
        });
    }

    public static void sortFiles(File[] files, int sortBy, boolean ascend) {
        // 0 name
        // 1 last modified
        // 2 size

        Collator collator = Collator.getInstance(Locale.CHINA);
        Arrays.sort(files, (o1, o2) -> {
            boolean b1 = o1.isDirectory();
            boolean b2 = o2.isDirectory();
            if (b1 == b2) {

                switch (sortBy) {
                    case 0:
                        int compare = collator.compare(o1.getName(), o2.getName());
                        return ascend ? compare :
                                compare * -1;
                    case 1:
                        long difLastModified = o1.lastModified() - o2.lastModified();
                        if (difLastModified > 0) {
                            return ascend ? 1 : -1;
                        } else if (difLastModified < 0) {
                            return ascend ? -1 : 1;
                        } else {
                            return 0;
                        }
                    case 2:
                        if (b1) return 0;
                        long difSize = o1.length() - o2.length();
                        if (difSize > 0) {
                            return ascend ? 1 : -1;
                        } else if (difSize < 0) {
                            return ascend ? -1 : 1;
                        } else {
                            return 0;
                        }
                    default:
                        return 0;
                }

            } else if (b1) {

                return ascend ? -1 : 1;
            } else {
                return ascend ? 1 : -1;
            }
        });
    }

    public static byte[] toByteArray(final InputStream input) throws IOException {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            copy(input, output);
            return output.toByteArray();
        }
    }
}

