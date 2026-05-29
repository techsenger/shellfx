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
        VBox.setVgrow(stackPane, Priority.ALWAYS);
        var box = new VBox(stackPane, new Label(styleClass));
        box.setSpacing(Spacing.VERTICAL_HALF);
        box.setAlignment(Pos.CENTER);
        if (minWidth != null) {
            box.setStyle("-fx-min-width: " + minWidth);
        }
        return box;
    }

    private static HBox createIconedButtons() {
        var w0 = wrapWithName(NodeFactory.createIconButton(StyleClasses.HUGE), StyleClasses.HUGE, "5em");
        var w1 = wrapWithName(NodeFactory.createIconButton(StyleClasses.LARGE), StyleClasses.LARGE, "5em");
        var w2 = wrapWithName(NodeFactory.createIconButton(null), DEFAULT_STYLE_NAME, "5em");
        var w3 = wrapWithName(NodeFactory.createIconButton(StyleClasses.DENSE), StyleClasses.DENSE, "5em");
        var w4 = wrapWithName(NodeFactory.createIconButton(StyleClasses.COMPRESSED), StyleClasses.COMPRESSED, "5em");
        return new HBox(w0, w1, w2, w3, w4);
    }

    private static HBox createTextFields() {
        var w0 = wrapWithName(NodeFactory.createTextField(TEXT, null), DEFAULT_STYLE_NAME);
        var w1 = wrapWithName(NodeFactory.createTextField(TEXT, StyleClasses.DENSE), StyleClasses.DENSE);
        var w2 = wrapWithName(NodeFactory.createTextField(TEXT, StyleClasses.COMPACT), StyleClasses.COMPACT);
        var w3 = wrapWithName(NodeFactory.createTextField(TEXT, StyleClasses.COMPRESSED), StyleClasses.COMPRESSED);
        return createSpacedHBox(w0, w1, w2, w3);
    }

    private static HBox createComboBoxes() {
        var w0 = wrapWithName(NodeFactory.createComboBox(TEXTS, null), DEFAULT_STYLE_NAME);
        var w1 = wrapWithName(NodeFactory.createComboBox(TEXTS, StyleClasses.DENSE), StyleClasses.DENSE);
        var w2 = wrapWithName(NodeFactory.createComboBox(TEXTS, StyleClasses.COMPACT), StyleClasses.COMPACT);
        var w3 = wrapWithName(NodeFactory.createComboBox(TEXTS, StyleClasses.COMPRESSED), StyleClasses.COMPRESSED);
        return createSpacedHBox(w0, w1, w2, w3);
    }

    private static VBox createTableViewBox() {
        var w0 = wrapWithName(NodeFactory.createTable(PERSONS, null), DEFAULT_STYLE_NAME);
        var w1 = wrapWithName(NodeFactory.createTable(PERSONS, StyleClasses.DENSE), StyleClasses.DENSE);
        var w2 = wrapWithName(NodeFactory.createTable(PERSONS, StyleClasses.COMPACT), StyleClasses.COMPACT);
        var w3 = wrapWithName(NodeFactory.createTable(PERSONS, StyleClasses.COMPRESSED), StyleClasses.COMPRESSED);

        var h0 = createSpacedHBox(w0, w1);
        VBox.setVgrow(h0, Priority.ALWAYS);
        var h1 = createSpacedHBox(w2, w3);
        VBox.setVgrow(h1, Priority.ALWAYS);
        var box = new VBox(Spacing.VERTICAL, h0, h1);
        box.setMaxHeight(450);
        return box;
    }

    private static VBox createTreeTableViewBox() {
        var w0 = wrapWithName(NodeFactory.createTreeTable(PERSONS, null), DEFAULT_STYLE_NAME);
        var w1 = wrapWithName(NodeFactory.createTreeTable(PERSONS, StyleClasses.DENSE), StyleClasses.DENSE);
        var w2 = wrapWithName(NodeFactory.createTreeTable(PERSONS, StyleClasses.COMPACT), StyleClasses.COMPACT);
        var w3 = wrapWithName(NodeFactory.createTreeTable(PERSONS, StyleClasses.COMPRESSED), StyleClasses.COMPRESSED);

        var h0 = createSpacedHBox(w0, w1);
        VBox.setVgrow(h0, Priority.ALWAYS);
        var h1 = createSpacedHBox(w2, w3);
        VBox.setVgrow(h1, Priority.ALWAYS);
        var box = new VBox(Spacing.VERTICAL, h0, h1);
        box.setMaxHeight(450);
        return box;
    }

    private static VBox createListViewBox() {
        var w0 = wrapWithName(NodeFactory.createListView(PERSONS, null), DEFAULT_STYLE_NAME);
        var w1 = wrapWithName(NodeFactory.createListView(PERSONS, StyleClasses.DENSE), StyleClasses.DENSE);
        var w2 = wrapWithName(NodeFactory.createListView(PERSONS, StyleClasses.COMPACT), StyleClasses.COMPACT);
        var w3 = wrapWithName(NodeFactory.createListView(PERSONS, StyleClasses.COMPRESSED), StyleClasses.COMPRESSED);

        var h0 = createSpacedHBox(w0, w1);
        VBox.setVgrow(h0, Priority.ALWAYS);
        var h1 = createSpacedHBox(w2, w3);
        VBox.setVgrow(h1, Priority.ALWAYS);
        var box = new VBox(Spacing.VERTICAL, h0, h1);
        box.setMaxHeight(450);
        return box;
    }

    private static VBox createTreeViewBox() {
        var w0 = wrapWithName(NodeFactory.createTreeView(PERSONS, null), DEFAULT_STYLE_NAME);
        var w1 = wrapWithName(NodeFactory.createTreeView(PERSONS, StyleClasses.DENSE), StyleClasses.DENSE);
        var w2 = wrapWithName(NodeFactory.createTreeView(PERSONS, StyleClasses.COMPACT), StyleClasses.COMPACT);
        var w3 = wrapWithName(NodeFactory.createTreeView(PERSONS, StyleClasses.COMPRESSED), StyleClasses.COMPRESSED);

        var h0 = createSpacedHBox(w0, w1);
        VBox.setVgrow(h0, Priority.ALWAYS);
        var h1 = createSpacedHBox(w2, w3);
        VBox.setVgrow(h1, Priority.ALWAYS);
        var box = new VBox(Spacing.VERTICAL, h0, h1);
        box.setMaxHeight(450);
        return box;
    }

    private static HBox createSpacedHBox(Node... nodes) {
        var hbox = new HBox(nodes);
        hbox.setSpacing(Spacing.HORIZONTAL);
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

        mainBox.setPadding(new Insets(Spacing.VERTICAL, Spacing.HORIZONTAL, Spacing.VERTICAL, Spacing.HORIZONTAL));
        mainBox.setSpacing(Spacing.VERTICAL);

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
