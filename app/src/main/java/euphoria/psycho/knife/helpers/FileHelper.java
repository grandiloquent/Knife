package euphoria.psycho.knife.helpers;

import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import euphoria.common.Strings;
import euphoria.psycho.common.C;
import euphoria.psycho.knife.DocumentInfo;

public class FileHelper {
    private static final String TAG = "TAG/" + FileHelper.class.getSimpleName();

    public static List<DocumentInfo> searchDocumentInfo(String dir, String pattern) throws IOException {

        Pattern p = Pattern.compile(pattern);
        return Files.walk(Paths.get(dir), Integer.MAX_VALUE)
                .filter(path -> p.matcher(path.getFileName().toString()).find())
                .map(path -> {
                    try {
                        return buildDocumentInfo(path);
                    } catch (Exception ignored) {

                    }
                    return null;
                })
                .collect(Collectors.toList());
    }

    public static DocumentInfo buildDocumentInfo(Path file) throws IOException {
        boolean isDirectory = Files.isDirectory(file);

        DocumentInfo.Builder builder = new DocumentInfo.Builder()
                .setFileName(file.getFileName().toString())
                .setLastModified(Files.getLastModifiedTime(file).to(TimeUnit.SECONDS))
                .setPath(file.toAbsolutePath().toString())
                .setType(isDirectory ? C.TYPE_DIRECTORY : getType(file));

        if (isDirectory) {
            builder.setSize(Files.list(file).count());
        } else {
            builder.setSize(Files.size(file));
        }
        return builder.build();
    }

    private static void checkDirectory(final File directory) {
        if (!directory.exists()) {
            throw new IllegalArgumentException(directory + " does not exist");
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(directory + " is not a directory");
        }
    }

