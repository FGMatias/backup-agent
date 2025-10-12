package org.iclassq.enums;

public enum TabType {
    DASHBOARD("Dashboard"),
    TASKS("Tareas"),
    HISTORY("Historial");

    private final String displayName;

    TabType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
