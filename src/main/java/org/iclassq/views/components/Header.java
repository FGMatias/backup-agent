package org.iclassq.views.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class Header extends VBox {
    public Header() {
        build();
    }

    private void build() {
        this.setPadding(new Insets(15, 20, 15, 20));
        this.getStyleClass().add(Styles.BG_DEFAULT);

        Label title = new Label("Backup Agente");
        title.getStyleClass().addAll(Styles.TITLE_1, Styles.TEXT_BOLD);
        this.getChildren().add(title);
    }
}
