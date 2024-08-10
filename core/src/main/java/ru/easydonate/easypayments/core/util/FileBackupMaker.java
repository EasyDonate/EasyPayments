package ru.easydonate.easypayments.core.util;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class FileBackupMaker {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy-HHmmss");

    public Path createFileBackup(Path filePath) throws IOException {
        if (!Files.isRegularFile(filePath))
            return null;

        Path backupPath = getBackupPath(filePath);
        if (Files.isDirectory(backupPath))
            Files.walkFileTree(backupPath, new DeletingFileVisitor());

        Files.copy(filePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        return backupPath;
    }

    public Path getBackupPath(Path originalPath) {
        Path parentDir = originalPath.getParent();
        String name = originalPath.getFileName().toString();
        String timestamp = LocalDateTime.now(ZoneOffset.UTC).format(DATE_TIME_FORMATTER);
        return parentDir.resolve(name + '@' + timestamp + ".bak");
    }

}
