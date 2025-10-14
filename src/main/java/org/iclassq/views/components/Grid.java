package org.iclassq.views.components;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class Grid extends GridPane {

    public Grid(int preferredColumns, Node... nodes) {
        this.setHgap(20);
        this.setVgap(15);
        this.setAlignment(Pos.CENTER);
        this.setMaxWidth(Double.MAX_VALUE);

        for (int i = 0; i < preferredColumns; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setPercentWidth(100.0 / preferredColumns);
            col.setMinWidth(200);
            this.getColumnConstraints().add(col);
        }

        for (int i = 0; i < nodes.length; i++) {
            this.add(nodes[i], i % preferredColumns, i / preferredColumns);
        }
    }

    public Grid(int preferredColumns, double hgap, double vgap, double minWidth, Node... nodes) {
        this.setHgap(hgap);
        this.setVgap(vgap);
        this.setAlignment(Pos.CENTER);
        this.setMaxWidth(Double.MAX_VALUE);

        for (int i = 0; i < preferredColumns; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setPercentWidth(100.0 / preferredColumns);
            col.setMinWidth(minWidth);
            this.getColumnConstraints().add(col);
        }

        for (int i = 0; i < nodes.length; i++) {
            this.add(nodes[i], i % preferredColumns, i / preferredColumns);
        }
    }
}
