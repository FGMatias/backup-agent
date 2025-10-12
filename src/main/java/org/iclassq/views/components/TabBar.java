package org.iclassq.views.components;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.iclassq.enums.TabType;
import org.iclassq.utils.Fonts;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;

public class TabBar extends TabPane {
    private Tab tabDashboard;
    private Tab tabTask;
    private Tab tabHistory;
    private TabChangeListener listener;

    public interface TabChangeListener {
        void onTabChanged(TabType tabType);
    }

    public TabBar() {
        build();
        setupListener();
    }

    private void build() {
        this.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        tabDashboard = new Tab("Dashboard");
        tabDashboard.setStyle(Fonts.cssMedium(18));
        tabDashboard.setGraphic(new FontIcon(Material2AL.DASHBOARD));
        tabDashboard.setUserData(TabType.DASHBOARD);

        tabTask = new Tab("Tareas");
        tabTask.setStyle(Fonts.cssMedium(18));
        tabTask.setGraphic(new FontIcon(Material2AL.EVENT_AVAILABLE));
        tabTask.setUserData(TabType.TASKS);

        tabHistory = new Tab("Historial");
        tabHistory.setStyle(Fonts.cssMedium(18));
        tabHistory.setGraphic(new FontIcon(Material2AL.HISTORY));
        tabHistory.setUserData(TabType.HISTORY);

        this.getTabs().addAll(tabDashboard, tabTask, tabHistory);
    }

    private void setupListener() {
        this.getSelectionModel().selectedItemProperty().addListener((observableValue, oldTab, newTab) -> {
            if (newTab != null && listener != null) {
                TabType tabType = (TabType) newTab.getUserData();
                listener.onTabChanged(tabType);
            }
        });
    }

    public void setOnTabChanged(TabChangeListener listener) {
        this.listener = listener;
    }

    public void setActiveTab(TabType tabType) {
        switch (tabType) {
            case DASHBOARD:
                this.getSelectionModel().select(tabDashboard);
                break;
            case TASKS:
                this.getSelectionModel().select(tabTask);
                break;
            case HISTORY:
                this.getSelectionModel().select(tabHistory);
                break;
        }
    }

    public TabType getCurrentTab() {
        Tab selected = this.getSelectionModel().getSelectedItem();
        return selected != null
                ? (TabType) selected.getUserData()
                : TabType.DASHBOARD;
    }
}
