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
import static com.techsenger.shellfx.demo.styles.StylePageData.DEFAULT_STYLE_NAME;
import static com.techsenger.shellfx.demo.styles.StylePageData.PERSONS;
import static com.techsenger.shellfx.demo.styles.StylePageData.TEXT;
import static com.techsenger.shellfx.demo.styles.StylePageData.TEXTS;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.material.style.StyleClasses;
import java.util.List;
import java.util.function.Function;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class VariantPageView<VM extends VariantPageViewModel<?>> extends AbstractPageView<VM> {

    private static final List<String> SIZES = List.of(StyleClasses.SIZE_L, StyleClasses.SIZE_M, StyleClasses.SIZE_S,
            StyleClasses.SIZE_XS);

    private static VBox wrapWithName(Node node, String styleClass) {
        return wrapWithName(node, styleClass, null);
    }

    private static VBox wrapWithName(Node node, String styleClass, String minWidth) {
        var stackPane = new StackPane(node);
        if (styleClass != null && !styleClass.equals(StylePageData.DEFAULT_STYLE_NAME)) {
            stackPane.getStyleClass().add(styleClass);
        }
        VBox.setVgrow(stackPane, Priority.ALWAYS);
        var box = new VBox(stackPane, new Label(styleClass));
        box.setSpacing(Spacing.getVerticalHalf());
        box.setAlignment(Pos.CENTER);
        if (minWidth != null) {
            box.setStyle("-fx-min-width: " + minWidth);
        }
        return box;
    }

    private static VBox wrapWithDensityName(Node node, String densityStyleClass) {
        var box = wrapWithName(node, densityStyleClass);
        box.getChildren().get(1).getStyleClass().add("density-name");
        return box;
    }

    private static GridPane createIconedButtons() {
        return createDensityGrid(VariantPageView::createIconedButtons);
    }

    private static GridPane createTextButtons() {
        return createDensityGrid(VariantPageView::createTextButtons);
    }

    private static GridPane createDensityGrid(Function<String, Node> nodeFactory) {
        var w0 = wrapWithDensityName(nodeFactory.apply(DEFAULT_STYLE_NAME), DEFAULT_STYLE_NAME);
        var w1 = wrapWithDensityName(nodeFactory.apply(StyleClasses.DENSITY_S), StyleClasses.DENSITY_S);
        var w2 = wrapWithDensityName(nodeFactory.apply(StyleClasses.DENSITY_XS), StyleClasses.DENSITY_XS);
        return createDensityGrid(w0, w1, w2);
    }

    /**
     * Puts the variants into a grid of two equal columns: two in the first row, one in the second.
     */
    private static GridPane createDensityGrid(Node first, Node second, Node third) {
        var grid = new GridPane();
        grid.setHgap(Spacing.getHorizontal());
        grid.setVgap(Spacing.getVertical());
        for (int i = 0; i < 2; i++) {
            var column = new ColumnConstraints();
            column.setPercentWidth(50);
            grid.getColumnConstraints().add(column);
            var row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS);
            grid.getRowConstraints().add(row);
        }
        grid.add(first, 0, 0);
        grid.add(second, 1, 0);
        grid.add(third, 0, 1);
        return grid;
    }

    private static HBox createIconedButtons(String densityStyleClass) {
        var box = new HBox();
        for (var size : SIZES) {
            box.getChildren().add(wrapWithName(NodeFactory.createIconButton(size), size, "5em"));
        }
        var crossButton = NodeFactory.createIconButton(StyleClasses.SIZE_XXS);
        crossButton.setGraphic(null);
        crossButton.getStyleClass().add(StyleClasses.CROSS_BUTTON);
        box.getChildren().add(wrapWithName(crossButton, StyleClasses.SIZE_XXS, "5em"));
        box.setMaxWidth(Region.USE_PREF_SIZE);
        box.getStyleClass().add(densityStyleClass);
        return box;
    }

    private static HBox createTextButtons(String densityStyleClass) {
        var box = new HBox();
        for (var size : SIZES) {
            box.getChildren().add(wrapWithName(NodeFactory.createTextButton("JjWw", size), size, "5em"));
        }
        box.setMaxWidth(Region.USE_PREF_SIZE);
        box.getStyleClass().add(densityStyleClass);
        return box;
    }

    private static VBox createToolBars() {
        var w0 = createToolBars(DEFAULT_STYLE_NAME);
        var w1 = createToolBars(StyleClasses.DENSITY_S);
        var w2 = createToolBars(StyleClasses.DENSITY_XS);
        return new VBox(Spacing.getVertical(), w0, w1, w2);
    }

    private static VBox createToolBars(String densityStyleClass) {
        var large = wrapWithName(NodeFactory.createToolBar(StyleClasses.SIZE_L), StyleClasses.SIZE_L);
        var medium = wrapWithName(NodeFactory.createToolBar(StyleClasses.SIZE_M), StyleClasses.SIZE_M);
        var small = wrapWithName(NodeFactory.createToolBar(StyleClasses.SIZE_S), StyleClasses.SIZE_S);
        var box = createSpacedHBox(large, medium, small);
        return wrapWithDensityName(box, densityStyleClass);
    }

    private static HBox createTextFields() {
        var w0 = wrapWithDensityName(NodeFactory.createTextField(TEXT), DEFAULT_STYLE_NAME);
        var w1 = wrapWithDensityName(NodeFactory.createTextField(TEXT), StyleClasses.DENSITY_S);
        var w2 = wrapWithDensityName(NodeFactory.createTextField(TEXT), StyleClasses.DENSITY_XS);
        return createSpacedHBox(w0, w1, w2);
    }

    private static HBox createComboBoxes() {
        var w0 = wrapWithDensityName(NodeFactory.createComboBox(TEXTS), DEFAULT_STYLE_NAME);
        var w1 = wrapWithDensityName(NodeFactory.createComboBox(TEXTS), StyleClasses.DENSITY_S);
        var w2 = wrapWithDensityName(NodeFactory.createComboBox(TEXTS), StyleClasses.DENSITY_XS);
        return createSpacedHBox(w0, w1, w2);
    }

    private static GridPane createTableViewPane() {
        var w0 = wrapWithDensityName(NodeFactory.createTable(PERSONS), DEFAULT_STYLE_NAME);
        var w1 = wrapWithDensityName(NodeFactory.createTable(PERSONS), StyleClasses.DENSITY_S);
        var w2 = wrapWithDensityName(NodeFactory.createTable(PERSONS), StyleClasses.DENSITY_XS);
        var grid = createDensityGrid(w0, w1, w2);
        grid.setMaxHeight(450);
        return grid;
    }

    private static GridPane createTreeTableViewPane() {
        var w0 = wrapWithDensityName(NodeFactory.createTreeTable(PERSONS), DEFAULT_STYLE_NAME);
        var w1 = wrapWithDensityName(NodeFactory.createTreeTable(PERSONS), StyleClasses.DENSITY_S);
        var w2 = wrapWithDensityName(NodeFactory.createTreeTable(PERSONS), StyleClasses.DENSITY_XS);
        var grid = createDensityGrid(w0, w1, w2);
        grid.setMaxHeight(450);
        return grid;
    }

    private static GridPane createListViewPane() {
        var w0 = wrapWithDensityName(NodeFactory.createListView(PERSONS), DEFAULT_STYLE_NAME);
        var w1 = wrapWithDensityName(NodeFactory.createListView(PERSONS), StyleClasses.DENSITY_S);
        var w2 = wrapWithDensityName(NodeFactory.createListView(PERSONS), StyleClasses.DENSITY_XS);
        var grid = createDensityGrid(w0, w1, w2);
        grid.setMaxHeight(450);
        return grid;
    }

    private static GridPane createTreeViewPane() {
        var w0 = wrapWithDensityName(NodeFactory.createTreeView(PERSONS), DEFAULT_STYLE_NAME);
        var w1 = wrapWithDensityName(NodeFactory.createTreeView(PERSONS), StyleClasses.DENSITY_S);
        var w2 = wrapWithDensityName(NodeFactory.createTreeView(PERSONS), StyleClasses.DENSITY_XS);
        var grid = createDensityGrid(w0, w1, w2);
        grid.setMaxHeight(450);
        return grid;
    }

    private static HBox createSpacedHBox(Node... nodes) {
        var hbox = new HBox(nodes);
        hbox.setSpacing(Spacing.getHorizontal());
        for (var node : nodes) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }
        return hbox;
    }

    private final VBox mainBox = new VBox();

    private final ScrollPane scrollPane = new ScrollPane(mainBox);

    public VariantPageView(VM viewModel) {
        super(viewModel);
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

        mainBox.setPadding(new Insets(Spacing.getVertical(), Spacing.getHorizontal(),
                Spacing.getVertical(), Spacing.getHorizontal()));
        mainBox.setSpacing(Spacing.getVertical());

        addToBox(NodeFactory.createSection("Icon Button"), createIconedButtons());
        addToBox(NodeFactory.createSection("Text Button"), createTextButtons());
        addToBox(NodeFactory.createSection("ToolBar"), createToolBars());
        addToBox(NodeFactory.createSection("TextField"), createTextFields());
        addToBox(NodeFactory.createSection("ComboBox"), createComboBoxes());
        addToBox(NodeFactory.createSection("ListView"), createListViewPane());
        addToBox(NodeFactory.createSection("TreeView"), createTreeViewPane());
        addToBox(NodeFactory.createSection("TableView"), createTableViewPane());
        addToBox(NodeFactory.createSection("TreeTableView"), createTreeTableViewPane());
    }

    private void addToBox(Node... nodes) {
        mainBox.getChildren().addAll(nodes);
    }
}
