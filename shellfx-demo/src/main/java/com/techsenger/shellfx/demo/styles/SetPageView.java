/*
 * Copyright 2024-2026 Pavel Castornii.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.techsenger.shellfx.demo.styles;

import com.techsenger.shellfx.core.page.AbstractPageView;
import static com.techsenger.shellfx.demo.styles.StylePageData.PERSONS;
import com.techsenger.shellfx.material.button.DefaultResultButtonName;
import com.techsenger.shellfx.material.button.ResultButton;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.material.style.StyleClasses;
import java.util.List;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class SetPageView<VM extends SetPageViewModel<?>> extends AbstractPageView<VM> {

    private static VBox createLeftBox(String styleClass) {
        var largeToolBar = NodeFactory.createToolBar(StyleClasses.SIZE_L);
        var mediumToolBar = NodeFactory.createToolBar(StyleClasses.SIZE_M);
        var smallToolBar = NodeFactory.createToolBar(StyleClasses.SIZE_S);
        GridPane grid = new GridPane();
        grid.setHgap(Spacing.getHorizontal());
        grid.setVgap(Spacing.getVertical());
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setHalignment(HPos.LEFT);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        fieldCol.setFillWidth(true);
        grid.getColumnConstraints().addAll(labelCol, fieldCol);

        // First Name
        grid.addRow(grid.getRowCount(),
                NodeFactory.createLabel("First Name:", styleClass),
                NodeFactory.createTextField("Alice"));

        // Last Name
        grid.addRow(grid.getRowCount(),
                NodeFactory.createLabel("Last Name:", styleClass),
                NodeFactory.createTextField("Martin"));

        // Gender — ComboBox (required)
        var genderBox = NodeFactory.createComboBox(List.of("Male", "Female"));
        genderBox.setEditable(true);
        genderBox.getSelectionModel().select(1);
        GridPane.setHgrow(genderBox, Priority.ALWAYS);
        grid.addRow(grid.getRowCount(), NodeFactory.createLabel("Gender:", styleClass), genderBox);

        // Country
        ComboBox<String> countryBox = NodeFactory.createComboBox(List.of(
                "United States", "United Kingdom", "France",
                "Germany", "Japan", "Australia", "Canada",
                "Spain", "Italy", "Netherlands"));
        GridPane.setHgrow(countryBox, Priority.ALWAYS);
        grid.addRow(grid.getRowCount(), NodeFactory.createLabel("Country:", styleClass), countryBox);

        // Biography — TextArea spanning full width
        TextArea bio = NodeFactory.createTextArea("Alice is a passionate software engineer with over 8 years of "
                + "experience in building scalable distributed systems. She holds a Master's degree in "
                + "Computer Science from MIT and has contributed to several open-source projects. "
                + "In her spare time she enjoys hiking, photography, and playing the piano.");
        GridPane.setHgrow(bio, Priority.ALWAYS);
        grid.addRow(grid.getRowCount(), NodeFactory.createLabel("Biography:", styleClass), bio);

        // Dialog buttons: one row split into three equal parts, two buttons of the sizes L, M and S in each
        var dialogButtons = new GridPane();
        dialogButtons.setHgap(Spacing.getHorizontal());
        var sizeClasses = List.of(StyleClasses.SIZE_L, StyleClasses.SIZE_M, StyleClasses.SIZE_S);
        for (int i = 0; i < sizeClasses.size(); i++) {
            var column = new ColumnConstraints();
            column.setPercentWidth(100.0 / sizeClasses.size());
            dialogButtons.getColumnConstraints().add(column);
            var cancelButton = new ResultButton(new DefaultResultButtonName(), "Cancel");
            var okButton = new ResultButton(new DefaultResultButtonName(), "OK");
            cancelButton.getStyleClass().add(sizeClasses.get(i));
            okButton.getStyleClass().add(sizeClasses.get(i));
            var cell = new HBox(Spacing.getHorizontal(), cancelButton, okButton);
            cell.setAlignment(Pos.CENTER_RIGHT);
            dialogButtons.add(cell, i, 0);
        }

        // Root
        var root = new VBox(largeToolBar, mediumToolBar, smallToolBar, grid, dialogButtons,
                NodeFactory.createTabPaneRow(true), NodeFactory.createTabPaneRow(false));
        root.setSpacing(Spacing.getVertical());
        return root;
    }

    private static VBox createRightBox(String styleClass) {
        var listView = NodeFactory.createListView(PERSONS);
        VBox.setVgrow(listView, Priority.ALWAYS);
        var treeView = NodeFactory.createTreeView(PERSONS);
        VBox.setVgrow(treeView, Priority.ALWAYS);
        var table = NodeFactory.createTable(PERSONS);
        VBox.setVgrow(table, Priority.ALWAYS);
        var treeTable = NodeFactory.createTreeTable(PERSONS);
        VBox.setVgrow(treeTable, Priority.ALWAYS);
        var rightBox = new VBox(listView, treeView, table, treeTable);
        rightBox.setSpacing(Spacing.getVertical());
        return rightBox;
    }

    private static GridPane createSetPane(String styleClass) {
        var gridPane = new GridPane();
        if (styleClass != null) {
            gridPane.getStyleClass().add(styleClass);
        }
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        gridPane.getColumnConstraints().addAll(col1, col2);

        var leftBox = createLeftBox(styleClass);
        GridPane.setVgrow(leftBox, Priority.ALWAYS);
        var rightBox = createRightBox(styleClass);
        GridPane.setVgrow(rightBox, Priority.ALWAYS);
        gridPane.addRow(gridPane.getRowCount(), leftBox, rightBox);
        gridPane.setHgap(Spacing.getHorizontal());
        return gridPane;
    }

    private final VBox mainBox = new VBox();

    private final ScrollPane scrollPane = new ScrollPane(mainBox);

    private final String styleName;

    public SetPageView(VM viewModel, String styleName) {
        super(viewModel);
        this.styleName = styleName;
        getNode().getStyleClass().add(styleName);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public Region getNode() {
        return scrollPane;
    }

    @Override
    protected void build() {
        super.build();
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToWidth(true);
        scrollPane.getStylesheets().add(StylesTabView.class.getResource("page.css").toExternalForm());

        mainBox.getChildren().addAll(createSetPane(styleName));
        mainBox.setPadding(new Insets(Spacing.getVertical(), Spacing.getHorizontal(),
                Spacing.getVertical(), Spacing.getHorizontal()));
        mainBox.setSpacing(Spacing.getVertical());
    }
}
