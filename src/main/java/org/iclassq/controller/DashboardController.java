package org.iclassq.controller;

import jakarta.annotation.PostConstruct;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import org.iclassq.views.DashboardContent;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class DashboardController {
    private DashboardContent view;
    private static final Logger logger = Logger.getLogger(DashboardController.class.getName());

    public DashboardController(DashboardContent view) {
        this.view = view;
    }

    @PostConstruct
    public void initialize() {
        setupEventHandlers();
//        init();
    }

    private void setupEventHandlers() {
//        view.getTabDashboard().setOnAction(evt -> handleDashboardTab());
//        view.getTabTask().setOnAction(evt -> handleTaskTab());
//        view.getTabHistory().setOnAction(evt -> handleHistoryTab());
//        view.getBtnExecuteNow().setOnAction(evt -> handleExecuteNow());
        view.getBtnExecuteNow().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                handleExecuteNow();
            }
        });
//        view.getBtnPostpone().setOnAction(evt -> handlePostpone());
    }

//    private void init() {
//        view.getStatBackups().setText("3");
//        view.getStatFiles().setText("47");
//        view.getStatSpace().setText("2.3 GB");
//        view.getStatErrors().setText("0");
//    }

    private void handleDashboardTab() {
        logger.info("Click Dashboard Tab");
    }

    private void handleTaskTab() {
        logger.info("Click Task Tab");
    }

    private void handleHistoryTab() {
        logger.info("Click History Tab");
    }

    private void handleExecuteNow() {
        logger.info("Click Execute Now");
    }

    private void handlePostpone() {
        logger.info("Click Postpone");
    }
}
