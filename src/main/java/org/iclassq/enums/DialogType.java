package org.iclassq.enums;

import atlantafx.base.theme.Styles;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

public enum DialogType {
    CONFIRMATION(Material2OutlinedAL.HELP_OUTLINE, Styles.ACCENT),
    WARNING(Material2OutlinedMZ.WARNING, Styles.WARNING),
    DANGER(Material2OutlinedAL.ERROR_OUTLINE, Styles.DANGER),
    INFO(Material2OutlinedAL.INFO, Styles.ACCENT);

    private final Ikon icon;
    private final String styleClass;

    DialogType(Ikon icon, String styleClass) {
        this.icon = icon;
        this.styleClass = styleClass;
    }

    public Ikon getIcon() {
        return icon;
    }

    public String getStyleClass() {
        return styleClass;
    }
}
