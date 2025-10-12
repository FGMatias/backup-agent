package org.iclassq;

import atlantafx.base.theme.*;
import javafx.application.Application;
import javafx.stage.Stage;
import org.iclassq.utils.Fonts;
import org.iclassq.utils.ViewNavigator;
import org.iclassq.views.MainView;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackupAgentApplication extends Application {
    private ConfigurableApplicationContext context;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        SpringApplication app = new SpringApplication(BackupAgentApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        context = app.run();

        Fonts.loadFonts();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());

        stage.setTitle("Agent Backup");
        ViewNavigator.setMainStage(stage);

        MainView view = context.getBean(MainView.class);
        view.show();

        stage.show();
    }

    @Override
    public void stop() throws Exception {
        if (context != null) {
            context.close();
        }
    }
}