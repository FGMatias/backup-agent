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

@Component("moveFilesExecutor")
public class MoveFilesExecutor implements TaskExecutorStrategy {
    private static final Logger logger = Logger.getLogger(MoveFilesExecutor.class.getName());

    @Override
    public ExecutionResult execute(Task task) {
        ExecutionResult result = new ExecutionResult(false, "Iniciando movimiento de archivos");

        try {
            logger.info("Moviendo archivos: " + task.getName());

            Path sourcePath = Paths.get(task.getSourcePath());
            Path destinationPath = Paths.get(task.getDestinationPath());
            String fileExtension = task.getFileExtension();

            if (!Files.exists(sourcePath)) {
                result.setMessage("Error: La ruta de origen no existe");
                result.setStatusId(2);
                return result;
            }

            if (!Files.exists(destinationPath)) {
                result.setMessage("Error: La ruta de destino no existe");
                result.setStatusId(2);
                return result;
            }

            AtomicLong movedCount = new AtomicLong(0);
            AtomicLong totalBytes = new AtomicLong(0);

            Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (fileExtension != null && !fileExtension.isEmpty()) {
                        String fileName = file.getFileName().toString().toLowerCase();
                        if (!fileName.endsWith(fileExtension.toLowerCase())) {
                            logger.fine("Omitiendo archivo (no coincide extensión): " + file.getFileName());
                            return FileVisitResult.CONTINUE;
                        }
                    }

                    Path targetPath = destinationPath.resolve(file.getFileName());

                    int counter = 1;
                    while (Files.exists(targetPath)) {
                        String fileName = file.getFileName().toString();
                        String baseName = fileName.contains(".")
                                ? fileName.substring(0, fileName.lastIndexOf('.'))
                                : fileName;
                        String extension = fileName.contains(".")
                                ? fileName.substring(fileName.lastIndexOf('.'))
                                : "";
                        targetPath = destinationPath.resolve(baseName + "_" + counter + extension);
                        counter++;
                    }

                    Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);

                    movedCount.incrementAndGet();
                    totalBytes.addAndGet(attrs.size());

                    logger.fine("Movido: " + file.getFileName());
                    return FileVisitResult.CONTINUE;
                }
            });

            result.setSuccess(true);
            result.setMessage(String.format("Movidos %d archivos", movedCount.get()));
            result.setSize(totalBytes.get());
            result.setFileCount(Math.toIntExact(movedCount.get()));
            result.setStatusId(1);

            logger.info(String.format("Operación completada: %d archivos movidos", movedCount.get()));

        } catch (Exception e) {
            logger.severe("Error al mover archivos: " + e.getMessage());
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