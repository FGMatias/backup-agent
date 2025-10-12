package org.iclassq.views;

import atlantafx.base.theme.Styles;
import jakarta.annotation.PostConstruct;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
//import org.iclassq.controller.HistoryController;
import org.iclassq.entity.History;
import org.iclassq.utils.Fonts;
import org.iclassq.utils.Utilitie;
import org.iclassq.views.components.Card;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class HistoryContent {
    private VBox mainContent;
    private TableView<History> historyTable;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private ComboBox<String> statusFilter;
    private ComboBox<String> typeFilter;
    private Pagination pagination;
    private Label totalExecutionsLabel;
    private Label successRateLabel;
    private Button exportButton;
    private Button clearHistoryButton;

    // Cards de resumen
    private Card totalExecutionsCard;
    private Card successfulCard;
    private Card failedCard;
    private Card avgDurationCard;

    @PostConstruct
    public void initialize() {
        buildContent();
//        new HistoryController(this);
    }

    private void buildContent() {
        mainContent = new VBox(20);
        mainContent.setPadding(new Insets(25));
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.getStyleClass().add(Styles.BG_DEFAULT);

        VBox contentContainer = new VBox(20);
        contentContainer.setMaxWidth(Utilitie.APP_WIDTH - 20);
        contentContainer.setAlignment(Pos.TOP_CENTER);

        // Header
        HBox header = createHeader();

        // Cards de resumen
        GridPane summaryCards = createSummaryCards();

        // Filtros y toolbar
        VBox filtersSection = createFiltersSection();

        // Tabla de historial
        VBox tableContainer = createTableSection();

        // Footer con paginación
        HBox footer = createFooter();

        contentContainer.getChildren().addAll(
                header,
                summaryCards,
                filtersSection,
                tableContainer,
                footer
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

    // ========== HEADER ==========
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Historial de Ejecuciones");
        title.getStyleClass().add(Styles.TITLE_2);
        title.setFont(Fonts.bold(24));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Botón para exportar
        exportButton = new Button("Exportar");
        exportButton.setGraphic(new FontIcon(Material2AL.FILE_COPY));
        exportButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED);
        exportButton.setFont(Fonts.semiBold(14));

        // Botón para limpiar historial antiguo
        clearHistoryButton = new Button("Limpiar Historial");
        clearHistoryButton.setGraphic(new FontIcon(Material2AL.DELETE_SWEEP));
        clearHistoryButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.DANGER);
        clearHistoryButton.setFont(Fonts.semiBold(14));

        header.getChildren().addAll(title, spacer, exportButton, clearHistoryButton);
        return header;
    }

    // ========== SUMMARY CARDS ==========
    private GridPane createSummaryCards() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setAlignment(Pos.CENTER);

        // Configurar 4 columnas iguales
        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setPercentWidth(25);
            col.setMinWidth(200);
            grid.getColumnConstraints().add(col);
        }

        totalExecutionsCard = new Card("Total Ejecuciones", "0");
        totalExecutionsCard.setMaxWidth(Double.MAX_VALUE);

        successfulCard = new Card("Exitosas", "0");
        successfulCard.setMaxWidth(Double.MAX_VALUE);

        failedCard = new Card("Fallidas", "0", true); // true para que sea roja si > 0
        failedCard.setMaxWidth(Double.MAX_VALUE);

        avgDurationCard = new Card("Duración Promedio", "0 min");
        avgDurationCard.setMaxWidth(Double.MAX_VALUE);

        grid.add(totalExecutionsCard, 0, 0);
        grid.add(successfulCard, 1, 0);
        grid.add(failedCard, 2, 0);
        grid.add(avgDurationCard, 3, 0);

        return grid;
    }

    // ========== FILTERS SECTION ==========
    private VBox createFiltersSection() {
        VBox section = new VBox(12);

        Label title = new Label("Filtros");
        title.getStyleClass().add(Styles.TITLE_4);
        title.setFont(Fonts.semiBold(16));

        HBox filtersBar = new HBox(15);
        filtersBar.setAlignment(Pos.CENTER_LEFT);
        filtersBar.setPadding(new Insets(15));
        filtersBar.setStyle(
                "-fx-background-color: -color-bg-subtle; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-radius: 8;"
        );

        // Rango de fechas
        Label dateLabel = new Label("Período:");
        dateLabel.getStyleClass().add(Styles.TEXT_MUTED);
        dateLabel.setFont(Fonts.regular(14));

        startDatePicker = new DatePicker();
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        startDatePicker.setPromptText("Fecha inicio");
        startDatePicker.setPrefWidth(150);

        Label toLabel = new Label("-");
        toLabel.getStyleClass().add(Styles.TEXT_MUTED);

        endDatePicker = new DatePicker();
        endDatePicker.setValue(LocalDate.now());
        endDatePicker.setPromptText("Fecha fin");
        endDatePicker.setPrefWidth(150);

        // Filtro por estado
        Label statusLabel = new Label("Estado:");
        statusLabel.getStyleClass().add(Styles.TEXT_MUTED);
        statusLabel.setFont(Fonts.regular(14));

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll(
                "Todos",
                "Exitoso",
                "Fallido",
                "En Proceso",
                "Cancelado"
        );
        statusFilter.setValue("Todos");
        statusFilter.setPrefWidth(130);

        // Filtro por tipo
        Label typeLabel = new Label("Tipo:");
        typeLabel.getStyleClass().add(Styles.TEXT_MUTED);
        typeLabel.setFont(Fonts.regular(14));

        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll(
                "Todos",
                "Backup BD",
                "Backup Archivos",
                "Limpieza",
                "Sincronización"
        );
        typeFilter.setValue("Todos");
        typeFilter.setPrefWidth(150);

        // Botón aplicar filtros
        Button applyButton = new Button("Aplicar");
        applyButton.getStyleClass().addAll(Styles.ACCENT);
        applyButton.setFont(Fonts.semiBold(14));
        applyButton.setOnAction(e -> applyFilters());

        // Botón limpiar filtros
        Button clearButton = new Button("Limpiar");
        clearButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        clearButton.setFont(Fonts.regular(14));
        clearButton.setOnAction(e -> clearFilters());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Total y tasa de éxito
        VBox statsBox = new VBox(5);
        statsBox.setAlignment(Pos.CENTER_RIGHT);

        totalExecutionsLabel = new Label("0 ejecuciones");
        totalExecutionsLabel.getStyleClass().add(Styles.TEXT_MUTED);
        totalExecutionsLabel.setFont(Fonts.regular(13));

        successRateLabel = new Label("Tasa de éxito: 0%");
        successRateLabel.getStyleClass().add(Styles.SUCCESS);
        successRateLabel.setFont(Fonts.semiBold(14));

        statsBox.getChildren().addAll(successRateLabel, totalExecutionsLabel);

        filtersBar.getChildren().addAll(
                dateLabel, startDatePicker, toLabel, endDatePicker,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                statusLabel, statusFilter,
                typeLabel, typeFilter,
                applyButton, clearButton,
                spacer,
                statsBox
        );

        section.getChildren().addAll(title, filtersBar);
        return section;
    }

    // ========== TABLE SECTION ==========
    private VBox createTableSection() {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);

        historyTable = new TableView<>();
        historyTable.getStyleClass().add(Styles.STRIPED);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Columnas
        TableColumn<History, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<History, String> taskNameCol = new TableColumn<>("Tarea");
        taskNameCol.setCellValueFactory(new PropertyValueFactory<>("taskName"));
        taskNameCol.setPrefWidth(150);

        TableColumn<History, String> typeCol = new TableColumn<>("Tipo");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(120);

        TableColumn<History, String> startTimeCol = new TableColumn<>("Inicio");
        startTimeCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        startTimeCol.setPrefWidth(130);
        startTimeCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<History, String> endTimeCol = new TableColumn<>("Fin");
        endTimeCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        endTimeCol.setPrefWidth(130);
        endTimeCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<History, String> durationCol = new TableColumn<>("Duración");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));
        durationCol.setPrefWidth(90);
        durationCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<History, String> statusCol = new TableColumn<>("Estado");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        statusCol.setStyle("-fx-alignment: CENTER;");

        // Cell factory para badges de estado
        statusCol.setCellFactory(column -> new TableCell<History, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(status);
                    badge.setPadding(new Insets(4, 12, 4, 12));
                    badge.setStyle(
                            "-fx-background-radius: 12; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-font-size: 11px;"
                    );

                    switch (status.toLowerCase()) {
                        case "exitoso":
                            badge.setStyle(badge.getStyle() +
                                    "-fx-background-color: -color-success-emphasis; " +
                                    "-fx-text-fill: white;");
                            badge.setGraphic(new FontIcon(Material2AL.CHECK_CIRCLE));
                            break;
                        case "fallido":
                            badge.setStyle(badge.getStyle() +
                                    "-fx-background-color: -color-danger-emphasis; " +
                                    "-fx-text-fill: white;");
                            badge.setGraphic(new FontIcon(Material2AL.ERROR));
                            break;
                        case "en proceso":
                            badge.setStyle(badge.getStyle() +
                                    "-fx-background-color: -color-warning-emphasis; " +
                                    "-fx-text-fill: white;");
                            badge.setGraphic(new FontIcon(Material2MZ.PENDING));
                            break;
                        case "cancelado":
                            badge.setStyle(badge.getStyle() +
                                    "-fx-background-color: -color-bg-default; " +
                                    "-fx-text-fill: -color-fg-muted;");
                            badge.setGraphic(new FontIcon(Material2AL.CANCEL));
                            break;
                    }
                    setGraphic(badge);
                }
            }
        });

        TableColumn<History, String> sizeCol = new TableColumn<>("Tamaño");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        sizeCol.setPrefWidth(90);
        sizeCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<History, Void> actionsCol = new TableColumn<>("Acciones");
        actionsCol.setPrefWidth(120);
        actionsCol.setStyle("-fx-alignment: CENTER;");

        // Cell factory para botones de acción
        actionsCol.setCellFactory(column -> new TableCell<History, Void>() {
            private final Button viewBtn = new Button();
            private final Button downloadBtn = new Button();
            private final HBox buttons = new HBox(5);

            {
                viewBtn.setGraphic(new FontIcon(Material2MZ.VISIBILITY));
                viewBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
                viewBtn.setTooltip(new Tooltip("Ver detalles"));

                downloadBtn.setGraphic(new FontIcon(Material2AL.FILE_COPY));
                downloadBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.ACCENT);
                downloadBtn.setTooltip(new Tooltip("Descargar log"));

                buttons.setAlignment(Pos.CENTER);
                buttons.getChildren().addAll(viewBtn, downloadBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);

                    History history = getTableView().getItems().get(getIndex());

                    viewBtn.setOnAction(e -> handleViewDetails(history));
                    downloadBtn.setOnAction(e -> handleDownloadLog(history));
                }
            }
        });

        historyTable.getColumns().addAll(
                idCol, taskNameCol, typeCol, startTimeCol, endTimeCol,
                durationCol, statusCol, sizeCol, actionsCol
        );

        // Placeholder
        Label placeholder = new Label("No hay registros en el historial");
        placeholder.setGraphic(new FontIcon(Material2AL.HISTORY));
        placeholder.getStyleClass().add(Styles.TEXT_MUTED);
        placeholder.setFont(Fonts.regular(16));
        historyTable.setPlaceholder(placeholder);

        VBox.setVgrow(historyTable, Priority.ALWAYS);
        container.getChildren().add(historyTable);

        return container;
    }

    // ========== FOOTER ==========
    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(10, 0, 0, 0));

        pagination = new Pagination();
        pagination.setPageCount(10);
        pagination.setCurrentPageIndex(0);
        pagination.setMaxPageIndicatorCount(7);

        footer.getChildren().add(pagination);

        return footer;
    }

    // ========== HANDLERS ==========
    private void applyFilters() {
        System.out.println("Aplicar filtros");
        // Lógica para aplicar filtros
    }

    private void clearFilters() {
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        endDatePicker.setValue(LocalDate.now());
        statusFilter.setValue("Todos");
        typeFilter.setValue("Todos");
    }

    private void handleViewDetails(History history) {
        System.out.println("Ver detalles: " + history.getId());
        // Abrir diálogo con detalles completos
    }

    private void handleDownloadLog(History history) {
        System.out.println("Descargar log: " + history.getId());
        // Descargar archivo de log
    }

    // ========== GETTERS ==========
    public TableView<History> getHistoryTable() { return historyTable; }
    public DatePicker getStartDatePicker() { return startDatePicker; }
    public DatePicker getEndDatePicker() { return endDatePicker; }
    public ComboBox<String> getStatusFilter() { return statusFilter; }
    public ComboBox<String> getTypeFilter() { return typeFilter; }
    public Pagination getPagination() { return pagination; }
    public Label getTotalExecutionsLabel() { return totalExecutionsLabel; }
    public Label getSuccessRateLabel() { return successRateLabel; }
    public Button getExportButton() { return exportButton; }
    public Button getClearHistoryButton() { return clearHistoryButton; }
    public Card getTotalExecutionsCard() { return totalExecutionsCard; }
    public Card getSuccessfulCard() { return successfulCard; }
    public Card getFailedCard() { return failedCard; }
    public Card getAvgDurationCard() { return avgDurationCard; }
}
