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

package com.techsenger.tabshell.dialogs.file;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.dialog.DialogResizeEvent;
import com.techsenger.tabshell.dialogs.simple.AbstractSimpleDialogView;
import com.techsenger.tabshell.dialogs.style.DialogIcons;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.list.TextFieldColumnListCell;
import com.techsenger.tabshell.material.style.SizeConstants;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.material.table.TableHistoryUtils;
import com.techsenger.tabshell.shared.style.SharedIcons;
import com.techsenger.tabshell.storage.GenericFile;
import com.techsenger.toolkit.fx.utils.ButtonUtils;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * There two modes - details and list. As both modes require sorting we use table and its sorting in both modes. But
 * in list mode the table is not added to scene and its cell factories are not called, so, only its sorting is
 * used.
 *
 * <p><b>Folder creation process</b>:
 *
 * <p>When user initiates new folder creation:
 * <ul>
 *   <li>A temporary "New Folder" entry appears at the top of the file list</li>
 *   <li>Name column becomes editable.
 *   <li>The system immediately enters inline editing mode</li>
 * </ul>
 *
 * If user confirms with Enter key:
 * <ul>
 *   <li>Name column becomes non editable.
 *   <li>The physical folder is created on disk</li>
 *   <li>The file list is refreshed and resorted</li>
 *   <li>The view automatically scrolls to show the new folder</li>
 *   <li>The new folder receives selection highlight</li>
 * </ul>
 *
 * If user cancels with Esc key:
 * <ul>
 *   <li>Name column becomes non editable.
 *   <li>The temporary entry is removed</li>
 *   <li>No disk operations are performed</li>
 * </ul>
 *
 * @author Pavel Castornii
 */
