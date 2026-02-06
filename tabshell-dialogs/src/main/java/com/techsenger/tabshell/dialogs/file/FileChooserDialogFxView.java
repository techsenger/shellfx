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
import com.techsenger.tabshell.core.dialog.AbstractDialogFxView;
import com.techsenger.tabshell.core.dialog.DialogPort;
import com.techsenger.tabshell.core.dialog.DialogResizeEvent;
import com.techsenger.tabshell.core.popup.OverlayScope;
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.dialogs.alert.AlertDialogFxView;
import com.techsenger.tabshell.dialogs.alert.AlertDialogPresenter;
import com.techsenger.tabshell.dialogs.alert.AlertDialogType;
import static com.techsenger.tabshell.dialogs.file.FileChooserType.OPEN;
import static com.techsenger.tabshell.dialogs.file.FileChooserType.SAVE_AS;
import com.techsenger.tabshell.dialogs.style.DialogIcons;
import com.techsenger.tabshell.material.button.ResultButton;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.list.TextFieldColumnListCell;
import com.techsenger.tabshell.material.style.SizeConstants;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.material.table.TableHistory;
import com.techsenger.tabshell.shared.style.SharedIcons;
import com.techsenger.tabshell.storage.GenericFile;
import java.util.Collections;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.util.StringConverter;

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
public class FileChooserDialogFxView<P extends FileChooserDialogPresenter<?, ?>>
        extends AbstractDialogFxView<P> implements FileChooserDialogView {

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

    public class Composer extends AbstractDialogFxView.Composer implements FileChooserDialogComposer {

        @Override
        public DialogPort addAlertDialog(OverlayScope scope, AlertDialogType type, String message) {
            var dialog = createAlertDialog(scope, type, message);
            dialog.getPresenter().initialize();
            getContainer().getComposer().addDialog(dialog);
            return dialog.getPresenter().getPort();
        }

        protected AlertDialogFxView<?> createAlertDialog(OverlayScope scope, AlertDialogType type, String message) {
            var view = new AlertDialogFxView(false);
            var presenter = new AlertDialogPresenter<>(view, scope, type, message);
            return view;
        }
    }

    private final class DialogTextFieldColumnListCell extends TextFieldColumnListCell<GenericFile> {

        private DialogTextFieldColumnListCell(StringConverter converter) {
            super(converter);
            addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                if (e.getClickCount() == 2) {
                    var file = getItem();
                    getPresenter().handleFileRequested(file);
                }
            });
            setManualEdit(true);
            setContextMenu(itemContextMenu);
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            getPresenter().handleEditCancelled(getItem());
        }

        @Override
        public void commitEdit(GenericFile newValue) {
            super.commitEdit(newValue);
            getPresenter().handleEditCommitted(newValue);
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
    }

    private final Label locationLabel = new Label();

    private final ComboBox<Location> locationComboBox = new ComboBox<>();

    private final Button levelUpButton = new Button(null, new FontIconView(DialogIcons.DIRECTORY_UP));

    private final Button homeButton = new Button(null, new FontIconView(DialogIcons.HOME));

    private final Button createButton = new Button(null, new FontIconView(DialogIcons.ADD_DIRECTORY));

    private final ToggleButton listButton = new ToggleButton(null, new FontIconView(DialogIcons.GRID_VIEW));

    private final ToggleButton detailsButton = new ToggleButton(null, new FontIconView(DialogIcons.LIST_VIEW));

    private final ToggleGroup toggleGroup = new ToggleGroup();

    private final HBox buttonBox = new HBox(levelUpButton, homeButton, createButton, listButton, detailsButton);

    private final HBox locationBox = new HBox(locationLabel, locationComboBox, buttonBox);

    private final Label fileNameLabel = new Label("File Name");

    private final TextField fileNameTextField = new TextField();

    private final Label filterLabel = new Label("Files of Type");

    private final ComboBox<ExtensionFilter> filterComboBox = new ComboBox<>();

    private final GridPane gridPane = new GridPane();

    private final VBox fileBox = new VBox();

    private final ObservableList<GenericFile> files = FXCollections.observableArrayList();

    private FileTableView fileTableView;

    private TableColumn<GenericFile, ?> nameColumn;

    private FileListView fileListView;

    private final VBox main = new VBox(locationBox, fileBox, gridPane);

    private final ContextMenu containerContextMenu = new ContextMenu();

    private final ContextMenu itemContextMenu = new ContextMenu();

    private final ResultButton cancelButton = new ResultButton(FileChooserButtons.CANCEL, "Cancel");

    private final ResultButton okButton = new ResultButton(FileChooserButtons.OK, "OK");

    private AppearanceSettings settings;

    public FileChooserDialogFxView(boolean  resizable) {
        super(resizable);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    public void setAppearanceSettings(AppearanceSettings settings) {
        this.settings = settings;
    }

    @Override
    public void setLocationCaption(String value) {
        this.locationLabel.setText(value);
    }

    @Override
    public String getLocationCaption() {
        return this.locationLabel.getText();
    }

    @Override
    public List<Location> getLocations() {
        return Collections.unmodifiableList(locationComboBox.getItems());
    }

    @Override
    public void setLocations(List<Location> locations) {
        locationComboBox.setItems(FXCollections.observableArrayList(locations));
    }

    @Override
    public Location getLocation() {
        return locationComboBox.getSelectionModel().getSelectedItem();
    }

    @Override
    public void setLocation(Location value) {
        this.locationComboBox.getSelectionModel().select(value);
    }

    @Override
    public boolean isListSelected() {
        return listButton.isSelected();
    }

    @Override
    public void setListSelected(boolean value) {
        this.listButton.setSelected(value);
        updateListSelected(value);
    }

    @Override
    public boolean isDetailsSelected() {
        return detailsButton.isSelected();
    }

    @Override
    public void setDetailsSelected(boolean value) {
        this.detailsButton.setSelected(value);
        updateDetailsSelected(value);
    }

    @Override
    public void setTableHistory(TableHistory history) {
        this.fileTableView.restoreHistory(history);
        this.nameColumn = this.fileTableView.findNameColumn();
        nameColumn.setOnEditCancel(e -> {
            var file = (GenericFile) e.getOldValue();
            getPresenter().handleEditCancelled(file);
            nameColumn.setEditable(false);
        });
        nameColumn.setOnEditCommit(e -> {
            //var oldFile = (GenericFile) e.getOldValue();
            var newFile = (GenericFile) e.getNewValue(); //from converter
            getPresenter().handleEditCommitted(newFile);
            nameColumn.setEditable(false);
        });
    }

    @Override
    public TableHistory getTableHistory() {
        return this.fileTableView.createHistory();
    }

    @Override
    public List<GenericFile> getFiles() {
        return Collections.unmodifiableList(this.files);
    }

    @Override
    public void setFiles(List<GenericFile> files) {
        this.files.clear();
        this.files.addAll(files);
        if (isListSelected()) {
            this.fileListView.refresh();
        }
    }

    @Override
    public void addFile(int index, GenericFile file) {
        this.files.add(index, file);
        if (this.listButton.isSelected()) {
            this.fileListView.refresh();
        }
    }

    @Override
    public void removeFile(int index) {
        this.files.remove(index);
        if (this.listButton.isSelected()) {
            this.fileListView.refresh();
        }
    }

    @Override
    public void selectFile(int index) {
        if (isListSelected()) {
            this.fileListView.getSelectionModel().select(index);
        } else {
            this.fileTableView.getSelectionModel().select(index);
        }
    }

    @Override
    public void scrollToFile(int index) {
        if (listButton.isSelected()) {
            var columnIndex = this.fileListView.resolveColumnIndex(index);
            this.fileListView.scrollToFirstColumn(columnIndex);
        } else {
            this.fileTableView.scrollTo(index);
        }
    }

    @Override
    public GenericFile getSelectedFile() {
        if (isListSelected()) {
            return this.fileListView.getSelectionModel().getSelectedItem();
        } else {
            return this.fileTableView.getSelectionModel().getSelectedItem();
        }
    }

    @Override
    public void editFile(int index) {
        if (listButton.isSelected()) {
            this.fileListView.getSelectionModel().select(index);
            this.fileListView.edit(index);
        } else {
            this.fileTableView.getSelectionModel().select(index);
            nameColumn.setEditable(true);
            this.fileTableView.edit(index, nameColumn);
        }
    }

    @Override
    public void sortFiles() {
        this.fileTableView.sort();
    }

    @Override
    public void setFileName(String fileName) {
        this.fileNameTextField.setText(fileName);
    }

    @Override
    public String getFileName() {
        return fileNameTextField.getText();
    }

    @Override
    public List<ExtensionFilter> getExtensionFilters() {
        return Collections.unmodifiableList(this.filterComboBox.getItems());
    }

    @Override
    public void setExtensionFilters(List<ExtensionFilter> filters) {
        this.filterComboBox.setItems(FXCollections.observableArrayList(filters));
    }

    @Override
    public ExtensionFilter getExtensionFilter() {
        return this.filterComboBox.getSelectionModel().getSelectedItem();
    }

    @Override
    public void setExtensionFilter(ExtensionFilter filter) {
        this.filterComboBox.getSelectionModel().select(filter);
    }

    @Override
    public void setupFor(FileChooserType type) {
        switch (type) {
            case OPEN:
                setTitle("Open");
                setIcon(SharedIcons.OPEN);
                setLocationCaption("Look In");
                break;
            case SAVE_AS:
                setTitle("Save As");
                setIcon(SharedIcons.SAVE_AS);
                setLocationCaption("Save In");
                break;
            default:
                throw new AssertionError();
        }
    }

    @Override
    protected Composer createComposer() {
        return new FileChooserDialogFxView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        this.fileTableView = new FileTableView(files, this.settings);
        this.fileListView = new FileListView(files, new ContextMenu(createRefreshMenuItem()));
        updateListSelected(true);

        locationLabel.setMinWidth(Region.USE_PREF_SIZE);
        HBox.setHgrow(locationComboBox, Priority.ALWAYS);
        locationComboBox.getStyleClass().add(Styles.DENSE);
        locationComboBox.setMaxWidth(Double.MAX_VALUE);
        locationComboBox.setCellFactory(cb -> {
            var cell = new LocationCell(false);
            cell.setOnMousePressed(e -> {
                getPresenter().handleLocationRequested(cell.getItem());
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
        //always one button selected
        toggleGroup.selectedToggleProperty().addListener((ov, oldToggle, newToggle) -> {
            if (newToggle == null) {
                oldToggle.setSelected(true);
            }
        });
        listButton.getStyleClass().addAll(StyleClasses.ICONED_BUTTON, Styles.FLAT);
        listButton.setTooltip(new Tooltip("List"));
        listButton.setToggleGroup(toggleGroup);
        listButton.setSelected(true);
        detailsButton.getStyleClass().addAll(StyleClasses.ICONED_BUTTON, Styles.FLAT);
        detailsButton.setTooltip(new Tooltip("Details"));
        detailsButton.setToggleGroup(toggleGroup);
        buttonBox.setSpacing(SizeConstants.THIRD_INSET);
        locationBox.setSpacing(SizeConstants.INSET);
        locationBox.setAlignment(Pos.CENTER_LEFT);

        VBox.setVgrow(fileBox, Priority.ALWAYS);
        itemContextMenu.getItems().addAll(createRenameMenuItem(), createRefreshMenuItem());
        this.fileTableView.setRowFactory(tv -> {
            TableRow<GenericFile> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    var file = row.getItem();
                    getPresenter().handleFileRequested(file);
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
        containerContextMenu.getItems().add(createRefreshMenuItem());
        this.fileTableView.setContextMenu(containerContextMenu);
        this.fileListView.setContextMenu(containerContextMenu);
        var converter = new FileStringConverter();
        this.fileListView.setCellFactory(listView -> new DialogTextFieldColumnListCell(converter));

        var columnConstraint1 = new ColumnConstraints();
        columnConstraint1.setHgrow(Priority.NEVER);
        var columnConstraint2 = new ColumnConstraints();
        columnConstraint2.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().addAll(columnConstraint1, columnConstraint2);
        gridPane.setHgap(SizeConstants.INSET);
        gridPane.setVgap(SizeConstants.INSET);

        fileNameLabel.setMinWidth(Region.USE_PREF_SIZE);
        gridPane.add(fileNameLabel, 0, 0);
        GridPane.setHgrow(fileNameTextField, Priority.ALWAYS);
        gridPane.add(fileNameTextField, 1, 0);

        filterLabel.setMinWidth(Region.USE_PREF_SIZE);
        gridPane.add(filterLabel, 0, 1);
        GridPane.setHgrow(filterComboBox, Priority.ALWAYS);
        filterComboBox.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(filterComboBox, 1, 1);

        VBox.setVgrow(main, Priority.ALWAYS);
        main.setSpacing(SizeConstants.INSET);
        main.getStylesheets().add(FileChooserDialogFxView.class.getResource("file-dialog.css").toExternalForm());
        getContentBox().getChildren().addAll(main);

        registerButtons(cancelButton, okButton);
        addRightButtons(FileChooserButtons.CANCEL, FileChooserButtons.OK);
        setButtonDefault(FileChooserButtons.OK, true);
    }

    @Override
    protected void addListeners() {
        this.fileListView.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            if (isListSelected()) {
                getPresenter().handleFileSelected(newV);
            }
        });
        this.fileTableView.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            if (isDetailsSelected()) {
                getPresenter().handleFileSelected(newV);
            }
        });
        this.filterComboBox.getSelectionModel().selectedItemProperty().addListener((ov, odlV, newV) -> {
            getPresenter().handleFilterSelected(newV);
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var presenter = getPresenter();
        this.levelUpButton.setOnAction(e -> presenter.handleNavigateUp());
        this.homeButton.setOnAction(e -> presenter.handleNavigateHome());
        this.createButton.setOnAction(e -> presenter.handleNewDirectoryRequested());
        this.listButton.setOnAction(e -> {
            if (listButton.isSelected()) {
                getPresenter().handleListSelected();
            }
        });
        this.detailsButton.setOnAction(e -> {
            if (detailsButton.isSelected()) {
                getPresenter().handleDetailsSelected();
            }
        });
        //when setOnShowing is used then popup height is calculated incorrectly
        //maybe because OnMousePressed handler is called before OnShowing handler.
        //another reason - update location property only after locations have been populated
        locationComboBox.setOnMousePressed(e -> {
            //update locations only when popup is shown
            getPresenter().handleLocationsOpened();
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
        return this.locationLabel;
    }

    protected ComboBox<Location> getLocationComboBox() {
        return locationComboBox;
    }

    protected Button getLevelUpButton() {
        return levelUpButton;
    }

    protected Button getHomeButton() {
        return homeButton;
    }

    protected Button getCreateButton() {
        return createButton;
    }

    protected ToggleButton getListButton() {
        return listButton;
    }

    protected ToggleButton getDetailsButton() {
        return detailsButton;
    }

    protected Label getFileNameLabel() {
        return fileNameLabel;
    }

    protected Label getFilterLabel() {
        return filterLabel;
    }

    protected FileListView getFileListView() {
        return fileListView;
    }

    private void updateListSelected(boolean selected) {
        if (selected) {
            updateFileBox(this.fileListView);
            this.fileListView.refresh();
            this.fileListView.getSelectionModel().select(fileTableView.getSelectionModel().getSelectedIndex());
        }
    }

    private void updateDetailsSelected(boolean selected) {
        if (selected) {
            updateFileBox(this.fileTableView);
            this.fileTableView.getSelectionModel().select(this.fileListView.getSelectionModel().getSelectedIndex());
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
            int index;
            if (listButton.isSelected()) {
                index = this.fileListView.getSelectionModel().getSelectedIndex();
            } else {
                index = this.fileTableView.getSelectionModel().getSelectedIndex();
            }
            if (index >= 0) {
                getPresenter().handleRenameRequested(index);
            }
        });
        return renameItem;
    }

    private MenuItem createRefreshMenuItem() {
        var menuItem = new MenuItem("Refresh");
        menuItem.setOnAction(e -> getPresenter().handleRefresh());
        return menuItem;
    }
}
