package redis.embedded.util;

import com.google.common.io.Resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class JarUtil {

    public static File extractExecutableFromJar(String executable) throws IOException {
        File command = extractFileFromJar(executable);
        command.setExecutable(true);

        return command;
    }

    public static File extractFileFromJar(String path) throws IOException {
        File tmpDir = Files.createTempDirectory(null).toFile();
        tmpDir.deleteOnExit();

        File file = new File(tmpDir, path);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            Resources.copy(Resources.getResource(path), fileOutputStream);
        }
        file.deleteOnExit();
        return file;
    }
}
