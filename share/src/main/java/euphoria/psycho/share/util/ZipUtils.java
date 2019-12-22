package euphoria.psycho.share.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {

    public static void exactInputStream(InputStream inputStream, File dstDir) throws IOException {
        ZipInputStream zis = new ZipInputStream(inputStream);

        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                File dir = new File(dstDir, entry.getName());
                if (!dir.exists()) dir.mkdirs();
            } else {
                File dstFile = new File(dstDir, entry.getName());
                if (!dstFile.isFile()) {
                    byte[] buffer = new byte[1024 * 4];
                    int count;
                    try (FileOutputStream os = new FileOutputStream(dstFile)) {
                        while ((count = zis.read(buffer)) != -1) {
                            os.write(buffer, 0, count);
                        }
                    }

                }
            }


        }

    }
}
