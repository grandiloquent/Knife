package euphoria.psycho.helpers;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import euphoria.common.Strings;

public class FileHelpers {

    private static final String TAG = "TAG/" + FileHelpers.class.getSimpleName();

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

    ;

}
