package org.iclassq.views;

import atlantafx.base.theme.Styles;
import jakarta.annotation.PostConstruct;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
//import org.iclassq.controller.HistoryController;
import org.iclassq.entity.History;
import org.iclassq.utils.Fonts;
import org.iclassq.utils.Utilitie;
import org.iclassq.views.components.Badge;
import org.iclassq.views.components.Card;
import org.iclassq.views.components.Grid;
import org.iclassq.views.components.Table;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Component
public class HistoryContent {
    private VBox mainContent;
    private Table<History> historyTable;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private ComboBox<String> statusFilter;
    private ComboBox<String> typeFilter;
    private Card totalExecutionsCard;
    private Card successfulCard;
    private Card failedCard;
    private Card avgDurationCard;
    private Button btnRefresh;
    private Button btnClearHistory;
    private Button btnSearch;
    private Button btnClearFilters;

    private Consumer<History> onShowDetails;
    private Consumer<History> onCancel;
    private Runnable onRefresh;
    private Runnable onClearHistory;
    private Runnable onSearch;
    private Runnable onClearFilters;

    private static final Logger logger = Logger.getLogger(HistoryContent.class.getName());

    public HistoryContent() {
    }

    @PostConstruct
    public void initialize() {
        buildContent();
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        btnRefresh.setOnAction(evt -> {
            if (onRefresh != null) onRefresh.run();
        });
        btnClearHistory.setOnAction(evt -> {
            if (onClearHistory != null) onClearHistory.run();
        });
        btnSearch.setOnAction(evt -> {
            if (onSearch != null) onSearch.run();
        });
        btnClearFilters.setOnAction(evt -> {
            if (onClearFilters != null) onClearFilters.run();
        });
    }

    private void buildContent() {
        mainContent = new VBox(20);
        mainContent.setPadding(new Insets(25));
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.getStyleClass().add(Styles.BG_DEFAULT);

        VBox contentContainer = new VBox(20);
//        contentContainer.setMaxWidth(Utilitie.APP_WIDTH - 20);
        contentContainer.setAlignment(Pos.TOP_CENTER);

        HBox header = createHeader();
        Grid summaryCards = createSummaryCards();
        VBox filtersSection = createFiltersSection();
        VBox tableContainer = createTableSection();

        contentContainer.getChildren().addAll(
                header,
                summaryCards,
                filtersSection,
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

        Label title = new Label("Historial de Ejecuciones");
        title.getStyleClass().add(Styles.TITLE_2);
        title.setFont(Fonts.bold(24));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnRefresh = new Button();
        btnRefresh.setGraphic(new FontIcon(Material2MZ.REFRESH));
        btnRefresh.getStyleClass().add(Styles.BUTTON_ICON);
        btnRefresh.setTooltip(new Tooltip("Refrescar"));

        btnClearHistory = new Button("Limpiar Historial");
        btnClearHistory.setGraphic(new FontIcon(Material2AL.DELETE_SWEEP));
        btnClearHistory.getStyleClass().addAll(Styles.DANGER, Styles.BUTTON_OUTLINED);
        btnClearHistory.setFont(Fonts.semiBold(16));

        header.getChildren().addAll(
                title,
                spacer,
                btnRefresh,
                btnClearHistory
        );
        return header;
    }

    private Grid createSummaryCards() {
        totalExecutionsCard = new Card("Total Ejecuciones", "0"); //totalExecutionsLabel.getText()
        successfulCard = new Card("Exitosas", "0"); // totalSuccessfulLabel.getText()
        failedCard = new Card("Fallidas", "0", true); // totalFailedLabel.getText()
        avgDurationCard = new Card("Duración Promedio", "0"); // totalAvgDurationLabel.getText()

        return new Grid(
                4,
                totalExecutionsCard,
                successfulCard,
                failedCard,
                avgDurationCard
        );
    }

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

        Label statusLabel = new Label("Estado:");
        statusLabel.getStyleClass().add(Styles.TEXT_MUTED);
        statusLabel.setFont(Fonts.regular(14));

        statusFilter = new ComboBox<>();
        statusFilter.setPrefWidth(130);

        Label typeLabel = new Label("Tipo:");
        typeLabel.getStyleClass().add(Styles.TEXT_MUTED);
        typeLabel.setFont(Fonts.regular(14));

        typeFilter = new ComboBox<>();
        typeFilter.setPrefWidth(150);

        btnSearch = new Button("Aplicar");
        btnSearch.getStyleClass().addAll(Styles.ACCENT);
        btnSearch.setFont(Fonts.semiBold(14));

        btnClearFilters = new Button("Limpiar");
        btnClearFilters.getStyleClass().add(Styles.BUTTON_OUTLINED);
        btnClearFilters.setFont(Fonts.regular(14));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        filtersBar.getChildren().addAll(
                dateLabel, startDatePicker, toLabel, endDatePicker,
                new Separator(Orientation.VERTICAL),
                statusLabel, statusFilter,
                typeLabel, typeFilter,
                spacer,
                btnSearch, btnClearFilters
        );

        section.getChildren().addAll(title, filtersBar);
        return section;
    }

