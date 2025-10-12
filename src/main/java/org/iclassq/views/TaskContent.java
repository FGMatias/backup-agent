package org.iclassq.views;

import atlantafx.base.theme.Styles;
import jakarta.annotation.PostConstruct;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
//import org.iclassq.controller.TasksController;
import org.iclassq.entity.Task;
import org.iclassq.service.TaskService;
import org.iclassq.utils.Fonts;
import org.iclassq.views.components.Notification;
import org.iclassq.views.components.Table;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

@Component
public class TaskContent {
    private final TaskService taskService;
    private VBox mainContent;
    private Table<Task> taskTable;
    private TextField searchField;
    private ComboBox<String> cboStatus;
    private Pagination pagination;
    private Label totalTasksLabel;
    private Button btnAddTask;
    private Button btnRefresh;
    private Button btnSearch;
    private static final Logger logger = Logger.getLogger(TaskContent.class.getName());

    public TaskContent(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostConstruct
    public void initialize() {
        buildContent();
//        new TasksController(this);
    }

    private void buildContent() {
        mainContent = new VBox(25);
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.getStyleClass().add(Styles.BG_DEFAULT);

        VBox contentContainer = new VBox(25);
//        contentContainer.setMaxWidth(Utilitie.APP_WIDTH - 20);
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
                .addColumn("ID", "id", 50, "CENTER")
                .addColumn("Nombre", "name", 200)
                .addColumnWithDefault("Tipo", "type", 120, "Sin Tipo")
                .addColumnWithExtractor("Programación", 120, "CENTER", task -> {
                    if (task.getScheduleTime() == null) {
                        return "-";
                    }

                    return task.getScheduleTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                })
                .addColumn("Ruta Origen", "sourcePath", 200)
                .addColumnWithDefault("Ruta Destino", "destinationPath", 200, "N/A")
                .addColumnWithDefault("Extensión", "fileExtension", 100, "CENTER", "-")
                .addColumn("Frecuencia", "frequency", 100)
                .addToggleSwitchColumn(
                        "Estado",
                        task -> task.getIsActive(),
                        task -> task.getStateDescription(),
                        120,
                        this::handleStatusChange
                )
                .addActionsColumn("Acciones", 150, List.of(
                        new Table.ActionButton<>("mdi2p-play-arrow", "Ejecutar", Styles.SUCCESS, this::handleExecute),
                        new Table.ActionButton<>("md12e-edit", "Editar", this::handleEdit),
                        new Table.ActionButton<>("mdi2d-delete", "Eliminar", Styles.DANGER, this::handleDelete)
                ))
                .setPlaceHolder("No hay tareas programadas", "mdoal-inbox");

        VBox.setVgrow(taskTable, Priority.ALWAYS);
        container.getChildren().add(taskTable);

        return container;
    }

    // ========== TABLA ==========
//    private VBox createTableSection() {
//        VBox container = new VBox(10);
//        container.setAlignment(Pos.CENTER);
//
//        taskTable = new TableView<>();
//        taskTable.getStyleClass().add(Styles.STRIPED);
//        taskTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
//
//        // Columnas
//        TableColumn<Task, Integer> idCol = new TableColumn<>("ID");
//        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
//        idCol.setPrefWidth(50);
//        idCol.setStyle("-fx-alignment: CENTER;");
//
//        TableColumn<Task, String> nameCol = new TableColumn<>("Nombre");
//        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
//        nameCol.setPrefWidth(200);
//
//        TableColumn<Task, String> typeCol = new TableColumn<>("Tipo");
//        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
//        typeCol.setPrefWidth(120);
//
//        TableColumn<Task, String> scheduleCol = new TableColumn<>("Programación");
//        scheduleCol.setCellValueFactory(new PropertyValueFactory<>("scheduleTime"));
//        scheduleCol.setPrefWidth(120);
//        scheduleCol.setStyle("-fx-alignment: CENTER;");
//
//        TableColumn<Task, String> frequencyCol = new TableColumn<>("Frecuencia");
//        frequencyCol.setCellValueFactory(new PropertyValueFactory<>("frequency"));
//        frequencyCol.setPrefWidth(100);
//        frequencyCol.setStyle("-fx-alignment: CENTER;");
//
//        TableColumn<Task, String> stateCol = new TableColumn<>("Estado");
//        stateCol.setCellValueFactory(new PropertyValueFactory<>("state"));
//        stateCol.setPrefWidth(100);
//        stateCol.setStyle("-fx-alignment: CENTER;");
//        // Cell factory para mostrar badges de estado
//        stateCol.setCellFactory(column -> new TableCell<Task, String>() {
//            @Override
//            protected void updateItem(String state, boolean empty) {
//                super.updateItem(state, empty);
//                if (empty || state == null) {
//                    setGraphic(null);
//                } else {
//                    Label badge = new Label(state);
//                    badge.setPadding(new Insets(4, 12, 4, 12));
//                    badge.setStyle(
//                            "-fx-background-radius: 12; " +
//                                    "-fx-font-weight: bold; " +
//                                    "-fx-font-size: 11px;"
//                    );
//
//                    switch (state.toLowerCase()) {
//                        case "activa":
//                            badge.getStyleClass().add(Styles.SUCCESS);
//                            badge.setStyle(badge.getStyle() +
//                                    "-fx-background-color: -color-success-emphasis; " +
//                                    "-fx-text-fill: white;");
//                            break;
//                        case "pausada":
//                            badge.getStyleClass().add(Styles.WARNING);
//                            badge.setStyle(badge.getStyle() +
//                                    "-fx-background-color: -color-warning-emphasis; " +
//                                    "-fx-text-fill: white;");
//                            break;
//                        case "completada":
//                            badge.getStyleClass().add(Styles.ACCENT);
//                            badge.setStyle(badge.getStyle() +
//                                    "-fx-background-color: -color-accent-emphasis; " +
//                                    "-fx-text-fill: white;");
//                            break;
//                        default:
//                            badge.getStyleClass().add(Styles.TEXT_MUTED);
//                    }
//                    setGraphic(badge);
//                }
//            }
//        });
//
//        TableColumn<Task, Void> actionsCol = new TableColumn<>("Acciones");
//        actionsCol.setPrefWidth(150);
//        actionsCol.setStyle("-fx-alignment: CENTER;");
//        // Cell factory para botones de acción
//        actionsCol.setCellFactory(column -> new TableCell<Task, Void>() {
//            private final Button editBtn = new Button();
//            private final Button deleteBtn = new Button();
//            private final Button playPauseBtn = new Button();
//            private final HBox buttons = new HBox(5);
//
//            {
//                editBtn.setGraphic(new FontIcon(Material2AL.EDIT));
//                editBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
//                editBtn.setTooltip(new Tooltip("Editar"));
//
//                deleteBtn.setGraphic(new FontIcon(Material2AL.DELETE));
//                deleteBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.DANGER);
//                deleteBtn.setTooltip(new Tooltip("Eliminar"));
//
//                playPauseBtn.setGraphic(new FontIcon(Material2MZ.PLAY_ARROW));
//                playPauseBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.SUCCESS);
//                playPauseBtn.setTooltip(new Tooltip("Ejecutar"));
//
//                buttons.setAlignment(Pos.CENTER);
//                buttons.getChildren().addAll(playPauseBtn, editBtn, deleteBtn);
//            }
//
//            @Override
//            protected void updateItem(Void item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty) {
//                    setGraphic(null);
//                } else {
//                    setGraphic(buttons);
//
//                    Task task = getTableView().getItems().get(getIndex());
//
//                    editBtn.setOnAction(e -> handleEdit(task));
//                    deleteBtn.setOnAction(e -> handleDelete(task));
//                    playPauseBtn.setOnAction(e -> handleExecute(task));
//                }
//            }
//        });
//
//        taskTable.getColumns().addAll(
//                idCol, nameCol, typeCol, scheduleCol,
//                frequencyCol, stateCol, actionsCol
//        );
//
//        // Placeholder cuando no hay datos
//        Label placeholder = new Label("No hay tareas programadas");
//        placeholder.setGraphic(new FontIcon(Material2AL.INBOX));
//        placeholder.getStyleClass().add(Styles.TEXT_MUTED);
//        placeholder.setFont(Fonts.regular(16));
//        taskTable.setPlaceholder(placeholder);
//
//        VBox.setVgrow(taskTable, Priority.ALWAYS);
//        container.getChildren().add(taskTable);
//
//        return container;
//    }

    // ========== FOOTER ==========
//    private HBox createFooter() {
//        HBox footer = new HBox(15);
//        footer.setAlignment(Pos.CENTER);
//        footer.setPadding(new Insets(10, 0, 0, 0));
//
//        // Paginación
//        pagination = new Pagination();
//        pagination.setPageCount(5); // Ejemplo
//        pagination.setCurrentPageIndex(0);
//        pagination.setMaxPageIndicatorCount(5);
//
//        footer.getChildren().add(pagination);
//
//        return footer;
//    }

    private void handleStatusChange(Task task, Boolean isActive) {
        try {
            task.setIsActive(isActive);
            taskService.update(task);

            logger.info(String.format(
                    "Tarea '%s' -> %s",
                    task.getName(),
                    isActive ? "Activo" : "Inactiva"
            ));

            Notification.showSuccess(
                    "Estado actualizado",
                    String.format("'%s' ahora está %s",
                            task.getName(),
                            isActive ? "Activo" : "Inactivo"
                    )
            );

            taskTable.refresh();
        } catch (Exception e) {
            logger.severe("Error al cambiar estado: " + e.getMessage());
            Notification.showError("Error", "No se pudo actualizar el estado");
            taskTable.refresh();
        }
    }

    private void handleEdit(Task task) {
        System.out.println("Editar tarea: " + task.getId());
        // Abrir diálogo de edición
    }

    private void handleDelete(Task task) {
        System.out.println("Eliminar tarea: " + task.getId());
        // Mostrar confirmación y eliminar
    }

    private void handleExecute(Task task) {
        System.out.println("Ejecutar tarea: " + task.getId());
        // Ejecutar tarea inmediatamente
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

    public Pagination getPagination() {
        return pagination;
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
