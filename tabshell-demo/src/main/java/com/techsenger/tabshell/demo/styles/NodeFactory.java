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

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.devtools.style.DevToolsIcons;
import com.techsenger.tabshell.dialogs.style.DialogIcons;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.icon.StyleFontIcon;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.style.SharedIcons;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 *
 * @author Pavel Castornii
 */
final class NodeFactory {

    private static final double DATA_NODE_MAX_HEIGHT = 300;

    static Label createSection(String title) {
        var label = NodeFactory.createLabel(title, null);
        label.getStyleClass().add("section");
        return label;
    }

    static Button createIconButton(String styleClass) {
        var b = new Button(null, new FontIconView(DialogIcons.DIRECTORY));
        b.getStyleClass().add(StyleClasses.ICON_BUTTON);
        if (styleClass != null) {
            b.getStyleClass().add(styleClass);
        }
        return b;
    }

    static ToolBar createToolBar(String toolBarStyleClass, String buttonStyleClass) {
        var toolbar = new ToolBar();
        if (toolBarStyleClass != null) {
            toolbar.getStyleClass().add(toolBarStyleClass);
        }
        toolbar.getItems().addAll(
                createToolBarButton(SharedIcons.HIGHLIGHT, buttonStyleClass),
                createToolBarButton(SharedIcons.MATCH_CASE, buttonStyleClass),
                new Separator(Orientation.VERTICAL),
                createToolBarButton(SharedIcons.WHOLE_WORD, buttonStyleClass),
                new Separator(Orientation.VERTICAL),
                createToolBarButton(SharedIcons.REG_EXP, buttonStyleClass),
                createToolBarButton(DevToolsIcons.CLEAR, buttonStyleClass),
                createToolBarButton(DevToolsIcons.EDIT, buttonStyleClass),
                createToolBarButton(DevToolsIcons.REFRESH, buttonStyleClass),
                createToolBarButton(DevToolsIcons.OPEN_IN_NEW, buttonStyleClass),
                createToolBarButton(DevToolsIcons.TOOLS, buttonStyleClass),
                createToolBarButton(DevToolsIcons.VIEW, buttonStyleClass));
        return toolbar;
    }

    static Label createLabel(String text, String styleClass) {
        Label lbl = new Label(text);
        if (styleClass != null) {
            lbl.getStyleClass().add(styleClass);
        }
        lbl.setMinWidth(Label.USE_PREF_SIZE);
        return lbl;
    }

    static TextField createTextField(String text) {
        TextField tf = new TextField(text);
        tf.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(tf, Priority.ALWAYS);
        return tf;
    }

    static ComboBox<String> createComboBox(List<String> values) {
        var c = new ComboBox<>(FXCollections.observableArrayList(values));
        c.setMaxWidth(Double.MAX_VALUE);
        c.getSelectionModel().select(0);
        return c;
    }

    static TextArea createTextArea(String text) {
        TextArea bio = new TextArea(text);
        bio.setWrapText(true);
        bio.setPrefRowCount(5);
        bio.setMaxWidth(Double.MAX_VALUE);
        return bio;
    }

    static ListView<Person> createListView(List<Person> persons) {
        ListView<Person> listView = new ListView<>(FXCollections.observableArrayList(persons));
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Person person, boolean empty) {
                super.updateItem(person, empty);
                if (empty || person == null) {
                    setText(null);
                } else {
                    setText(person.getFirstName() + " " + person.getLastName()
                        + " — " + person.getGender() + ", " + person.getCountry());
                }
            }
        });
        listView.setMaxHeight(DATA_NODE_MAX_HEIGHT);
        listView.getSelectionModel().select(0);
        return listView;
    }

    static TreeView<Person> createTreeView(List<Person> persons) {
        TreeView<Person> treeView = new TreeView<>(createTreeRoot(persons));
        treeView.setShowRoot(false);
        treeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(Person person, boolean empty) {
                super.updateItem(person, empty);
                if (empty || person == null) {
                    setText(null);
                } else if (person.getLastName().isEmpty()) {
                    // group header
                    setText(person.getFirstName());
                } else {
                    setText(person.getFirstName() + " " + person.getLastName()
                        + " — " + person.getGender() + ", " + person.getCountry());
                }
            }
        });
        treeView.setMaxHeight(DATA_NODE_MAX_HEIGHT);
        treeView.getSelectionModel().select(0);
        return treeView;
    }

    static TableView<Person> createTable(List<Person> persons) {
        TableView<Person> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Person, String> colFirst = new TableColumn<>("First Name");
        colFirst.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFirstName()));

        TableColumn<Person, String> colLast = new TableColumn<>("Last Name");
        colLast.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLastName()));

        TableColumn<Person, String> colGender = new TableColumn<>("Gender");
        colGender.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGender()));

        table.getColumns().addAll(colFirst, colLast, colGender);
        table.setItems(FXCollections.observableArrayList(persons));

        // Select first row so the form looks populated
        table.getSelectionModel().selectFirst();
        table.setMaxHeight(DATA_NODE_MAX_HEIGHT);
        table.getSelectionModel().select(0);
        return table;
    }

    static TreeTableView<Person> createTreeTable(List<Person> persons) {
        var firstNameCol = new TreeTableColumn<Person, String>("First Name");
        firstNameCol.setCellValueFactory(p ->
            new SimpleStringProperty(p.getValue().getValue().getFirstName()));

        var lastNameCol = new TreeTableColumn<Person, String>("Last Name");
        lastNameCol.setCellValueFactory(p ->
            new SimpleStringProperty(p.getValue().getValue().getLastName()));

        var genderCol = new TreeTableColumn<Person, String>("Gender");
        genderCol.setCellValueFactory(p ->
            new SimpleStringProperty(p.getValue().getValue().getGender()));

        var root = createTreeRoot(persons);
        var treeTable = new TreeTableView<>(root);
        treeTable.setShowRoot(false);
        treeTable.getColumns().addAll(firstNameCol, lastNameCol, genderCol);

        treeTable.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        treeTable.setMaxHeight(DATA_NODE_MAX_HEIGHT);
        treeTable.getSelectionModel().select(0);
        return treeTable;
    }

    private static TreeItem<Person> createTreeRoot(List<Person> persons) {
        var root = new TreeItem<Person>();

        var titles = List.of("Software Developer", "Designer", "Manager", "Analyst");
        int[] splits = {3, 3, 3, 1};
        int index = 0;
        for (int i = 0; i < titles.size(); i++) {
            var group = new TreeItem<>(new Person(titles.get(i), "", "", ""));
            for (int j = 0; j < splits[i]; j++) {
                group.getChildren().add(new TreeItem<>(persons.get(index++)));
            }
            group.setExpanded(true);
            root.getChildren().add(group);
        }
        return root;
    }

    private static Button createToolBarButton(StyleFontIcon icon, String styleClass) {
        var button = new Button(null, new FontIconView(icon));
        button.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT);
        if (styleClass != null) {
            button.getStyleClass().add(styleClass);
        }
        return button;
    }

    private NodeFactory() {
        // empty
    }
}
