package org.iclassq.views.dialogs;

import atlantafx.base.theme.Styles;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.iclassq.entity.Frequency;
import org.iclassq.entity.Task;
import org.iclassq.entity.TypeTask;
import org.iclassq.utils.Fonts;
import org.iclassq.validation.TaskValidator;
import org.iclassq.views.components.Message;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;

import java.io.File;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class TaskFormDialog extends Dialog<Task> {
    private static final Logger logger = Logger.getLogger(TaskFormDialog.class.getName());

    private ComboBox<TypeTask> cboTypeTask;
    private TextField txtName;
    private ComboBox<Frequency> cboFrequency;
    private Spinner<Integer> spinnerHour;
    private Spinner<Integer> spinnerMinute;
    private TextField txtSourcePath;
    private TextField txtDestinationPath;
    private TextField txtFileExtension;
    private Button btnBrowseSource;
    private Button btnBrowseDestination;
    private TextField txtDatabase;
    private VBox dynamicFormContainer;
    private Task taskToEdit;
    private boolean isEditMode = false;
    private List<TypeTask> typeTaskList;
    private List<Frequency> frequencyList;

    public TaskFormDialog() {
        this(null);
    }

    public TaskFormDialog(Task task) {
        this.taskToEdit = task;
        this.isEditMode = task != null;

        initializeDialog();
        buildContent();
        setupResultConverter();
    }

    private void initializeDialog() {
        setTitle(isEditMode ? "Editar Tarea" : "Nueva Tarea");
        setHeaderText(isEditMode
                ? "Modifica los datos de la tarea"
                : "Selecciona el tipo de tarea y completa la información"
        );

        setGraphic(new FontIcon(isEditMode ? Material2AL.EDIT : Material2AL.ADD_CIRCLE_OUTLINE));

        ButtonType btnSave = new ButtonType(isEditMode ? "Guardar" : "Crear", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(btnSave, btnCancel);

        getDialogPane().getStyleClass().add(Styles.BG_DEFAULT);

        Button saveButton = (Button) getDialogPane().lookupButton(btnSave);
        saveButton.addEventFilter(ActionEvent.ACTION, evt -> {
            if (!validateForm()) {
                evt.consume();
            }
        });
    }

    private void buildContent() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(25));
        container.setPrefWidth(600);
        container.setMinHeight(500);

        VBox typeSection = createTypeSection();

        dynamicFormContainer = new VBox(20);
        dynamicFormContainer.setVisible(false);
        dynamicFormContainer.setManaged(false);
        dynamicFormContainer.setMinHeight(400);

        container.getChildren().addAll(typeSection, dynamicFormContainer);

        getDialogPane().setContent(container);
    }

    private VBox createTypeSection() {
        VBox section = new VBox(15);

        Label title = new Label("Tipo de Tarea");
        title.setFont(Fonts.bold(18));
        title.getStyleClass().add(Styles.TITLE_3);

        Label description = new Label("Selecciona el tipo de tarea que deseas crear:");
        description.setFont(Fonts.regular(14));
        description.getStyleClass().add(Styles.TEXT_MUTED);

        cboTypeTask = new ComboBox<>();
        cboTypeTask.setPromptText("Selecciona un tipo de tarea");
        cboTypeTask.setPrefWidth(400);
        cboTypeTask.setMaxWidth(Double.MAX_VALUE);

        cboTypeTask.setOnAction(evt -> {
            TypeTask selected = cboTypeTask.getValue();
            logger.info("Tipo de tarea seleccionado: " + (selected != null ? selected.getDescription() : "null"));
            if (selected != null) {
                updateFormForType(selected);
            }
        });

        section.getChildren().addAll(title, description, cboTypeTask);
        return section;
    }

    private void updateFormForType(TypeTask typeTask) {
        logger.info("Actualizando formulario para tipo: " + typeTask.getDescription() + " (ID: " + typeTask.getId() + ")");

        dynamicFormContainer.getChildren().clear();

        int typeId = typeTask.getId();

        switch (typeId) {
            case 1:
                buildBackupDatabaseForm();
                break;
            case 2:
            case 3:
                buildFileOperationForm();
                break;
            case 4:
                buildCleanFolderForm();
                break;
            default:
                buildGenericForm();
        }

        loadFrequencyOptions();

        dynamicFormContainer.setVisible(true);
        dynamicFormContainer.setManaged(true);
        dynamicFormContainer.requestLayout();

        logger.info("Formulario actualizado correctamente");
    }

    private void buildBackupDatabaseForm() {
        VBox form = new VBox(20);

        Separator separator = new Separator();

        Label title = new Label("Configuración de Backup de Base de Datos");
        title.setFont(Fonts.semiBold(16));
        title.getStyleClass().add(Styles.TITLE_4);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        int row = 0;

        Label lblName = new Label("Nombre de la tarea:");
        lblName.setFont(Fonts.medium(14));
        txtName = new TextField();
        txtName.setPromptText("Ej: Backup diario BD Producción");
        txtName.setPrefWidth(400);
        grid.add(lblName, 0, row);
        grid.add(txtName, 1, row);
        row++;

        Label lblDatabase = new Label("Base de Datos:");
        lblDatabase.setFont(Fonts.medium(14));
        txtDatabase = new TextField();
        txtDatabase.setPromptText("Ej: test_ventas");
        txtDatabase.setPrefWidth(400);
        grid.add(lblDatabase, 0, row);
        grid.add(txtDatabase, 1, row);
        row++;

        Label lblDestination = new Label("Carpeta Destino:");
        lblDestination.setFont(Fonts.medium(14));

        HBox destinationBox = new HBox(10);
        txtDestinationPath = new TextField();
        txtDestinationPath.setPromptText("Selecciona dónde guardar el archivo .sql");
        txtDestinationPath.setPrefWidth(320);

        btnBrowseDestination = new Button();
        btnBrowseDestination.setGraphic(new FontIcon(Material2AL.FOLDER_OPEN));
        btnBrowseDestination.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.ACCENT);
        btnBrowseDestination.setOnAction(evt -> browseDirectory(txtDestinationPath));

        destinationBox.getChildren().addAll(txtDestinationPath, btnBrowseDestination);

        grid.add(lblDestination, 0, row);
        grid.add(destinationBox, 1, row);
        row++;

        addFrequencyField(grid, row);
        row++;

        addTimePickerField(grid, row);

        form.getChildren().addAll(separator, title, grid);
        dynamicFormContainer.getChildren().add(form);
    }

    private void buildFileOperationForm() {
        VBox form = new VBox(20);

        Separator separator = new Separator();

        TypeTask type = cboTypeTask.getValue();
        String formTitle = type.getId() == 2
                ? "Configuración de Backup de Archivos"
                : "Configuración para Mover Archivos";

        Label title = new Label(formTitle);
        title.setFont(Fonts.semiBold(16));
        title.getStyleClass().add(Styles.TITLE_4);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        int row = 0;

        Label lblName = new Label("Nombre de la tarea:");
        lblName.setFont(Fonts.medium(14));
        txtName = new TextField();
        txtName.setPromptText("Ej: Backup documentos importantes");
        txtName.setPrefWidth(400);
        grid.add(lblName, 0, row);
        grid.add(txtName, 1, row);
        row++;

        Label lblSource = new Label("Carpeta origen:");
        lblSource.setFont(Fonts.medium(14));

        HBox sourceBox = new HBox(10);
        txtSourcePath = new TextField();
        txtSourcePath.setPromptText("Selecciona la carpeta de origen");
        txtSourcePath.setPrefWidth(320);

        btnBrowseSource = new Button();
        btnBrowseSource.setGraphic(new FontIcon(Material2AL.FOLDER_OPEN));
        btnBrowseSource.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.ACCENT);
        btnBrowseSource.setOnAction(evt -> browseDirectory(txtSourcePath));

        sourceBox.getChildren().addAll(txtSourcePath, btnBrowseSource);

        grid.add(lblSource, 0, row);
        grid.add(sourceBox, 1, row);
        row++;

        Label lblDestination = new Label("Carpeta Destino:");
        lblDestination.setFont(Fonts.medium(14));

        HBox destinationBox = new HBox(10);
        txtDestinationPath = new TextField();
        txtDestinationPath.setPromptText("Selecciona la carpeta de destino");
        txtDestinationPath.setPrefWidth(320);

        btnBrowseDestination = new Button();
        btnBrowseDestination.setGraphic(new FontIcon(Material2AL.FOLDER_OPEN));
        btnBrowseDestination.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.ACCENT);
        btnBrowseDestination.setOnAction(evt -> browseDirectory(txtDestinationPath));

        destinationBox.getChildren().addAll(txtDestinationPath, btnBrowseDestination);

        grid.add(lblDestination, 0, row);
        grid.add(destinationBox, 1, row);
        row++;

        Label lblFileExtension = new Label("Extensión:");
        lblFileExtension.setFont(Fonts.medium(14));
        txtFileExtension = new TextField();
        txtFileExtension.setPromptText("Ej: .pdf, .doc, .xlsx");
        txtFileExtension.setPrefWidth(320);
        grid.add(lblFileExtension, 0, row);
        grid.add(txtFileExtension, 1, row);
        row++;

        addFrequencyField(grid, row);
        row++;

        addTimePickerField(grid, row);

        form.getChildren().addAll(separator, title, grid);
        dynamicFormContainer.getChildren().add(form);
    }

    private void buildCleanFolderForm() {
        VBox form = new VBox(20);

        Separator separator = new Separator();

        Label title = new Label("Configuración de Limpieza de Carpeta");
        title.setFont(Fonts.semiBold(16));
        title.getStyleClass().add(Styles.TITLE_4);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        int row = 0;

        Label lblName = new Label("Nombre de la tarea:");
        lblName.setFont(Fonts.medium(14));
        txtName = new TextField();
        txtName.setPromptText("Ej: Limpiar carpeta temporal");
        txtName.setPrefWidth(400);
        grid.add(lblName, 0, row);
        grid.add(txtName, 1, row);
        row++;

        Label lblSource = new Label("Carpeta a limpiar:");
        lblSource.setFont(Fonts.medium(14));

        HBox sourceBox = new HBox(10);
        txtSourcePath = new TextField();
        txtSourcePath.setPromptText("Selecciona la carpeta que será limpiada");
        txtSourcePath.setPrefWidth(320);

        btnBrowseSource = new Button();
        btnBrowseSource.setGraphic(new FontIcon(Material2AL.FOLDER_OPEN));
        btnBrowseSource.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.ACCENT);
        btnBrowseSource.setOnAction(evt -> browseDirectory(txtSourcePath));

        sourceBox.getChildren().addAll(txtSourcePath, btnBrowseSource);

        grid.add(lblSource, 0, row);
        grid.add(sourceBox, 1, row);
        row++;

        addFrequencyField(grid, row);
        row++;

        addTimePickerField(grid, row);

        form.getChildren().addAll(separator, title, grid);
        dynamicFormContainer.getChildren().add(form);
    }

    private void buildGenericForm() {
        VBox form = new VBox(20);

        Label title = new Label("Configuración de Tarea");
        title.setFont(Fonts.semiBold(16));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        Label lblName = new Label("Nombre:");
        txtName = new TextField();
        txtName.setPromptText("Nombre de la tarea");

        grid.add(lblName, 0, 0);
        grid.add(txtName, 1, 0);

        form.getChildren().addAll(title, grid);
        dynamicFormContainer.getChildren().add(form);
    }

    private void addFrequencyField(GridPane grid, int row) {
        Label lblFrequency = new Label("Frecuencia:");
        lblFrequency.setFont(Fonts.medium(14));

        cboFrequency = new ComboBox<>();
        cboFrequency.setPromptText("Selecciona la frecuencia");
        cboFrequency.setPrefWidth(200);

        cboFrequency.setConverter(new javafx.util.StringConverter<Frequency>() {
            @Override
            public String toString(Frequency frequency) {
                return frequency != null ? frequency.getDescription() : "";
            }

            @Override
            public Frequency fromString(String string) {
                return null;
            }
        });

        cboFrequency.setOnAction(evt -> updateTimePickerVisibility());

        grid.add(lblFrequency, 0, row);
        grid.add(cboFrequency, 1, row);
    }

    private void addTimePickerField(GridPane grid, int row) {
        Label lblTime = new Label("Hora de ejecución:");
        lblTime.setFont(Fonts.medium(14));
        lblTime.setId("lblTime");

        HBox timeBox = new HBox(10);
        timeBox.setAlignment(Pos.CENTER_LEFT);
        timeBox.setId("timeBox");

        spinnerHour = new Spinner<>(0, 23, 12);
        spinnerHour.setPrefWidth(70);
        spinnerHour.setEditable(true);

        Label lblColon = new Label(":");
        lblColon.setFont(Fonts.bold(20));

        spinnerMinute = new Spinner<>(0, 59, 0);
        spinnerMinute.setPrefWidth(70);
        spinnerMinute.setEditable(true);

        spinnerHour.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12) {
            @Override
            public void decrement(int steps) {
                int newValue = getValue() - steps;
                if (newValue < 0) newValue = 23;
                setValue(newValue);
            }

            @Override
            public void increment(int steps) {
                int newValue = getValue() + steps;
                if (newValue > 23) newValue = 0;
                setValue(newValue);
            }
        });

        spinnerMinute.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0) {
            @Override
            public void decrement(int steps) {
                int newValue = getValue() - steps;
                if (newValue < 0) newValue = 59;
                setValue(newValue);
            }

            @Override
            public void increment(int steps) {
                int newValue = getValue() + steps;
                if (newValue > 59) newValue = 0;
                setValue(newValue);
            }
        });

        Label lblFormat = new Label("(HH:MM)");
        lblFormat.getStyleClass().add(Styles.TEXT_MUTED);
        lblFormat.setFont(Fonts.small());

        timeBox.getChildren().addAll(spinnerHour, lblColon, spinnerMinute, lblFormat);

        grid.add(lblTime, 0, row);
        grid.add(timeBox, 1, row);

        lblTime.setVisible(false);
        lblTime.setManaged(false);
        timeBox.setVisible(false);
        timeBox.setManaged(false);
    }

    private void updateTimePickerVisibility() {
        Frequency frequency = cboFrequency.getValue();
        if (frequency == null) return;

        boolean showTimePicker = frequency.getId() != 1;

        dynamicFormContainer.lookupAll("#lblTime").forEach(node -> {
            node.setVisible(showTimePicker);
            node.setManaged(showTimePicker);
        });

        dynamicFormContainer.lookupAll("#timeBox").forEach(node -> {
            node.setVisible(showTimePicker);
            node.setManaged(showTimePicker);
        });
    }

    private void browseDirectory(TextField targetField) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Seleccionar Carpeta");

        String currentPath = targetField.getText();
        if (currentPath != null && !currentPath.isEmpty()) {
            File currentDir = new File(currentPath);
            if (currentDir.exists()) {
                chooser.setInitialDirectory(currentDir);
            }
        }

        File selectedDir = chooser.showDialog(getDialogPane().getScene().getWindow());
        if (selectedDir != null) {
            targetField.setText(selectedDir.getAbsolutePath());
        }
    }

    private boolean validateForm() {
        Task tempTask = buildTaskFromForm();

        if (tempTask == null) {
            return false;
        }

        Map<String, String> errors = TaskValidator.validate(tempTask);

        if (!errors.isEmpty()) {
            Message.showValidationErrors("Errores de validación", errors);
            return false;
        }

        return true;
    }

    private void setupResultConverter() {
        setResultConverter(dialogButton -> {
            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return buildTaskFromForm();
            }
            return null;
        });
    }

    private Task buildTaskFromForm() {
        TypeTask type = cboTypeTask.getValue();
        if (type == null) {
            Message.showWarning("Campo requerido", "Debes seleccionar un tipo de tarea");
            return null;
        }

        if (txtName == null || txtName.getText().trim().isEmpty()) {
            Message.showWarning("Campo requerido", "El nombre de la tarea es obligatorio");
            return null;
        }

        if (cboFrequency == null || cboFrequency.getValue() == null) {
            Message.showWarning("Campo requerido", "Debes seleccionar una frecuencia");
            return null;
        }

        Task task = isEditMode ? taskToEdit : new Task();

        task.setName(txtName.getText().trim());
        task.setType(type);
        task.setFrequency(cboFrequency.getValue());

        int typeId = type.getId();

        if (typeId == 1) {
            task.setSourcePath(null);
            if (txtDestinationPath != null && !txtDestinationPath.getText().trim().isEmpty()) {
                task.setDestinationPath(txtDestinationPath.getText().trim());
            }
            if (txtDatabase != null && !txtDatabase.getText().trim().isEmpty()) {
                task.setDatabaseName(txtDatabase.getText().trim());
            }
        } else if (typeId == 2 || typeId == 3) {
            if (txtSourcePath != null && !txtSourcePath.getText().trim().isEmpty()) {
                task.setSourcePath(txtSourcePath.getText().trim());
            }
            if (txtDestinationPath != null && !txtDestinationPath.getText().trim().isEmpty()) {
                task.setDestinationPath(txtDestinationPath.getText().trim());
            }
            task.setFileExtension(txtFileExtension.getText().trim());
        } else if (typeId == 4) {
            if (txtSourcePath != null && !txtSourcePath.getText().trim().isEmpty()) {
                task.setSourcePath(txtSourcePath.getText().trim());
            }
            task.setDestinationPath(null);
        }

        if (cboFrequency.getValue().getId() != 1 && spinnerHour != null && spinnerMinute != null) {
            int hour = spinnerHour.getValue();
            int minute = spinnerMinute.getValue();
            task.setScheduleTime(LocalTime.of(hour, minute));
        } else {
            task.setScheduleTime(null);
        }

        task.setIsActive(true);

        return task;
    }

    private void loadTaskData() {
        if (taskToEdit == null) return;

        cboTypeTask.setValue(taskToEdit.getType());
        updateFormForType(taskToEdit.getType());

        if (txtName != null) {
            txtName.setText(taskToEdit.getName());
        }

        if (taskToEdit.getSourcePath() != null && txtSourcePath != null) {
            txtSourcePath.setText(taskToEdit.getSourcePath());
        }

        if (taskToEdit.getDestinationPath() != null && txtDestinationPath != null) {
            txtDestinationPath.setText(taskToEdit.getDestinationPath());
        }

        if (taskToEdit.getDatabaseName() != null && txtDatabase != null) {
            txtDatabase.setText(taskToEdit.getDatabaseName());
        }

        if (taskToEdit.getFileExtension() != null && txtFileExtension != null) {
            txtFileExtension.setText(taskToEdit.getFileExtension());
        }

        if (taskToEdit.getFrequency() != null && cboFrequency != null) {
            cboFrequency.setValue(taskToEdit.getFrequency());
            updateTimePickerVisibility();
        }

        if (taskToEdit.getScheduleTime() != null && spinnerHour != null && spinnerMinute != null) {
            spinnerHour.getValueFactory().setValue(taskToEdit.getScheduleTime().getHour());
            spinnerMinute.getValueFactory().setValue(taskToEdit.getScheduleTime().getMinute());
        }
    }

    public void setTypeOptions(List<TypeTask> types) {
        this.typeTaskList = types;
        cboTypeTask.getItems().setAll(types);
        logger.info("Tipos de tarea cargados: " + types.size());
    }

    public void setFrequencyOptions(List<Frequency> frequencies) {
        this.frequencyList = frequencies;
        logger.info("Frecuencias cargadas: " + frequencies.size());

        if (isEditMode && taskToEdit != null) {
            loadTaskData();
        }
    }

    private void loadFrequencyOptions() {
        if (frequencyList != null && cboFrequency != null) {
            cboFrequency.getItems().setAll(frequencyList);
            logger.info("Frecuencias cargadas en el ComboBox");
        }
    }
}