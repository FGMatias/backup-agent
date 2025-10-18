package org.iclassq.views.components;

import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.iclassq.utils.Fonts;
import org.kordamp.ikonli.javafx.FontIcon;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Table<T> extends VBox {
    private final TableView<T> tableView;
    private final ObservableList<T> allItems;
    private final ObservableList<T> currentPageItems;
    private Pagination pagination;
    private int itemsPerPage = 20;
    private boolean paginationEnabled = true;
    private Label infoLabel;
    private  boolean showInfoLabel = true;

    public Table() {
        this.tableView = new TableView<>();
        this.allItems = FXCollections.observableArrayList();
        this.currentPageItems = FXCollections.observableArrayList();

        setupTable();
        build();
    }

    private void setupTable() {
        tableView.getStyleClass().addAll(Styles.BORDERED, Styles.STRIPED);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setItems(currentPageItems);
    }

    private void build() {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(10);

        this.getChildren().add(tableView);

        createPagination();
        createInfoLabel();
    }

    private void createPagination() {
        pagination = new Pagination();
        pagination.setPageCount(1);
        pagination.setCurrentPageIndex(0);
        pagination.setMaxPageIndicatorCount(7);
        pagination.setPageFactory(this::createPage);
        pagination.setVisible(paginationEnabled);
        pagination.setManaged(paginationEnabled);

        pagination.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) customizePaginationButtons();
        });

        this.getChildren().add(pagination);
    }

    private void customizePaginationButtons() {
        pagination.lookupAll(".number-button").forEach(node -> {
            if (node instanceof Button) {
                Button btn = (Button) node;
                btn.setCursor(javafx.scene.Cursor.HAND);

                btn.setOnMouseEntered(e -> {
                    if (!btn.getStyleClass().contains("selected")) {
                        btn.setStyle(
                                "-fx-background-color: -color-accent-subtle; " +
                                "-fx-text-fill: -color-accent-emphasis;"
                        );
                    }
                });

                btn.setOnMouseExited(e -> {
                    if (!btn.getStyleClass().contains("selected")) {
                        btn.setStyle("");
                    }
                });
            }
        });

        pagination.lookupAll(".left-arrow-button, .right-arrow-button").forEach(node -> {
            if (node instanceof Button) {
                Button btn = (Button) node;
                btn.setCursor(javafx.scene.Cursor.HAND);
            }
        });
    }

    private void createInfoLabel() {
        infoLabel = new Label();
        infoLabel.getStyleClass().add(Styles.TEXT_MUTED);
        infoLabel.setFont(Fonts.regular(13));
        infoLabel.setVisible(showInfoLabel);
        infoLabel.setManaged(showInfoLabel);

        HBox infoBox = new HBox();
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(5, 0, 0, 0));

        this.getChildren().add(infoBox);

        updateInfoLabel();
    }

    private TableView<T> createPage(int pageIndex) {
        int fromIndex = pageIndex * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, allItems.size());

        currentPageItems.clear();

        if (fromIndex < allItems.size()) {
            currentPageItems.addAll(allItems.subList(fromIndex, toIndex));
        }

        updateInfoLabel();
        Platform.runLater(this::customizePaginationButtons);

        return null;
    }

    private void updateInfoLabel() {
        if (!showInfoLabel || allItems.isEmpty()) {
            infoLabel.setText("");
            return;
        }

        if (paginationEnabled) {
            int currentPage = pagination.getCurrentPageIndex() + 1;
            int totalPages = pagination.getPageCount();
            int showing = currentPageItems.size();
            int total = allItems.size();

            infoLabel.setText(String.format(
                "Mostrando %d de %d registros (Página %d de %d)",
                showing, total, currentPage, totalPages
            ));
        } else {
            infoLabel.setText(String.format("Total: %d registros", allItems.size()));
        }
    }

    public Table<T> setPaginationEnabled(boolean enabled) {
        this.paginationEnabled = enabled;
        pagination.setVisible(enabled);
        pagination.setManaged(enabled);

        if (!enabled) {
            currentPageItems.clear();
            currentPageItems.addAll(allItems);
        } else {
            updatePagination();
        }

        updateInfoLabel();
        return this;
    }

    public Table<T> setItemsPerPage(int items) {
        this.itemsPerPage = items;
        updatePagination();
        return this;
    }

    public Table<T> setShowInfoLabel(boolean show) {
        this.showInfoLabel = show;
        infoLabel.setVisible(show);
        infoLabel.setManaged(show);
        updateInfoLabel();
        return this;
    }

    public Table<T> setMaxPageIndicators(int max) {
        pagination.setMaxPageIndicatorCount(max);
        return this;
    }

    private void updatePagination() {
        if (!paginationEnabled) return;

        int pageCount = (int) Math.ceil((double) allItems.size() / itemsPerPage);
        pagination.setPageCount(Math.max(1, pageCount));

        pagination.setCurrentPageIndex(0);
        createPage(0);
    }

    public Table<T> addColumn(
            String header,
            String property,
            double width
    ) {
        TableColumn<T, Object> column = new TableColumn<>(header);

        if (property.contains(".")) {
            column.setCellValueFactory(cellData -> {
                try {
                    Object value = getNestedProperty(cellData.getValue(), property);
                    return new SimpleObjectProperty<>(value);
                } catch (Exception e) {
                    return new SimpleObjectProperty<>(null);
                }
            });
        } else {
            column.setCellValueFactory(new PropertyValueFactory<>(property));
        }

        column.setPrefWidth(width);
        tableView.getColumns().add(column);
        return this;
    }

    public Table<T> addColumn(
            String header,
            String property,
            double width,
            String alignment
    ) {
        addColumn(header, property, width);
        TableColumn<T, ?> lastColumn = tableView.getColumns().get(tableView.getColumns().size() - 1);
        lastColumn.setStyle("-fx-alignment: " + alignment + ";");
        return this;
    }

    public Table<T> addColumnWithExtractor(
            String header,
            double width,
            Function<T, String> valueExtractor
    ) {
        TableColumn<T, String> column = new TableColumn<>(header);
        column.setCellValueFactory(cellData ->
            new SimpleStringProperty(valueExtractor.apply(cellData.getValue()))
        );

        column.setPrefWidth(width);
        tableView.getColumns().add(column);
        return this;
    }

    public Table<T> addColumnWithExtractor(
            String header,
            double width,
            String alignment,
            Function<T, String> valueExtractor
    ) {
        addColumnWithExtractor(header, width, valueExtractor);
        TableColumn<T, ?> lastColumn = tableView.getColumns().get(tableView.getColumns().size() - 1);
        lastColumn.setStyle("-fx-alignment: " + alignment + ";");
        return this;
    }

    public Table<T> addColumnWithCellFactory(
            String header,
            String property,
            double width,
            Callback<TableColumn<T, String>, TableCell<T, String>> cellFactory
    ) {
        TableColumn<T, String> column = new TableColumn<>(header);

        if (property.contains(".")) {
            column.setCellValueFactory(cellData -> {
                try {
                    Object value = getNestedProperty(cellData.getValue(), property);
                    return new SimpleStringProperty(value != null ? value.toString() : "");
                } catch (Exception e) {
                    return new SimpleStringProperty("");
                }
            });
        } else {
            column.setCellValueFactory(new PropertyValueFactory<>(property));
        }

        column.setPrefWidth(width);
        column.setCellFactory(cellFactory);
        tableView.getColumns().add(column);
        return this;
    }

    public Table<T> addColumnWithDefault(
            String header,
            String property,
            double width,
            String defaultValue
    ) {
        return addColumnWithExtractor(header, width, item -> {
            try {
                Object value = getNestedProperty(item, property);
                if (value == null) {
                    return defaultValue;
                }
                String strValue = value.toString();
                return strValue.isEmpty() ? defaultValue : strValue;
            } catch (Exception e) {
                return defaultValue;
            }
        });
    }

    public Table<T> addColumnWithDefault(
            String header,
            String property,
            double width,
            String alignment,
            String defaultValue
    ) {
        addColumnWithDefault(header, property, width, defaultValue);
        TableColumn<T, ?> lastColumn = tableView.getColumns().get(tableView.getColumns().size() - 1);
        lastColumn.setStyle("-fx-alignment: " + alignment + ";");
        return this;
    }

    public Table<T> addBadgeColumn(
            String header,
            String property,
            double width,
            BadgeStyleProvider styleProvider
    ) {
        TableColumn<T, String> column = new TableColumn<>(header);

        if (property.contains(".")) {
            column.setCellValueFactory(cellData -> {
                try {
                    Object value = getNestedProperty(cellData.getValue(), property);
                    return new SimpleStringProperty(value != null ? value.toString() : "");
                } catch (Exception e) {
                    return new SimpleStringProperty("");
                }
            });
        } else {
            column.setCellValueFactory(new PropertyValueFactory<>(property));
        }

        column.setPrefWidth(width);
        column.setStyle("-fx-alignment: CENTER;");

        column.setCellFactory(col -> new TableCell<T, String>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null || value.isEmpty()) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = createBadge(value, styleProvider);
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        tableView.getColumns().add(column);
        return this;
    }

    public Table<T> addBadgeColumnWhitId(
            String header,
            Function<T, Integer> idExtractor,
            Function<T, String> descriptionExtractor,
            double width,
            BadgeStyleProviderById styleProviderById
    ) {
        TableColumn<T, String> column = new TableColumn<>(header);

        column.setPrefWidth(width);
        column.setStyle("-fx-alignment: CENTER;");

        column.setCellValueFactory(cellData -> {
            T item = cellData.getValue();
            String description = descriptionExtractor.apply(item);
            return new SimpleStringProperty(description);
        });

        column.setCellFactory(col -> new TableCell<T, String>() {
            @Override
            protected void updateItem(String description, boolean empty) {
                super.updateItem(description, empty);

                if (empty || description == null || description.isEmpty()) {
                    setGraphic(null);
                    setText(null);
                } else {
                    T item = (T) getTableRow().getItem();
                    if (item != null) {
                        String desc = description;

                        if (desc == null || desc.isEmpty()) {
                            desc = descriptionExtractor.apply(item);
                        }

                        Integer id = idExtractor.apply(item);

                        if (desc != null && !desc.isEmpty()) {
                            Label badge = createBadgeById(description, id, styleProviderById);
                            setGraphic(badge);
                            setText(null);
                        } else {
                            setGraphic(null);
                            setText("-");
                        }
                    } else {
                        setGraphic(null);
                        setText(null);
                    }
                }
            }
        });

        tableView.getColumns().add(column);
        return this;
    }

    public Table<T> addActionsColumn(
            String header,
            double width,
            List<ActionButton<T>> actions
    ) {
        TableColumn<T, Void> column = new TableColumn<>(header);
        column.setPrefWidth(width);
        column.setStyle("-fx-alignment: CENTER;");

        column.setCellFactory(col -> new TableCell<T, Void>() {
            private final HBox buttons = new HBox(5);
            private final Map<ActionButton<T>, Button> buttonMap = new HashMap<>();

            {
                buttons.setAlignment(Pos.CENTER);

                for (ActionButton<T> action : actions) {
                    Button btn = new Button();

                    if (action.getIcon() != null) {
                        btn.setGraphic(new FontIcon(action.getIcon()));
                    }

                    btn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);

                    if (action.getStyleClass() != null) {
                        btn.getStyleClass().add(action.getStyleClass());
                    }

                    if (action.getTooltip() != null) {
                        btn.setTooltip(new Tooltip(action.getTooltip()));
                    }

                    btn.setOnAction(evt -> {
                        T item = getTableView().getItems().get(getIndex());
                        action.getAction().accept(item);
                    });

                    buttonMap.put(action, btn);
                    buttons.getChildren().add(btn);
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    T rowItem = getTableView().getItems().get(getIndex());

                    for (Map.Entry<ActionButton<T>, Button> entry : buttonMap.entrySet()) {
                        ActionButton<T> action = entry.getKey();
                        Button btn = entry.getValue();

                        if (action.getVisibilityCondition() != null) {
                            boolean shouldBeVisible = action.getVisibilityCondition().apply(rowItem);
                            btn.setVisible(shouldBeVisible);
                            btn.setManaged(shouldBeVisible);
                        } else {
                            btn.setVisible(true);
                            btn.setManaged(true);
                        }
                    }

                    setGraphic(buttons);
                }
            }
        });

        tableView.getColumns().add(column);
        return this;
    }

    public Table<T> setPlaceHolder(String text, String icon) {
        Label placeholder = new Label(text);

        if (icon != null && !icon.isEmpty()) {
            try {
                placeholder.setGraphic(new FontIcon(icon));
            } catch (Exception e) {
            }
        }

        placeholder.getStyleClass().add(Styles.TEXT_MUTED);
        placeholder.setFont(Fonts.regular(16));
        tableView.setPlaceholder(placeholder);

        return this;
    }

    public Table<T> setPlaceHolder(String text) {
        return setPlaceHolder(text, null);
    }

    public void setData(List<T> data) {
        allItems.clear();

        if (data != null) {
            allItems.addAll(data);
        }
        updatePagination();
    }

    public void addItem(T item) {
        allItems.add(item);
        updatePagination();
    }

    public void addItems(List<T> newItems) {
        if (newItems != null) {
            allItems.addAll(newItems);
            updatePagination();
        }
    }

    public void removeItem(T item) {
        allItems.remove(item);
        updatePagination();
    }

    public void updateItem(int index, T item) {
        if (index >= 0 && index < allItems.size()) {
            allItems.set(index, item);
            refresh();
        }
    }

    public void clearData() {
        allItems.clear();
        updatePagination();
    }

    public void refresh() {
        tableView.refresh();
        updateInfoLabel();
    }

    public void filter(Function<T, Boolean> predicate) {
        List<T> filtered = allItems.stream()
                .filter(predicate::apply)
                .collect(Collectors.toList());

        currentPageItems.clear();

        if (paginationEnabled) {
            int fromIndex = pagination.getCurrentPageIndex() * itemsPerPage;
            int toIndex = Math.min(fromIndex + itemsPerPage, filtered.size());

            if (fromIndex < filtered.size()) {
                currentPageItems.addAll(filtered.subList(fromIndex, toIndex));
            }

            int pageCount = (int) Math.ceil((double) filtered.size() / itemsPerPage);
            pagination.setPageCount(Math.max(1, pageCount));
        } else {
            currentPageItems.addAll(filtered);
        }

        updateInfoLabel();
    }

    public T getSelectedItem() {
        return tableView.getSelectionModel().getSelectedItem();
    }

    public int getSelectedIndex() {
        return tableView.getSelectionModel().getFocusedIndex();
    }

    public void selectItem(int index) {
        tableView.getSelectionModel().select(index);
    }

    public void selectItem(T index) {
        tableView.getSelectionModel().select(index);
    }

    public void clearSelection() {
        tableView.getSelectionModel().clearSelection();
    }

    public Table<T> setOnSelectionChange(Consumer<T> listener) {
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (listener != null) {
                        listener.accept(newValue);
                    }
                }
        );

        return this;
    }

    public ObservableList<T> getItems() {
        return allItems;
    }

    public ObservableList<T> getCurrentPageItems() {
        return currentPageItems;
    }

    public TableView<T> getTableView() {
        return tableView;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public int getItemCount() {
        return allItems.size();
    }

    public int getCurrentPage() {
        return pagination.getCurrentPageIndex();
    }

    public int getTotalPages() {
        return pagination.getPageCount();
    }

    private Label createBadge(String text, BadgeStyleProvider styleProvider) {
        Label badge = new Label(text);
        badge.setPadding(new Insets(4, 12, 4, 12));
        badge.setStyle(
                "-fx-background-radius: 12; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 11px;"
        );

        String customStyle = styleProvider.getStyle(text);
        if (customStyle != null && !customStyle.isEmpty()) {
            badge.setStyle(badge.getStyle() + customStyle);
        }

        FontIcon icon = styleProvider.getIcon(text);
        if (icon != null) {
            badge.setGraphic(icon);
        }

        return badge;
    }

    private Label createBadgeById(
            String text,
            Integer id,
            BadgeStyleProviderById styleProvider
    ) {
        Label badge = new Label(text);
        badge.setPadding(new Insets(4, 12, 4, 12));
        badge.setStyle(
                "-fx-background-radius: 12; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 11px;"
        );

        String customStyle = styleProvider.getStyle(id);
        if (customStyle != null && !customStyle.isEmpty()) {
            badge.setStyle(badge.getStyle() + customStyle);
        } else {
            badge.setStyle(badge.getStyle() + " -fx-background-color: gray; -fx-text-fill: white;");
        }

        FontIcon icon = styleProvider.getIcon(id);
        if (icon != null) {
            icon.setIconSize(14);
            badge.setGraphic(icon);
            badge.setGraphicTextGap(6);
        }

        return badge;
    }

    private Object getNestedProperty(Object obj, String propertyPath) throws Exception {
        String[] parts = propertyPath.split("\\.");
        Object current = obj;

        for (String part : parts) {
            if (current == null) return null;

            String methodName = "get" + part.substring(0, 1).toUpperCase() + part.substring(1);

            try {
                Method method = current.getClass().getMethod(methodName);
                current = method.invoke(current);
            } catch (NoSuchMethodException e) {
                methodName = "is" + part.substring(0, 1).toUpperCase() + part.substring(1);
                Method method = current.getClass().getMethod(methodName);
                current = method.invoke(current);
            }
        }

        return current;
    }

    public Table<T> addToggleSwitchColumn(
            String header,
            Function<T, Boolean> statusExtractor,
            Function<T, String> descriptionExtractor,
            double width,
            BiConsumer<T, Boolean> onToggle
    ) {
        TableColumn<T, Void> column = new TableColumn<>(header);
        column.setPrefWidth(width);
        column.setStyle("-fx-alignment: CENTER;");

        column.setCellFactory(col -> new TableCell<T, Void>() {
            private final ToggleSwitch toggleSwitch = new ToggleSwitch();
            private final Label label = new Label();
            private final HBox container = new HBox(8);
            private boolean isUpdating = false;

            {
                container.setAlignment(Pos.CENTER);
                container.getChildren().addAll(toggleSwitch, label);

                toggleSwitch.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (isUpdating) return;

                    T item = getTableView().getItems().get(getIndex());

                    boolean previousState = oldValue;

                    if (onToggle != null) {
                        isUpdating = true;

                        try {
                            onToggle.accept(item, newValue);

                            Boolean currentItemState = statusExtractor.apply(item);

                            if (currentItemState != null && currentItemState != newValue) {
                                toggleSwitch.setSelected(currentItemState);
                                updateLabelStyle(currentItemState);
                            }
                        } finally {
                            isUpdating = false;
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    T rowItem = getTableView().getItems().get(getIndex());
                    Boolean isActive = statusExtractor.apply(rowItem);
                    String description = descriptionExtractor.apply(rowItem);
                    boolean switchState = (isActive != null && isActive);

                    isUpdating = true;
                    toggleSwitch.setSelected(switchState);

                    label.setText(description != null ? description : "Sin estado");
                    label.setStyle(
                            switchState
                                ? "-fx-text-fill: -color-success-emphasis; -fx-font-weight: bold;"
                                : "-fx-text-fill: -color-danger-emphasis;"
                    );

                    isUpdating = false;

                    setGraphic(container);
                }
            }

            private void updateLabelStyle(boolean isActive) {
                label.setStyle(
                        isActive
                            ? "-fx-text-fill: -color-success-emphasis; -fx-font-weight: bold;"
                            : "-fx-text-fill: -color-danger-emphasis;"
                );
            }
        });

        tableView.getColumns().add(column);
        return this;
    }

    @FunctionalInterface
    public interface BadgeStyleProvider {
        String getStyle(String value);
        default FontIcon getIcon(String value) {
            return null;
        }
    }

    @FunctionalInterface
    public interface BadgeStyleProviderById {
        String getStyle(Integer id);

        default FontIcon getIcon(Integer id) {
            return null;
        }
    }

    public static class ActionButton<T> {
        private final String icon;
        private final String tooltip;
        private final String styleClass;
        private final Consumer<T> action;
        private final Function<T, Boolean> visibilityCondition;

        public ActionButton(String icon, String tooltip, String styleClass, Consumer<T> action, Function<T, Boolean> visibilityCondition) {
            this.icon = icon;
            this.tooltip = tooltip;
            this.styleClass = styleClass;
            this.action = action;
            this.visibilityCondition = visibilityCondition;
        }

        public ActionButton(String icon, String tooltip, String styleClass, Consumer<T> action) {
            this(icon, tooltip, styleClass, action, null);
        }

        public ActionButton(String icon, String tooltip, Consumer<T> action) {
            this(icon, tooltip, null, action, null);
        }

        public ActionButton(String icon, Consumer<T> action) {
            this(icon, null, null, action, null);
        }

        public String getIcon() {
            return icon;
        }

        public String getTooltip() {
            return tooltip;
        }

        public String getStyleClass() {
            return styleClass;
        }

        public Consumer<T> getAction() {
            return action;
        }

        public Function<T, Boolean> getVisibilityCondition() {
            return visibilityCondition;
        }
    }
}
