/*
 * Copyright 2026 Pavel Castornii.
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

import com.techsenger.tabshell.dialogs.style.DialogIcons;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.StyleClasses;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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

    static Label createLabel(String text, String styleClass) {
        Label lbl = new Label(text);
        if (styleClass != null) {
            lbl.getStyleClass().add(styleClass);
        }
        lbl.setMinWidth(Label.USE_PREF_SIZE);
        return lbl;
    }

    static TextField createTextField(String text, String styleClass) {
        TextField tf = new TextField(text);
        if (styleClass != null) {
            tf.getStyleClass().add(styleClass);
        }
        tf.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(tf, Priority.ALWAYS);
        return tf;
    }

    static ComboBox<String> createComboBox(List<String> values, String styleClass) {
        var c = new ComboBox<>(FXCollections.observableArrayList(values));
        c.setMaxWidth(Double.MAX_VALUE);
        c.getSelectionModel().select(0);
        if (styleClass != null) {
            c.getStyleClass().add(styleClass);
        }
        return c;
    }

    static TextArea createTextArea(String text, String styleClass) {
        TextArea bio = new TextArea(text);
        if (styleClass != null) {
            bio.getStyleClass().add(styleClass);
        }
        bio.setWrapText(true);
        bio.setPrefRowCount(5);
        bio.setMaxWidth(Double.MAX_VALUE);
        return bio;
    }


    static ListView<Person> createListView(List<Person> persons, String styleClass) {
        ListView<Person> listView = new ListView<>(FXCollections.observableArrayList(persons));
        if (styleClass != null) {
            listView.getStyleClass().add(styleClass);
        }
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
        return listView;
    }

    static TreeView<Person> createTreeView(List<Person> persons, String styleClass) {
        TreeView<Person> treeView = new TreeView<>(createTreeRoot(persons));
        if (styleClass != null) {
            treeView.getStyleClass().add(styleClass);
        }
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
        return treeView;
    }

    static TableView<Person> createTable(List<Person> persons, String styleClass) {
        TableView<Person> table = new TableView<>();
        if (styleClass != null) {
            table.getStyleClass().add(styleClass);
        }
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
        return table;
    }

    static TreeTableView<Person> createTreeTable(List<Person> persons, String styleClass) {
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
        var treeTableView = new TreeTableView<>(root);
        treeTableView.setShowRoot(false);
        treeTableView.getColumns().addAll(firstNameCol, lastNameCol, genderCol);
        if (styleClass != null) {
            treeTableView.getStyleClass().add(styleClass);
        }
        treeTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        return treeTableView;
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

    private NodeFactory() {
        // empty
    }
}
