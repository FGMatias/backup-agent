package org.iclassq.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

@Entity
@Table(name = "task")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Column(name = "name")
    private String name;

    @NotNull(message = "El tipo de tarea es obligatorio")
    @ManyToOne
    @JoinColumn(name = "type", referencedColumnName = "id")
    private TypeTask type;

    @NotBlank(message = "La ruta de origen es obligatoria")
    @Column(name = "source_path")
    private String sourcePath;

    @Column(name = "destination_path")
    private String destinationPath;

    @Column(name = "file_extension")
    private String fileExtension;

    @Column(name = "schedule_time")
    private LocalTime scheduleTime;

    @NotNull(message = "La frecuencia es obligatoria")
    @ManyToOne
    @JoinColumn(name = "frequency", referencedColumnName = "id")
    private Frequency frequency;

    @Column(name = "is_active")
    private Boolean isActive;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TypeTask getType() {
        return type;
    }

    public void setType(TypeTask type) {
        this.type = type;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public LocalTime getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(LocalTime scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        this.isActive = active;
    }

    public String getStateDescription() {
        return isActive ? "Activo" : "Inactivo";
    }
}
