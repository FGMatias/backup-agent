package org.iclassq.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.iclassq.entity.Task;

import java.nio.file.Files;
import java.nio.file.Paths;
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
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        Map<String, String> errors = violations.stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing
                ));

        validatePaths(task, errors);
        validateBusinessRules(task, errors);

        return errors;
    }

    private static void validatePaths(Task task, Map<String, String> errors) {
        if (task.getSourcePath() != null && !task.getSourcePath().trim().isEmpty()) {
            try {
                if (!Files.exists(Paths.get(task.getSourcePath()))) {
                    errors.put("sourcePath", "La ruta de origen no existe");
                } else if (!Files.isReadable(Paths.get(task.getSourcePath()))) {
                    errors.put("sourcePath", "La ruta de origen no tiene permisos de lectura");
                }
            } catch (Exception e) {
                errors.put("sourcePath", "Ruta inválida");
            }
        }

        if (task.getDestinationPath() != null && !task.getDestinationPath().trim().isEmpty()) {
            try {
                if (!Files.exists(Paths.get(task.getDestinationPath()))) {
                    errors.put("destinationPath", "La ruta de destino no existe");
                } else if (!Files.isWritable(Paths.get(task.getDestinationPath()))) {
                    errors.put("destinationPath", "La ruta de destino no tiene permisos de escritura");
                }
            } catch (Exception e) {
                errors.put("destinationPath", "Ruta de destino inválida");
            }
        }
    }

    private static void validateBusinessRules(Task task, Map<String, String> errors) {
        if (task.getSourcePath() != null && task.getDestinationPath() != null) {
            if (task.getSourcePath().equals(task.getDestinationPath())) {
                errors.put("destinationPath", "La ruta de destino no puede ser igual a la de origen");
            }
        }

        if (task.getFileExtension() != null && !task.getFileExtension().trim().isEmpty()) {
            String ext = task.getFileExtension().trim();
            if (!ext.startsWith(".")) {
                errors.put("fileExtension", "La extensión debe comenzar con punto (ej: .txt)");
            }
        }
    }
}
