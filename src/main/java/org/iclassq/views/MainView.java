package org.iclassq.views;

import atlantafx.base.theme.Styles;
import jakarta.annotation.PostConstruct;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.iclassq.enums.TabType;
import org.iclassq.utils.Fonts;
import org.iclassq.utils.Utilitie;
import org.iclassq.utils.ViewNavigator;
import org.iclassq.views.components.Header;
import org.iclassq.views.components.Notification;
import org.iclassq.views.components.TabBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class MainView {
    private static final Logger logger = Logger.getLogger(MainView.class.getName());
    private StackPane root;
    private BorderPane contentPane;
    private TabBar tabBar;

    @Autowired
    private DashboardContent dashboardContent;

    @Autowired
    private TaskContent taskContent;

    @Autowired
    private HistoryContent historyContent;

    @PostConstruct
    public void initialize() {
        createScene();
    }

    public void show() {
        Scene scene = createScene();
        ViewNavigator.switchViews(scene);
    }

    public Scene createScene() {
        root = new StackPane();
        root.getStyleClass().add(Styles.BG_DEFAULT);
        root.setStyle("-fx-font-family: '" + Fonts.getFontFamily() + "';");

        contentPane = new BorderPane();
        contentPane.getStyleClass().add(Styles.BG_DEFAULT);

        VBox topSection = createTopSection();
        contentPane.setTop(topSection);

        loadContent(TabType.DASHBOARD);

        root.getChildren().add(contentPane);

        Notification.initialize(root);

        return new Scene(root, Utilitie.APP_WIDTH, Utilitie.APP_HEIGHT);
    }

    private VBox createTopSection() {
        VBox topSection = new VBox();

        Header header = new Header();

        tabBar = new TabBar();
        tabBar.setOnTabChanged(this::handleTabChange);

        topSection.getChildren().addAll(header, tabBar);
        return topSection;
    }

    private void handleTabChange(TabType tabType) {
        logger.info("Canbiando a tab: " + tabType.getDisplayName());
        loadContent(tabType);
    }

    private void loadContent(TabType tabType) {
        ScrollPane content;

        switch (tabType) {
            case DASHBOARD:
                content = dashboardContent.getContent();
                break;
            case TASKS:
                content = taskContent.getContent();
                break;
            case HISTORY:
                content = historyContent.getContent();
                break;
            default:
                content = dashboardContent.getContent();
        }

        contentPane.setCenter(content);
    }

    public TabBar getTabBar() {
        return tabBar;
    }

    public StackPane getRoot() {
        return root;
    }

    public BorderPane getContentPane() {
        return contentPane;
    }
}
