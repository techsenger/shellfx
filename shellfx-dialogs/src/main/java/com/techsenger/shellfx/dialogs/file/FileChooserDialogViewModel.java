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
import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.config.ConfigUtils;
import com.techsenger.shellfx.core.dialog.AbstractDialogViewModel;
import com.techsenger.shellfx.core.settings.AppearanceSettings;
import com.techsenger.shellfx.core.settings.SettingsSubscription;
import com.techsenger.shellfx.dialogs.alert.AlertDialogConfig;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogType;
import com.techsenger.shellfx.dialogs.style.DialogIcons;
import com.techsenger.shellfx.material.RequestSetter;
import com.techsenger.shellfx.material.UiExecutor;
import com.techsenger.shellfx.material.button.ResultButtonName;
import com.techsenger.shellfx.material.table.TableColumnInfo;
import com.techsenger.shellfx.material.table.TableColumnName;
import com.techsenger.shellfx.material.table.TableConfig;
import com.techsenger.shellfx.storage.Comparators;
import com.techsenger.shellfx.storage.FileEntryType;
import com.techsenger.shellfx.storage.FileStorage;
import com.techsenger.shellfx.storage.FileStorageUtils;
import com.techsenger.shellfx.storage.OperationType;
import com.techsenger.shellfx.storage.StorageFile;
import com.techsenger.shellfx.storage.UriUtils;
import com.techsenger.shellfx.storage.async.AsyncResult;
import com.techsenger.shellfx.storage.async.AsyncStatus;
import com.techsenger.shellfx.storage.async.AsyncTask;
import com.techsenger.shellfx.storage.async.DefaultFileProvider;
import com.techsenger.shellfx.storage.async.DefaultFileRefiner;
import com.techsenger.shellfx.storage.async.FileProvider;
import com.techsenger.shellfx.storage.async.FileRefinement;
import com.techsenger.shellfx.storage.async.FileRefiner;
import com.techsenger.shellfx.storage.async.TwoPhaseSortPolicy;
import com.techsenger.shellfx.storage.style.FileDecorationType;
import com.techsenger.shellfx.storage.view.StorageErrors;
import com.techsenger.toolkit.core.file.FileUtils;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
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
import static com.techsenger.shellfx.dialogs.file.FileChooserType.OPEN;
import static com.techsenger.shellfx.dialogs.file.FileChooserType.SAVE_AS;

/**
 *
 * @author Pavel Castornii
 */
