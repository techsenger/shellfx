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

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.shellfx.core.CloseCheckResult;
import com.techsenger.shellfx.core.ClosePreparationResult;
import com.techsenger.shellfx.core.dialog.AbstractDialogPresenter;
import com.techsenger.shellfx.dialogs.DialogComponents;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogType;
import static com.techsenger.shellfx.dialogs.file.FileChooserType.OPEN;
import static com.techsenger.shellfx.dialogs.file.FileChooserType.SAVE_AS;
import com.techsenger.shellfx.dialogs.style.DialogIcons;
import com.techsenger.shellfx.material.button.ResultButtonName;
import com.techsenger.shellfx.material.icon.StyleFontIcon;
import com.techsenger.shellfx.material.table.TableColumnInfo;
import com.techsenger.shellfx.material.table.TableColumnName;
import com.techsenger.shellfx.material.table.TableHistory;
import com.techsenger.shellfx.storage.Comparators;
import com.techsenger.shellfx.storage.FileColumns;
import com.techsenger.shellfx.storage.FileStorage;
import static com.techsenger.shellfx.storage.FileStorageType.BASE;
import static com.techsenger.shellfx.storage.FileStorageType.NETWORK;
import static com.techsenger.shellfx.storage.FileStorageType.OPTICAL;
import com.techsenger.shellfx.storage.FileStorageUtils;
import com.techsenger.shellfx.storage.FileType;
import com.techsenger.shellfx.storage.GenericFile;
import com.techsenger.shellfx.storage.UriUtils;
import com.techsenger.toolkit.core.file.FileUtils;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.scene.control.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class FileChooserDialogPresenter<V extends FileChooserDialogView>
        extends AbstractDialogPresenter<V> implements FileChooserDialogPort {

    private static final Logger logger = LoggerFactory.getLogger(FileChooserDialogPresenter.class);

    private enum EditType {
        NEW_DIRECTORY, RENAME_FILE
    }

    private List<Location> locations;

    private Location location;

    private boolean listSelected;

    private boolean detailsSelected;

    private List<GenericFile> files;

    private int selectedFileIndex;

    private ExtensionFilter extensionFilter;

    private String fileName;

    private List<ExtensionFilter> extensionFilters;

    private final FileChooserType chooserType;

    private final URI initialDirectory;

    private final String initialFileName;

    /**
     * The current location is represented by {@link #storage} and {@link #directory}. A {@link GenericFile} cannot be
     * used because when the user is at the storage root, there is no current directory.
     */
    private FileStorage storage;

    private URI directory;

    private boolean locationsUpdated;

    private List<FileStorage> storages;

    private GenericFile resultFile;

    private EditType editType;

    private final Map<TableColumnName, TableColumnInfo> columns = new HashMap<>();

    private Comparator<GenericFile> fileComparator;

    private String locationCaption;

    public FileChooserDialogPresenter(V view, FileChooserDialogParams params) {
        super(view, params);
        this.chooserType = params.getChooserType();
        this.storages = params.getStorages();
        this.initialDirectory = params.getInitialDirectory();
        this.directory = initialDirectory;
        if (this.directory != null) {
            var s = FileStorageUtils.findByUri(storages, directory);
            if (s.isEmpty()) {
                this.storage = null;
            } else {
                this.storage = s.get();
            }
        }
        this.initialFileName = params.getInitialFileName();
        this.fileName = initialFileName;
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
    public void onResult(ResultButtonName name) {
        if (name == FileChooserDialogButtons.OK) {
            this.resultFile = getResultFile();
            if (this.resultFile == null) {
                return;
            }
        }
        super.onResult(name);
    }

    @Override
    public List<Location> getLocations() {
        return locations;
    }

    @Override
    public void setLocations(List<Location> locations) {
        this.locations = locations;
        getView().setLocations(locations);
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void setLocation(Location location) {
        this.location = location;
        getView().setLocation(location);
    }

    @Override
    public boolean isListSelected() {
        return listSelected;
    }

    @Override
    public void setListSelected(boolean listSelected) {
        this.listSelected = listSelected;
        getView().setListSelected(listSelected);
    }

    @Override
    public boolean isDetailsSelected() {
        return detailsSelected;
    }

    @Override
    public void setDetailsSelected(boolean detailsSelected) {
        this.detailsSelected = detailsSelected;
        getView().setDetailsSelected(detailsSelected);
    }

    @Override
    public List<GenericFile> getFiles() {
        return files;
    }

    @Override
    public void setFiles(List<GenericFile> files) {
        this.files = files;
        getView().setFiles(files);
    }

    @Override
    public GenericFile getSelectedFile() {
        if (this.selectedFileIndex >= 0) {
            return this.files.get(selectedFileIndex);
        } else {
            return null;
        }
    }

    @Override
    public int getSelectedFileIndex() {
        return selectedFileIndex;
    }

    @Override
    public ExtensionFilter getExtensionFilter() {
        return extensionFilter;
    }

    @Override
    public void setExtensionFilter(ExtensionFilter extensionFilter) {
        this.extensionFilter = extensionFilter;
        getView().setExtensionFilter(extensionFilter);
        updateFiles(getSelectedFile());
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
        getView().setFileName(fileName);
    }

    @Override
    public List<ExtensionFilter> getExtensionFilters() {
        return extensionFilters;
    }

    @Override
    public void setExtensionFilters(List<ExtensionFilter> extensionFilters) {
        this.extensionFilters = extensionFilters;
        getView().setExtensionFilters(extensionFilters);
        updateFiles(getSelectedFile());
    }

    @Override
    public GenericFile getResult() {
        return this.resultFile;
    }

    @Override
    public String getLocationCaption() {
        return locationCaption;
    }

    @Override
    public void setLocationCaption(String locationCaption) {
        if (Objects.equals(this.locationCaption, locationCaption)) {
            return;
        }
        this.locationCaption = locationCaption;
        getView().setLocationCaption(locationCaption);
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(DialogComponents.FILE_CHOOSER_DIALOG);
    }

    @Override
    protected FileChooserDialogHistory getHistory() {
        return (FileChooserDialogHistory) super.getHistory();
    }

    @Override
    protected void preInitialize() {
        super.preInitialize();
        getView().setAppearanceSettings(getAppearanceSettings());
    }

    @Override
    protected void applyAppearance() {
        super.applyAppearance();
        setWidth(800);
        setHeight(500);
        createInitialColumns();
        getView().addColumns(columns);
    }

    @Override
    protected void saveAppearance() {
        super.saveAppearance();
        var history = getHistory();
        history.setListSelected(isListSelected());
        history.setDetailsSelected(isDetailsSelected());
        var tableHistory = new TableHistory(this.columns.values().stream().toList());
        history.setTable(tableHistory);
    }

    @Override
    protected void restoreAppearance() {
        super.restoreAppearance();
        var history = getHistory();
        setListSelected(history.isListSelected());
        setDetailsSelected(history.isDetailsSelected());
        for (var c : history.getTable().getColumns()) {
            this.columns.put(c.getName(), c);
        }
        getView().addColumns(this.columns);
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
        updateFiles(null);
        setRightButtons(FileChooserDialogButtons.CANCEL, FileChooserDialogButtons.OK);
        setButtonDefault(FileChooserDialogButtons.OK, true);
        setMinWidth(600);
        setMinHeight(400);
    }

    protected URI getDirectory() {
        return directory;
    }

    protected FileStorage getStorage() {
        return storage;
    }

    protected void onLocationRequested(Location location) {
        navigateTo(location.getStorage(), location.getUri());
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
            if (this.storage == storage && this.directory != null) {
                var segments = UriUtils.getPathSegments(storage.getRootUri(), directory);
                if (segments.isEmpty()) {
                    selectedLocation = storageLocation;
                }
                var previousUri = storage.getRootUri();
                for (var i = 0; i < segments.size(); i++) {
                    var segment = segments.get(i);
                    var segmentUri = UriUtils.resolvePath(previousUri, segment);
                    var directoryLocation = new Location(
                            DialogIcons.DIRECTORY,
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

    protected void onNavigateDown(GenericFile file) {
        if (file.isDirectory()) {
            navigateTo(file.getStorage(), file.getUri());
            getView().scrollToFile(0);
        }
    }

    protected void onNavigateUp() {
        if (storage == null || directory == null) {
            return;
        }
        var parentUri = UriUtils.getParentUri(storage.getRootUri(), directory);
        if (parentUri == null) {
            return;
        }
        this.directory = parentUri;
        updateFiles(null);
    }

    protected void onNavigateHome() {
        var file = GenericFile.getHome(storages);
        if (file != null) {
            onNavigateDown(file);
        }
    }

    protected void onNewDirectory() {
        if (this.editType != null) {
            return;
        }
        // creating a fake directory
        var builder = new GenericFile.Builder();
        builder.name("New Folder");
        builder.storage(storage);
        builder.type(FileType.DIRECTORY);
        var file = builder.build();
        getView().addFile(0, file);
        getView().scrollToFile(0);
        getView().editFile(0);
        this.editType = EditType.NEW_DIRECTORY;
    }

    protected void onRename(int fileIndex) {
        if (this.editType != null) {
            return;
        }
        this.editType = EditType.RENAME_FILE;
        getView().editFile(fileIndex);
    }

    protected void onEditCommitted(GenericFile file) {
        switch (editType) {
            case NEW_DIRECTORY -> {
                var dirUri = UriUtils.resolvePath(directory, file.getName());
                try {
                    this.storage.createDirectory(dirUri);
                    updateFiles(file);
                } catch (Exception ex) {
                    logger.error("{} Error creating new directory at {}", getDescriptor().getLogPrefix(), dirUri, ex);
                }
            }
            case RENAME_FILE -> {
                try {
                    if (this.storage != null) {
                        this.storage.renameFile(file.getUri(), file.getName());
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
            case NEW_DIRECTORY -> {
                getView().removeFile(0);
            }
            case RENAME_FILE -> {
                // do nothing
            }
            default -> throw new AssertionError();
        }
        this.editType = null;
    }

    protected void onList(boolean selected) {
        setListSelected(selected);
        if (this.selectedFileIndex >= 0) {
            getView().scrollToFile(selectedFileIndex);
        }
    }

    protected void onDetails(boolean selected) {
        setDetailsSelected(selected);
        if (this.selectedFileIndex >= 0) {
            getView().scrollToFile(selectedFileIndex);
        }
    }

    protected void onFileSelected(int index) {
        this.selectedFileIndex = index;
        var file = getSelectedFile();
        if (file != null && !file.isDirectory()) {
            setFileName(file.getName());
        } else {
            setFileName(null);
        }
    }

    protected void onRefresh() {
        updateFiles(getSelectedFile());
    }

    protected void onFilterSelected(ExtensionFilter filter) {
        this.extensionFilter = filter;
        updateFiles(null);
    }

    protected void onColumnWidthChanged(TableColumnName name, double width) {
        var info = this.columns.get(name);
        info.setWidth(width);
    }

    protected void onColumnSortTypeChanged(TableColumnName name, TableColumn.SortType sortType) {
        var info = this.columns.get(name);
        info.setSortType(sortType);
    }

    protected void onColumnIndexChanged(TableColumnName name, int index) {
        var info = this.columns.get(name);
        info.setIndex(index);
    }

    protected void onColumnSortIndexChanged(TableColumnName name, Integer index) {
        var info = this.columns.get(name);
        info.setSortIndex(index);
    }

    protected void onFileComparatorChanged(Comparator<GenericFile> comparator) {
        this.fileComparator = Comparators.directoryFirst(comparator);
    }

    protected Comparator<GenericFile> getFileComparator() {
        return fileComparator;
    }

    private void navigateTo(FileStorage storage, URI uri) {
        this.storage = storage;
        this.directory = uri;
        updateFiles(null);
    }

    private void updateFiles(GenericFile selectedFile) {
        setFiles(Collections.emptyList());
        boolean defaultUsed = false;
        if (this.storage == null || this.directory == null) {
            setDefaultStorageAndDirectory();
            defaultUsed = true;
            selectedFile = null;
        }
        List<GenericFile> storageFiles = getFilesFromStorage();
        if (storageFiles == null && !defaultUsed) {
            //the second attempt
            setDefaultStorageAndDirectory();
            storageFiles = getFilesFromStorage();
            selectedFile = null;
        }
        if (storageFiles == null) {
            this.storage = null;
            this.directory = null;
            return;
        }
        updateLocation();
        var extFilter = getExtensionFilter();
        List<GenericFile> filteredFiles = null;
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
        files.sort(this.fileComparator);
        setFiles(files);
        //only after sorting we can find the selected file index
        this.selectedFileIndex = -1;
        if (selectedFile != null) {
            for (int i = 0; i < files.size(); i++) {
                var file = files.get(i);
                if (file.getType() == selectedFile.getType() && file.getName() != null
                        && file.getName().equals(selectedFile.getName())) {
                    selectedFileIndex = i;
                    break;
                }
            }
            if (selectedFileIndex != -1) {
                getView().selectFile(selectedFileIndex);
                getView().scrollToFile(selectedFileIndex);
            }
        }
    }

    private void showWarning(String text) {
        var params = new AlertDialogParams(getWindowType(), getAppearanceSettings(), AlertDialogType.WARNING);
        getView().getComposer().addAlertDialog(params, text);
    }

    private void setDefaultStorageAndDirectory() {
        var s = FileStorageUtils.findPrimary(storages);
        if (s.isEmpty()) {
            this.storage = null;
        } else {
            this.storage = s.get();
        }
        if (this.storage != null) {
            this.directory = this.storage.getRootUri();
        }
    }

    private List<GenericFile> getFilesFromStorage() {
        try {
            return this.storage.getFiles(directory);
        } catch (Exception ex) {
            logger.error("{} Error getting files at {}", getDescriptor().getLogPrefix(), this.directory, ex);
        }
        return null;
    }

    private void updateLocation() {
        var segments = UriUtils.getPathSegments(this.storage.getRootUri(), directory);
        Location location = null;
        if (segments.isEmpty()) {
            location = createLocation(storage);
        } else {
            location = new Location(
                    DialogIcons.DIRECTORY,
                    segments.get(segments.size() - 1),
                    segments.size(),
                    storage,
                    directory);
        }
        // The created location must be added to the locations list.
        // We add only one location at a time - the currently selected one.
        // If the user clicks the combobox, all locations will be updated.
        setLocations(List.of(location));
        setLocation(location);
        this.locationsUpdated = false;
    }

    private Location createLocation(FileStorage storage) {
        StyleFontIcon icon = null;
        switch (storage.getType()) {
            case BASE:
                icon = DialogIcons.BASE_DISK;
                break;
            case NETWORK:
                icon = DialogIcons.NETWORK_DISK;
                break;
            case FLOPPY:
                icon = DialogIcons.FLOPPY;
                break;
            case OPTICAL:
                icon = DialogIcons.DISC;
                break;
            default:
                throw new AssertionError();
        }
        var location = new Location(
                icon,
                storage.getDisplayName(),
                0,
                storage,
                storage.getRootUri());
        return location;
    }

    private void createInitialColumns() {
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

    private GenericFile getResultFile() {
        if (getDirectory() == null || getStorage() == null) {
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
        URI fileUri = UriUtils.resolvePath(getDirectory(), fileName);
        var builder = new GenericFile.Builder();
        builder.storage(getStorage());
        builder.uri(fileUri);
        builder.name(fileName);
        builder.virtual(true);
        var file = builder.build();
        return file;
    }
}