public class FileChooserDialogView<T extends FileChooserDialogViewModel<?>, S extends FileChooserDialogComponent<?>>
        extends AbstractSimpleDialogView<T, S> {

    private static class LocationCell extends ListCell<Location> {

        private final Label label = new Label();

        private final HBox box = new HBox();

        private final boolean valueCell;

        LocationCell(boolean valueCell) {
            this.valueCell = valueCell;
            this.box.setAlignment(Pos.CENTER_LEFT);
        }

        @Override
        protected void updateItem(Location item, boolean empty) {
            //many updates happening, resulting in visible flickering of the value cell's content
            if (valueCell && item == getItem()) {
                return;
            }
            super.updateItem(item, empty);
            setText(null);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                if (!this.valueCell) {
                    this.box.setPadding(new Insets(0, 0, 0, item.getLevel() * SizeConstants.INSET));
                }
                this.label.setText(item.getName());
                this.box.getChildren().clear();
                this.box.getChildren().addAll(new FontIconView(item.getIcon()), this.label);
                setGraphic(this.box);
            }
        }
    }

    private final Label locationLabel = new Label();

    private final ComboBox<Location> locationComboBox = new ComboBox<>();

    private final Button levelUpButton = new Button(null, new FontIconView(DialogIcons.DIRECTORY_UP));

    private final Button homeButton = new Button(null, new FontIconView(DialogIcons.HOME));

    private final Button createButton = new Button(null, new FontIconView(DialogIcons.ADD_DIRECTORY));

    private final ToggleButton listButton = new ToggleButton(null, new FontIconView(DialogIcons.GRID_VIEW));

    private final ToggleButton detailsButton = new ToggleButton(null, new FontIconView(DialogIcons.LIST_VIEW));

    private final HBox buttonBox = new HBox(levelUpButton, homeButton, createButton, listButton, detailsButton);

    private final HBox locationBox = new HBox(locationLabel, locationComboBox, buttonBox);

    private final Label nameLabel = new Label("File Name");

    private final TextField nameTextField = new TextField();

    private final Label filterLabel = new Label("Files of Type");

    private final ComboBox<ExtensionFilter> filterComboBox = new ComboBox<>();

    private final GridPane gridPane = new GridPane();

    private final VBox fileBox = new VBox();

    private FileTableView fileTableView;

    private TableColumn<GenericFile, ?> nameColumn;

    private FileListView fileListView;

    private final VBox main = new VBox(locationBox, fileBox, gridPane);

    private final ContextMenu containerContextMenu = new ContextMenu(createRefreshMenuItem());

    private final ContextMenu itemContextMenu = new ContextMenu(createRenameMenuItem(), createRefreshMenuItem());

    public FileChooserDialogView(T viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    protected void initialize() {
        super.initialize();
        getViewModel().updateFiles(null);
    }

    @Override
    protected void deinitialize() {
        getViewModel().setTableHistory(TableHistoryUtils.createHistory(fileTableView));
        super.deinitialize();
    }

    @Override
    protected void build() {
        super.build();
        var viewModel = getViewModel();
        this.fileTableView = new FileTableView(viewModel, viewModel.getSettings());
        this.nameColumn = this.fileTableView.findNameColumn();
        this.fileListView = new FileListView(viewModel, new ContextMenu(createRefreshMenuItem()));

        locationLabel.setMinWidth(Region.USE_PREF_SIZE);
        HBox.setHgrow(locationComboBox, Priority.ALWAYS);
        locationComboBox.getStyleClass().add(Styles.DENSE);
        locationComboBox.setMaxWidth(Double.MAX_VALUE);
        locationComboBox.setItems(viewModel.getLocations());
        locationComboBox.setCellFactory(cb -> {
            var cell = new LocationCell(false);
            cell.setOnMousePressed(e -> {
                viewModel.navigateTo(cell.getItem().getStorage(), cell.getItem().getUri());
            });
            return cell;
        });
        locationComboBox.setButtonCell(new LocationCell(true));
        locationComboBox.getStyleClass().add("location");
        levelUpButton.getStyleClass().addAll(StyleClasses.ICONED_BUTTON, Styles.FLAT);
        levelUpButton.setTooltip(new Tooltip("Up One Level"));
        homeButton.getStyleClass().addAll(StyleClasses.ICONED_BUTTON, Styles.FLAT);
        homeButton.setTooltip(new Tooltip("Home"));
        createButton.getStyleClass().addAll(StyleClasses.ICONED_BUTTON, Styles.FLAT);
        createButton.setTooltip(new Tooltip("Create New Folder"));
        ToggleGroup toggleGroup = new ToggleGroup();
        //always one button selected
        toggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                oldToggle.setSelected(true);
            }
        });
        detailsButton.getStyleClass().addAll(StyleClasses.ICONED_BUTTON, Styles.FLAT);
        detailsButton.setTooltip(new Tooltip("Details"));
        detailsButton.setToggleGroup(toggleGroup);
        listButton.getStyleClass().addAll(StyleClasses.ICONED_BUTTON, Styles.FLAT);
        listButton.setTooltip(new Tooltip("List"));
        listButton.setToggleGroup(toggleGroup);
        buttonBox.setSpacing(SizeConstants.THIRD_INSET);
        locationBox.setSpacing(SizeConstants.INSET);
        locationBox.setAlignment(Pos.CENTER_LEFT);

        VBox.setVgrow(fileBox, Priority.ALWAYS);

        this.fileTableView.setRowFactory(tv -> {
            TableRow<GenericFile> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    var file = row.getItem();
                    viewModel.navigateTo(file);
                }
            });
            row.setContextMenu(itemContextMenu);
            row.emptyProperty().addListener((ov, oldV, newV) -> {
                if (newV) {
                    row.setContextMenu(null);
                } else {
                    row.setContextMenu(itemContextMenu);
                }
            });
            return row;

        });
        this.fileTableView.setContextMenu(containerContextMenu);
        this.fileListView.setContextMenu(containerContextMenu);
        var converter = new FileStringConverter();
        this.fileListView.setCellFactory(listView -> new TextFieldColumnListCell<>(converter) {

            {
                addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                    if (e.getClickCount() == 2) {
                        var file = getItem();
                        viewModel.navigateTo(file);
                    }
                });
                setManualEdit(true);
                setContextMenu(itemContextMenu);
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                doOnCancelCommit(getItem());
            }

            @Override
            public void commitEdit(GenericFile newValue) {
                super.commitEdit(newValue);
                doOnEditCommit(newValue);
            }

            @Override
            protected void updateItem(GenericFile item, boolean empty) {
                if (item == null || empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    if (item.getType() != null) {
                        if (item.isDirectory()) {
                            setGraphic(new FontIconView(SharedIcons.DIRECTORY));
                        } else {
                            setGraphic(new FontIconView(SharedIcons.FILE));
                        }
                    } else {
                        setGraphic(null);
                    }
                    setText(item.getName());
                }
                super.updateItem(item, empty);
            }
        });

        var columnConstraint1 = new ColumnConstraints();
        columnConstraint1.setHgrow(Priority.NEVER);
        var columnConstraint2 = new ColumnConstraints();
        columnConstraint2.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().addAll(columnConstraint1, columnConstraint2);
        gridPane.setHgap(SizeConstants.INSET);
        gridPane.setVgap(SizeConstants.INSET);

        nameLabel.setMinWidth(Region.USE_PREF_SIZE);
        gridPane.add(nameLabel, 0, 0);
        GridPane.setHgrow(nameTextField, Priority.ALWAYS);
        gridPane.add(nameTextField, 1, 0);

        filterLabel.setMinWidth(Region.USE_PREF_SIZE);
        gridPane.add(filterLabel, 0, 1);
        GridPane.setHgrow(filterComboBox, Priority.ALWAYS);
        filterComboBox.setMaxWidth(Double.MAX_VALUE);
        filterComboBox.setItems(viewModel.getExtensionFilters());
        gridPane.add(filterComboBox, 1, 1);

        VBox.setVgrow(main, Priority.ALWAYS);
        main.setSpacing(SizeConstants.INSET);
        main.setPadding(new Insets(SizeConstants.INSET, SizeConstants.INSET, 0, SizeConstants.INSET));
        main.getStylesheets().add(FileChooserDialogView.class.getResource("file-dialog.css").toExternalForm());

        getButtonBox().getChildren().addAll(getCancelButton(), getOkButton());
        getContentPane().getChildren().addAll(main, getButtonBox());
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        locationLabel.textProperty().bind(viewModel.locationTextProperty());
        locationComboBox.valueProperty().bind(viewModel.locationProperty());
        listButton.selectedProperty().bindBidirectional(viewModel.listSelectedProperty());
        detailsButton.selectedProperty().bindBidirectional(viewModel.detailsSelectedProperty());
        nameTextField.textProperty().bindBidirectional(viewModel.fileNameWrapper());
        this.filterComboBox.valueProperty().bindBidirectional(viewModel.selectedExtensionFilterProperty());
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        ValueUtils.callAndAddListener(listButton.selectedProperty(), (ov, oldV, newV) -> {
            if (newV) {
                //index can be set to -1 when nodes are removed, so it is saved
                var selectedIndex = viewModel.selectedFileIndexProperty().get();
                updateFileBox(this.fileListView);
                this.fileListView.refresh();
                this.fileListView.getSelectionModel().select(selectedIndex);
            }
        });
        ValueUtils.callAndAddListener(detailsButton.selectedProperty(), (ov, oldV, newV) -> {
            if (newV) {
                //index can be set to -1 when nodes are removed, so it is saved
                var selectedIndex = viewModel.selectedFileIndexProperty().get();
                updateFileBox(this.fileTableView);
                this.fileTableView.getSelectionModel().select(selectedIndex);
            }
        });
        this.fileTableView.getSelectionModel().selectedIndexProperty().addListener((ov, oldV, newV) -> {
            if (this.detailsButton.selectedProperty().get()
                    && viewModel.selectedFileIndexProperty().get() != newV.intValue()) {
                viewModel.selectedFileIndexProperty().set(newV.intValue());
            }
        });
        this.fileListView.getSelectionModel().selectedIndexProperty().addListener((ov, oldV, newV) -> {
            if (this.listButton.selectedProperty().get()
                    && viewModel.selectedFileIndexProperty().get() != newV.intValue()) {
                viewModel.selectedFileIndexProperty().set(newV.intValue());
            }
        });
        viewModel.sortRequiredSource().addListener((v) -> {
            if (Boolean.TRUE.equals(v)) {
                this.fileTableView.sort();
            }
        });
        viewModel.listRefreshRequiredSource().addListener((v) -> {
            if (Boolean.TRUE.equals(v) && this.fileListView != null) {
                this.fileListView.refresh();
            }
        });
        viewModel.selectedFileIndexProperty().addListener((ov, oldV, newV) -> {
            if (this.detailsButton.selectedProperty().get()) {
                if (newV.intValue() < 0) {
                    this.fileTableView.getSelectionModel().clearSelection();
                } else {
                    this.fileTableView.getSelectionModel().select(newV.intValue());
                }
            } else {
                if (newV.intValue() < 0) {
                    this.fileListView.getSelectionModel().clearSelection();
                } else {
                    this.fileListView.getSelectionModel().select(newV.intValue());
                }
            }
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var viewModel = getViewModel();
        this.levelUpButton.setOnAction(e -> viewModel.navigateUp());
        this.homeButton.setOnAction(e -> viewModel.navigateHome());
        this.createButton.setOnAction(e -> {
            viewModel.createFakeNewDirectory();
            if (this.listButton.isSelected()) {
                this.fileListView.refresh();
                this.fileListView.scrollToFirstColumn();
                this.fileListView.getSelectionModel().select(0);
                this.fileListView.edit(0);
            } else {
                this.fileTableView.scrollTo(0);
                this.fileTableView.getSelectionModel().select(0);
                nameColumn.setEditable(true);
                this.fileTableView.edit(0, nameColumn);
            }
        });
        this.listButton.setOnAction(e -> {
            if (listButton.isSelected()) {
                e.consume();
            }
        });
        this.detailsButton.setOnAction(e -> {
            if (detailsButton.isSelected()) {
                e.consume();
            }
        });
        //when setOnShowing is used then popup height is calculated incorrectly
        //maybe because OnMousePressed handler is called before OnShowing handler.
        //another reason - update location property only after locations have been populated
        locationComboBox.setOnMousePressed(e -> {
            //update locations only when popup is shown
            viewModel.updateLocations();
        });
        nameColumn.setOnEditCancel(e -> {
            var file = (GenericFile) e.getOldValue();
            doOnCancelCommit(file);
            nameColumn.setEditable(false);
        });
        nameColumn.setOnEditCommit(e -> {
            //var oldFile = (GenericFile) e.getOldValue();
            var newFile = (GenericFile) e.getNewValue(); //from converter
            doOnEditCommit(newFile);
            nameColumn.setEditable(false);
        });
        getNode().addEventHandler(DialogResizeEvent.DIALOG_RESIZE_STARTED, e -> {
            if (listButton.selectedProperty().get()) {
                this.fileListView.doOnResizeStarted();
            }
        });
        getNode().addEventHandler(DialogResizeEvent.DIALOG_RESIZE_FINISHED, e -> {
            if (listButton.selectedProperty().get()) {
                this.fileListView.doOnResizeFinished();
            }
        });
    }

    protected Label getLocationLabel() {
        return locationLabel;
    }

    @Override
    protected void makeEqualButtons() {
        if (getViewModel().getCancel().isVisible()) {
            ButtonUtils.makeEqualWidthBySize(getCancelButton(), getOkButton());
        }
    }

    private void updateFileBox(Node node) {
        fileBox.getChildren().clear();
        VBox.setVgrow(node, Priority.ALWAYS);
        fileBox.getChildren().add(node);
    }

    private MenuItem createRenameMenuItem() {
        var renameItem = new MenuItem("Rename");
        renameItem.setOnAction(e -> {
            if (listButton.isSelected()) {
                this.fileListView.edit(getViewModel().selectedFileIndexProperty().get());
            } else {
                nameColumn.setEditable(true);
                this.fileTableView.edit(this.fileTableView.getSelectionModel().getSelectedIndex(), nameColumn);
            }
        });
        return renameItem;
    }

    private MenuItem createRefreshMenuItem() {
        var menuItem = new MenuItem("Refresh");
        menuItem.setOnAction(e -> getViewModel().updateFiles(null));
        return menuItem;
    }

    private void doOnCancelCommit(GenericFile file) {
        var viewModel = getViewModel();
        if (file.getUri() == null) {
            viewModel.removeFakeNewDirectory();
        }
    }

    private void doOnEditCommit(GenericFile newFile) {
        var viewModel = getViewModel();
        if (newFile.getUri() == null) {
            viewModel.createRealNewDirectory(newFile);
        } else {
            viewModel.renameFile(newFile);
            if (viewModel.selectedFileIndexProperty().get() >= 0) {
                if (listButton.isSelected()) {
                    var columnIndex = this.fileListView.resolveColumnIndex(viewModel.selectedFileIndexProperty().get());
                    this.fileListView.scrollToFirstColumn(columnIndex);
                } else {
                    this.fileTableView.scrollTo(viewModel.selectedFileIndexProperty().get());
                }
            }
        }
    }
}
