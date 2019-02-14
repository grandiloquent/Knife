package euphoria.psycho.knife;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import euphoria.psycho.common.Log;
import euphoria.psycho.common.StringUtils;

public class UnZipJob {

    public interface UnZipListener {
        void onError(Exception exception);
    }

    public UnZipJob(UnZipListener listener) {
        mUnZipListener = listener;
    }

    private UnZipListener mUnZipListener;

    public void unzip(String srcPath) {
        if (srcPath.endsWith(".tar.gz")) {
            try {
                unTarGz(new File(srcPath), null);
            } catch (Exception e) {
                if (mUnZipListener != null) {
                    mUnZipListener.onError(e);
                }
            }
        }
    }

    public static byte[] readAllBytes(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] result = null;
        try {
            byte[] fetchByte = new byte[1024];
            int readByte = fis.read(fetchByte);
            while (readByte != -1) {
                baos.write(fetchByte, 0, readByte);
                readByte = fis.read(fetchByte);
            }
            result = baos.toByteArray();
        } catch (Exception e) {
        } finally {
            baos.close();
            fis.close();
        }
        return result;
    }



    private void unTarGz(File srcFile, File outputDirectory) throws Exception {
        if (outputDirectory == null) {
            outputDirectory = new File(srcFile.getParentFile(), StringUtils.substringBeforeLast(srcFile.getName(), ".tar.gz"));
            if (!outputDirectory.isDirectory())
                outputDirectory.mkdir();
        }
        byte[] data = readAllBytes(srcFile);


        ByteArrayInputStream bai = new ByteArrayInputStream(data);
        GzipCompressorInputStream gci = new GzipCompressorInputStream(bai);
        TarArchiveInputStream tai = new TarArchiveInputStream(gci);

        try {
            byte[] buffer = new byte[1024];
            TarArchiveEntry entry = tai.getNextTarEntry();
            while (entry != null) {
                if (entry.isDirectory()) {
                    File dir = new File(outputDirectory, entry.getName());
                    dir.mkdirs();
                } else {

                    File targetFile = new File(outputDirectory, entry.getName());
                    FileOutputStream outputStream = new FileOutputStream(targetFile);
                    while (tai.read(buffer) != -1) {
                        outputStream.write(buffer);
                    }
                    outputStream.close();


                }


                // readTarRecursively(entry);
//                readByte = tai.read(buffer);
//                while (readByte != -1) {
//
//
//                }
                entry = tai.getNextTarEntry();
            }
        } finally {
            tai.close();
            gci.close();
            bai.close();
        }

    }
}
