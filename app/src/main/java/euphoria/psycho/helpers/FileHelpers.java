package euphoria.psycho.helpers;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import euphoria.common.Strings;
import euphoria.psycho.common.C;
import euphoria.psycho.knife.DocumentInfo;

public class FileHelpers {

    private static final String TAG = "TAG/" + FileHelpers.class.getSimpleName();

    private static DocumentInfo buildDocumentInfo(Path file) throws IOException {
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

    private static int getType(Path file) {


        String ext = Strings.substringAfterLast(file.getFileName().toString(), ".");
        if (ext == null) return C.TYPE_OTHER;
        ext = ext.toLowerCase();

        if (euphoria.common.Files.isAudio(ext)) return C.TYPE_AUDIO;
        if (euphoria.common.Files.isVideo(ext)) return C.TYPE_VIDEO;

        switch (ext) {

            // https://developer.android.com/guide/appendix/media-formats.html

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

    public static List<DocumentInfo> listDocuments(String dir, int sortBy) throws IOException {
        return Files.list(Paths.get(dir)).parallel()
                .sorted(new Comparator<Path>() {
                    @Override
                    public int compare(Path o1, Path o2) {
                        return 0;
                    }
                }).map(path -> {
                    try {
                        return buildDocumentInfo(path);
                    } catch (IOException e) {
                        return null;
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
}
