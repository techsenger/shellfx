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

import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.dialog.AbstractDialogPresenter;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.popup.OverlayScope;
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.dialogs.DialogComponents;
import com.techsenger.tabshell.dialogs.alert.AlertDialogType;
import com.techsenger.tabshell.dialogs.style.DialogIcons;
import com.techsenger.tabshell.material.button.ResultButtonName;
import com.techsenger.tabshell.material.icon.StyleFontIcon;
import com.techsenger.tabshell.material.table.TableColumnHistory;
import com.techsenger.tabshell.material.table.TableHistory;
import com.techsenger.tabshell.shared.style.SharedIcons;
import com.techsenger.tabshell.storage.FileColumnNames;
import com.techsenger.tabshell.storage.FileStorage;
import static com.techsenger.tabshell.storage.FileStorageType.BASE;
import static com.techsenger.tabshell.storage.FileStorageType.NETWORK;
import static com.techsenger.tabshell.storage.FileStorageType.OPTICAL;
import com.techsenger.tabshell.storage.FileStorages;
import com.techsenger.tabshell.storage.FileType;
import com.techsenger.tabshell.storage.GenericFile;
import com.techsenger.tabshell.storage.UriUtils;
import com.techsenger.toolkit.core.file.FileUtils;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import javafx.scene.control.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class FileChooserDialogPresenter<V extends FileChooserDialogView, C extends FileChooserDialogComposer>
        extends AbstractDialogPresenter<V, C> {

    private static final Logger logger = LoggerFactory.getLogger(FileChooserDialogPresenter.class);

    private enum EditType {
        NEW_DIRECTORY, RENAME_FILE
    }

    protected class Port extends AbstractDialogPresenter.Port implements FileChooserDialogPort {

        private final FileChooserDialogPresenter<V, C> presenter = FileChooserDialogPresenter.this;

        @Override
        public GenericFile getResult() {
            return presenter.resultFile;
        }
    }

    private final FileChooserType type;

    private URI initialDirectory;

    private String initialFileName;

    private FileStorage storage;

    private String fileName;

    private URI directory;

    private boolean locationsUpdated;

    private List<FileStorage> storages;

    private final AppearanceSettings settings;

    private GenericFile resultFile;

    private EditType editType;

    public FileChooserDialogPresenter(V view, OverlayScope scope, FileChooserType type, AppearanceSettings settings,
            HistoryManager historyManager) {
        this(view, scope, type, FileStorages.getAll(true), settings, historyManager);
    }

    public FileChooserDialogPresenter(V view, OverlayScope scope, FileChooserType type, List<FileStorage> storages,
                    AppearanceSettings settings, HistoryManager historyManager) {
        super(view, scope);
        this.settings = settings;
        this.type = type;
        this.storages = storages;
        setHistoryPolicy(HistoryPolicy.APPEARANCE);
        setHistoryProvider(() -> historyManager.getOrCreateHistory(FileChooserDialogHistory.class,
                FileChooserDialogHistory::new));
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
    public FileChooserDialogPort getPort() {
        return (FileChooserDialogPort) super.getPort();
    }

    public FileChooserType getType() {
        return type;
    }

    public URI getInitialDirectory() {
        return initialDirectory;
    }

    public void setInitialDirectory(URI initialDirectory) {
        this.initialDirectory = initialDirectory;
        this.directory = initialDirectory;
        if (this.storage != null) {
            this.storage = FileStorages.findByUri(storages, directory);
        }
    }

    public String getInitialFileName() {
        return initialFileName;
    }

    public void setInitialFileName(String initialFileName) {
        this.initialFileName = initialFileName;
        this.fileName = initialFileName;
    }

    @Override
    public void onResult(ResultButtonName name) {
        if (name == FileChooserButtons.OK) {
            this.resultFile = getResultFile();
            if (this.resultFile == null) {
                return;
            }
        }
        super.onResult(name);
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DialogComponents.FILE_CHOOSER_DIALOG);
    }

    @Override
    protected Port createPort() {
        return new FileChooserDialogPresenter.Port();
    }

    @Override
    protected FileChooserDialogHistory getHistory() {
        return (FileChooserDialogHistory) super.getHistory();
    }

    @Override
    protected void preInitialize() {
        super.preInitialize();
        getView().setAppearanceSettings(settings);
    }

    @Override
    protected void saveAppearance() {
        super.saveAppearance();
        var history = getHistory();
        var view = getView();
        history.setListSelected(view.isListSelected());
        history.setDetailsSelected(view.isDetailsSelected());
        history.setTable(view.getTableHistory());
    }

    @Override
    protected void restoreAppearance() {
        super.restoreAppearance();
        var history = getHistory();
        var view = getView();
        view.setListSelected(history.isListSelected());
        view.setDetailsSelected(history.isDetailsSelected());
        view.setTableHistory(history.getTable());
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        var view = getView();
        view.setButtonWidthEqual(true);
        view.setPrefWidth(800);
        view.setPrefHeight(500);
        view.setupFor(type);
        if (getHistory().isNew()) {
            var tableHistory = createInitialTableHistory();
            view.setTableHistory(tableHistory);
        }
        updateFiles(null);
    }

    protected URI getDirectory() {
        return directory;
    }

    protected FileStorage getStorage() {
        return storage;
    }

    protected AppearanceSettings getSettings() {
        return settings;
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
                            SharedIcons.DIRECTORY,
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
        getView().setLocations(locations);
        getView().setLocation(selectedLocation);
        this.locationsUpdated = true;
    }

    protected void onFileRequested(GenericFile file) {
        if (file.isDirectory()) {
            navigateTo(file.getStorage(), file.getUri());
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
            onFileRequested(file);
        }
    }

    protected void onNewDirectoryRequested() {
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

    protected void onRenameRequested(int fileIndex) {
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

    protected void onListSelected() {
        getView().setListSelected(true);
    }

    protected void onDetailsSelected() {
        getView().setDetailsSelected(true);
    }

    protected void onFileSelected(GenericFile file) {
        if (file != null && !file.isDirectory()) {
            getView().setFileName(file.getName());
        } else {
            getView().setFileName(null);
        }
    }

    protected void onRefresh() {
        updateFiles(getView().getSelectedFile());
    }

    protected void onFilterSelected(ExtensionFilter filter) {
        updateFiles(null);
    }



    private void navigateTo(FileStorage storage, URI uri) {
        this.storage = storage;
        this.directory = uri;
        updateFiles(null);
    }

    private void updateFiles(GenericFile selectedFile) {
        var view = getView();
        view.setFiles(Collections.emptyList());
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
        List<GenericFile> filteredFiles = new ArrayList<>();
        var extFilter = view.getExtensionFilter();
        if (extFilter != null && !extFilter.matchesAllFiles()) {
            for (var f : storageFiles) {
                if (f.isDirectory()) {
                    filteredFiles.add(f);
                } else {
                    if (extFilter.matches(f.getName())) {
                        filteredFiles.add(f);
                    }
                }
            }
            view.setFiles(filteredFiles);
        } else {
            view.setFiles(storageFiles);
        }
        view.sortFiles();
        //only after sorting we can find the selected file index
        var selectedFileIndex = -1;
        if (selectedFile != null) {
            var files = view.getFiles();
            for (int i = 0; i < files.size(); i++) {
                var file = files.get(i);
                if (file.getType() == selectedFile.getType() && file.getName() != null
                        && file.getName().equals(selectedFile.getName())) {
                    selectedFileIndex = i;
                    break;
                }
            }
            if (selectedFileIndex != -1) {
                view.selectFile(selectedFileIndex);
                view.scrollToFile(selectedFileIndex);
            }
        }
    }

    private void showWarning(String text) {
        getComposer().addAlertDialog(getOverlayScope(), AlertDialogType.WARNING, text);
    }

    private void setDefaultStorageAndDirectory() {
        this.storage = FileStorages.findPrimary(storages);
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
                    SharedIcons.DIRECTORY,
                    segments.get(segments.size() - 1),
                    segments.size(),
                    storage,
                    directory);
        }
        // The created location must be added to the locations list.
        // We add only one location at a time - the currently selected one.
        // If the user clicks the combobox, all locations will be updated.
        var view = getView();
        view.setLocations(List.of(location));
        view.setLocation(location);
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

    private TableHistory createInitialTableHistory() {
        var columns = new ArrayList<TableColumnHistory>();
        var typeColumn = new TableColumnHistory(FileColumnNames.TYPE.toString());
        columns.add(typeColumn);
        typeColumn.setSortIndex(0);
        typeColumn.setSortType(TableColumn.SortType.ASCENDING);
        var nameColumn = new TableColumnHistory(FileColumnNames.NAME.toString());
        columns.add(nameColumn);
        nameColumn.setSortIndex(1);
        nameColumn.setSortType(TableColumn.SortType.ASCENDING);
        var sizeColumn = new TableColumnHistory(FileColumnNames.SIZE.toString());
        columns.add(sizeColumn);
        var modifiedColumn = new TableColumnHistory(FileColumnNames.LAST_MODIFIED.toString());
        columns.add(modifiedColumn);
        return new TableHistory(columns);
    }

    private GenericFile getResultFile() {
        if (getDirectory() == null || getStorage() == null) {
            showWarning("Storage or/and directory are not selected.");
            return null;
        }
        var view = getView();
        var fileName = view.getFileName();
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
        for (var file : view.getFiles()) {
            if (!file.isDirectory() && file.getName().equals(fileName)) {
                return file;
            }
        }
        //there is no file with such name
        if (this.type == FileChooserType.SAVE_AS) {
            if (!view.getExtensionFilters().isEmpty() && view.getExtensionFilter() != null
                    && !view.getExtensionFilter().matchesAllFiles()) {
                var extension = FileUtils.getExtension(fileName);
                var filter = view.getExtensionFilter();
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
