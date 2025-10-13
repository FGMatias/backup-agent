package org.iclassq.executor.impl;

import org.iclassq.entity.Task;
import org.iclassq.executor.ExecutionResult;
import org.iclassq.executor.TaskExecutorStrategy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

@Component("cleanFolderExecutor")
public class CleanFolderExecutor implements TaskExecutorStrategy {
    private static final Logger logger = Logger.getLogger(CleanFolderExecutor.class.getName());

    @Override
    public ExecutionResult execute(Task task) {
        ExecutionResult result = new ExecutionResult(false, "Iniciando limpieza de carpeta");

        try {
            logger.info("Limpiando carpeta: " + task.getSourcePath());

            Path folderPath = Paths.get(task.getSourcePath());

            if (!Files.exists(folderPath)) {
                result.setMessage("Error: La carpeta no existe");
                result.setStatusId(2);
                return result;
            }

            AtomicLong deletedCount = new AtomicLong(0);
            AtomicLong freedBytes = new AtomicLong(0);

            Files.walkFileTree(folderPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    long fileSize = attrs.size();
                    Files.delete(file);
                    deletedCount.incrementAndGet();
                    freedBytes.addAndGet(fileSize);
                    logger.fine("Eliminado: " + file.getFileName());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (!dir.equals(folderPath)) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            result.setSuccess(true);
            result.setMessage(String.format("Eliminados %d archivos", deletedCount.get()));
            result.setSize(formatFileSize(freedBytes.get()));
            result.setStatusId(1);

            logger.info(String.format("Limpieza completada: %d archivos, %s liberados",
                    deletedCount.get(), formatFileSize(freedBytes.get())));

        } catch (Exception e) {
            logger.severe("Error al limpiar carpeta: " + e.getMessage());
            result.setMessage("Error: " + e.getMessage());
            result.setStatusId(2);
        } finally {
            result.finish();
        }

        return result;
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
}