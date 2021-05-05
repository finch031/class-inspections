package com.github.clazz.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static com.github.clazz.utils.Utils.checkNotNull;

public class FilterFileVisitor extends SimpleFileVisitor<Path> {
    private final Predicate<Path> fileFilter;
    private final List<Path> files;

    FilterFileVisitor(Predicate<Path> fileFilter) {
        this.fileFilter = checkNotNull(fileFilter);
        this.files = new ArrayList<>();
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException {
        FileVisitResult fileVisitResult = super.visitFile(file, attrs);

        if (fileFilter.test(file)) {
            files.add(file);
        }

        return fileVisitResult;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs){
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc){
        return FileVisitResult.CONTINUE;
    }

    Collection<Path> getFiles() {
        return Collections.unmodifiableCollection(files);
    }
}
