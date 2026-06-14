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
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.core.window.WindowPosition;
import com.techsenger.tabshell.dialogs.alert.AlertDialogFxView;
import com.techsenger.tabshell.dialogs.alert.AlertDialogParams;
import com.techsenger.tabshell.dialogs.alert.AlertDialogPresenter;
import com.techsenger.tabshell.dialogs.style.DialogIcons;
import com.techsenger.tabshell.material.button.ResultButton;
import com.techsenger.tabshell.material.button.ResultButtonName;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.list.TextFieldColumnListCell;
import com.techsenger.tabshell.material.style.Spacing;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.material.table.TableColumnInfo;
import com.techsenger.tabshell.material.table.TableColumnManager;
import com.techsenger.tabshell.material.table.TableColumnName;
import com.techsenger.tabshell.storage.FileColumnBuilder;
import com.techsenger.tabshell.storage.FileColumns;
import com.techsenger.tabshell.storage.GenericFile;
import com.techsenger.toolkit.fx.value.ValueUtils;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
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
public class FileChooserDialogFxView<P extends FileChooserDialogPresenter<?>>
        extends AbstractDialogFxView<P> implements FileChooserDialogView {

    public class Composer extends AbstractDialogFxView<P>.Composer implements FileChooserDialogView.Composer {

        @Override
        public DialogPort addAlertDialog(AlertDialogParams params, String message) {
            var dialog = createAlertDialog(params);
            dialog.getPresenter().setMessage(message);
            getContainer().getComposer().addWindow(dialog);
            getContainer().getComposer().alignWindowToStage(dialog, WindowPosition.CENTER);
            return dialog.getPresenter();
        }

        protected AlertDialogFxView<?> createAlertDialog(AlertDialogParams params) {
            var view = new AlertDialogFxView();
            var presenter = new AlertDialogPresenter<>(view, params);
            presenter.initialize();
            return view;
        }
    }

    private final class DialogTextFieldColumnListCell extends TextFieldColumnListCell<GenericFile> {

        private DialogTextFieldColumnListCell(StringConverter converter) {
            super(converter);
            addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                if (e.getClickCount() == 2) {
                    var file = getItem();
                    getPresenter().onFileRequested(file);
                }
            });
            setManualEdit(true);
            setContextMenu(itemContextMenu);
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            getPresenter().onEditCancelled(getItem());
        }

        @Override
        public void commitEdit(GenericFile newValue) {
            super.commitEdit(newValue);
            getPresenter().onEditCommitted(newValue);
        }

        @Override
        protected void updateItem(GenericFile item, boolean empty) {
            if (item == null || empty) {
                setGraphic(null);
                setText(null);
            } else {
                if (item.getType() != null) {
                    if (item.isDirectory()) {
                        setGraphic(new FontIconView(DialogIcons.DIRECTORY));
                    } else {
                        setGraphic(new FontIconView(DialogIcons.FILE));
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

    private final TableView<GenericFile> fileTableView = new TableView<>(this.files);

    private TableColumnManager fileColumnManager = new TableColumnManager(fileTableView);

    private FileListView fileListView;

    private final VBox main = new VBox(locationBox, fileBox, gridPane);

    private final ContextMenu containerContextMenu = new ContextMenu();

    private final ContextMenu itemContextMenu = new ContextMenu();

    private final ResultButton cancelButton = new ResultButton(FileChooserDialogButtons.CANCEL, "Cancel");

    private final ResultButton okButton = new ResultButton(FileChooserDialogButtons.OK, "OK");

    private AppearanceSettings settings;

    public FileChooserDialogFxView() {
        super();
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
    public void setLocations(List<Location> locations) {
        locationComboBox.setItems(FXCollections.observableArrayList(locations));
    }

    @Override
    public void setLocation(Location value) {
        this.locationComboBox.getSelectionModel().select(value);
    }

    @Override
    public void setListSelected(boolean value) {
        this.listButton.setSelected(value);
        updateListSelected(value);
    }

    @Override
    public void setDetailsSelected(boolean value) {
        this.detailsButton.setSelected(value);
        updateDetailsSelected(value);
    }

    @Override
    public void setLocationCaption(String value) {
        this.locationLabel.setText(value);
    }

    @Override
    public void addColumns(Map<TableColumnName, TableColumnInfo> infosByName) {
        this.fileColumnManager.addColumns(infosByName);
    }

    @Override
    public void setFiles(List<GenericFile> files) {
        this.files.clear();
        this.files.addAll(files);
        if (this.listButton.isSelected()) {
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
        if (this.listButton.isSelected()) {
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
    public void editFile(int index) {
        if (listButton.isSelected()) {
            this.fileListView.getSelectionModel().select(index);
            this.fileListView.edit(index);
        } else {
            this.fileTableView.getSelectionModel().select(index);
            var column = fileColumnManager.getColumnsByName().get(FileColumns.NAME);
            column.setEditable(true);
            this.fileTableView.edit(index, (TableColumn<GenericFile, Object>) column);
        }
    }

    @Override
    public void setFileName(String fileName) {
        this.fileNameTextField.setText(fileName);
    }

    @Override
    public void setExtensionFilters(List<ExtensionFilter> filters) {
        this.filterComboBox.setItems(FXCollections.observableArrayList(filters));
    }

    @Override
    public void setExtensionFilter(ExtensionFilter filter) {
        this.filterComboBox.getSelectionModel().select(filter);
    }

    @Override
    public void setRightButtons(ResultButtonName... names) {
        super.setRightButtons(names);
        makeButtonsEqualWidth();
    }

    protected void makeButtonsEqualWidth() {
        makeEqualWidth(getRightButtons(true));
    }

    @Override
    protected Composer createComposer() {
        return new FileChooserDialogFxView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        this.fileTableView.getStyleClass().add(StyleClasses.SAME_SPACING_COLUMN);
        this.fileTableView.setEditable(true);
        this.fileTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        this.fileTableView.setPlaceholder(new Label(""));
        var columnBuilder = new FileColumnBuilder(settings.getRegularFont());
        this.fileColumnManager.registerColumnFactory(FileColumns.TYPE, () -> {
            var column = columnBuilder.buildTypeColumn(DialogIcons.DIRECTORY, DialogIcons.FILE);
            column.setEditable(false);
            column.getStyleClass().add(StyleClasses.SAME_SPACING_COLUMN_FIRST);
            return column;
        });
        var strConverter = new FileStringConverter();
        this.fileColumnManager.registerColumnFactory(FileColumns.NAME, () -> {
            var column = columnBuilder.buildNameColumn();
            column.setEditable(false);
            column.setCellFactory(r -> new TextFieldTableCell<>(strConverter));
            column.setOnEditCancel(e -> {
                var file = (GenericFile) e.getOldValue();
                getPresenter().onEditCancelled(file);
                column.setEditable(false);
            });
            column.setOnEditCommit(e -> {
                var newFile = (GenericFile) e.getNewValue(); //from converter
                getPresenter().onEditCommitted(newFile);
                column.setEditable(false);
            });
            return column;
        });
        this.fileColumnManager.registerColumnFactory(FileColumns.SIZE, () -> {
            var column = columnBuilder.buildSizeColumn();
            column.setEditable(false);
            return column;
        });
        this.fileColumnManager.registerColumnFactory(FileColumns.LAST_MODIFIED, () -> {
            var coumn = columnBuilder.buildLastModifiedColumn();
            coumn.setEditable(false);
            coumn.getStyleClass().add(StyleClasses.SAME_SPACING_COLUMN_LAST);
            return coumn;
        });

        this.fileListView = new FileListView(files, new ContextMenu(createRefreshMenuItem()));
        updateListSelected(true);

        locationLabel.setMinWidth(Region.USE_PREF_SIZE);
        HBox.setHgrow(locationComboBox, Priority.ALWAYS);
        locationComboBox.setMaxWidth(Double.MAX_VALUE);
        locationComboBox.setCellFactory(cb -> {
            var cell = new LocationCell(false);
            cell.setOnMousePressed(e -> {
                getPresenter().onLocationRequested(cell.getItem());
            });
            return cell;
        });
        locationComboBox.setButtonCell(new LocationCell(true));
        locationComboBox.getStyleClass().add("location");
        levelUpButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT);
        levelUpButton.setTooltip(new Tooltip("Up One Level"));
        homeButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT);
        homeButton.setTooltip(new Tooltip("Home"));
        createButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT);
        createButton.setTooltip(new Tooltip("Create New Folder"));
        //always one button selected
        toggleGroup.selectedToggleProperty().addListener((ov, oldToggle, newToggle) -> {
            if (newToggle == null) {
                oldToggle.setSelected(true);
            }
        });
        listButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT);
        listButton.setTooltip(new Tooltip("List"));
        listButton.setToggleGroup(toggleGroup);
        listButton.setSelected(true);
        detailsButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT);
        detailsButton.setTooltip(new Tooltip("Details"));
        detailsButton.setToggleGroup(toggleGroup);
        buttonBox.setSpacing(Spacing.getHorizontalThird());
        locationBox.setSpacing(Spacing.getHorizontal());
        locationBox.setAlignment(Pos.CENTER_LEFT);

        VBox.setVgrow(fileBox, Priority.ALWAYS);
        itemContextMenu.getItems().addAll(createRenameMenuItem(), createRefreshMenuItem());
        this.fileTableView.setRowFactory(tv -> {
            TableRow<GenericFile> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    var file = row.getItem();
                    getPresenter().onFileRequested(file);
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
        gridPane.setHgap(Spacing.getHorizontal());
        gridPane.setVgap(Spacing.getVertical());

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
        main.setSpacing(Spacing.getVertical());
        main.getStylesheets().add(FileChooserDialogFxView.class.getResource("file-dialog.css").toExternalForm());
        getContentBox().getChildren().addAll(main);

        registerButtons(cancelButton, okButton);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        this.fileListView.getSelectionModel().selectedIndexProperty().addListener((ov, oldV, newV) -> {
            if (this.listButton.isSelected()) {
                getPresenter().onFileSelected(newV.intValue());
            }
        });
        this.fileTableView.getSelectionModel().selectedIndexProperty().addListener((ov, oldV, newV) -> {
            if (this.detailsButton.isSelected()) {
                getPresenter().onFileSelected(newV.intValue());
            }
        });
        ValueUtils.callAndAddListener(this.fileTableView.comparatorProperty(), (ov, oldV, newV) ->
                getPresenter().onFileComparatorChanged(newV));
        this.filterComboBox.getSelectionModel().selectedItemProperty().addListener((ov, odlV, newV) -> {
            getPresenter().onFilterSelected(newV);
        });
        this.fileColumnManager.setWidthListener(getPresenter()::onColumnWidthChanged);
        this.fileColumnManager.setSortTypeListener(getPresenter()::onColumnSortTypeChanged);
        this.fileColumnManager.setIndexListener(getPresenter()::onColumnIndexChanged);
        this.fileColumnManager.setSortIndexListener(getPresenter()::onColumnSortIndexChanged);
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var presenter = getPresenter();
        this.levelUpButton.setOnAction(e -> presenter.onNavigateUp());
        this.homeButton.setOnAction(e -> presenter.onNavigateHome());
        this.createButton.setOnAction(e -> presenter.onNewDirectory());
        this.listButton.setOnAction(e -> getPresenter().onList(listButton.isSelected()));
        this.detailsButton.setOnAction(e -> getPresenter().onDetails(detailsButton.isSelected()));
        //when setOnShowing is used then popup height is calculated incorrectly
        //maybe because OnMousePressed handler is called before OnShowing handler.
        //another reason - update location property only after locations have been populated
        locationComboBox.setOnMousePressed(e -> {
            //update locations only when popup is shown
            getPresenter().onLocationsOpened();
        });
        getNode().addEventHandler(DialogResizeEvent.DIALOG_RESIZE_STARTED, e -> {
            if (listButton.selectedProperty().get()) {
                this.fileListView.onResizeStarted();
            }
        });
        getNode().addEventHandler(DialogResizeEvent.DIALOG_RESIZE_FINISHED, e -> {
            if (listButton.selectedProperty().get()) {
                this.fileListView.onResizeFinished();
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
                getPresenter().onRename(index);
            }
        });
        return renameItem;
    }

    private MenuItem createRefreshMenuItem() {
        var menuItem = new MenuItem("Refresh");
        menuItem.setOnAction(e -> getPresenter().onRefresh());
        return menuItem;
    }
}
