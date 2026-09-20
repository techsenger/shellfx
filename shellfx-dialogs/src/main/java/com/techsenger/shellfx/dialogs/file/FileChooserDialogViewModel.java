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

import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.shellfx.core.CloseCheckResult;
import com.techsenger.shellfx.core.ClosePreparationResult;
import com.techsenger.shellfx.core.dialog.AbstractDialogViewModel;
import com.techsenger.shellfx.core.settings.AppearanceSettings;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogType;
import static com.techsenger.shellfx.dialogs.file.FileChooserType.OPEN;
import static com.techsenger.shellfx.dialogs.file.FileChooserType.SAVE_AS;
import com.techsenger.shellfx.dialogs.style.DialogIcons;
import com.techsenger.shellfx.material.RequestSetter;
import com.techsenger.shellfx.material.button.ResultButtonName;
import com.techsenger.shellfx.material.icon.FontIcon;
import com.techsenger.shellfx.material.table.TableColumnInfo;
import com.techsenger.shellfx.material.table.TableColumnName;
import com.techsenger.shellfx.material.table.TableHistory;
import com.techsenger.shellfx.storage.Comparators;
import com.techsenger.shellfx.storage.FileColumns;
import com.techsenger.shellfx.storage.FileEntryType;
import com.techsenger.shellfx.storage.FileStorage;
import com.techsenger.shellfx.storage.FileStorageUtils;
import com.techsenger.shellfx.storage.GenericFile;
import com.techsenger.shellfx.storage.UriUtils;
import com.techsenger.shellfx.storage.style.StorageIcons;
import com.techsenger.toolkit.core.file.FileUtils;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class FileChooserDialogViewModel<C extends FileChooserDialogComposer, T extends GenericFile>
        extends AbstractDialogViewModel<C> implements FullFileChooserDialogPort<T> {

    private static final Logger logger = LoggerFactory.getLogger(FileChooserDialogViewModel.class);

    private enum EditType {
        NEW_DIRECTORY, RENAME_FILE
    }

    private final ObservableList<T> modifiableFiles = FXCollections.observableArrayList();

    private final ObservableList<T> files = FXCollections.unmodifiableObservableList(modifiableFiles);

    private final ObservableSource<Integer> selectFileSource = new SimpleObservableSource<>();

    private final ObservableSource<Integer> scrollToFileSource = new SimpleObservableSource<>();

    private final ObservableSource<Integer> editFileSource = new SimpleObservableSource<>();

    private final ObservableList<Location> modifiableLocations = FXCollections.observableArrayList();

    private final ObservableList<Location> locations = FXCollections.unmodifiableObservableList(modifiableLocations);

    private final ReadOnlyObjectWrapper<Location> location = new ReadOnlyObjectWrapper<>();

    private final ObservableSource<Location> locationSource = new SimpleObservableSource<>();

    private final ReadOnlyObjectWrapper<Mode> mode = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<ExtensionFilter> extensionFilter = new ReadOnlyObjectWrapper<>();

    private final ObservableSource<ExtensionFilter> extensionFilterSource = new SimpleObservableSource<>();

    private final StringProperty fileName = new SimpleStringProperty();

    private final ObservableList<ExtensionFilter> modifiableExtensionFilters = FXCollections.observableArrayList();

    private final ObservableList<ExtensionFilter> extensionFilters =
            FXCollections.unmodifiableObservableList(modifiableExtensionFilters);

    private final StringProperty locationCaption = new SimpleStringProperty();

    private final ObservableMap<TableColumnName, TableColumnInfo> columns = FXCollections.observableHashMap();

    private final ReadOnlyObjectWrapper<T> file = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyIntegerWrapper selectedFileIndex = new ReadOnlyIntegerWrapper(-1);

    private final FileChooserType chooserType;

    private final URI initialDirectory;

    private final String initialFileName;

    private final ReadOnlyObjectWrapper<T> directory = new ReadOnlyObjectWrapper<>();

    private boolean locationsUpdated;

    private final List<? extends FileStorage<T>> storages;

    private final ObjectProperty<Comparator<T>> fileComparator = new SimpleObjectProperty<>();

    private T resultFile;

    private EditType editType;

    private Comparator<T> sortComparator;

    public FileChooserDialogViewModel(FileChooserDialogParams<T> params) {
        super(params);
        this.chooserType = params.getChooserType();
        this.storages = params.getStorages();
        this.initialDirectory = params.getInitialDirectory();
        this.initialFileName = params.getInitialFileName();
        this.fileName.set(initialFileName);
        fileComparator.addListener((obs, oldV, newV) -> this.sortComparator =
                Comparators.directoryFirst(newV));
        selectedFileIndex.addListener((obs, oldV, newV) -> {
            var f = computeFile();
            this.file.set(f);
            if (f != null && !f.isDirectory()) {
                setFileName(f.getName());
            } else {
                setFileName(null);
            }
        });
        extensionFilter.addListener((obs, oldV, newV) -> updateFiles(getFile()));
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileChooserType getChooserType() {
        return chooserType;
    }

    @Override
    public URI getInitialDirectory() {
        return initialDirectory;
    }

    @Override
    public String getInitialFileName() {
        return initialFileName;
    }

    @Override
    public T getDirectory() {
        return directory.get();
    }

    @Override
    public ReadOnlyObjectProperty<T> directoryProperty() {
        return directory.getReadOnlyProperty();
    }

    @Override
    public ObservableList<Location> getLocations() {
        return locations;
    }

    @Override
    public void setLocations(List<Location> locations) {
        modifiableLocations.setAll(locations);
    }

    @Override
    public Location getLocation() {
        return location.get();
    }

    @Override
    @RequestSetter
    public void setLocation(Location location) {
        locationSource.next(location);
    }

    @Override
    public ReadOnlyObjectProperty<Location> locationProperty() {
        return location.getReadOnlyProperty();
    }

    @Override
    public Mode getMode() {
        return mode.get();
    }

    @Override
    public void setMode(Mode mode) {
        this.mode.set(mode);
        if (this.selectedFileIndex.get() >= 0) {
            scrollToFileSource.next(selectedFileIndex.get());
        }
    }

    @Override
    public ReadOnlyObjectProperty<Mode> modeProperty() {
        return mode.getReadOnlyProperty();
    }

    @Override
    public AppearanceSettings getAppearanceSettings() {
        return super.getAppearanceSettings();
    }

    @Override
    public @Unmodifiable ObservableList<T> getFiles() {
        return files;
    }

    @Override
    public T getFile() {
        return file.get();
    }

    @Override
    public ReadOnlyObjectProperty<T> fileProperty() {
        return file.getReadOnlyProperty();
    }

    @Override
    public int getFileIndex() {
        return selectedFileIndex.get();
    }

    @Override
    public ReadOnlyIntegerProperty fileIndexProperty() {
        return selectedFileIndex.getReadOnlyProperty();
    }

    @Override
    public ExtensionFilter getExtensionFilter() {
        return extensionFilter.get();
    }

    @Override
    @RequestSetter
    public void setExtensionFilter(ExtensionFilter extensionFilter) {
        extensionFilterSource.next(extensionFilter);
    }

    @Override
    public ReadOnlyObjectProperty<ExtensionFilter> extensionFilterProperty() {
        return extensionFilter.getReadOnlyProperty();
    }

    @Override
    public String getFileName() {
        return fileName.get();
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    @Override
    public StringProperty fileNameProperty() {
        return fileName;
    }

    @Override
    public @Unmodifiable ObservableList<ExtensionFilter> getExtensionFilters() {
        return extensionFilters;
    }

    @Override
    public void setExtensionFilters(List<ExtensionFilter> extensionFilters) {
        modifiableExtensionFilters.setAll(extensionFilters);
        updateFiles(getFile());
    }

    @Override
    public T getResult() {
        return this.resultFile;
    }

    @Override
    public String getLocationCaption() {
        return locationCaption.get();
    }

    @Override
    public void setLocationCaption(String locationCaption) {
        this.locationCaption.set(locationCaption);
    }

    @Override
    public StringProperty locationCaptionProperty() {
        return locationCaption;
    }

    @Override
    protected FileChooserDialogHistory getHistory() {
        return (FileChooserDialogHistory) super.getHistory();
    }

    @Override
    protected void onResult(ResultButtonName name) {
        if (name == FileChooserDialogButtons.OK) {
            this.resultFile = getResultFile();
            if (this.resultFile == null) {
                return;
            }
        }
        super.onResult(name);
    }

    @Override
    protected void applyPersistentState() {
        super.applyPersistentState();
        setWidth(800);
        setHeight(500);
        createInitialColumns();
        setMode(Mode.LIST);
    }

    @Override
    protected void savePersistentState() {
        super.savePersistentState();
        var history = getHistory();
        history.setMode(mode.get());
        var tableHistory = new TableHistory(this.columns.values().stream().toList());
        history.setTable(tableHistory);
    }

    @Override
    protected void restorePersistentState() {
        super.restorePersistentState();
        var history = getHistory();
        setMode(history.getMode());
        // put() in index order: the view builds/appends the real column on each map change, so this order
        // also decides the columns' left-to-right order in the table
        history.getTable().getColumns().stream()
                .sorted(Comparator.comparingInt(TableColumnInfo::getIndex))
                .forEach((c) -> {
                    columns.put(c.getName(), c);
                });
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        switch (chooserType) {
            case OPEN -> {
                setTitle("Open");
                setIcon(DialogIcons.OPEN);
                setLocationCaption("Look In");
            }
            case SAVE_AS -> {
                setTitle("Save As");
                setIcon(DialogIcons.SAVE_AS);
                setLocationCaption("Save In");
            }
            default -> throw new AssertionError();
        }
        if (this.initialDirectory != null) {
            var s = FileStorageUtils.findByUri(storages, initialDirectory);
            if (s.isPresent()) {
                this.directory.set(resolveDirectory(s.get(), this.initialDirectory));
            }
        }
        updateFiles(null);
        setRightButtons(FileChooserDialogButtons.CANCEL, FileChooserDialogButtons.OK);
        setButtonDefault(FileChooserDialogButtons.OK, true);
        setMinWidth(600);
        setMinHeight(400);
    }

    protected FileStorage getStorage() {
        return currentStorage();
    }

    protected ObservableList<T> getModifiableFiles() {
        return modifiableFiles;
    }

    protected void onLocationRequested(Location location) {
        navigateTo(location.getStorage(), location.getUri());
    }

    protected void onNavigateDown(GenericFile file) {
        if (file.isDirectory()) {
            navigateTo(file.getStorage(), file.getUri());
            scrollToFileSource.next(0);
        }
    }

    protected void onNavigateUp() {
        var storage = currentStorage();
        if (storage == null) {
            return;
        }
        var parentUri = UriUtils.getParentUri(storage.getUri(), directoryUri());
        if (parentUri == null) {
            return;
        }
        var currentDirectory = this.directory.get();
        this.directory.set(resolveDirectory(storage, parentUri));
        updateFiles(currentDirectory);
    }

    protected void onNavigateHome() {
        var file = FileStorageUtils.getHome(storages);
        if (file.isPresent()) {
            onNavigateDown(file.get());
        }
    }

    protected void onNewDirectory() {
        if (this.editType != null) {
            return;
        }
        // creating a fake directory
        var file = currentStorage().createVirtual(FileEntryType.DIRECTORY, "New Folder", null);
        modifiableFiles.add(0, file);
        scrollToFileSource.next(0);
        editFileSource.next(0);
        this.editType = EditType.NEW_DIRECTORY;
    }

    protected void onRename(int fileIndex) {
        if (this.editType != null) {
            return;
        }
        this.editType = EditType.RENAME_FILE;
        editFileSource.next(fileIndex);
    }

    protected void onEditCommitted(T file) {
        switch (editType) {
            case NEW_DIRECTORY -> {
                var dirUri = UriUtils.resolvePath(directoryUri(), file.getName(), true);
                try {
                    currentStorage().createDirectory(dirUri);
                    updateFiles(file);
                } catch (Exception ex) {
                    logger.error("{} Error creating new directory at {}", getDescriptor().getLogPrefix(), dirUri, ex);
                }
            }
            case RENAME_FILE -> {
                try {
                    var storage = currentStorage();
                    if (storage != null) {
                        storage.renameFile(file.getUri(), file.getName());
                        updateFiles(file);
                    }
                } catch (Exception ex) {
                    logger.error("{} Error renaming file at {} to {}", getDescriptor().getLogPrefix(), file.getUri(),
                            file.getName(), ex);
                }
            }
            default -> throw new AssertionError();
        }
        this.editType = null;
    }

    protected void onEditCancelled(GenericFile file) {
        switch (editType) {
            case NEW_DIRECTORY -> modifiableFiles.remove(0);
            case RENAME_FILE -> {
                // do nothing
            }
            default -> throw new AssertionError();
        }
        this.editType = null;
    }

    protected void onList() {
        setMode(Mode.LIST);
    }

    protected void onDetails() {
        setMode(Mode.DETAILS);
    }

    protected void onRefresh() {
        updateFiles(getFile());
    }

    protected void setColumnWidth(TableColumnName name, double width) {
        var info = this.columns.get(name);
        info.setWidth(width);
    }

    protected void setColumnSortType(TableColumnName name, TableColumn.SortType sortType) {
        var info = this.columns.get(name);
        info.setSortType(sortType);
    }

    protected void setColumnIndex(TableColumnName name, int index) {
        var info = this.columns.get(name);
        info.setIndex(index);
    }

    protected void setColumnSortIndex(TableColumnName name, Integer index) {
        var info = this.columns.get(name);
        info.setSortIndex(index);
    }

    protected Comparator<T> getFileComparator() {
        return sortComparator;
    }

    ObservableMap<TableColumnName, TableColumnInfo> getColumns() {
        return columns;
    }

    ObjectProperty<Comparator<T>> fileComparatorProperty() {
        return fileComparator;
    }

    /**
     * Framework contract: written directly by the View to report the table/list's actual selected index,
     * intended exclusively for {@code FileChooserDialogFxView}. Direct invocation by user code results in
     * undefined behavior.
     */
    ReadOnlyIntegerWrapper selectedFileIndexWrapper() {
        return selectedFileIndex;
    }

    /**
     * Framework contract: written directly by the View to report the location combo box's actual selected item,
     * intended exclusively for {@code FileChooserDialogFxView}. Direct invocation by user code results in
     * undefined behavior.
     */
    ReadOnlyObjectWrapper<Location> locationWrapper() {
        return location;
    }

    ObservableSource<Location> locationSource() {
        return locationSource;
    }

    /**
     * Framework contract: written directly by the View to report the filter combo box's actual selected item,
     * intended exclusively for {@code FileChooserDialogFxView}. Direct invocation by user code results in
     * undefined behavior.
     */
    ReadOnlyObjectWrapper<ExtensionFilter> extensionFilterWrapper() {
        return extensionFilter;
    }

    ObservableSource<ExtensionFilter> extensionFilterSource() {
        return extensionFilterSource;
    }

    ObservableSource<Integer> selectFileSource() {
        return selectFileSource;
    }

    ObservableSource<Integer> scrollToFileSource() {
        return scrollToFileSource;
    }

    ObservableSource<Integer> editFileSource() {
        return editFileSource;
    }

    /**
     * Locations updated only when user clicks enter to show the list. At all other times, the list contains only one
     * element with the current directory.
     */
    void onLocationsOpened() {
        if (locationsUpdated) {
            return;
        }
        Location selectedLocation = null;
        List<Location> locations = new ArrayList<>();
        for (var storage : storages) {
            var storageLocation = createLocation(storage);
            locations.add(storageLocation);
            if (currentStorage() == storage) {
                var segments = UriUtils.getPathSegments(storage.getUri(), directoryUri());
                if (segments.isEmpty()) {
                    selectedLocation = storageLocation;
                }
                var previousUri = storage.getUri();
                for (var i = 0; i < segments.size(); i++) {
                    var segment = segments.get(i);
                    var segmentUri = UriUtils.resolvePath(previousUri, segment, true);
                    var directoryLocation = new Location(
                            StorageIcons.FOLDER,
                            segment,
                            i + 1,
                            storage,
                            segmentUri);
                    locations.add(directoryLocation);
                    if (i + 1 == segments.size()) {
                        selectedLocation = directoryLocation;
                    }
                    previousUri = segmentUri;
                }
            }
        }
        setLocations(locations);
        setLocation(selectedLocation);
        this.locationsUpdated = true;
    }

    private void navigateTo(FileStorage storage, URI uri) {
        FileStorage<T> typedStorage = storage;
        this.directory.set(resolveDirectory(typedStorage, uri));
        updateFiles(null);
    }

    private void updateFiles(T selectedFile) {
        setFiles(Collections.emptyList());
        boolean defaultUsed = false;
        if (this.directory.get() == null) {
            setDefaultDirectory();
            defaultUsed = true;
            selectedFile = null;
        }
        List<T> storageFiles = getFilesFromStorage();
        if (storageFiles == null && !defaultUsed) {
            //the second attempt
            setDefaultDirectory();
            storageFiles = getFilesFromStorage();
            selectedFile = null;
        }
        if (storageFiles == null) {
            this.directory.set(null);
            return;
        }
        updateLocation();
        var extFilter = getExtensionFilter();
        List<T> filteredFiles = null;
        if (extFilter != null && !extFilter.matchesAllFiles()) {
            filteredFiles = new ArrayList<>();
            for (var f : storageFiles) {
                if (f.isDirectory()) {
                    filteredFiles.add(f);
                } else {
                    if (extFilter.matches(f.getName())) {
                        filteredFiles.add(f);
                    }
                }
            }
        }
        var files = storageFiles;
        if (filteredFiles != null) {
            files = filteredFiles;
        }
        files.sort(this.sortComparator);
        setFiles(files);
        //only after sorting we can find the selected file index
        var foundIndex = -1;
        if (selectedFile != null) {
            for (int i = 0; i < files.size(); i++) {
                var file = files.get(i);
                if (file.getEntryType() == selectedFile.getEntryType() && file.getName() != null
                        && file.getName().equals(selectedFile.getName())) {
                    foundIndex = i;
                    break;
                }
            }
        }
        this.selectedFileIndex.set(foundIndex);
        if (foundIndex != -1) {
            selectFileSource.next(foundIndex);
            scrollToFileSource.next(foundIndex);
        }
    }

    private void setFiles(List<T> files) {
        modifiableFiles.setAll(files);
    }

    private T computeFile() {
        if (this.selectedFileIndex.get() >= 0) {
            return this.files.get(selectedFileIndex.get());
        } else {
            return null;
        }
    }

    private void showWarning(String text) {
        var params = new AlertDialogParams(getWindowType(), getAppearanceSettings(), AlertDialogType.WARNING);
        getComposer().addAlertDialog(params, text);
    }

    private void setDefaultDirectory() {
        var s = FileStorageUtils.findLocal(storages).stream().findFirst();
        this.directory.set(s.isPresent() ? s.get().getRootDirectory() : null);
    }

    private T resolveDirectory(FileStorage<T> storage, URI uri) {
        if (uri.equals(storage.getUri())) {
            return storage.getRootDirectory();
        }
        try {
            return storage.getFile(uri);
        } catch (Exception ex) {
            logger.error("{} Error getting directory at {}", getDescriptor().getLogPrefix(), uri, ex);
            return null;
        }
    }

    private URI directoryUri() {
        var d = this.directory.get();
        return d != null ? d.getUri() : null;
    }

    private FileStorage<T> currentStorage() {
        var d = this.directory.get();
        if (d == null) {
            return null;
        }
        FileStorage raw = d.getStorage();
        return raw;
    }

    private List<T> getFilesFromStorage() {
        try {
            return currentStorage().getFiles(directoryUri());
        } catch (Exception ex) {
            logger.error("{} Error getting files at {}", getDescriptor().getLogPrefix(), directoryUri(), ex);
        }
        return null;
    }

    private void updateLocation() {
        var storage = currentStorage();
        var segments = UriUtils.getPathSegments(storage.getUri(), directoryUri());
        Location location = null;
        if (segments.isEmpty()) {
            location = createLocation(storage);
        } else {
            location = new Location(
                    StorageIcons.FOLDER,
                    segments.get(segments.size() - 1),
                    segments.size(),
                    storage,
                    directoryUri());
        }
        // The created location must be added to the locations list.
        // We add only one location at a time - the currently selected one.
        // If the user clicks the combobox, all locations will be updated.
        setLocations(List.of(location));
        setLocation(location);
        this.locationsUpdated = false;
    }

    private Location createLocation(FileStorage storage) {
        FontIcon<?> icon = storage.getIcon();
        var location = new Location(
                icon,
                storage.getDisplayName(),
                0,
                storage,
                storage.getUri());
        return location;
    }

    private void createInitialColumns() {
        // put() one at a time, in index order: the view builds/appends the real column on each map change,
        // so this order also decides the columns' left-to-right order in the table
        var nameColumn = new TableColumnInfo(FileColumns.NAME);
        nameColumn.setIndex(0);
        nameColumn.setSortIndex(0);
        nameColumn.setSortType(TableColumn.SortType.ASCENDING);
        columns.put(nameColumn.getName(), nameColumn);

        var sizeColumn = new TableColumnInfo(FileColumns.SIZE);
        sizeColumn.setIndex(1);
        columns.put(sizeColumn.getName(), sizeColumn);

        var modifiedColumn = new TableColumnInfo(FileColumns.LAST_MODIFIED);
        modifiedColumn.setIndex(2);
        columns.put(modifiedColumn.getName(), modifiedColumn);
    }

    private @Nullable T getResultFile() {
        if (getDirectory() == null) {
            showWarning("Storage or/and directory are not selected.");
            return null;
        }
        var fileName = getFileName();
        if (fileName == null) {
            showWarning("File name is not specified.");
            return null;
        }
        fileName = fileName.trim();
        if (fileName.isEmpty()) {
            showWarning("File name is not specified.");
            return null;
        }
        //there is a file with such name
        for (var file : getFiles()) {
            if (!file.isDirectory() && file.getName().equals(fileName)) {
                return file;
            }
        }
        //there is no file with such name
        if (this.chooserType == FileChooserType.SAVE_AS) {
            if (!getExtensionFilters().isEmpty() && getExtensionFilter() != null
                    && !getExtensionFilter().matchesAllFiles()) {
                var extension = FileUtils.getExtension(fileName);
                var filter = getExtensionFilter();
                if (extension != null) {
                    if (!filter.matches(fileName)) {
                        showWarning("The file '" + fileName + "' does not satisfy the filter criteria.");
                        return null;
                    }
                } else {
                    extension = filter.getPureExtensions().get(0);
                    fileName = fileName + "." + extension;
                }
            }
        } else {
            showWarning("The file '" + fileName + "' does not exist.");
            return null;
        }
        URI fileUri = UriUtils.resolvePath(getDirectory().getUri(), fileName, false);
        var file = currentStorage().createVirtual(null, fileName, fileUri);
        return file;
    }
}
