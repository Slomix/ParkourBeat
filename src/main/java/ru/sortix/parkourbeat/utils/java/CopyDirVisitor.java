package ru.sortix.parkourbeat.utils.java;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CopyDirVisitor extends SimpleFileVisitor<Path> {
    private final @NonNull Logger logger;
    private final @NonNull Path sourceDir;
    private final @NonNull Path targetDir;
    @Getter private boolean failed = false;

    @Override
    public FileVisitResult visitFile(@NonNull Path file, @NonNull BasicFileAttributes attrs) {
        Path targetPath = this.targetDir.resolve(this.sourceDir.relativize(file));
        try {
            Files.copy(file, targetPath);
            return FileVisitResult.CONTINUE;
        } catch (IOException e) {
            this.logger.log(Level.SEVERE, "Unable to copy file from " + file + " to " + targetPath, e);
            this.failed = true;
            return FileVisitResult.TERMINATE;
        }
    }

    @Override
    public FileVisitResult preVisitDirectory(@NonNull Path dir, @NonNull BasicFileAttributes attrs) {
        Path newDir = this.targetDir.resolve(this.sourceDir.relativize(dir));
        try {
            Files.createDirectory(newDir);
            return FileVisitResult.CONTINUE;
        } catch (IOException e) {
            this.logger.log(Level.SEVERE, "Unable to create directory " + newDir, e);
            this.failed = true;
            return FileVisitResult.TERMINATE;
        }
    }
}
