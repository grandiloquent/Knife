package euphoria.psycho.notes.common;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import euphoria.common.Files;
import euphoria.common.Keys;

public class Utils {

    public static void moveFiles(Context context, File directory) {
        File[] htmlFiles = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile() && pathname.getName().endsWith(".html")) return true;
                return false;
            }
        });
        if (htmlFiles == null || htmlFiles.length == 0) return;
        Arrays.sort(htmlFiles, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });


        File dstDir = new File(Environment.getExternalStorageDirectory(),
                "Browser/static/pictures");
        if (!dstDir.isDirectory()) {
            dstDir.mkdirs();
        }
        StringBuilder sb = new StringBuilder();
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        for (int i = 0, j = htmlFiles.length; i < j; i++) {
            moveImages(htmlFiles[i], dstDir, sb);
        }
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null, sb.toString()));
        try {
            Files.combineHtmls(directory, directory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void moveImages(File htmlFile, File dstDir, StringBuilder sb) {
        Pattern imagePattern = Pattern.compile("(?<=src=\")[^\"]+(?=\")");

        try {
            // " src="../images/00202.jpeg"/></div>

            byte[] buf = euphoria.common.Files.readFully(htmlFile);
            Matcher imageMatcher = imagePattern.matcher(new String(buf, "UTF-8"));
            List<String> images = new ArrayList<>();
            while (imageMatcher.find()) {
                images.add(imageMatcher.group());
            }
            Collections.sort(images, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
            File parent = htmlFile.getParentFile();
            for (int i = 0, j = images.size(); i < j; i++) {
                File imageFile = new File(parent, images.get(i)).getCanonicalFile();
                InputStream in = new FileInputStream(imageFile);
                File dst = new File(dstDir, Long.toString(Keys.crc64Long(Keys.getBytes(parent.getAbsolutePath()))) + "_" + imageFile.getName());
                OutputStream out = new FileOutputStream(dst);
                euphoria.common.Files.copy(in, out);
                in.close();
                out.close();
                String img = String.format("<div class=\"img-center\"><img alt=\"\" src=\"../static/pictures/%s\"><div class=\"img-caption\"></div></div>",
                        dst.getName());
                sb.append(img).append("\n\n");
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

