package euphoria.psycho.knife;

import android.content.Context;
import android.content.res.ColorStateList;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.content.res.AppCompatResources;
import euphoria.psycho.common.C;
import euphoria.psycho.common.StringUtils;


public class DocumentUtils {
    private static final String TAG = "TAG/" + DocumentUtils.class.getSimpleName();

    static {
        System.loadLibrary("native-lib");
    }

    public static List<DocumentInfo> getDocumentInfos(File dir, int sortBy) {

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) return null;

        List<DocumentInfo> infos = new ArrayList<>();
        boolean isAscending = false;

        for (File file : files) {
            DocumentInfo.Builder builder = new DocumentInfo.Builder()
                    .setFileName(file.getName())
                    .setLastModified(file.lastModified())
                    .setPath(file.getAbsolutePath())
                    .setType(getType(file));

            if (file.isDirectory()) {
                File[] fs = file.listFiles();
                builder.setSize(fs == null ? 0 : fs.length);
            } else {
                builder.setSize(file.length());
            }


            infos.add(builder.build());
        }

        switch (sortBy) {
            case C.SORT_BY_NAME:
                Collator collator = Collator.getInstance(Locale.CHINA);
                Collections.sort(infos, (o1, o2) -> {
                    boolean b1 = o1.getType() == C.TYPE_DIRECTORY;
                    boolean b2 = o2.getType() == C.TYPE_DIRECTORY;
                    if (b1 == b2)
                        if (isAscending)
                            return collator.compare(o1.getFileName(), o2.getFileName());
                        else
                            return collator.compare(o1.getFileName(), o2.getFileName()) * -1;
                    else if (b1) {

                        if (isAscending) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else {
                        if (isAscending) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                });
                break;
            case C.SORT_BY_SIZE:
                Collections.sort(infos, (o1, o2) -> {
                    boolean b1 = o1.getType() == C.TYPE_DIRECTORY;
                    boolean b2 = o2.getType() == C.TYPE_DIRECTORY;

                    if (b1 == b2) {
                        if (isAscending)
                            return o1.getSize() >= o2.getSize() ? 1 : -1;
                        else return o1.getSize() >= o2.getSize() ? -1 : 1;
                    } else if (b1) {
                        if (isAscending) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else {
                        if (isAscending) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                });
                break;
            case C.SORT_BY_DATE_MODIFIED:

                Collections.sort(infos, (o1, o2) -> {
                    boolean b1 = o1.getType() == C.TYPE_DIRECTORY;
                    boolean b2 = o2.getType() == C.TYPE_DIRECTORY;

                    if (b1 == b2) {
                        if (isAscending)
                            return o1.getLastModified() >= o2.getLastModified() ? 1 : -1;
                        else
                            return o1.getLastModified() <= o2.getLastModified() ? 1 : -1;

                    } else if (b1) {
                        if (isAscending) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else {
                        if (isAscending) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                });
                break;
        }

        return infos;
    }

    public static int getType(File file) {
        if (file.isDirectory()) return C.TYPE_DIRECTORY;
        String ext = StringUtils.substringAfterLast(file.getName(), ".");
        if (ext == null) return C.TYPE_OTHER;
        ext = ext.toLowerCase();
        switch (ext) {
            case "mp3":
                return C.TYPE_AUDIO;
            case "mp4":
                return C.TYPE_VIDEO;
            case "txt":
            case "css":
            case "log":
            case "js":
            case "htm":
            case "srt":
                return C.TYPE_TEXT;
            case "pdf":
                return C.TYPE_PDF;
            case "apk":
                return C.TYPE_APK;
            case "zip":
            case "rar":
            case "gz":
                return C.TYPE_ZIP;
            default:
                return C.TYPE_OTHER;
        }
    }

    public static ColorStateList getIconForegroundColorList(Context context) {
        return AppCompatResources.getColorStateList(context, R.color.white_mode_tint);
    }

    public static native int deleteDirectories(String[] directories);

    public static native long calculateDirectory(String dir);
}