    private static int compareFileSize(DocumentInfo path1, DocumentInfo path2) {
        boolean a = path1.getType() == C.TYPE_DIRECTORY;
        boolean b = path2.getType() == C.TYPE_DIRECTORY;

        long size1 = 0;
        if (a) {
            size1 = 0;
        } else {
            size1 = path1.getSize();
        }

        long size2 = 0;
        if (b) {
            size2 = 0;
        } else {
            size2 = path2.getSize();
        }
        long result = size1 - size2;
        if (result < 0) {
            return -1;
        } else if (result > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    private static int compareFileName(DocumentInfo path1, DocumentInfo path2) {

        boolean a = path1.getType() == C.TYPE_DIRECTORY;
        boolean b = path2.getType() == C.TYPE_DIRECTORY;


        if (a == b) {
            Collator collator = Collator.getInstance(Locale.CHINA);
            return collator.compare(path1.getFileName(),
                    path2.getFileName());
        } else {
            return -1;
        }
    }

    private static int compareFileLastModified(DocumentInfo path1, DocumentInfo path2) {

        boolean a = path1.getType() == C.TYPE_DIRECTORY;
        boolean b = path2.getType() == C.TYPE_DIRECTORY;


        if (a == b) {
            long result =
                    path1.getLastModified()
                            - path2.getLastModified();
            if (result < 0) {
                return -1;
            } else if (result > 0) {
                return 1;
            } else {
                return 0;
            }


        } else if (a) {// 如果是目录 返回 -1 默认排在前面(升序)
            return -1;
        } else {
            return 1;
        }
    }

    public static int getType(Path file) {


        String ext = Strings.substringAfterLast(file.getFileName().toString(), ".");
        if (ext == null) return C.TYPE_OTHER;
        ext = ext.toLowerCase();

        switch (ext) {

            // https://developer.android.com/guide/appendix/media-formats.html
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
                return C.TYPE_AUDIO;
            case "3gp":
            case "mkv":
            case "mp4":
            case "ts":
            case "webm":
            case "vm":
            case "crdownload":
                return C.TYPE_VIDEO;
            case "txt":
            case "css":
            case "log":
            case "js":
            case "java":
            case "xml":
            case "htm":
            case "html":
            case "xhtml":
            case "srt":
            case "mht":
            case "md":
                return C.TYPE_TEXT;
            case "pdf":
                return C.TYPE_PDF;
            case "apk":
                return C.TYPE_APK;
            case "zip":
            case "rar":
            case "gz":
            case "epub":
                return C.TYPE_ZIP;
            case "bmp":
            case "gif":
            case "jpg":
            case "png":
            case "webp":
                return C.TYPE_IMAGE;
            default:
                return C.TYPE_OTHER;
        }
    }

    public static boolean isSymlink(final File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("File must not be null");
        }
        return Files.isSymbolicLink(file.toPath());
    }

    public static List<DocumentInfo> listDocuments(String dir, int sortBy, boolean isAscending, FilenameFilter filenameFilter) throws IOException {
        // https://github.com/apache/commons-io/tree/master/src/main/java/org/apache/commons/io/comparator
        final int direction = isAscending ? 1 : -1;

        return Files.list(Paths.get(dir)).parallel()
                .map(path -> {
                    try {
                        return buildDocumentInfo(path);
                    } catch (IOException e) {
                        return null;
                    }
                }).sorted(new Comparator<DocumentInfo>() {
                    @Override
                    public int compare(DocumentInfo o1, DocumentInfo o2) {

                        switch (sortBy) {
                            case C.SORT_BY_SIZE:
                                return compareFileSize(o1, o2) * direction;

                            case C.SORT_BY_DATE_MODIFIED:
                                return compareFileLastModified(o1, o2) * direction;
                            default:
                                return compareFileName(o1, o2) * direction;
                        }


                    }
                }).collect(Collectors.toList());

    }

    public static void moveFilesByKeywords(String dir) throws IOException {
        String[] keywords = new String[]{
                "C#",
                "C++",
                "C",
                "Python",
                "JavaScript",
                "OpenCV",
                "Google",
                "Adobe",
                "Kotlin",
                "AI",
                "TensorFlow",
                "OpenGL",
                "Android",
                "CSS",
                "Java",
                "HTTP",
                "Blockchain",
                "SQL",
                "Node",
                "HTML",
                "NET",
                "Wireshark",
                "Embedded",
                "Drones",
                "Microservices",
                "Microservice",
                "Web",
                "Cookbook",
                "CentOS",
                "Linux",
                "UX",
                "Go",
                "HBR",
                "Windows",
                "Make",
                "TypeScript",
                "PostgreSQL",
                "Nginx",
                "Elixir",
                "Audio",
                "Haskell",
                "Kubernetes",
                "SVG",
                "Git",
                "Security",
                "Hacking",
                "React",
                "Analysis",
                "Computer Vision",
                "Machine Learning",
                "Neural Networks",
                "Assembly Language",
                "Deep Learning",
                "MongoDB",
                "Nutshell",
                "Data Science",
                "Regular Expressions",
                "Jump Start",
                "Natural Language",
                "For Dummies",
                "Data",
        };
        File baseDirectory = new File(dir, "Extensions");

        if (!baseDirectory.isDirectory()) baseDirectory.mkdir();

        Files.newDirectoryStream(Paths.get(dir), path -> path.toFile().isFile())
                .forEach(file -> {
//                    if (file.getParent().toAbsolutePath().toString().contains("/Extensions/")) {
//                        return;
//                    }
                    Log.e(TAG, "Debug: moveFilesByKeywords, " + file.toAbsolutePath());

                    String fileName = file.getFileName().toString();
                    //List<String> pieces = Arrays.asList(Strings.substringBeforeLast(fileName, '.').split(" "));
                    String f = Strings.substringBeforeLast(fileName, '.');

                    for (String k : keywords) {
                        if (Pattern.compile("\\b" + k + "\\b").matcher(f).find()) {
                            File targetDirectory = new File(baseDirectory, k);
                            if (!targetDirectory.isDirectory()) targetDirectory.mkdir();
                            File targetFile = new File(targetDirectory, fileName);
                            file.toFile().renameTo(targetFile);
                            //Log.e(TAG, "Debug: moveFilesByKeywords, " + targetFile.getAbsolutePath());


                            break;
                        }
                    }
                });
    }

    private static long sizeOf0(final File file) {
        if (file.isDirectory()) {
            return sizeOfDirectory0(file);
        }
        return file.length(); // will be 0 if file does not exist
    }

    public static long sizeOfDirectory(final File directory) {
        checkDirectory(directory);
        return sizeOfDirectory0(directory);
    }

    private static long sizeOfDirectory0(final File directory) {
        final File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            return 0L;
        }
        long size = 0;

        for (final File file : files) {
            try {
                if (!isSymlink(file)) {
                    size += sizeOf0(file); // internal method
                    if (size < 0) {
                        break;
                    }
                }
            } catch (final IOException ioe) {
                // Ignore exceptions caught when asking if a File is a symlink.
            }
        }

        return size;
    }
}
