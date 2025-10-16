package org.iclassq.views;

import atlantafx.base.theme.Styles;
import jakarta.annotation.PostConstruct;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.iclassq.entity.Task;
import org.iclassq.utils.Fonts;
import org.iclassq.views.components.Table;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Component
public class TaskContent {
    private VBox mainContent;
    private Table<Task> taskTable;
    private TextField searchField;
    private ComboBox<String> cboStatus;
    private Label totalTasksLabel;
    private Button btnAddTask;
    private Button btnRefresh;
    private Button btnSearch;

    private Consumer<Task> onEdit;
    private Consumer<Task> onDelete;
    private Consumer<Task> onExecute;
    private BiConsumer<Task, Boolean> onStatusChange;
    private Runnable onAdd;
    private Runnable onRefresh;
    private Runnable onSearch;

    private static final Logger logger = Logger.getLogger(TaskContent.class.getName());

    public TaskContent() {

    }

    @PostConstruct
    public void initialize() {
        buildContent();
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        btnAddTask.setOnAction(evt -> {
            if (onAdd != null) onAdd.run();
        });
        btnRefresh.setOnAction(evt -> {
            if (onRefresh != null) onRefresh.run();
        });
        btnSearch.setOnAction(evt -> {
            if (onSearch != null) onSearch.run();
        });
    }

    private void buildContent() {
        mainContent = new VBox(25);
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.getStyleClass().add(Styles.BG_DEFAULT);

        VBox contentContainer = new VBox(25);
        contentContainer.setAlignment(Pos.TOP_CENTER);

        HBox header = createHeader();
        HBox toolbar = createToolbar();
        VBox tableContainer = createTableSection();

        contentContainer.getChildren().addAll(
                header,
                toolbar,
                tableContainer
        );

        mainContent.getChildren().add(contentContainer);
    }

    public ScrollPane getContent() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("edge-to-edge");
        scrollPane.setContent(mainContent);
        return scrollPane;
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Gestión de Tareas");
        title.getStyleClass().addAll(Styles.TITLE_2, Styles.TEXT_BOLD);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnAddTask = new Button("Nueva Tarea");
        btnAddTask.setGraphic(new FontIcon(Material2AL.ADD_CIRCLE_OUTLINE));
        btnAddTask.getStyleClass().addAll(Styles.ACCENT, Styles.BUTTON_OUTLINED);
        btnAddTask.setFont(Fonts.semiBold(16));

        btnRefresh = new Button();
        btnRefresh.setGraphic(new FontIcon(Material2MZ.REFRESH));
        btnRefresh.getStyleClass().add(Styles.BUTTON_ICON);
        btnRefresh.setTooltip(new Tooltip("Refrescar"));

        header.getChildren().addAll(title, spacer, btnRefresh, btnAddTask);
        return header;
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 15, 10, 15));
        toolbar.setStyle(
                "-fx-background-color: -color-bg-subtle; " +
                "-fx-background-radius: 8; " +
                "-fx-border-radius: 8;"
        );

        searchField = new TextField();
        searchField.setPromptText("Buscar tareas...");
        searchField.setPrefWidth(300);
        searchField.getStyleClass().add(Styles.LEFT_PILL);

        btnSearch = new Button();
        btnSearch.setGraphic(new FontIcon(Material2MZ.SEARCH));
        btnSearch.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.RIGHT_PILL);

        HBox searchBox = new HBox(searchField, btnSearch);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = new Label("Estado:");
        filterLabel.getStyleClass().add(Styles.TEXT_MUTED);

        cboStatus = new ComboBox<>();
        cboStatus.setPrefWidth(150);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        totalTasksLabel = new Label();
        totalTasksLabel.getStyleClass().add(Styles.TEXT_MUTED);

        toolbar.getChildren().addAll(
            searchBox,
            new Separator(javafx.geometry.Orientation.VERTICAL),
            filterLabel,
            cboStatus,
            spacer,
            totalTasksLabel
        );

        return toolbar;
    }

    private VBox createTableSection() {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);

        taskTable = new Table<>();

        taskTable.setItemsPerPage(15)
                .setMaxPageIndicators(7)
                .setShowInfoLabel(true)
                .addColumn("ID", "id", 50)
                .addColumn("Nombre", "name", 200)
                .addColumnWithDefault("Tipo", "type", 120, "Sin Tipo")
                .addColumnWithExtractor("Programación", 120, task -> {
                    if (task.getScheduleTime() == null) {
                        return "-";
                    }

                    return task.getScheduleTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                })
                .addColumnWithDefault("Base de Datos", "databaseName", 120, "N/A")
                .addColumnWithDefault("Ruta Origen", "sourcePath", 200, "-")
                .addColumnWithDefault("Ruta Destino", "destinationPath", 200, "N/A")
                .addColumnWithDefault("Extensión", "fileExtension", 100, "-")
                .addColumn("Frecuencia", "frequency", 100)
                .addToggleSwitchColumn(
                        "Estado",
                        task -> task.getIsActive(),
                        task -> task.getStateDescription(),
                        120,
                        (task, isActive) -> {
                            if (onStatusChange != null) onStatusChange.accept(task, isActive);
                        }
                )
                .addActionsColumn("Acciones", 150, List.of(
                        new Table.ActionButton<>(
                                "mdmz-play_arrow",
                                "Ejecutar",
                                Styles.SUCCESS,
                                task -> {
                                    if (onExecute != null) onExecute.accept(task);
                                },
                                task -> task.getIsActive() != null && task.getIsActive()
                        ),
                        new Table.ActionButton<>(
                                "mdral-edit",
                                "Editar",
                                task -> {
                                    if (onEdit != null) onEdit.accept(task);
                                }
                        ),
                        new Table.ActionButton<>(
                                "mdal-delete",
                                "Eliminar",
                                Styles.DANGER,
                                task -> {
                                    if (onDelete != null) onDelete.accept(task);
                                },
                                task -> task.getIsActive() == null || !task.getIsActive()
                        )
                ))
                .setPlaceHolder("No hay tareas programadas", "mdoal-inbox");

        VBox.setVgrow(taskTable, Priority.ALWAYS);
        container.getChildren().add(taskTable);

        return container;
    }

    public void refreshTable(List<Task> tasks) {
        taskTable.setData(tasks);
    }

    public void updateTaskCount(long count) {
        totalTasksLabel.setText(count + " tareas");
    }

    public void setOnAdd(Runnable callback) {
        this.onAdd = callback;
    }

    public void setOnEdit(Consumer<Task> callback) {
        this.onEdit = callback;
    }

    public void setOnDelete(Consumer<Task> callback) {
        this.onDelete = callback;
    }

    public void setOnExecute(Consumer<Task> callback) {
        this.onExecute = callback;
    }

    public void setOnStatusChange(BiConsumer<Task, Boolean> callback) {
        this.onStatusChange = callback;
    }

    public void setOnRefresh(Runnable callback) {
        this.onRefresh = callback;
    }

    public void setOnSearch(Runnable callback) {
        this.onSearch = callback;
    }

    public VBox getMainContent() {
        return mainContent;
    }

    public Table<Task> getTaskTable() {
        return taskTable;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public ComboBox<String> getCboStatus() {
        return cboStatus;
    }

    public Label getTotalTasksLabel() {
        return totalTasksLabel;
    }

    public Button getBtnAddTask() {
        return btnAddTask;
    }

    public Button getBtnRefresh() {
        return btnRefresh;
    }

    public Button getBtnSearch() {
        return btnSearch;
    }
}
