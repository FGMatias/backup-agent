package org.iclassq.views.dialogs;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.iclassq.entity.History;
import org.iclassq.utils.Fonts;
import org.iclassq.views.components.Badge;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public class HistoryDialog extends Dialog<Void> {
    private static final Logger logger = Logger.getLogger(HistoryDialog.class.getName());

    private History history;
    private Badge badgeProvider;

    public HistoryDialog(History history) {
        this.history = history;
        this.badgeProvider = new Badge();

        initializeDialog();
        buildContent();
    }

    private void initializeDialog() {
        setTitle("Detalles de Ejecución");

        ButtonType btnClose = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().add(btnClose);

        getDialogPane().getStyleClass().add(Styles.BG_DEFAULT);

        Button closeBtn = (Button) getDialogPane().lookupButton(btnClose);
        closeBtn.getStyleClass().add(Styles.BUTTON_OUTLINED);
        closeBtn.setFont(Fonts.regular(14));
    }

    private void buildContent() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(25));
        container.setPrefWidth(600);
        container.setMaxHeight(700);

        HBox header = createHeader();
        VBox generalInfo = createGeneralInfo();
        VBox detailsSection = createDetailsSection();
        VBox messageSection = createMessageSection();

        container.getChildren().addAll(
                header,
                new Separator(),
                generalInfo,
                new Separator(),
                detailsSection,
                new Separator(),
                messageSection
        );

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("edge-to-edge");

        getDialogPane().setContent(scrollPane);
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Ejecución");
        title.setFont(Fonts.bold(24));
        title.getStyleClass().add(Styles.TITLE_2);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label badge = createStatusBadge();

        header.getChildren().addAll(title, spacer, badge);
        return header;
    }

    private Label createStatusBadge() {
        Integer statusId = history.getStatus() != null ? history.getStatus().getId() : 0;
        String statusDesc = history.getStatus() != null ? history.getStatus().getDescription() : "Sin Estado";

        Label badge = new Label(statusDesc);
        badge.setPadding(new Insets(6, 16, 6, 16));

        String baseStyle = "-fx-background-radius: 12; " +
                           "-fx-font-weight: bold; " +
                           "-fx-font-size: 13px;";

        String customStyle = badgeProvider.getStyle(statusId);
        badge.setStyle(baseStyle + " " + customStyle);

        FontIcon icon = badgeProvider.getIcon(statusId);
        if (icon != null) {
            icon.setIconSize(16);
            badge.setGraphic(icon);
            badge.setGraphicTextGap(8);
        }

        return badge;
    }

    private VBox createGeneralInfo() {
        VBox section = new VBox(15);

        Label sectionTitle = new Label("Información General");
        sectionTitle.setFont(Fonts.semiBold(16));
        sectionTitle.getStyleClass().add(Styles.TITLE_4);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);

        int row = 0;

        Label lblTaskName = new Label("Tarea:");
        lblTaskName.setFont(Fonts.medium(14));
        lblTaskName.getStyleClass().add(Styles.TEXT_MUTED);

        TextField txtTaskName = new TextField();
        txtTaskName.setText(history.getTaskId().getName());
        txtTaskName.setEditable(false);
        txtTaskName.setFont(Fonts.regular(14));
        txtTaskName.setPrefWidth(400);

        grid.add(lblTaskName, 0, row);
        grid.add(txtTaskName, 1, row);
        row++;

        Label lblDate = new Label("Fecha:");
        lblDate.setFont(Fonts.medium(14));
        lblDate.getStyleClass().add(Styles.TEXT_MUTED);

        TextField txtDate = new TextField();
        txtDate.setText(history.getCreatedAt().toLocalDate()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtDate.setEditable(false);
        txtDate.setFont(Fonts.regular(14));

        grid.add(lblDate, 0, row);
        grid.add(txtDate, 1, row);

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    private VBox createDetailsSection() {
        VBox section = new VBox(15);

        Label sectionTitle = new Label("Detalles de Ejecución");
        sectionTitle.setFont(Fonts.semiBold(16));
        sectionTitle.getStyleClass().add(Styles.TITLE_4);

        Integer typeId = history.getTypeTask() != null ? history.getTypeTask().getId() : 0;

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(15);
        detailsGrid.setVgap(12);

        int row = 0;

        Label lblType = new Label("Tipo:");
        lblType.setFont(Fonts.medium(14));
        lblType.getStyleClass().add(Styles.TEXT_MUTED);

        TextField txtType = new TextField();
        txtType.setText(history.getTypeTask() != null ? history.getTypeTask().getDescription() : "-");
        txtType.setEditable(false);
        txtType.setFont(Fonts.regular(14));
        txtType.setPrefWidth(400);

        detailsGrid.add(lblType, 0, row, 2, 1);
        detailsGrid.add(txtType, 2, row, 2, 1);
        row++;

        if (typeId == 1) {
            Label lblDatabase = new Label("Base de Datos:");
            lblDatabase.setFont(Fonts.medium(14));
            lblDatabase.getStyleClass().add(Styles.TEXT_MUTED);

            TextField txtDatabase = new TextField();
            txtDatabase.setText(history.getTaskId() != null && history.getTaskId().getDatabaseName() != null
                    ? history.getTaskId().getDatabaseName()
                    : "-");
            txtDatabase.setEditable(false);
            txtDatabase.setFont(Fonts.regular(14));

            detailsGrid.add(lblDatabase, 0, row, 2, 1);
            detailsGrid.add(txtDatabase, 2, row, 2, 1);
            row++;
        }

        Label lblStartTime = new Label("Inicio:");
        lblStartTime.setFont(Fonts.medium(14));
        lblStartTime.getStyleClass().add(Styles.TEXT_MUTED);

        TextField txtStartTime = new TextField();
        txtStartTime.setText(history.getStartTime() != null ? history.getStartTime().toString() : "-");
        txtStartTime.setEditable(false);
        txtStartTime.setFont(Fonts.regular(14));
        txtStartTime.setPrefWidth(120);

        Label lblEndTime = new Label("Fin:");
        lblEndTime.setFont(Fonts.medium(14));
        lblEndTime.getStyleClass().add(Styles.TEXT_MUTED);

        TextField txtEndTime = new TextField();
        txtEndTime.setText(history.getEndTime() != null ? history.getEndTime().toString() : "-");
        txtEndTime.setEditable(false);
        txtEndTime.setFont(Fonts.regular(14));
        txtEndTime.setPrefWidth(120);

        detailsGrid.add(lblStartTime, 0, row);
        detailsGrid.add(txtStartTime, 1, row);
        detailsGrid.add(lblEndTime, 2, row);
        detailsGrid.add(txtEndTime, 3, row);
        row++;

        Label lblDuration = new Label("Duración:");
        lblDuration.setFont(Fonts.medium(14));
        lblDuration.getStyleClass().add(Styles.TEXT_MUTED);

        TextField txtDuration = new TextField();
        txtDuration.setText(history.getDuration() != null ? history.getDuration() : "-");
        txtDuration.setEditable(false);
        txtDuration.setFont(Fonts.regular(14));
        txtDuration.setPrefWidth(120);

        Label lblSize = new Label("Tamaño:");
        lblSize.setFont(Fonts.medium(14));
        lblSize.getStyleClass().add(Styles.TEXT_MUTED);

        TextField txtSize = new TextField();
        txtSize.setText(history.getFormattedSize());
        txtSize.setEditable(false);
        txtSize.setFont(Fonts.regular(14));
        txtSize.setPrefWidth(120);

        detailsGrid.add(lblDuration, 0, row);
        detailsGrid.add(txtDuration, 1, row);
        detailsGrid.add(lblSize, 2, row);
        detailsGrid.add(txtSize, 3, row);
        row++;

        if (typeId == 2 || typeId == 3 || typeId == 4) {
            Label lblFileCount = new Label("Archivos procesados:");
            lblFileCount.setFont(Fonts.medium(14));
            lblFileCount.getStyleClass().add(Styles.TEXT_MUTED);

            TextField txtFileCount = new TextField();
            txtFileCount.setText(history.getFileCount() != null ? history.getFileCount().toString() : "0");
            txtFileCount.setEditable(false);
            txtFileCount.setFont(Fonts.regular(14));

            detailsGrid.add(lblFileCount, 0, row, 2, 1);
            detailsGrid.add(txtFileCount, 2, row, 2, 1);
            row++;

            Label lblSource = new Label("Ruta origen:");
            lblSource.setFont(Fonts.medium(14));
            lblSource.getStyleClass().add(Styles.TEXT_MUTED);

            TextField txtSource = new TextField();
            txtSource.setText(history.getTaskId() != null && history.getTaskId().getSourcePath() != null
                    ? history.getTaskId().getSourcePath()
                    : "-");
            txtSource.setEditable(false);
            txtSource.setFont(Fonts.regular(13));

            detailsGrid.add(lblSource, 0, row, 2, 1);
            detailsGrid.add(txtSource, 2, row, 2, 1);
            row++;

            if (typeId == 2 || typeId == 3) {
                Label lblDestination = new Label("Ruta destino:");
                lblDestination.setFont(Fonts.medium(14));
                lblDestination.getStyleClass().add(Styles.TEXT_MUTED);

                TextField txtDestination = new TextField();
                txtDestination.setText(history.getTaskId() != null && history.getTaskId().getDestinationPath() != null
                        ? history.getTaskId().getDestinationPath()
                        : "-");
                txtDestination.setEditable(false);
                txtDestination.setFont(Fonts.regular(13));

                detailsGrid.add(lblDestination, 0, row, 2, 1);
                detailsGrid.add(txtDestination, 2, row, 2, 1);
                row++;
            }
        }

        section.getChildren().addAll(sectionTitle, detailsGrid);
        return section;
    }

    private VBox createMessageSection() {
        VBox section = new VBox(10);

        Label sectionTitle = new Label("Mensaje de Ejecución");
        sectionTitle.setFont(Fonts.semiBold(16));
        sectionTitle.getStyleClass().add(Styles.TITLE_4);

        TextArea txtMessage = new TextArea();
        txtMessage.setText(history.getMessage() != null ? history.getMessage() : "Sin mensaje");
        txtMessage.setEditable(false);
        txtMessage.setWrapText(true);
        txtMessage.setPrefRowCount(4);
        txtMessage.setFont(Fonts.regular(14));
        txtMessage.setStyle("-fx-control-inner-background: -color-bg-subtle;");

        VBox.setVgrow(txtMessage, Priority.ALWAYS);

        section.getChildren().addAll(sectionTitle, txtMessage);
        return section;
    }
}