    private VBox createTableSection() {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);

        historyTable = new Table<>();

        historyTable.setItemsPerPage(15)
                    .setMaxPageIndicators(7)
                    .setShowInfoLabel(true)
                    .addColumn("ID", "id", 50)
                    .addColumn("Nombre", "taskId.name", 150)
                    .addColumnWithDefault("Tipo", "typeTask.description", 120, "Sin Tipo")
                    .addColumn("Inicio", "startTime", 60)
                    .addColumn("Fin", "endTime", 60)
                    .addColumn("Duración", "duration", 60)
                    .addBadgeColumnWhitId(
                            "Estado",
                            history -> history.getStatus() != null ? history.getStatus().getId() : 0,
                            history -> history.getStatus() != null ? history.getStatus().getDescription() : "Desconocido",
                            100,
                            new Badge()
                    )
                    .addColumn("Cantidad Archivos", "fileCount", 70)
                    .addColumnWithExtractor("Tamaño", 60, History::getFormattedSize)
                    .addColumnWithExtractor("Fecha", 60, history -> {
                        if (history.getCreatedAt() == null) return "-";

                        return history.getCreatedAt().toLocalDate()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    })
                    .addActionsColumn("Acciones", 90, List.of(
                            new Table.ActionButton<>(
                                    "mdmz-visibility",
                                    "Ver Detalles",
                                    Styles.ACCENT,
                                    history -> {
                                        if (onShowDetails != null) onShowDetails.accept(history);
                                    }
                            ),
                            new Table.ActionButton<>(
                                    "mdral-cancel",
                                    "Cancelar",
                                    Styles.DANGER,
                                    history -> {
                                        if (onCancel != null) onCancel.accept(history);
                                    },
                                    history -> history.getStatus().getId() == 3
                            )
                    ))
                    .setPlaceHolder("No hay registros en el historial", "mdal-history");

        VBox.setVgrow(historyTable, Priority.ALWAYS);
        container.getChildren().add(historyTable);

        return container;
    }

    public void refreshTable(List<History> histories) {
        historyTable.setData(histories);
    }

    public void updateExecutionsCount(long count) {
        totalExecutionsCard.updateValue(String.valueOf(count));
    }

    public void updateSuccessfulCount(long count) {
        successfulCard.updateValue(String.valueOf(count));
    }

    public void updateFailedCount(long count) {
        failedCard.updateValue(String.valueOf(count));
    }

    public void updateAvgDurationCount(String avgDuration) {
        avgDurationCard.updateValue(avgDuration);
    }

    public VBox getMainContent() {
        return mainContent;
    }

    public Table<History> getHistoryTable() {
        return historyTable;
    }

    public DatePicker getStartDatePicker() {
        return startDatePicker;
    }

    public DatePicker getEndDatePicker() {
        return endDatePicker;
    }

    public ComboBox<String> getStatusFilter() {
        return statusFilter;
    }

    public ComboBox<String> getTypeFilter() {
        return typeFilter;
    }

    public Button getBtnRefresh() {
        return btnRefresh;
    }

    public Button getBtnClearHistory() {
        return btnClearHistory;
    }

    public Button getBtnSearch() {
        return btnSearch;
    }

    public Button getBtnClearFilters() {
        return btnClearFilters;
    }

    public void setOnShowDetails(Consumer<History> callback) {
        this.onShowDetails = callback;
    }

    public void setOnCancel(Consumer<History> callback) {
        this.onCancel = callback;
    }

    public void setOnRefresh(Runnable callback) {
        this.onRefresh = callback;
    }

    public void setOnClearHistory(Runnable callback) {
        this.onClearHistory = callback;
    }

    public void setOnSearch(Runnable callback) {
        this.onSearch = callback;
    }

    public void setOnClearFilters(Runnable callback) {
        this.onClearFilters = callback;
    }
}
