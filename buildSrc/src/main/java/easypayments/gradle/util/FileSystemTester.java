package easypayments.gradle.util;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public class FileSystemTester {

    public boolean isDirectoryNotEmpty(Path dir) throws IOException {
        if (!Files.exists(dir))
            return false;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            return stream.iterator().hasNext();
        }
    }

}
