package org.iclassq.executor.impl;

import org.iclassq.entity.Task;
import org.iclassq.executor.ExecutionResult;
import org.iclassq.executor.TaskExecutorStrategy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

@Component("fileBackupExecutor")
public class FileBackupExecutor implements TaskExecutorStrategy {
    private static final Logger logger = Logger.getLogger(FileBackupExecutor.class.getName());

    @Override
    public ExecutionResult execute(Task task) {
        ExecutionResult result = new ExecutionResult(false, "Iniciando backup de archivos");

        try {
            logger.info("Iniciando backup de archivos: " + task.getName());

            Path sourcePath = Paths.get(task.getSourcePath());
            Path destinationBasePath = Paths.get(task.getDestinationPath());
            String fileExtension = task.getFileExtension();

            if (!Files.exists(sourcePath)) {
                result.setMessage("Error: La ruta de origen no existe");
                result.setStatusId(2);
                return result;
            }

            if (!Files.exists(destinationBasePath)) {
                result.setMessage("Error: LA ruta de destino no existe");
                result.setStatusId(2);
                return result;
            }

            String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String folderName = task.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_" + timeStamp;
            Path destinationPath = destinationBasePath.resolve(folderName);
            Files.createDirectories(destinationPath);

            AtomicLong totalBytes = new AtomicLong(0);
            AtomicLong fileCount = new AtomicLong(0);

            if (fileExtension != null && !fileExtension.isEmpty()) {
                logger.info("Filtrando archivos por extensión: " + fileExtension);
            } else {
                logger.info("Copiando todos los archivos (sin filtro de extensión)");
            }

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

                    Path relativePath = sourcePath.relativize(file);
                    Path targetPath = destinationPath.resolve(relativePath);

                    Files.createDirectories(targetPath.getParent());

                    Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);

                    totalBytes.addAndGet(attrs.size());
                    fileCount.incrementAndGet();

                    logger.fine("Copiado: " + file.getFileName());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path relativePath = sourcePath.relativize(dir);
                    Path targetPath = destinationPath.resolve(relativePath);
                    Files.createDirectories(targetPath);
                    return FileVisitResult.CONTINUE;
                }
            });

            if (fileCount.get() == 0) {
                result.setSuccess(false);
                result.setMessage("No se encontraron archivos con la extensión especificada: " + fileExtension);
                result.setStatusId(2);
                logger.warning("No se encontraron archivos para copiar");
            } else {
                result.setSuccess(true);
                result.setMessage(String.format("Backup completado: %d archivos copiados", fileCount.get()));
                result.setSize(totalBytes.get());
                result.setFileCount((int) fileCount.get());
                result.setStatusId(1);

                logger.info(String.format("Backup completado: %d archivos, %s",
                        fileCount.get(), formatFileSize(totalBytes.get())));
            }
        } catch (Exception e) {
            logger.severe("Error en backup de archivos: " + e.getMessage());
            e.printStackTrace();
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
