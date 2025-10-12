package org.iclassq.views.dialogs;

import atlantafx.base.theme.Styles;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.iclassq.entity.Frequency;
import org.iclassq.entity.Task;
import org.iclassq.entity.TypeTask;
import org.iclassq.utils.Fonts;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;

import java.util.List;

public class TaskFormDialog extends Dialog<Task> {
    private ComboBox<TypeTask> cboTypeTask;
    private TextField txtName;
    private ComboBox<Frequency> cboFrequency;
    private Spinner<Integer> spinnerHour;
    private Spinner<Integer> spinnerMinute;
    private TextField txtSourcePath;
    private TextField txtDestinationPath;
    private Button btnBrowseSource;
    private Button btnBrowseDestination;
    private TextField txtDatabase;
    private VBox dynamicForContainer;
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

        if (isEditMode) {
            loadTaskData();
        }
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

        VBox typeSection = createTypeSection();

        dynamicForContainer = new VBox(20);
        dynamicForContainer.setVisible(false);
        dynamicForContainer.setManaged(false);

        container.getChildren().addAll(typeSection, dynamicForContainer);

        getDialogPane().setContent(container);
    }

    private VBox createTypeSection() {
        VBox section = new VBox(15);

        Label title = new Label("Tipo de Tarea");
        title.setFont(Fonts.bold(18));
        title.getStyleClass().add(Styles.TITLE_3);

        Label description = new Label("Selecciona el tipo de tarea que deseas crear: ");
        description.setFont(Fonts.regular(14));
        description.getStyleClass().add(Styles.TEXT_MUTED);

        cboTypeTask = new ComboBox<>();
        cboTypeTask.setPromptText("Selecciona un tipo de tarea");
        cboTypeTask.setPrefWidth(400);
        cboTypeTask.setMaxWidth(Double.MAX_VALUE);

        cboTypeTask.setOnAction(evt -> {
            TypeTask selected = cboTypeTask.getValue();
            if (selected != null) {
                updateFormForType(selected);
            }
        });

        section.getChildren().addAll(title, description, cboTypeTask);
        return section;
    }

    private void updateFormForType(TypeTask typeTask) {
        dynamicForContainer.getChildren().clear();
        dynamicForContainer.setVisible(true);
        dynamicForContainer.setManaged(true);

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

        Label lblDatabase = new Label("Nombre de la Base de Datos:");
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
        txtDestinationPath.setPromptText("Selecciona una carpeta de destino");
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
        dynamicForContainer.getChildren().add(form);
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
        txtName.setPromptText("Ej: Limpiar carpeta videos");
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
        dynamicForContainer.getChildren().add(form);
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
        dynamicForContainer.getChildren().add(form);
    }

    private void addFrequencyField(GridPane grid, int row) {
        Label lblFrequency = new Label("Frecuencia:");
        lblFrequency.setFont(Fonts.medium(14));

        cboFrequency = new ComboBox<>();
        cboFrequency.setPromptText("Selecciona una frecuencia");
        cboFrequency.setPrefWidth(200);

        cboFrequency.setOnAction(evt -> updateTimePickerVisibility());

        grid.add(lblFrequency, 0, row);
        grid.add(cboFrequency, 1, row);
    }

    private void addTimePickerField(GridPane grid, int row) {
        Label lblTime = new Label("Hora de la ejecución:");
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


}
