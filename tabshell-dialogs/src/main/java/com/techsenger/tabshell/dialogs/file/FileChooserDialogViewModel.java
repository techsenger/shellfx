/*
 * Copyright 2024-2025 Pavel Castornii.
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

import com.techsenger.mvvm4fx.core.HistoryPolicy;
import com.techsenger.tabshell.core.dialog.DialogKey;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.core.file.FileStorage;
import static com.techsenger.tabshell.core.file.FileStorageType.BASE;
import static com.techsenger.tabshell.core.file.FileStorageType.FLOPPY;
import static com.techsenger.tabshell.core.file.FileStorageType.NETWORK;
import static com.techsenger.tabshell.core.file.FileStorageType.OPTICAL;
import com.techsenger.tabshell.core.file.FileStorages;
import com.techsenger.tabshell.core.file.FileType;
import com.techsenger.tabshell.core.file.GenericFile;
import com.techsenger.tabshell.core.file.UriUtils;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.core.style.CoreIcons;
import com.techsenger.tabshell.dialogs.AbstractSimpleDialogViewModel;
import com.techsenger.tabshell.dialogs.DialogComponentKeys;
import com.techsenger.tabshell.dialogs.alert.AlertDialogType;
import com.techsenger.tabshell.dialogs.alert.AlertDialogViewModel;
import com.techsenger.tabshell.dialogs.style.DialogIcons;
import com.techsenger.tabshell.material.icon.FontIcon;
import com.techsenger.tabshell.material.table.TableHistory;
import com.techsenger.toolkit.core.file.FileUtils;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class FileChooserDialogViewModel extends AbstractSimpleDialogViewModel {

    private static final Logger logger = LoggerFactory.getLogger(FileChooserDialogViewModel.class);

    private final FileChooserType type;

    private final BooleanProperty listSelected = new SimpleBooleanProperty();

    private final BooleanProperty detailsSelected = new SimpleBooleanProperty();

    private final ObservableList<GenericFile> files = FXCollections.observableArrayList();

    private final IntegerProperty selectedFileIndex = new SimpleIntegerProperty();

    private final ObservableSource<Boolean> sortRequired = new SimpleObservableSource<>();

    private final ObservableSource<Boolean> listRefreshRequired = new SimpleObservableSource<>();

    private final ObjectProperty<URI> initialDirectory = new SimpleObjectProperty<>();

    private final StringProperty initialFileName = new SimpleStringProperty();

    private final ReadOnlyStringWrapper fileName = new ReadOnlyStringWrapper();

    private final ObservableList<Location> locations = FXCollections.observableArrayList();

    private final ObjectProperty<Location> location = new SimpleObjectProperty<>();

    private final ObjectProperty<ExtensionFilter> selectedExtensionFilter = new SimpleObjectProperty<>();

    private final ObservableList<ExtensionFilter> extensionFilters = FXCollections.observableArrayList();

    private final StringProperty locationText = new SimpleStringProperty();

    private final ReadOnlyObjectWrapper<URI> directory = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<FileStorage> storage = new ReadOnlyObjectWrapper<>();

    private boolean locationsUpdated;

    private final List<FileStorage> storages;

    private final AppearanceSettings appearanceSettings;

    private TableHistory tableHistory;

    public FileChooserDialogViewModel(DialogScope scope, FileChooserType type, AppearanceSettings settings,
            HistoryManager historyManager) {
        this(scope, type, settings, FileStorages.getDefault(true), historyManager);
    }

    public FileChooserDialogViewModel(DialogScope scope, FileChooserType type, AppearanceSettings settings,
            List<FileStorage> storages, HistoryManager historyManager) {
        super(scope, true);
        this.type = type;
        this.appearanceSettings = settings;
        this.storages = storages;
        this.initialDirectory.addListener((ov, oldV, newV) -> {
            this.directory.set(newV);
            if (this.directory.get() != null) {
                this.storage.set(FileStorages.findByUri(storages, directory.get()));
            }
        });
        this.initialFileName.addListener((ov, oldV, newV) -> this.fileName.set(newV));
        this.selectedFileIndex.addListener((ov, oldV, newV) -> {
            if (newV.intValue() != -1) {
                var file = this.files.get(newV.intValue());
                if (!file.isDirectory()) {
                    this.fileName.set(file.getName());
                }
            }
        });
        this.selectedExtensionFilter.addListener((ov, oldV, newV) -> {
            updateFiles(null);
        });
        switch (type) {
            case OPEN:
                setTitle("Open");
                setIcon(new FontIcon(CoreIcons.OPEN));
                setLocationText("Look In");
                break;
            case SAVE_AS:
                setTitle("Save As");
                setIcon(new FontIcon(CoreIcons.SAVE_AS));
                setLocationText("Save In");
                break;
            default:
                throw new AssertionError();
        }
        setHistoryPolicy(HistoryPolicy.APPEARANCE);
        setHistoryProvider(() -> historyManager.getHistory(FileChooserDialogHistory.class,
                FileChooserDialogHistory::new));
    }

    @Override
    public DialogKey getKey() {
        return DialogComponentKeys.FILE_DIALOG;
    }

    public FileChooserType getType() {
        return type;
    }

    public URI getInitialDirectory() {
        return initialDirectory.get();
    }

    public ObjectProperty<URI> initialDirectoryProperty() {
        return initialDirectory;
    }

    public void setInitialDirectory(URI directory) {
        this.initialDirectory.set(directory);
    }

    public String getInitialFileName() {
        return initialFileName.get();
    }

    public StringProperty initialFileNameProperty() {
        return initialFileName;
    }

    public void setInitialFileName(String name) {
        initialFileName.set(name);
    }

    public IntegerProperty selectedFileIndexProperty() {
        return selectedFileIndex;
    }

    public void setSelectedFileIndex(int index) {
        this.selectedFileIndex.set(index);
    }

    public int getSelectedFileIndex() {
        return this.selectedFileIndex.get();
    }

    public ObservableList<ExtensionFilter> getExtensionFilters() {
        return extensionFilters;
    }

    public ObjectProperty<ExtensionFilter> selectedExtensionFilterProperty() {
        return selectedExtensionFilter;
    }

    public ExtensionFilter getSelectedExtensionFilter() {
        return selectedExtensionFilter.get();
    }

    public void setSelectedExtensionFilter(ExtensionFilter filter) {
        this.selectedExtensionFilter.set(filter);
    }

    public StringProperty locationTextProperty() {
        return locationText;
    }

    public String getLocationText() {
        return this.locationText.get();
    }

    public void setLocationText(String text) {
        this.locationText.set(text);
    }

    public ReadOnlyObjectProperty<URI> directoryProperty() {
        return directory.getReadOnlyProperty();
    }

    public URI getDirectory() {
        return directory.get();
    }

    public ReadOnlyObjectProperty<FileStorage> storageProperty() {
        return storage.getReadOnlyProperty();
    }

    public FileStorage getStorage() {
        return storage.get();
    }

    public ReadOnlyStringProperty fileNameProperty() {
        return fileName.getReadOnlyProperty();
    }

    public String getFileName() {
        return fileName.get();
    }

    public GenericFile getResultFile() {
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
        for (var file : files) {
            if (!file.isDirectory() && file.getName().equals(fileName)) {
                return file;
            }
        }
        //there is no file with such name
        if (this.type == FileChooserType.SAVE_AS) {
            if (!this.extensionFilters.isEmpty() && getSelectedExtensionFilter() != null
                    && !getSelectedExtensionFilter().matchesAllFiles()) {
                var extension = FileUtils.getExtension(fileName);
                if (extension != null) {
                    var filter = this.selectedExtensionFilter.get();
                    if (!filter.matches(fileName)) {
                        showWarning("The file '" + fileName + "' does not satisfy the filter criteria.");
                        return null;
                    }
                } else {
                    extension = getSelectedExtensionFilter().getPureExtensions().get(0);
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

    protected void updateFiles(GenericFile selectedFile) {
        if (selectedFile != null) {
            selectedFileIndex.set(-1); //there must be change event
        }
        this.files.clear();
        boolean defaultUsed = false;
        if (this.storage.get() == null || this.directory.get() == null) {
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
            this.storage.set(null);
            this.directory.set(null);
            return;
        }
        updateLocation();
        List<GenericFile> filteredFiles = new ArrayList<>();
        var extFilter = getSelectedExtensionFilter();
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
            files.addAll(filteredFiles);
        } else {
            files.addAll(storageFiles);
        }
        this.sortRequired.next(Boolean.TRUE);
        //only after sorting we can find the selected file index
        var selectedFileIndex = -1;
        if (selectedFile != null) {
            for (int i = 0; i < files.size(); i++) {
                var file = files.get(i);
                if (file.getType() == selectedFile.getType() && file.getName() != null
                        && file.getName().equals(selectedFile.getName())) {
                    selectedFileIndex = i;
                }
            }
        }
        if (listSelected.get()) {
            this.listRefreshRequired.next(Boolean.TRUE);
        }
        if (selectedFileIndex != -1) {
            this.selectedFileIndex.set(selectedFileIndex);
        }
    }

    protected ObservableList<GenericFile> getFiles() {
        return files;
    }

    protected AppearanceSettings getAppearanceSettings() {
        return appearanceSettings;
    }

    void navigateUp() {
        if (storage.get() == null || directory.get() == null) {
            return;
        }
        var parentUri = UriUtils.getParentUri(storage.get().getRootUri(), directory.get());
        if (parentUri == null) {
            return;
        }
        this.directory.set(parentUri);
        updateFiles(null);
    }

    void navigateTo(FileStorage storage, URI uri) {
        this.storage.set(storage);
        this.directory.set(uri);
        updateFiles(null);
    }

    void navigateTo(GenericFile file) {
        if (file.isDirectory()) {
            navigateTo(file.getStorage(), file.getUri());
        }
    }

    void navigateHome() {
        var file = GenericFile.getHome(storages);
        if (file != null) {
            navigateTo(file);
        }
    }

    void createFakeNewDirectory() {
        var builder = new GenericFile.Builder();
        builder.name("New Folder");
        builder.storage(storage.get());
        builder.type(FileType.DIRECTORY);
        var file = builder.build();
        this.files.add(0, file);
    }

    void removeFakeNewDirectory() {
        this.files.remove(0);
    }

    void createRealNewDirectory(GenericFile file) {
        var dirUri = UriUtils.resolvePath(directory.get(), file.getName());
        try {
            this.storage.get().createDirectory(dirUri);
            updateFiles(file);
        } catch (Exception ex) {
            logger.error("Error creating new directory at {}", dirUri, ex);
        }
    }

    void renameFile(GenericFile newFile) {
        try {
            this.storage.get().renameFile(newFile.getUri(), newFile.getName());
            updateFiles(newFile);
        } catch (Exception ex) {
            logger.error("Error renaming file at {} to {}", newFile.getUri(), newFile.getName(), ex);
        }
    }

    ObservableSource<Boolean> getSortRequired() {
        return sortRequired;
    }

    ObservableSource<Boolean> getListRefreshRequired() {
        return listRefreshRequired;
    }

    BooleanProperty listSelectedProperty() {
        return listSelected;
    }

    BooleanProperty detailsSelectedProperty() {
        return detailsSelected;
    }

    TableHistory getTableHistory() {
        return tableHistory;
    }

    ObservableList<Location> getLocations() {
        return locations;
    }

    ObjectProperty<Location> locationProperty() {
        return location;
    }

    StringProperty fileNameWrapper() {
        return fileName;
    }

    /**
     * Locations updated only when user clicks enter to show the list. At all other times, the list contains only one
     * element with the current directory.
     */
    void updateLocations() {
        if (locationsUpdated) {
            return;
        }
        Location selectedLocation = null;
        this.locations.clear();
        for (var storage : storages) {
            var storageLocation = createLocation(storage);
            this.locations.add(storageLocation);
            if (this.storage.get() == storage && this.directory.get() != null) {
                var segments = UriUtils.getPathSegments(storage.getRootUri(), directory.get());
                if (segments.isEmpty()) {
                    selectedLocation = storageLocation;
                }
                var previousUri = storage.getRootUri();
                for (var i = 0; i < segments.size(); i++) {
                    var segment = segments.get(i);
                    var segmentUri = UriUtils.resolvePath(previousUri, segment);
                    var directoryLocation = new Location(
                            new FontIcon(CoreIcons.DIRECTORY),
                            segment,
                            i + 1,
                            storage,
                            segmentUri);
                    this.locations.add(directoryLocation);
                    if (i + 1 == segments.size()) {
                        selectedLocation = directoryLocation;
                    }
                    previousUri = segmentUri;
                }
            }
        }
        this.location.set(selectedLocation);
        this.locationsUpdated = true;
    }

    void setTableHistory(TableHistory tableHistory) {
        this.tableHistory = tableHistory;
    }

    private void showWarning(String text) {
        var alerViewModel = new AlertDialogViewModel(getScope(), AlertDialogType.WARNING, text);
        getComponentHelper().openAlertDialog(alerViewModel);
    }

    private void setDefaultStorageAndDirectory() {
        this.storage.set(FileStorages.findPrimary(storages));
        if (this.storage.get() != null) {
            this.directory.set(this.storage.get().getRootUri());
        }
    }

    private List<GenericFile> getFilesFromStorage() {
        try {
            return this.storage.get().getFiles(directory.get());
        } catch (Exception ex) {
            logger.error("Error getting files at {}", this.directory.get(), ex);
        }
        return null;
    }

    private void updateLocation() {
        var segments = UriUtils.getPathSegments(this.storage.get().getRootUri(), directory.get());
        Location l = null;
        if (segments.isEmpty()) {
            l = createLocation(storage.get());
        } else {
            l = new Location(
                    new FontIcon(CoreIcons.DIRECTORY),
                    segments.get(segments.size() - 1),
                    segments.size(),
                    storage.get(),
                    directory.get());
        }
        //created location must be in locations list
        //otherwise it won't be displayed correctly
        this.locations.clear();
        this.locations.add(l);
        this.location.set(l);
        this.locationsUpdated = false;
    }

    private Location createLocation(FileStorage storage) {
        FontIcon icon = null;
        switch (storage.getType()) {
            case BASE:
                icon = new FontIcon(DialogIcons.BASE_DISK);
                break;
            case NETWORK:
                icon = new FontIcon(DialogIcons.NETWORK_DISK);
                break;
            case FLOPPY:
                icon = new FontIcon(DialogIcons.FLOPPY);
                break;
            case OPTICAL:
                icon = new FontIcon(DialogIcons.DISC);
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
}
