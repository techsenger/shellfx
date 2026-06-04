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
import static com.techsenger.tabshell.demo.styles.StylePageData.DEFAULT_STYLE_NAME;
import static com.techsenger.tabshell.demo.styles.StylePageData.PERSONS;
import static com.techsenger.tabshell.demo.styles.StylePageData.TEXT;
import static com.techsenger.tabshell.demo.styles.StylePageData.TEXTS;
import com.techsenger.tabshell.material.style.Spacing;
import com.techsenger.tabshell.material.style.StyleClasses;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class VariantPageFxView extends AbstractPageFxView<VariantPagePresenter> implements VariantPageView {

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

    private static VBox createIconedButtons() {
        var w0 = wrapWithName(createIconedButtons(DEFAULT_STYLE_NAME), DEFAULT_STYLE_NAME);
        var w1 = wrapWithName(createIconedButtons(StyleClasses.DENSITY_M), StyleClasses.DENSITY_M);
        var w2 = wrapWithName(createIconedButtons(StyleClasses.DENSITY_S), StyleClasses.DENSITY_S);
        var w3 = wrapWithName(createIconedButtons(StyleClasses.DENSITY_XS), StyleClasses.DENSITY_XS);
        var vbox = new VBox(Spacing.getVertical());
        var h0 = createSpacedHBox(w0, w1);
        VBox.setVgrow(h0, Priority.ALWAYS);
        var h1 = createSpacedHBox(w2, w3);
        VBox.setVgrow(h1, Priority.ALWAYS);
        vbox.getChildren().addAll(h0, h1);
        return vbox;
    }

    private static HBox createIconedButtons(String densityStyleClass) {
        var w0 = wrapWithName(NodeFactory.createIconButton(StyleClasses.SIZE_XL), StyleClasses.SIZE_XL, "5em");
        var w1 = wrapWithName(NodeFactory.createIconButton(StyleClasses.SIZE_L), StyleClasses.SIZE_L, "5em");
        var w2 = wrapWithName(NodeFactory.createIconButton(StyleClasses.SIZE_M), StyleClasses.SIZE_M, "5em");
        var w3 = wrapWithName(NodeFactory.createIconButton(StyleClasses.SIZE_S), StyleClasses.SIZE_S, "5em");
        var w4 = wrapWithName(NodeFactory.createIconButton(StyleClasses.SIZE_XS), StyleClasses.SIZE_XS, "5em");
        var box = new HBox(w0, w1, w2, w3, w4);
        box.setMaxWidth(Region.USE_PREF_SIZE);
        box.getStyleClass().add(densityStyleClass);
        return box;
    }

    private static HBox createTextFields() {
        var w0 = wrapWithName(NodeFactory.createTextField(TEXT), DEFAULT_STYLE_NAME);
        var w1 = wrapWithName(NodeFactory.createTextField(TEXT), StyleClasses.DENSITY_M);
        var w2 = wrapWithName(NodeFactory.createTextField(TEXT), StyleClasses.DENSITY_S);
        var w3 = wrapWithName(NodeFactory.createTextField(TEXT), StyleClasses.DENSITY_XS);
        return createSpacedHBox(w0, w1, w2, w3);
    }

    private static HBox createComboBoxes() {
        var w0 = wrapWithName(NodeFactory.createComboBox(TEXTS), DEFAULT_STYLE_NAME);
        var w1 = wrapWithName(NodeFactory.createComboBox(TEXTS), StyleClasses.DENSITY_M);
        var w2 = wrapWithName(NodeFactory.createComboBox(TEXTS), StyleClasses.DENSITY_S);
        var w3 = wrapWithName(NodeFactory.createComboBox(TEXTS), StyleClasses.DENSITY_XS);
        return createSpacedHBox(w0, w1, w2, w3);
    }

    private static VBox createTableViewBox() {
        var w0 = wrapWithName(NodeFactory.createTable(PERSONS), DEFAULT_STYLE_NAME);
        var w1 = wrapWithName(NodeFactory.createTable(PERSONS), StyleClasses.DENSITY_M);
        var w2 = wrapWithName(NodeFactory.createTable(PERSONS), StyleClasses.DENSITY_S);
        var w3 = wrapWithName(NodeFactory.createTable(PERSONS), StyleClasses.DENSITY_XS);

        var h0 = createSpacedHBox(w0, w1);
        VBox.setVgrow(h0, Priority.ALWAYS);
        var h1 = createSpacedHBox(w2, w3);
        VBox.setVgrow(h1, Priority.ALWAYS);
        var box = new VBox(Spacing.getVertical(), h0, h1);
        box.setMaxHeight(450);
        return box;
    }

    private static VBox createTreeTableViewBox() {
        var w0 = wrapWithName(NodeFactory.createTreeTable(PERSONS), DEFAULT_STYLE_NAME);
        var w1 = wrapWithName(NodeFactory.createTreeTable(PERSONS), StyleClasses.DENSITY_M);
        var w2 = wrapWithName(NodeFactory.createTreeTable(PERSONS), StyleClasses.DENSITY_S);
        var w3 = wrapWithName(NodeFactory.createTreeTable(PERSONS), StyleClasses.DENSITY_XS);

        var h0 = createSpacedHBox(w0, w1);
        VBox.setVgrow(h0, Priority.ALWAYS);
        var h1 = createSpacedHBox(w2, w3);
        VBox.setVgrow(h1, Priority.ALWAYS);
        var box = new VBox(Spacing.getVertical(), h0, h1);
        box.setMaxHeight(450);
        return box;
    }

    private static VBox createListViewBox() {
        var w0 = wrapWithName(NodeFactory.createListView(PERSONS), DEFAULT_STYLE_NAME);
        var w1 = wrapWithName(NodeFactory.createListView(PERSONS), StyleClasses.DENSITY_M);
        var w2 = wrapWithName(NodeFactory.createListView(PERSONS), StyleClasses.DENSITY_S);
        var w3 = wrapWithName(NodeFactory.createListView(PERSONS), StyleClasses.DENSITY_XS);

        var h0 = createSpacedHBox(w0, w1);
        VBox.setVgrow(h0, Priority.ALWAYS);
        var h1 = createSpacedHBox(w2, w3);
        VBox.setVgrow(h1, Priority.ALWAYS);
        var box = new VBox(Spacing.getVertical(), h0, h1);
        box.setMaxHeight(450);
        return box;
    }

    private static VBox createTreeViewBox() {
        var w0 = wrapWithName(NodeFactory.createTreeView(PERSONS), DEFAULT_STYLE_NAME);
        var w1 = wrapWithName(NodeFactory.createTreeView(PERSONS), StyleClasses.DENSITY_M);
        var w2 = wrapWithName(NodeFactory.createTreeView(PERSONS), StyleClasses.DENSITY_S);
        var w3 = wrapWithName(NodeFactory.createTreeView(PERSONS), StyleClasses.DENSITY_XS);

        var h0 = createSpacedHBox(w0, w1);
        VBox.setVgrow(h0, Priority.ALWAYS);
        var h1 = createSpacedHBox(w2, w3);
        VBox.setVgrow(h1, Priority.ALWAYS);
        var box = new VBox(Spacing.getVertical(), h0, h1);
        box.setMaxHeight(450);
        return box;
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

        mainBox.setPadding(new Insets(Spacing.getVertical(), Spacing.getHorizontal(),
                Spacing.getVertical(), Spacing.getHorizontal()));
        mainBox.setSpacing(Spacing.getVertical());

        addToBox(NodeFactory.createSection("Icon Buttons"), createIconedButtons());
        addToBox(NodeFactory.createSection("Text Fields"), createTextFields());
        addToBox(NodeFactory.createSection("ComboBoxes"), createComboBoxes());
        addToBox(NodeFactory.createSection("ListView"), createListViewBox());
        addToBox(NodeFactory.createSection("TreeView"), createTreeViewBox());
        addToBox(NodeFactory.createSection("TableView"), createTableViewBox());
        addToBox(NodeFactory.createSection("TreeTableView"), createTreeTableViewBox());
    }

    private void addToBox(Node... nodes) {
        mainBox.getChildren().addAll(nodes);
    }
}
