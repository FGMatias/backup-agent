package org.iclassq.views.components;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;

public class Badge implements Table.BadgeStyleProviderById {
    @Override
    public String getStyle(Integer statusId) {
        if (statusId == null || statusId == 0) {
            return "-fx-background-color: -color-bg-default; -fx-text-fill: -color-fg-muted;";
        }

        return switch (statusId) {
            case 1 -> "-fx-background-color: -color-success-7; -fx-text-fill: white;";
            case 2 -> "-fx-background-color: -color-danger-7; -fx-text-fill: white;";
            case 3 -> "-fx-background-color: -color-accent-7; -fx-text-fill: white;";
            case 4 -> "-fx-background-color: -color-warning-7; -fx-text-fill: white;";
            default -> "-fx-background-color: -color-bg-default; -fx-text-fill: -color-fg-muted;";
        };
    }

    @Override
    public FontIcon getIcon(Integer statusId) {
        if (statusId == null || statusId == 0) {
            return new FontIcon(Material2AL.HELP_OUTLINE);
        }

        return switch (statusId) {
            case 1 -> new FontIcon(Material2AL.CHECK_CIRCLE);
            case 2 -> new FontIcon(Material2AL.ERROR);
            case 3 -> new FontIcon(Material2MZ.PENDING);
            case 4 -> new FontIcon(Material2AL.CANCEL);
            default -> new FontIcon(Material2AL.HELP_OUTLINE);
        };
    }
}
