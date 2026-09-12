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

package com.techsenger.shellfx.dialogs.file;

import atlantafx.base.theme.Styles;
import com.techsenger.shellfx.core.dialog.AbstractDialogView;
import com.techsenger.shellfx.core.dialog.DialogPort;
import com.techsenger.shellfx.core.dialog.DialogResizeEvent;
import com.techsenger.shellfx.core.window.AbstractWindowView;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogView;
import com.techsenger.shellfx.dialogs.alert.AlertDialogViewModel;
import com.techsenger.shellfx.dialogs.style.DialogIcons;
import com.techsenger.shellfx.material.button.ResultButton;
import com.techsenger.shellfx.material.column.TextFieldColumnListCell;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.shellfx.material.table.TableColumnManager;
import com.techsenger.shellfx.storage.Comparators;
import com.techsenger.shellfx.storage.FileColumnBuilder;
import com.techsenger.shellfx.storage.FileColumns;
import com.techsenger.shellfx.storage.FileStringConverter;
import com.techsenger.shellfx.storage.FileViewConstants;
import com.techsenger.shellfx.storage.GenericFile;
import com.techsenger.toolkit.fx.value.ValueUtils;
import java.util.Comparator;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
public class FileChooserDialogView<VM extends FileChooserDialogViewModel<?, T>, T extends GenericFile>
        extends AbstractDialogView<VM> {

    public class Composer extends AbstractWindowView<VM>.Composer implements FileChooserDialogComposer {

        @Override
        public DialogPort addAlertDialog(AlertDialogParams params, String message) {
            var dialog = createAlertDialog(params);
            dialog.getViewModel().setMessage(message);
            if (dialog.getViewModel().getWindowType() == WindowType.NESTED) {
                getParent().getComposer().addDialog(dialog);
            } else {
                dialog.getStage().initOwner(getNode().getScene().getWindow());
                dialog.getStage().show();
            }
            dialog.requestFocus();
            return dialog.getViewModel();
        }

        protected AlertDialogView<?> createAlertDialog(AlertDialogParams params) {
            var viewModel = new AlertDialogViewModel<>(params);
            var view = new AlertDialogView<>(viewModel);
            view.initialize();
            return view;
        }
    }

    private final class DialogTextFieldColumnListCell extends TextFieldColumnListCell<T> {

        private final FontIconView iconView = new FontIconView();

        private DialogTextFieldColumnListCell(StringConverter converter) {
            super(converter);
            addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                if (e.getClickCount() == 2) {
                    var file = getItem();
                    getViewModel().onNavigateDown(file);
                }
            });
            setManualEdit(true);
            setContextMenu(itemContextMenu);
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            getViewModel().onEditCancelled(getItem());
        }

        @Override
        public void commitEdit(T newValue) {
            super.commitEdit(newValue);
            getViewModel().onEditCommitted(newValue);
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            if (item == null || empty) {
                setGraphic(null);
                setText(null);
            } else {
                if (item.getEntryType() != null) {
                    iconView.setIcon(item.getIcon());
                    if (item.isHidden()) {
                        iconView.setOpacity(FileViewConstants.HIDDEN_FILE_OPACITY);
                    } else {
                        iconView.setOpacity(1.0);
                    }
                    setGraphic(iconView);
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

    private final Label extensionFilterLabel = new Label("Files of Type");

    private final ComboBox<ExtensionFilter> extensionFilterComboBox = new ComboBox<>();

    private final GridPane gridPane = new GridPane();

    private final VBox fileBox = new VBox();

    private final TableView<T> fileTableView = new TableView<>();

    private TableColumnManager<T> fileColumnManager = new TableColumnManager<>(fileTableView);

    private FileListView<T> fileListView;

    private final VBox main = new VBox(locationBox, fileBox, gridPane);

    private final ContextMenu containerContextMenu = new ContextMenu();

    private final ContextMenu itemContextMenu = new ContextMenu();

    private final ResultButton cancelButton = new ResultButton(FileChooserDialogButtons.CANCEL, "Cancel");

    private final ResultButton okButton = new ResultButton(FileChooserDialogButtons.OK, "OK");

    public FileChooserDialogView(VM viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new FileChooserDialogView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        var viewModel = getViewModel();
        this.fileTableView.setItems(viewModel.getFiles());
        this.fileTableView.setEditable(true);
        this.fileTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        this.fileTableView.setPlaceholder(new Label(""));
        this.fileTableView.setSortPolicy(tv -> {
            // bottom layer: column comparators (user-defined column rules)
            // middle layer: TableView aggregate comparator (built from sortOrder)
            // top layer: sorting execution hook (applies comparator to items)
            Comparator<T> base = tv.getComparator();
            // note, that the Comparators.directoryFirst is also used in the view model
            Comparator<T> decorated = Comparators.directoryFirst(base);
            FXCollections.sort(tv.getItems(), decorated);
            return true;
        });

        var columnBuilder = new FileColumnBuilder(viewModel.getAppearanceSettings().getRegularFont());
        this.fileColumnManager.registerColumnFactory(FileColumns.NAME, () -> {
            var column = columnBuilder.<T>buildNameColumn();
            column.setEditable(false);
            column.setOnEditCancel(e -> {
                var file = (T) e.getOldValue();
                getViewModel().onEditCancelled(file);
                column.setEditable(false);
            });
            column.setOnEditCommit(e -> {
                var newFile = (T) e.getNewValue(); //from converter
                getViewModel().onEditCommitted(newFile);
                column.setEditable(false);
            });
            return column;
        });
        this.fileColumnManager.registerColumnFactory(FileColumns.SIZE, () -> {
            var column = columnBuilder.<T>buildSizeColumn();
            column.setEditable(false);
            return column;
        });
        this.fileColumnManager.registerColumnFactory(FileColumns.LAST_MODIFIED, () -> {
            var column = columnBuilder.<T>buildLastModifiedColumn();
            column.setEditable(false);
            return column;
        });
        this.fileColumnManager.addColumns(viewModel.getColumns());

        this.fileListView = new FileListView<>(viewModel.getFiles(), new ContextMenu(createRefreshMenuItem()));
        locationLabel.setMinWidth(Region.USE_PREF_SIZE);
        HBox.setHgrow(locationComboBox, Priority.ALWAYS);
        locationComboBox.setMaxWidth(Double.MAX_VALUE);
        locationComboBox.setCellFactory(cb -> {
            var cell = new LocationCell(false);
            cell.setOnMousePressed(e -> {
                getViewModel().onLocationRequested(cell.getItem());
            });
            return cell;
        });
        locationComboBox.setButtonCell(new LocationCell(true));
        locationComboBox.getStyleClass().add("location");
        levelUpButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.SIZE_M);
        levelUpButton.setTooltip(new Tooltip("Up One Level"));
        homeButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.SIZE_M);
        homeButton.setTooltip(new Tooltip("Home"));
        createButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.SIZE_M);
        createButton.setTooltip(new Tooltip("Create New Folder"));
        //always one button selected
        toggleGroup.selectedToggleProperty().addListener((ov, oldToggle, newToggle) -> {
            if (newToggle == null) {
                oldToggle.setSelected(true);
            }
        });
        listButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.SIZE_M);
        listButton.setTooltip(new Tooltip("List"));
        listButton.setToggleGroup(toggleGroup);
        listButton.setSelected(true);
        detailsButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.SIZE_M);
        detailsButton.setTooltip(new Tooltip("Details"));
        detailsButton.setToggleGroup(toggleGroup);
        buttonBox.setSpacing(Spacing.getHorizontalHalf());
        locationBox.setSpacing(Spacing.getHorizontal());
        locationBox.setAlignment(Pos.CENTER_LEFT);

        VBox.setVgrow(fileBox, Priority.ALWAYS);
        itemContextMenu.getItems().addAll(createRenameMenuItem(), createRefreshMenuItem());
        this.fileTableView.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    var file = row.getItem();
                    getViewModel().onNavigateDown(file);
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
        var converter = new FileStringConverter<GenericFile>();
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

        extensionFilterLabel.setMinWidth(Region.USE_PREF_SIZE);
        gridPane.add(extensionFilterLabel, 0, 1);
        GridPane.setHgrow(extensionFilterComboBox, Priority.ALWAYS);
        extensionFilterComboBox.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(extensionFilterComboBox, 1, 1);

        VBox.setVgrow(main, Priority.ALWAYS);
        main.setSpacing(Spacing.getVertical());
        main.getStylesheets().add(FileChooserDialogView.class.getResource("file-dialog.css").toExternalForm());
        getContentBox().getChildren().addAll(main);

        registerButtons(cancelButton, okButton);
        getButtonWidthGroup().add(cancelButton, okButton);
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        locationLabel.textProperty().bind(viewModel.locationCaptionProperty());
        fileNameTextField.textProperty().bind(viewModel.fileNameProperty());
        viewModel.locationWrapper().bind(locationComboBox.getSelectionModel().selectedItemProperty());
        viewModel.extensionFilterWrapper().bind(extensionFilterComboBox.getSelectionModel().selectedItemProperty());
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        this.fileListView.getSelectionModel().selectedIndexProperty().addListener((ov, oldV, newV) -> {
            if (this.listButton.isSelected()) {
                viewModel.selectedFileIndexWrapper().set(newV.intValue());
            }
        });
        this.fileTableView.getSelectionModel().selectedIndexProperty().addListener((ov, oldV, newV) -> {
            if (this.detailsButton.isSelected()) {
                viewModel.selectedFileIndexWrapper().set(newV.intValue());
            }
        });
        ValueUtils.callAndAddListener(this.fileTableView.comparatorProperty(), (ov, oldV, newV) ->
                viewModel.fileComparatorProperty().set(newV));
        this.fileColumnManager.setWidthListener(viewModel::setColumnWidth);
        this.fileColumnManager.setSortTypeListener(viewModel::setColumnSortType);
        this.fileColumnManager.setIndexListener(viewModel::setColumnIndex);
        this.fileColumnManager.setSortIndexListener(viewModel::setColumnSortIndex);

        viewModel.getFiles().addListener((ListChangeListener<T>) change -> updateFilesChanged());
        locationComboBox.setItems(viewModel.getLocations());
        updateLocation(viewModel.getLocation());
        viewModel.locationSource().addListener((location) -> updateLocation(location));
        ValueUtils.callAndAddListener(viewModel.modeProperty(), (ov, oldV, newV) -> updateMode(newV));
        extensionFilterComboBox.setItems(viewModel.getExtensionFilters());
        updateExtensionFilter(viewModel.getExtensionFilter());
        viewModel.extensionFilterSource().addListener((filter) -> updateExtensionFilter(filter));
        viewModel.selectFileSource().addListener((index) -> updateSelectFile(index));
        viewModel.scrollToFileSource().addListener((index) -> updateScrollToFile(index));
        viewModel.editFileSource().addListener((index) -> updateEditFile(index));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var viewModel = getViewModel();
        this.levelUpButton.setOnAction(e -> viewModel.onNavigateUp());
        this.homeButton.setOnAction(e -> viewModel.onNavigateHome());
        this.createButton.setOnAction(e -> viewModel.onNewDirectory());
        this.listButton.setOnAction(e -> viewModel.onList());
        this.detailsButton.setOnAction(e -> viewModel.onDetails());
        // Bubbling handlers (not filters): an in-progress rename's TextField already consumes ENTER/ESCAPE
        // itself (see TextFieldColumnListCell), so these never see the key in that case. Only consumed here
        // when the selected entry is a directory, so ENTER on a file still reaches the dialog's default (OK)
        // button and confirms the selection as before.
        this.fileListView.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                var file = this.fileListView.getSelectionModel().getSelectedItem();
                if (file != null && file.isDirectory()) {
                    e.consume();
                    viewModel.onNavigateDown(file);
                }
            }
        });
        this.fileTableView.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                var file = this.fileTableView.getSelectionModel().getSelectedItem();
                if (file != null && file.isDirectory()) {
                    e.consume();
                    viewModel.onNavigateDown(file);
                }
            }
        });
        //when setOnShowing is used then popup height is calculated incorrectly
        //maybe because OnMousePressed handler is called before OnShowing handler.
        //another reason - update location property only after locations have been populated
        locationComboBox.setOnMousePressed(e -> {
            //update locations only when popup is shown
            viewModel.onLocationsOpened();
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

    protected Label getExtensionFilterLabel() {
        return extensionFilterLabel;
    }

    protected ComboBox<ExtensionFilter> getExtensionFilterComboBox() {
        return extensionFilterComboBox;
    }

    protected FileListView getFileListView() {
        return fileListView;
    }

    private void updateMode(Mode mode) {
        if (mode == Mode.LIST) {
            updateFileBox(this.fileListView);
            this.fileListView.refresh();
            this.fileListView.getSelectionModel().select(fileTableView.getSelectionModel().getSelectedIndex());
            this.listButton.setSelected(true);
            this.detailsButton.setSelected(false);
        } else {
            updateFileBox(this.fileTableView);
            this.fileTableView.getSelectionModel().select(this.fileListView.getSelectionModel().getSelectedIndex());
            this.listButton.setSelected(false);
            this.detailsButton.setSelected(true);
        }
    }

    private void updateFilesChanged() {
        if (this.listButton.isSelected()) {
            this.fileListView.refresh();
        }
    }

    private void updateExtensionFilter(ExtensionFilter filter) {
        extensionFilterComboBox.getSelectionModel().select(filter);
    }

    private void updateLocation(Location location) {
        locationComboBox.getSelectionModel().select(location);
    }

    private void updateSelectFile(int index) {
        if (this.listButton.isSelected()) {
            this.fileListView.getSelectionModel().select(index);
        } else {
            this.fileTableView.getSelectionModel().select(index);
        }
    }

    private void updateScrollToFile(int index) {
        if (listButton.isSelected()) {
            var columnIndex = this.fileListView.resolveColumnIndex(index);
            if (columnIndex >= 0) {
                this.fileListView.scrollToFirstColumn(columnIndex);
            }
        } else {
            this.fileTableView.scrollTo(index);
        }
    }

    private void updateEditFile(int index) {
        if (listButton.isSelected()) {
            this.fileListView.getSelectionModel().select(index);
            this.fileListView.edit(index);
        } else {
            this.fileTableView.getSelectionModel().select(index);
            var column = fileColumnManager.getColumnsByName().get(FileColumns.NAME);
            column.setEditable(true);
            this.fileTableView.edit(index, (TableColumn<T, Object>) column);
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
                getViewModel().onRename(index);
            }
        });
        return renameItem;
    }

    private MenuItem createRefreshMenuItem() {
        var menuItem = new MenuItem("Refresh");
        menuItem.setOnAction(e -> getViewModel().onRefresh());
        return menuItem;
    }
}
