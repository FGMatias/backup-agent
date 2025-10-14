package org.iclassq.executor.impl;

import org.iclassq.entity.Task;
import org.iclassq.executor.ExecutionResult;
import org.iclassq.executor.TaskExecutorStrategy;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

@Component("databaseBackupExecutor")
public class DatabaseBackupExecutor implements TaskExecutorStrategy {
    private static final Logger logger = Logger.getLogger(DatabaseBackupExecutor.class.getName());

    private static final String XAMPP_MYSQLDUMP_PATH = "C:\\xampp\\mysql\\bin\\mysqldump.exe";
    private static final String MYSQL_HOST = "localhost";
    private static final String MYSQL_PORT = "3306";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASSWORD = "";

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

            File mysqldumpFile = new File(XAMPP_MYSQLDUMP_PATH);
            if (!mysqldumpFile.exists()) {
                result.setMessage("Error: No se encontró mysqldump en la ruta: " + XAMPP_MYSQLDUMP_PATH);
                result.setStatusId(2);
                logger.severe("mysqldump no encontrado. Verifica que XAMPP esté instalado en C:\\xampp");
                return result;
            }

            String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("%s_backup_%s.sql", databaseName, timeStamp);
            String fullPath = Paths.get(destinationPath, fileName).toString();

            ProcessBuilder processBuilder;

            if (MYSQL_PASSWORD == null || MYSQL_PASSWORD.isEmpty()) {
                processBuilder = new ProcessBuilder(
                        XAMPP_MYSQLDUMP_PATH,
                        "-h", MYSQL_HOST,
                        "-P", MYSQL_PORT,
                        "-u", MYSQL_USER,
                        "--databases", databaseName,
                        "--result-file=" + fullPath,
                        "--single-transaction",
                        "--quick",
                        "--lock-tables=false"
                );
            } else {
                processBuilder = new ProcessBuilder(
                        XAMPP_MYSQLDUMP_PATH,
                        "-h", MYSQL_HOST,
                        "-P", MYSQL_PORT,
                        "-u", MYSQL_USER,
                        "-p" + MYSQL_PASSWORD,
                        "--databases", databaseName,
                        "--result-file=" + fullPath,
                        "--single-transaction",
                        "--quick",
                        "--lock-tables=false"
                );
            }

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    logger.fine(line);
                }
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                File backupFile = new File(fullPath);

                if (backupFile.exists() && backupFile.length() > 0) {
                    long fileSizeBytes = backupFile.length();

                    result.setSuccess(true);
                    result.setMessage("Backup completado exitosamente");
                    result.setFileCount(1);
                    result.setSize(fileSizeBytes);
                    result.setStatusId(1);

                    logger.info("Backup completado: " + fileName + " (" + formatFileSize(fileSizeBytes) + ")");
                } else {
                    result.setMessage("Error: El archivo de backup está vacío o no se creó");
                    result.setStatusId(2);
                    logger.severe("El archivo de backup no se generó correctamente");
                }
            } else {
                String errorMsg = output.toString();
                result.setMessage("Error al ejecutar mysqldump (código: " + exitCode + "): " + errorMsg);
                result.setStatusId(2);
                logger.severe("Error en mysqldump: " + errorMsg);
            }
        } catch (Exception e) {
            logger.severe("Error en backup de base de datos: " + e.getMessage());
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
