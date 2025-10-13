package org.iclassq.executor.impl;

import org.iclassq.entity.Task;
import org.iclassq.executor.ExecutionResult;
import org.iclassq.executor.TaskExecutorStrategy;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

@Component("databaseBackupExecutor")
public class DatabaseBackupExecutor implements TaskExecutorStrategy {
    private static final Logger logger = Logger.getLogger(DatabaseBackupExecutor.class.getName());

    @Override
    public ExecutionResult execute(Task task) {
        ExecutionResult result = new ExecutionResult(false, "Iniciando backup de base de datos");

        try {
            logger.info("Ejecutando backup de base de datos: " + task.getDatabaseName());

            String databaseName = task.getDatabaseName();
            String destinationPath = task.getDestinationPath();

            if (databaseName == null || databaseName.isEmpty()) {
                result.setMessage("Error: No se especificó el nombre de la base de datos");
                result.setStatusId(2);
                return result;
            }

            if (destinationPath == null || !Files.exists(Paths.get(destinationPath))) {
                result.setMessage("Error: La ruta de destino no existe");
                result.setStatusId(2);
                return result;
            }

            String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("%s_backup_%s.sql", databaseName, timeStamp);
            String fullPath = Paths.get(destinationPath, fileName).toString();

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "mysqldump",
                    "-u", "root",
                    "-p" + getDatabasePassword(),
                    "--databases", databaseName,
                    "--result-file=" + fullPath
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                File backupFile = new File(fullPath);
                long fileSizeBytes = backupFile.length();
                String fileSize = formatFileSize(fileSizeBytes);

                result.setSuccess(true);
                result.setMessage("Backup completado exitosamente");
                result.setSize(fileSize);
                result.setStatusId(1);

                logger.info("Backup compleado: " + fileName + " (" + fileSize + ")");
            } else {
                result.setMessage("Error al ejecutar mysqldump (codigo: " + exitCode + ")");
                result.setStatusId(2);
            }
        } catch (Exception e) {
            logger.severe("Error en backup de base de datos: " + e.getMessage());
            result.setMessage("Error: " + e.getMessage());
            result.setStatusId(2);
        } finally {
            result.finish();
        }

        return result;
    }

    private String getDatabasePassword() {
        return System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "";
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";

        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
