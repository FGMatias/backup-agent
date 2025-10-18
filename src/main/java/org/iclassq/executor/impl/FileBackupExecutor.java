package org.iclassq.executor.impl;

import org.iclassq.entity.Task;
import org.iclassq.executor.ExecutionResult;
import org.iclassq.executor.TaskExecutorStrategy;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@Component("fileBackupExecutor")
public class FileBackupExecutor implements TaskExecutorStrategy {
    private static final Logger logger = Logger.getLogger(FileBackupExecutor.class.getName());

    private static final String[] COMPRESSION_PATHS = {
            "C:\\Program Files\\7-Zip\\7z.exe",
            "C:\\Program Files (x86)\\7-Zip\\7z.exe",
            "C:\\Program Files\\WinRAR\\WinRAR.exe",
            "C:\\Program Files (x86)\\WinRAR\\WinRAR.exe",
            "C:\\Program Files\\WinRAR\\Rar.exe",
            "C:\\Program Files (x86)\\WinRAR\\Rar.exe",
            "C:\\Program Files\\WinRAR\\UnRAR.exe",
            "C:\\Program Files (x86)\\WinRAR\\UnRAR.exe"
    };

    @Override
    public ExecutionResult execute(Task task) {
        ExecutionResult result = new ExecutionResult(false, "Iniciando backup de archivos");

        try {
            Path sourcePath = Paths.get(task.getSourcePath());
            Path destinationBasePath = Paths.get(task.getDestinationPath());
            String fileExtension = task.getFileExtension();

            if (!Files.exists(sourcePath)) {
                result.setMessage("Error: La ruta de origen no existe");
                result.setStatusId(2);
                return result;
            }

            if (!Files.exists(destinationBasePath)) {
                result.setMessage("Error: La ruta de destino no existe");
                result.setStatusId(2);
                return result;
            }

            CompressionTool tool = detectCompressionTool();
            if (tool == null) {
                result.setMessage("Error: No se encontró ninguna herramienta de compresión instalada");
                result.setStatusId(2);
                return result;
            }
            File lastBackup = findLastBackup(destinationBasePath, task.getName());
            List<Path> sourceFiles = getFilteredFiles(sourcePath, fileExtension);

            if (sourceFiles.isEmpty()) {
                result.setMessage("No se encontraron archivos para respaldar" +
                        (fileExtension != null ? " con extensión: " + fileExtension : ""));
                result.setStatusId(2);
                return result;
            }

            Set<String> filesToBackup;
            boolean isIncremental = false;

            if (lastBackup != null) {
                Set<String> filesInBackup = getFilesFromArchive(lastBackup, tool);

                if (!filesInBackup.isEmpty() && filesInBackup.size() <= 10) {
                    filesInBackup.forEach(f -> logger.info("    • " + f));
                } else if (!filesInBackup.isEmpty()) {
                    filesInBackup.stream().limit(5).forEach(f -> logger.info("    • " + f));
                }

                filesToBackup = determineFilesToBackup(sourceFiles, filesInBackup, sourcePath);
                isIncremental = true;

                if (!filesToBackup.isEmpty() && filesToBackup.size() <= 10) {
                    filesToBackup.forEach(f -> logger.info("    • " + f));
                } else if (!filesToBackup.isEmpty()) {
                    filesToBackup.stream().limit(5).forEach(f -> logger.info("    • " + f));
                }

            } else {
                filesToBackup = sourceFiles.stream()
                        .map(p -> sourcePath.relativize(p).toString().replace('\\', '/'))
                        .collect(Collectors.toSet());
            }

            if (filesToBackup.isEmpty()) {
                result.setSuccess(true);
                result.setMessage("No hay archivos nuevos. Todos los archivos ya están respaldados.");
                result.setFileCount(0);
                result.setSize(0L);
                result.setStatusId(1);
                return result;
            }

            String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupName = "audios_" + timeStamp;
            Path backupFile = destinationBasePath.resolve(backupName + tool.extension);
            Path tempDir = Files.createTempDirectory("backup_temp_");

            try {
                long totalBytes = 0;
                for (String relativeFile : filesToBackup) {
                    Path sourceFile = sourcePath.resolve(relativeFile);
                    Path targetFile = tempDir.resolve(relativeFile);

                    Files.createDirectories(targetFile.getParent());
                    Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    totalBytes += Files.size(sourceFile);
                }
                boolean compressed = compressFiles(tempDir, backupFile, tool);

                if (!compressed) {
                    result.setMessage("Error: No se pudo crear el archivo comprimido");
                    result.setStatusId(2);
                    return result;
                }

                long compressedSize = Files.size(backupFile);
                double compressionRatio = ((double)(totalBytes - compressedSize) / totalBytes) * 100;

                result.setSuccess(true);
                result.setMessage(String.format("%s: %d archivos (%s → %s, %.1f%% reducción)",
                        isIncremental ? "Backup incremental completado" : "Backup completo",
                        filesToBackup.size(),
                        formatFileSize(totalBytes),
                        formatFileSize(compressedSize),
                        compressionRatio));
                result.setSize(compressedSize);
                result.setFileCount(filesToBackup.size());
                result.setStatusId(1);
            } finally {
                deleteDirectory(tempDir);
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.setMessage("Error: " + e.getMessage());
            result.setStatusId(2);
        } finally {
            result.finish();
        }

        return result;
    }

    private CompressionTool detectCompressionTool() {
        String[] priorityPaths = {
                "C:\\Program Files\\7-Zip\\7z.exe",
                "C:\\Program Files (x86)\\7-Zip\\7z.exe",
                "C:\\Program Files\\WinRAR\\Rar.exe",
                "C:\\Program Files (x86)\\WinRAR\\Rar.exe",
                "C:\\Program Files\\WinRAR\\WinRAR.exe",
                "C:\\Program Files (x86)\\WinRAR\\WinRAR.exe",
                "C:\\Program Files\\WinRAR\\UnRAR.exe",
                "C:\\Program Files (x86)\\WinRAR\\UnRAR.exe"
        };

        for (String path : priorityPaths) {
            File file = new File(path);
            if (file.exists()) {
                if (path.contains("7-Zip") || path.contains("7z")) {
                    return new CompressionTool("7-Zip", path, ".7z", CompressionType.SEVEN_ZIP);
                } else if (path.toLowerCase().contains("winrar") || path.toLowerCase().contains("rar")) {
                    return new CompressionTool("WinRAR", path, ".rar", CompressionType.WINRAR);
                }
            }
        }

        String pathEnv = System.getenv("PATH");
        if (pathEnv != null) {
            String[] paths = pathEnv.split(";");
            for (String dir : paths) {
                File sevenZip = new File(dir, "7z.exe");
                File rar = new File(dir, "Rar.exe");
                File winrar = new File(dir, "WinRAR.exe");
                File unrar = new File(dir, "UnRAR.exe");

                if (sevenZip.exists()) {
                    return new CompressionTool("7-Zip", sevenZip.getAbsolutePath(), ".7z", CompressionType.SEVEN_ZIP);
                }
                if (rar.exists()) {
                    return new CompressionTool("WinRAR", rar.getAbsolutePath(), ".rar", CompressionType.WINRAR);
                }
                if (winrar.exists()) {
                    return new CompressionTool("WinRAR", winrar.getAbsolutePath(), ".rar", CompressionType.WINRAR);
                }
                if (unrar.exists()) {
                    return new CompressionTool("WinRAR", unrar.getAbsolutePath(), ".rar", CompressionType.WINRAR);
                }
            }
        }

        return new CompressionTool("Java ZIP", "native", ".zip", CompressionType.JAVA_ZIP);
    }

    private File findLastBackup(Path destinationPath, String taskName) throws IOException {
        String prefix = taskName.replaceAll("[^a-zA-Z0-9]", "_");

        List<File> backups = Files.list(destinationPath)
                .map(Path::toFile)
                .filter(File::isFile)
                .filter(f -> {
                    String name = f.getName().toLowerCase();
                    return name.startsWith(prefix.toLowerCase()) &&
                            (name.endsWith(".zip") || name.endsWith(".rar") || name.endsWith(".7z"));
                })
                .sorted((f1, f2) -> {
                    String timestamp1 = extractTimestamp(f1.getName());
                    String timestamp2 = extractTimestamp(f2.getName());

                    if (timestamp1 != null && timestamp2 != null) {
                        return timestamp2.compareTo(timestamp1); // Descendente
                    }

                    return Long.compare(f2.lastModified(), f1.lastModified());
                })
                .collect(Collectors.toList());

        return backups.isEmpty() ? null : backups.get(0);
    }

    private String extractTimestamp(String fileName) {
        try {
            String pattern = "\\d{8}_\\d{6}";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(fileName);

            if (m.find()) {
                return m.group();
            }
        } catch (Exception e) {

        }
        return null;
    }

    private List<Path> getFilteredFiles(Path sourcePath, String fileExtension) throws IOException {
        List<Path> files = new ArrayList<>();

        Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (fileExtension != null && !fileExtension.isEmpty()) {
                    String fileName = file.getFileName().toString().toLowerCase();
                    if (!fileName.endsWith(fileExtension.toLowerCase())) {
                        return FileVisitResult.CONTINUE;
                    }
                }
                files.add(file);
                return FileVisitResult.CONTINUE;
            }
        });

        return files;
    }

    private Set<String> getFilesFromArchive(File archiveFile, CompressionTool tool) {
        Set<String> files = new HashSet<>();

        try {
            if (tool.type == CompressionType.JAVA_ZIP || archiveFile.getName().endsWith(".zip")) {
                files = getFilesFromZip(archiveFile);
            } else {
                files = getFilesFromCompressedArchive(archiveFile, tool);
            }
        } catch (Exception e) {
        }

        return files;
    }

    private Set<String> getFilesFromZip(File zipFile) throws IOException {
        Set<String> files = new HashSet<>();

        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    files.add(entry.getName().replace('\\', '/'));
                }
            }
        }

        return files;
    }

    private Set<String> getFilesFromCompressedArchive(File archiveFile, CompressionTool tool) {
        Set<String> files = new HashSet<>();

        try {
            List<String> command = new ArrayList<>();
            String archivePath = archiveFile.getAbsolutePath();

            if (tool.type == CompressionType.SEVEN_ZIP) {
                command.add(tool.path);
                command.add("l");
                command.add("-slt");
                command.add(archivePath);

            } else if (tool.type == CompressionType.WINRAR) {
                String exeName = new File(tool.path).getName().toLowerCase();
                String listPath = tool.path;

                if (!exeName.equals("unrar.exe")) {
                    File toolDir = new File(tool.path).getParentFile();
                    File unrar = new File(toolDir, "UnRAR.exe");

                    if (unrar.exists()) {
                        listPath = unrar.getAbsolutePath();
                        exeName = "unrar.exe";
                    }
                }

                command.add(listPath);

                if (exeName.equals("unrar.exe")) {
                    command.add("lb");
                } else if (exeName.equals("rar.exe")) {
                    command.add("lb");
                } else {
                    command.add("l");
                }

                command.add(archivePath);
            }

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder fullOutput = new StringBuilder();
            int lineCount = 0;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    lineCount++;
                    fullOutput.append(line).append("\n");

                    if (tool.type == CompressionType.SEVEN_ZIP) {
                        if (line.startsWith("Path = ")) {
                            String fileName = line.substring(7).trim();
                            if (!fileName.isEmpty() && !fileName.equals(archiveFile.getName())) {
                                files.add(fileName.replace('\\', '/'));
                            }
                        }
                    } else if (tool.type == CompressionType.WINRAR) {
                        String exeName = new File(command.get(0)).getName().toLowerCase();
                        line = line.trim();

                        if (exeName.equals("unrar.exe") || exeName.equals("rar.exe")) {
                            if (!line.isEmpty() &&
                                    !line.startsWith("UNRAR") &&
                                    !line.startsWith("RAR ") &&
                                    !line.toLowerCase().contains("freeware") &&
                                    !line.toLowerCase().contains("copyright") &&
                                    !line.toLowerCase().contains("evaluation") &&
                                    !line.toLowerCase().contains("trial version") &&
                                    !line.toLowerCase().contains("usage:") &&
                                    !line.toLowerCase().contains("uso:") &&
                                    !line.contains("---") &&
                                    !line.matches("^\\s*$")) {

                                if (line.length() > 2 && !line.contains(":")) {
                                    files.add(line.replace('\\', '/'));
                                }
                            }
                        } else {
                        }
                    }
                }
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                if (files.isEmpty() && lineCount > 0) {
                    String[] lines = fullOutput.toString().split("\n");
                    for (int i = 0; i < Math.min(20, lines.length); i++) {
                        logger.warning("  " + i + ": " + lines[i]);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return files;
    }

    private Set<String> determineFilesToBackup(List<Path> sourceFiles,
                                               Set<String> filesInBackup,
                                               Path sourcePath) {
        Set<String> filesToBackup = new HashSet<>();

        for (Path sourceFile : sourceFiles) {
            String relativePath = sourcePath.relativize(sourceFile).toString().replace('\\', '/');

            if (!filesInBackup.contains(relativePath)) {
                filesToBackup.add(relativePath);
            }
        }

        return filesToBackup;
    }

    private boolean compressFiles(Path sourceDir, Path targetFile, CompressionTool tool) {
        try {
            if (tool.type == CompressionType.JAVA_ZIP) {
                return compressWithJavaZip(sourceDir, targetFile);
            } else {
                return compressWithExternalTool(sourceDir, targetFile, tool);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean compressWithJavaZip(Path sourceDir, Path targetFile) throws IOException {

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetFile.toFile()))) {

            zos.setLevel(9);

            Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String zipEntryName = sourceDir.relativize(file).toString().replace('\\', '/');
                    zos.putNextEntry(new ZipEntry(zipEntryName));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        boolean success = Files.exists(targetFile) && Files.size(targetFile) > 0;
        return success;
    }

    private boolean compressWithExternalTool(Path sourceDir, Path targetFile, CompressionTool tool)
            throws IOException, InterruptedException {

        String exeName = new File(tool.path).getName().toLowerCase();

        if (exeName.equals("unrar.exe")) {
            File unrarDir = new File(tool.path).getParentFile();
            File rarExe = new File(unrarDir, "Rar.exe");
            File winrarExe = new File(unrarDir, "WinRAR.exe");

            if (rarExe.exists()) {
                tool = new CompressionTool("WinRAR", rarExe.getAbsolutePath(), ".rar", CompressionType.WINRAR);
                exeName = "rar.exe";
            } else if (winrarExe.exists()) {
                tool = new CompressionTool("WinRAR", winrarExe.getAbsolutePath(), ".rar", CompressionType.WINRAR);
                exeName = "winrar.exe";
            } else {
                return false;
            }
        }

        List<String> command = new ArrayList<>();

        if (tool.type == CompressionType.SEVEN_ZIP) {
            command.add(tool.path);
            command.add("a");
            command.add("-mx9");
            command.add(targetFile.toAbsolutePath().toString());
            command.add(sourceDir.toAbsolutePath().toString() + File.separator + "*");

        } else if (tool.type == CompressionType.WINRAR) {
            boolean isRarExe = exeName.equals("rar.exe");

            command.add(tool.path);
            command.add("a");
            command.add("-m5");
            command.add("-ep1");

            if (!isRarExe) {
                command.add("-ibck");
            }

            command.add(targetFile.toAbsolutePath().toString());
            command.add(sourceDir.toAbsolutePath().toString() + File.separator + "*");
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.fine(line);
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        boolean success = exitCode == 0 && Files.exists(targetFile) && Files.size(targetFile) > 0;

        return success;
    }

    private void deleteDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private static class CompressionTool {
        final String name;
        final String path;
        final String extension;
        final CompressionType type;

        CompressionTool(String name, String path, String extension, CompressionType type) {
            this.name = name;
            this.path = path;
            this.extension = extension;
            this.type = type;
        }
    }

    private enum CompressionType {
        SEVEN_ZIP,
        WINRAR,
        JAVA_ZIP
    }
}