public class FileChooserDialogViewModel<C extends FileChooserDialogComposer, T extends StorageFile>
        extends AbstractDialogViewModel<C> implements FileChooserDialogPort<T> {

    private static final Logger logger = LoggerFactory.getLogger(FileChooserDialogViewModel.class);

    private enum EditType {
        NEW_DIRECTORY, RENAME_FILE
    }

    private final ObservableList<T> modifiableFiles = FXCollections.observableArrayList();

    private final ObservableList<T> files = FXCollections.unmodifiableObservableList(modifiableFiles);

    private final ObservableSource<Integer> selectFileSource = new SimpleObservableSource<>();

    private final ObservableSource<Integer> scrollToFileSource = new SimpleObservableSource<>();

    private final ObservableSource<Integer> editFileSource = new SimpleObservableSource<>();

    /**
     * Tells the view that the icon and the style of the files are outdated, which happens when the second loading
     * phase refines them. The view decorates the files again from scratch.
     */
    private final ObservableSource<List<T>> decorateSource = new SimpleObservableSource<>();

    /**
     * Tells the view that only the style of the files is outdated, as a theme or a mark changes nothing else. The
     * view keeps their icons and resolves just the styles again.
     */
    private final ObservableSource<List<T>> restyleSource = new SimpleObservableSource<>();

    private final ObservableList<T> modifiableLocations = FXCollections.observableArrayList();

    private final ObservableList<T> locations = FXCollections.unmodifiableObservableList(modifiableLocations);

    private final ReadOnlyObjectWrapper<T> location = new ReadOnlyObjectWrapper<>();

    private final ObservableSource<T> locationSource = new SimpleObservableSource<>();

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

    private final FileProvider<T> fileProvider;

    private final TwoPhaseSortPolicy twoPhaseSortPolicy;

    private final FileRefiner<T> fileRefiner;

    private final ObjectProperty<Comparator<T>> fileComparator = new SimpleObjectProperty<>();

    private T resultFile;

    private EditType editType;

    private Comparator<T> sortComparator;

    private SettingsSubscription themeSubscription;

    private long loadToken;

    private final List<AsyncTask> tasks = new ArrayList<>();

    /**
     * The files that are shown provisionally, as the second phase is still to refine them. The files are compared by
     * identity, as the refinement of a file can change what it is equal to.
     */
    private final Set<T> pending = Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));

    private final Queue<FileRefinement<T>> refinements = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean refinementsScheduled = new AtomicBoolean();

    public FileChooserDialogViewModel(FileChooserDialogParams<T> params) {
        super(params);
        this.chooserType = params.getChooserType();
        var paramsProvider = params.getFileProvider();
        this.fileProvider = paramsProvider != null ? paramsProvider : createDefaultFileProvider(params.getStorages());
        this.storages = fileProvider.getStorages();
        this.twoPhaseSortPolicy = params.getTwoPhaseSortPolicy();
        var paramsRefiner = params.getFileRefiner();
        this.fileRefiner = paramsRefiner != null ? paramsRefiner : createDefaultFileRefiner();
        this.initialDirectory = params.getInitialDirectory();
        this.initialFileName = params.getInitialFileName();
        this.fileName.set(initialFileName);
        fileComparator.addListener((obs, oldV, newV) -> this.sortComparator =
                Comparators.directoryFirst(newV));
        selectedFileIndex.addListener((obs, oldV, newV) -> {
            var f = computeFile();
            this.file.set(f);
            if (f != null && !f.isDirectory() && !f.isDirectoryLink()) {
                setFileName(f.getName());
            } else {
                setFileName(null);
            }
        });
        extensionFilter.addListener((obs, oldV, newV) -> reload(getFile()));
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
    public ObservableList<T> getLocations() {
        return locations;
    }

    @Override
    public void setLocations(List<T> locations) {
        modifiableLocations.setAll(locations);
    }

    @Override
    public T getLocation() {
        return location.get();
    }

    @Override
    @RequestSetter
    public void setLocation(T location) {
        locationSource.next(location);
    }

    @Override
    public ReadOnlyObjectProperty<T> locationProperty() {
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
        reload(getFile());
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
    protected @Nullable FileChooserDialogConfig getConfig() {
        return (FileChooserDialogConfig) super.getConfig();
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
    protected void loadStateFromConfig() {
        super.loadStateFromConfig();
        var config = getConfig();
        setMode(config.getMode());
        // put() one at a time, in index order: the view builds/appends the real column on each map change, so
        // this order also decides the columns' left-to-right order in the table
        config.getTable().getColumns().stream()
                .map(TableColumnInfo::new)
                .sorted(Comparator.comparingInt(TableColumnInfo::getIndex))
                .forEach((c) -> {
                    columns.put(c.getName(), c);
                });
    }

    @Override
    protected void observeStateForConfig() {
        super.observeStateForConfig();
        var config = getConfig();
        ConfigUtils.observe(mode, config, FileChooserDialogConfig::setMode);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        this.themeSubscription = getAppearanceSettings().onThemeChanged((oldV, newV) -> decorateShownFiles());
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
        var navigated = false;
        if (this.initialDirectory != null) {
            var initialStorage = FileStorageUtils.findByUri(storages, initialDirectory);
            if (initialStorage.isPresent()) {
                navigateTo(initialStorage.get(), this.initialDirectory, null);
                navigated = true;
            }
        }
        if (!navigated) {
            reload(null);
        }
        setRightButtons(FileChooserDialogButtons.CANCEL, FileChooserDialogButtons.OK);
        setButtonDefault(FileChooserDialogButtons.OK, true);
        setMinWidth(600);
        setMinHeight(400);
    }

    @Override
    protected void postDeinitialize() {
        super.postDeinitialize();
        themeSubscription.unsubscribe();
        // the results of the loading that is still running are not wanted any more
        loadToken++;
        cancelTasks();
    }

    protected FileStorage getStorage() {
        return currentStorage();
    }

    protected ObservableList<T> getModifiableFiles() {
        return modifiableFiles;
    }

    protected void onLocationRequested(T location) {
        navigateTo(location.getStorage(), location.getUri(), null);
    }

    /**
     * Enters the directory the file is or points to.
     *
     * @param file the file to enter, which may be {@code null} when nothing is selected
     * @return {@code true} if the file was entered, {@code false} if it is not a directory or a link to one
     */
    protected boolean onNavigateDown(@Nullable StorageFile file) {
        if (file == null) {
            return false;
        }
        // a link is entered by going to the directory it points to, which may lie in another storage
        var link = file.getLink();
        var directory = file.isDirectory() ? file : (link != null ? link.getTarget() : null);
        if (directory == null || !directory.isDirectory()) {
            return false;
        }
        navigateTo(directory.getStorage(), directory.getUri(), null);
        return true;
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
        navigateTo(storage, parentUri, this.directory.get());
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
        var file = currentStorage().createVirtual(null, FileEntryType.DIRECTORY, "New Folder");
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
                    currentStorage().createDirectory(dirUri, OperationType.PRIMARY);
                    reload(file);
                } catch (Exception ex) {
                    logger.error("{} Error creating new directory at {}", getDescriptor().getLogPrefix(), dirUri, ex);
                }
            }
            case RENAME_FILE -> {
                try {
                    var storage = currentStorage();
                    if (storage != null) {
                        storage.renameFile(file.getUri(), file.getName(), OperationType.PRIMARY);
                        reload(file);
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

    protected void onEditCancelled(StorageFile file) {
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
        reload(getFile());
    }

    protected void setColumnWidth(TableColumnName name, double width) {
        var info = this.columns.get(name);
        info.setWidth(width);
        updateConfigColumns();
    }

    protected void setColumnSortType(TableColumnName name, TableColumn.SortType sortType) {
        var info = this.columns.get(name);
        info.setSortType(sortType);
        updateConfigColumns();
    }

    protected void setColumnIndex(TableColumnName name, int index) {
        var info = this.columns.get(name);
        info.setIndex(index);
        updateConfigColumns();
    }

    protected void setColumnSortIndex(TableColumnName name, Integer index) {
        var info = this.columns.get(name);
        info.setSortIndex(index);
        updateConfigColumns();
    }

    protected Comparator<T> getFileComparator() {
        return sortComparator;
    }

    protected FileProvider<T> createDefaultFileProvider(List<? extends FileStorage<T>> storages) {
        return new DefaultFileProvider<T>(storages);
    }

    protected FileRefiner<T> createDefaultFileRefiner() {
        return new DefaultFileRefiner<T>(storages);
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
    ReadOnlyObjectWrapper<T> locationWrapper() {
        return location;
    }

    ObservableSource<T> locationSource() {
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

    ObservableSource<List<T>> decorateSource() {
        return decorateSource;
    }

    ObservableSource<List<T>> restyleSource() {
        return restyleSource;
    }

    /**
     * Tells how the view has to decorate the file at the moment it is about to be shown.
     */
    FileDecorationType getDecorationType(T file) {
        return pending.contains(file) ? FileDecorationType.TEMP : FileDecorationType.FINAL;
    }

    /**
     * Locations updated only when user clicks enter to show the list. At all other times, the list contains only one
     * element with the current directory.
     */
    void onLocationsOpened() {
        if (locationsUpdated) {
            return;
        }
        var requestedStorage = currentStorage();
        var requestedDirectory = this.directory.get();
        var token = loadToken;
        tasks.add(fileProvider.provideRootsAsync(
                roots -> {
                    prepareToShow(roots);
                    UiExecutor.execute(() -> {
                        //the user could navigate elsewhere while the roots were being read
                        if (isCurrent(token)) {
                            loadHierarchy(roots, requestedStorage, requestedDirectory, token);
                        }
                    });
                },
                result -> { }));
    }

    /**
     * Starts loading that supersedes the one that is running, which the user waits for, and clears the files.
     *
     * @return the token that tells the results of this loading from those of a superseded one
     */
    private long startLoading() {
        loadToken++;
        cancelTasks();
        setWaiting(true);
        modifiableFiles.forEach(pending::remove);
        setFiles(Collections.emptyList());
        return loadToken;
    }

    private boolean isCurrent(long token) {
        return token == loadToken;
    }

    /**
     * Shows the files of the directory at the URI, which is made the current one. If it can't be opened, the user is
     * told so and stays where they were; only the first opening, when there is no current directory yet, falls
     * back to the default directory.
     */
    private void navigateTo(FileStorage storage, URI uri, @Nullable T selectedFile) {
        FileStorage<T> typedStorage = storage;
        var previous = this.directory.get();
        var previousSelection = computeFile();
        var token = startLoading();
        Consumer<@Nullable Exception> onFailed = cause ->
                returnAfterFailure(previous, previousSelection, uri, token, cause);
        Consumer<T> onLoaded = directory -> {
            prepareToShow(List.of(directory));
            UiExecutor.execute(() -> {
                if (isCurrent(token)) {
                    listDirectory(directory, selectedFile, token, onFailed);
                }
            });
        };
        Consumer<AsyncResult> onFinished = result -> {
            if (result.getStatus() == AsyncStatus.FAILED) {
                UiExecutor.execute(() -> {
                    if (isCurrent(token)) {
                        onFailed.accept(result.getException());
                    }
                });
            }
        };
        if (uri.equals(storage.getUri())) {
            tasks.add(fileProvider.provideRootAsync(typedStorage, onLoaded, onFinished));
        } else {
            tasks.add(fileProvider.provideFileAsync(typedStorage, uri, OperationType.PRIMARY, onLoaded, onFinished));
        }
    }

    /**
     * Shows the files of the current directory again, or of the default one if there is no current directory.
     */
    private void reload(@Nullable T selectedFile) {
        var token = startLoading();
        var current = this.directory.get();
        if (current == null) {
            listDefaultDirectory(token);
        } else {
            listDirectory(current, selectedFile, token,
                    cause -> returnAfterFailure(current, selectedFile, current.getUri(), token, cause));
        }
    }

    /**
     * Tells the user that the directory can't be opened and shows the directory they were in again. With no such
     * directory, as at the first opening, the default one is shown.
     */
    private void returnAfterFailure(@Nullable T previous, @Nullable T previousSelection, URI failedUri, long token,
            @Nullable Exception cause) {
        if (previous == null) {
            listDefaultDirectory(token);
            return;
        }
        var path = failedUri.getPath() != null ? failedUri.getPath() : failedUri.toString();
        showWarning("The directory '" + path + "' can't be opened: " + StorageErrors.describe(cause) + ".");
        // a failure of the directory they were in is not repeated endlessly: nothing is shown then
        listDirectory(previous, previousSelection, startLoading(), failure -> showNothing());
    }

    private void showNothing() {
        this.directory.set(null);
        setWaiting(false);
    }

    private void listDefaultDirectory(long token) {
        var home = FileStorageUtils.getHome(storages);
        if (home.isPresent()) {
            prepareToShow(List.of(home.get()));
            listDirectory(home.get(), null, token, failure -> listMainDirectory(token));
        } else {
            listMainDirectory(token);
        }
    }

    private void listMainDirectory(long token) {
        tasks.add(fileProvider.provideRootAsync(FileStorageUtils.getMain(storages),
                root -> {
                    prepareToShow(List.of(root));
                    UiExecutor.execute(() -> {
                        if (isCurrent(token)) {
                            listDirectory(root, null, token, failure -> showNothing());
                        }
                    });
                },
                result -> {
                    if (result.getStatus() == AsyncStatus.FAILED) {
                        UiExecutor.execute(() -> {
                            if (isCurrent(token)) {
                                showNothing();
                            }
                        });
                    }
                }));
    }

    /**
     * Makes the directory the current one and lists it; {@code onFailed} is called with the cause if it can't be
     * listed.
     */
    private void listDirectory(T newDirectory, @Nullable T selectedFile, long token,
            Consumer<@Nullable Exception> onFailed) {
        this.directory.set(newDirectory);
        tasks.add(fileProvider.provideFilesAsync(currentStorage(), directoryUri(), OperationType.PRIMARY,
                storageFiles -> {
                    prepareToShow(storageFiles);
                    UiExecutor.execute(() -> {
                        if (isCurrent(token)) {
                            showFiles(storageFiles, selectedFile);
                        }
                    });
                },
                result -> UiExecutor.execute(() -> {
                    if (!isCurrent(token)) {
                        return;
                    }
                    if (result.getStatus() == AsyncStatus.SUCCESS) {
                        setWaiting(false);
                    } else if (result.getStatus() == AsyncStatus.FAILED) {
                        onFailed.accept(result.getException());
                    }
                })));
    }

    /**
     * Reads the directories that lead to the current one, after the roots of the storages are known.
     */
    private void loadHierarchy(List<T> roots, @Nullable FileStorage<T> requestedStorage,
            @Nullable T requestedDirectory, long token) {
        if (requestedStorage == null) {
            showLocations(roots, null, List.of());
            return;
        }
        tasks.add(fileProvider.provideHierarchyAsync(requestedStorage, requestedDirectory.getUri(),
                OperationType.PRIMARY, hierarchy -> {
                    prepareToShow(hierarchy);
                    UiExecutor.execute(() -> {
                        if (isCurrent(token)) {
                            showLocations(roots, requestedStorage, hierarchy);
                        }
                    });
                },
                result -> {
                    if (result.getStatus() == AsyncStatus.FAILED) {
                        UiExecutor.execute(() -> {
                            if (isCurrent(token)) {
                                showLocations(roots, requestedStorage, List.of(requestedDirectory));
                            }
                        });
                    }
                }));
    }

    private void showLocations(List<T> roots, @Nullable FileStorage<T> requestedStorage, List<T> hierarchy) {
        T selectedLocation = null;
        List<T> newLocations = new ArrayList<>();
        for (var root : roots) {
            if (root.getStorage() != requestedStorage) {
                newLocations.add(root);
                continue;
            }
            newLocations.addAll(hierarchy);
            selectedLocation = hierarchy.get(hierarchy.size() - 1);
        }
        setLocations(newLocations);
        setLocation(selectedLocation);
        this.locationsUpdated = true;
        refineAsync(newLocations, () -> { });
    }

    private void showFiles(List<T> storageFiles, @Nullable T selectedFile) {
        updateLocation();
        var extFilter = getExtensionFilter();
        List<T> filteredFiles = null;
        if (extFilter != null && !extFilter.matchesAllFiles()) {
            filteredFiles = new ArrayList<>();
            for (var f : storageFiles) {
                if (f.isDirectory() || f.isDirectoryLink()) {
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
        // a directory with nothing to refine is final after the first phase, so it is sorted whatever the policy is
        var refinable = files.stream().anyMatch(pending::contains);
        if (!refinable || twoPhaseSortPolicy == TwoPhaseSortPolicy.AFTER_FIRST_PHASE
                || twoPhaseSortPolicy == TwoPhaseSortPolicy.AFTER_EACH_PHASE) {
            files.sort(this.sortComparator);
        }
        setFiles(files);
        // the second phase: the files that are not ready are refined in the background, and the cells that show them
        // follow their properties
        refineAsync(files, () -> UiExecutor.execute(this::sortRefinedFiles));
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
        } else {
            scrollToFileSource.next(0);
        }
    }

    private void setFiles(List<T> files) {
        modifiableFiles.setAll(files);
    }

    /**
     * Sorts the files when the second phase is finished, if the policy asks for it. The list is sorted in place, so
     * the table moves its selection together with the file, and then the view scrolls to the selected file.
     */
    private void sortRefinedFiles() {
        if (twoPhaseSortPolicy != TwoPhaseSortPolicy.AFTER_SECOND_PHASE
                && twoPhaseSortPolicy != TwoPhaseSortPolicy.AFTER_EACH_PHASE) {
            return;
        }
        modifiableFiles.sort(this.sortComparator);
        var index = this.selectedFileIndex.get();
        if (index >= 0) {
            scrollToFileSource.next(index);
        }
    }

    private T computeFile() {
        if (this.selectedFileIndex.get() >= 0) {
            return this.files.get(selectedFileIndex.get());
        } else {
            return null;
        }
    }

    private void showWarning(String text) {
        var params = new AlertDialogParams(new AlertDialogConfig(), getWindowType(), getAppearanceSettings(),
                AlertDialogType.WARNING);
        getComposer().addAlertDialog(params, text);
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

    private void updateLocation() {
        // We add only one location at a time - the currently selected one.
        // If the user clicks the combobox, all locations will be updated.
        var location = this.directory.get();
        setLocations(List.of(location));
        setLocation(location);
        this.locationsUpdated = false;
        refineAsync(List.of(location), () -> { });
    }

    /**
     * Marks the files the second phase has to refine as pending, on the background thread of the provider, before
     * they are shown.
     */
    private void prepareToShow(List<T> files) {
        files.stream().filter(fileRefiner::isRefinable).forEach(pending::add);
    }

    private void decorateShownFiles() {
        restyleSource.next(new ArrayList<>(modifiableFiles));
        restyleSource.next(new ArrayList<>(modifiableLocations));
    }

    /**
     * Starts the second phase for the pending files. The refinements are queued as they come, and one UI task applies
     * all that are queued by then and tells the view to decorate their files, so a directory of many files doesn't
     * schedule a task for each file.
     */
    private void refineAsync(List<T> files, Runnable onFinished) {
        var refinable = files.stream().filter(pending::contains).toList();
        tasks.add(fileRefiner.refineFilesAsync(refinable, refinement -> {
            refinements.add(refinement);
            if (refinementsScheduled.compareAndSet(false, true)) {
                UiExecutor.execute(this::applyRefinements);
            }
        }, result -> onFinished.run()));
    }

    private void applyRefinements() {
        refinementsScheduled.set(false);
        var refinedFiles = new ArrayList<T>();
        var refinement = refinements.poll();
        while (refinement != null) {
            var file = refinement.getFile();
            refinement.apply();
            pending.remove(file);
            refinedFiles.add(file);
            refinement = refinements.poll();
        }
        decorateSource.next(refinedFiles);
    }

    private void cancelTasks() {
        tasks.forEach(AsyncTask::cancel);
        tasks.clear();
    }

    private void updateConfigColumns() {
        var config = getConfig();
        if (config != null) {
            config.setTable(new TableConfig(columns.values().stream().map(TableColumnInfo::new).toList()));
            config.notifyListeners();
        }
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
            if (!file.isDirectory() && !file.isDirectoryLink() && file.getName().equals(fileName)) {
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
        var file = currentStorage().createVirtual(fileUri, null, fileName);
        return file;
    }
}
