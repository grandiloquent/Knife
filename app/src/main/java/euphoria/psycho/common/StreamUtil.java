package euphoria.psycho.common;

import java.io.Closeable;
import java.io.IOException;
import java.util.zip.ZipFile;

/**
 * Helper methods to deal with stream related tasks.
 */
public class StreamUtil {
    public static final int DEFAULT_BUFFER_SIZE= 4096;
    /**
     * Handle closing a {@link Closeable} via {@link Closeable#close()} and catch
     * the potentially thrown {@link IOException}.
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

    /**
     * Overload of the above function for {@link ZipFile} which implements Closeable only starting
     * from api19.
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
}
