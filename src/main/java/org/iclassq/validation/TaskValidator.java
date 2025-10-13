package org.iclassq.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.iclassq.entity.Task;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TaskValidator {
    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    public static Map<String, String> validate(Task task) {
        Map<String, String> errors = new LinkedHashMap<>();

        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        errors.putAll(violations.stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing
                ))
        );

        if (task.getType() != null) {
            validateByTaskType(task, errors);
        }

        validatePaths(task, errors);
        validateBusinessRules(task, errors);

        return errors;
    }

    private static void validateByTaskType(Task task, Map<String, String> errors) {
        int typeId = task.getType().getId();

        switch (typeId) {
            case 1:
                if (task.getDestinationPath() == null || task.getDestinationPath().trim().isEmpty()) {
                    errors.put("destinationPath", "La ruta de destino es obligatoria para Backup de Base de Datos");
                }
                break;
            case 2:
            case 3:
                if (task.getSourcePath() == null || task.getSourcePath().trim().isEmpty()) {
                    errors.put("sourcePath", "La ruta de origen es obligatoria");
                }

                if (task.getDestinationPath() == null || task.getDestinationPath().trim().isEmpty()) {
                    errors.put("destinationPath", "La ruta de destino es obligatoria");
                }
                break;
            case 4:
                if (task.getSourcePath() == null || task.getSourcePath().trim().isEmpty()) {
                    errors.put("sourcePath", "Debes especificar la carpeta a limpiar");
                }
                break;
        }
    }

    private static void validatePaths(Task task, Map<String, String> errors) {
        if (task.getSourcePath() != null && !task.getSourcePath().trim().isEmpty()) {
            String sourcePath = task.getSourcePath().trim();

            try {
                if (!Files.exists(Paths.get(sourcePath))) {
                    errors.put("sourcePath", "La ruta de origen no existe");
                } else if (!Files.isReadable(Paths.get(sourcePath))) {
                    errors.put("sourcePath", "La ruta de origen no tiene permisos de lectura");
                } else if (!Files.isDirectory(Paths.get(sourcePath))) {
                    errors.put("sourcePath", "La ruta de origen debe ser una carpeta");
                }
            } catch (Exception e) {
                errors.put("sourcePath", "Ruta de origen inválida: " + e.getMessage());
            }
        }

        if (task.getDestinationPath() != null && !task.getDestinationPath().trim().isEmpty()) {
            String destinationPath = task.getDestinationPath().trim();
            try {
                if (!Files.exists(Paths.get(destinationPath))) {
                    errors.put("destinationPath", "La ruta de destino no existe");
                } else if (!Files.isWritable(Paths.get(destinationPath))) {
                    errors.put("destinationPath", "La ruta de destino no tiene permisos de escritura");
                } else if (!Files.isDirectory(Paths.get(destinationPath))) {
                    errors.put("destinationPath", "La ruta de destino debe ser una carpeta");
                }
            } catch (Exception e) {
                errors.put("destinationPath", "Ruta de destino inválida: " + e.getMessage());
            }
        }
    }

    private static void validateBusinessRules(Task task, Map<String, String> errors) {
        if (task.getSourcePath() != null && task.getDestinationPath() != null) {
            String sourcePath = task.getSourcePath().trim();
            String destinationPath = task.getDestinationPath().trim();

            if (!sourcePath.isEmpty() && !destinationPath.isEmpty()) {
                if (sourcePath.equals(destinationPath)) {
                    errors.put("destinationPath", "La ruta de destino no puede ser igual a la de origen");
                }

                try {
                    if (Paths.get(destinationPath).startsWith(Paths.get(sourcePath))) {
                        errors.put("destinationPath", "La carpeta de destino no puede estar dentro de la carpeta de origen");
                    }
                } catch (Exception e) {

                }
            }
        }

        if (task.getFileExtension() != null && !task.getFileExtension().trim().isEmpty()) {
            String ext = task.getFileExtension().trim();
            if (!ext.startsWith(".")) {
                errors.put("fileExtension", "La extensión debe comenzar con punto (ej: .txt)");
            } else if (ext.length() < 2) {
                errors.put("fileExtension", "La extensión debe tener al menos un carácter después del punto");
            } else if (!ext.matches("^\\.[a-zA-Z0-9]+$")) {
                errors.put("fileExtension", "La extensión solo puede contener letrar y números");
            }
        }

        if (task.getFrequency() != null && task.getFrequency().getId() != 1) {
            if (task.getScheduleTime() == null) {
                errors.put("scheduleTime", "Debes especificar una hora de ejecución para frecuencias programadas");
            }
        }
    }
}
