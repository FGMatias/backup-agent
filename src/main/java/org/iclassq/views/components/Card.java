package org.iclassq.views.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.iclassq.utils.Fonts;

public class Card extends VBox {
    private Label valueLabel;
    private boolean isErrorCard;

    public Card(String title, String value, boolean isErrorCard) {
        this.isErrorCard = isErrorCard;
        build(title, value);
    }

    public Card(String title, String value) {
        this(title, value, false);
    }

    private void build(String title, String value) {
        this.setPadding(new Insets(25));
        this.setAlignment(Pos.CENTER_LEFT);
        this.setSpacing(10);
        this.setMaxWidth(Double.MAX_VALUE);
        this.setStyle(
            "-fx-background-color: -color-bg-subtle; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add(Styles.TEXT_MUTED);
        titleLabel.setFont(Fonts.regular(16));

        valueLabel = new Label(value);
        valueLabel.getStyleClass().addAll(Styles.TITLE_2, Styles.TEXT_BOLD);

        if (isErrorCard) {
            applyErrorStyle(value);
        }

        this.getChildren().addAll(titleLabel, valueLabel);
    }

    private void applyErrorStyle(String value) {
        try {
            int errorCount = Integer.parseInt(value.trim());

            if (errorCount == 0) {
                valueLabel.getStyleClass().add(Styles.SUCCESS);
            } else {
                valueLabel.getStyleClass().add(Styles.DANGER);
            }
        } catch (NumberFormatException e) {
            valueLabel.getStyleClass().add(Styles.DANGER);
        }
    }

    public void updateValue(String newValue) {
        valueLabel.setText(newValue);

        if (isErrorCard) {
            updateErrorStyle(newValue);
        }
    }

    private void updateErrorStyle(String value) {
        valueLabel.getStyleClass().removeAll(Styles.SUCCESS, Styles.DANGER);
        applyErrorStyle(value);
    }

    public Label getValueLabel() {
        return valueLabel;
    }

    public boolean isErrorCard() {
        return isErrorCard;
    }
}
