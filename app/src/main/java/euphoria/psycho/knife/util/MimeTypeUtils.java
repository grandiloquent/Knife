package euphoria.psycho.knife.util;

public class MimeTypeUtils {
    public static String getMimeTypeForIntent(String fileName) {

        String ext = StringUtils.substringAfterLast(fileName, ".");
        if (ext == null) return "application/*";
        ext = ext.toLowerCase();

        switch (ext) {
            case "aac":
            case "flac":
            case "imy":
            case "m4a":
            case "mid":
            case "mp3":
            case "mxmf":
            case "ogg":
            case "ota":
            case "rtttl":
            case "rtx":
            case "wav":
            case "xmf":
                return "";
            // https://developer.android.com/guide/appendix/media-formats.html
            case "3gp":
            case "mkv":
            case "mp4":
            case "ts":
            case "webm":
                return "";
            case "txt":
            case "css":
            case "log":
            case "js":
            case "java":
            case "xml":
            case "htm":
            case "srt":
                return "";
            case "pdf":
                return "";
            case "apk":
                return "";
            case "zip":
            case "rar":
            case "gz":
                return "";
            case "bmp":
            case "gif":
            case "jpg":
            case "png":
            case "webp":
                return "image/*";
            default:
                return "";
        }
    }

}
