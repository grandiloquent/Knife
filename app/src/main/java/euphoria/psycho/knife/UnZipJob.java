package euphoria.psycho.knife;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import euphoria.psycho.common.FileUtils;
import euphoria.psycho.common.StringUtils;

public class UnZipJob {

    private UnZipListener mUnZipListener;

    public UnZipJob(UnZipListener listener) {
        mUnZipListener = listener;
    }


    private void unTarGz(File srcFile, File outputDirectory) throws Exception {
        if (outputDirectory == null) {
            outputDirectory = new File(srcFile.getParentFile(), StringUtils.substringBeforeLast(srcFile.getName(), ".tar.gz"));
            if (!outputDirectory.isDirectory())
                outputDirectory.mkdir();
        }
        FileInputStream inputStream = new FileInputStream(srcFile);
        byte[] data = FileUtils.toByteArray(inputStream);
        FileUtils.closeSilently(inputStream);

        ByteArrayInputStream bai = new ByteArrayInputStream(data);
        GzipCompressorInputStream gci = new GzipCompressorInputStream(bai);
        TarArchiveInputStream tai = new TarArchiveInputStream(gci);

        try {
            byte[] buffer = new byte[FileUtils.DEFAULT_BUFFER_SIZE];
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

    public void unzip(String srcPath) {
        if (srcPath.endsWith(".tar.gz")) {
            try {
                unTarGz(new File(srcPath), null);
            } catch (Exception e) {
                if (mUnZipListener != null) {
                    mUnZipListener.onError(e);
                }
            }
        } else if (srcPath.endsWith(".zip") || srcPath.endsWith(".epub")) {
            File srcFile = new File(srcPath);
            File outdir = new File(srcFile.getParentFile(), StringUtils.substringAfterLast(srcFile.getName(), "."));
            outdir.mkdirs();
            extract(new File(srcPath), outdir);
        }
    }

    private static String dirpart(String name) {
        int s = name.lastIndexOf(File.separatorChar);
        return s == -1 ? null : name.substring(0, s);
    }

    public static void extract(File zipfile, File outdir) {
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

    private static void extractFile(ZipInputStream in, File outdir, String name) throws IOException {
        byte[] buffer = new byte[FileUtils.DEFAULT_BUFFER_SIZE];
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(outdir, name)));
        int count = -1;
        while ((count = in.read(buffer)) != -1)
            out.write(buffer, 0, count);
        out.close();
    }

    private static void mkdirs(File outdir, String path) {
        File d = new File(outdir, path);
        if (!d.exists())
            d.mkdirs();
    }


    public interface UnZipListener {
        void onError(Exception exception);
    }
}