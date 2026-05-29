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

package com.techsenger.tabshell.demo.styles;

import com.techsenger.tabshell.core.page.AbstractPageFxView;
import static com.techsenger.tabshell.demo.styles.StylePageData.PERSONS;
import com.techsenger.tabshell.material.style.Spacing;
import java.util.List;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
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
public class SetPageFxView extends AbstractPageFxView<SetPagePresenter> implements SetPageView {

    private static GridPane createSetPane(String styleClass) {
        var gridPane = new GridPane();
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        gridPane.getColumnConstraints().addAll(col1, col2);

        var leftBox = createGridPaneLeftBox(styleClass);
        GridPane.setVgrow(leftBox, Priority.ALWAYS);
        var rightBox = createGridPaneRightBox(styleClass);
        GridPane.setVgrow(rightBox, Priority.ALWAYS);
        gridPane.addRow(gridPane.getRowCount(), leftBox, rightBox);
        gridPane.setHgap(Spacing.HORIZONTAL);
        return gridPane;
    }

    private static VBox createGridPaneLeftBox(String styleClass) {
        GridPane grid = new GridPane();
        grid.setHgap(Spacing.HORIZONTAL);
        grid.setVgap(Spacing.VERTICAL);
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setHalignment(HPos.LEFT);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        fieldCol.setFillWidth(true);
        grid.getColumnConstraints().addAll(labelCol, fieldCol);

        // First Name
        grid.addRow(grid.getRowCount(),
                NodeFactory.createLabel("First Name:", styleClass),
                NodeFactory.createTextField("Alice", styleClass));

        // Last Name
        grid.addRow(grid.getRowCount(),
                NodeFactory.createLabel("Last Name:", styleClass),
                NodeFactory.createTextField("Martin", styleClass));

        // Gender — ComboBox (required)
        var genderBox = NodeFactory.createComboBox(List.of("Male", "Female"), styleClass);
        genderBox.setEditable(true);
        genderBox.getSelectionModel().select(1);
        GridPane.setHgrow(genderBox, Priority.ALWAYS);
        grid.addRow(grid.getRowCount(), NodeFactory.createLabel("Gender:", styleClass), genderBox);

        // Country
        ComboBox<String> countryBox = NodeFactory.createComboBox(List.of(
                "United States", "United Kingdom", "France",
                "Germany", "Japan", "Australia", "Canada",
                "Spain", "Italy", "Netherlands"), styleClass);
        GridPane.setHgrow(countryBox, Priority.ALWAYS);
        grid.addRow(grid.getRowCount(), NodeFactory.createLabel("Country:", styleClass), countryBox);

        // Biography — TextArea spanning full width
        TextArea bio = NodeFactory.createTextArea("Alice is a passionate software engineer with over 8 years of "
                + "experience in building scalable distributed systems. She holds a Master's degree in "
                + "Computer Science from MIT and has contributed to several open-source projects. "
                + "In her spare time she enjoys hiking, photography, and playing the piano.", styleClass);
        GridPane.setHgrow(bio, Priority.ALWAYS);
        grid.addRow(grid.getRowCount(), NodeFactory.createLabel("Biography:", styleClass), bio);

        var listView = NodeFactory.createListView(PERSONS, styleClass);
        VBox.setVgrow(listView, Priority.ALWAYS);
        var treeView = NodeFactory.createTreeView(PERSONS, styleClass);
        VBox.setVgrow(treeView, Priority.ALWAYS);

        // Buttons
        Button btnCancel = new Button("Cancel");
        Button btnApply  = new Button("Apply");
        Button btnOk     = new Button("OK");

        btnOk.setDefaultButton(true);
        btnCancel.setCancelButton(true);

        HBox buttonBar = new HBox(8, btnCancel, btnApply, btnOk);
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonBar.getChildren().add(0, spacer);

        // Root
        var root = new VBox(grid, listView, treeView, buttonBar);
        VBox.setVgrow(grid, Priority.ALWAYS);
        root.setSpacing(Spacing.VERTICAL);
        return root;
    }

    private static VBox createGridPaneRightBox(String styleClass) {
        var table = NodeFactory.createTable(PERSONS, styleClass);
        VBox.setVgrow(table, Priority.ALWAYS);
        var treeTable = NodeFactory.createTreeTable(PERSONS, styleClass);
        VBox.setVgrow(treeTable, Priority.ALWAYS);
        var rightBox = new VBox(table, treeTable);
        rightBox.setSpacing(Spacing.VERTICAL);
        return rightBox;
    }

    private final VBox mainBox = new VBox();

    private final ScrollPane scrollPane = new ScrollPane(mainBox);

    private final String styleName;

    public SetPageFxView(String styleName) {
        this.styleName = styleName;
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
        scrollPane.getStylesheets().add(StylesTabFxView.class.getResource("page.css").toExternalForm());

        mainBox.getChildren().addAll(createSetPane(styleName));
        mainBox.setPadding(new Insets(Spacing.VERTICAL, Spacing.HORIZONTAL, Spacing.VERTICAL, Spacing.HORIZONTAL));
        mainBox.setSpacing(Spacing.VERTICAL);
    }
}
