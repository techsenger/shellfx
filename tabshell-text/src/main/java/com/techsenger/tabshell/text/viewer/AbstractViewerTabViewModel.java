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

package com.techsenger.tabshell.text.viewer;

import com.techsenger.tabshell.core.TabShellViewModel;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.core.file.FileTaskProvider;
import com.techsenger.tabshell.core.file.GenericFile;
import com.techsenger.tabshell.core.file.TextFileTaskProvider;
import com.techsenger.tabshell.core.menu.EditMenuKeys;
import com.techsenger.tabshell.core.menu.FileMenuKeys;
import com.techsenger.tabshell.core.menu.SimpleMenuHelper;
import com.techsenger.tabshell.core.menu.SimpleMenuItemHelper;
import com.techsenger.tabshell.core.settings.ViewerSettings;
import com.techsenger.tabshell.dialogs.alert.AlertDialogType;
import com.techsenger.tabshell.dialogs.alert.AlertDialogViewModel;
import com.techsenger.tabshell.dialogs.file.FileOpenerViewModel;
import com.techsenger.tabshell.dialogs.file.FileSaverViewModel;
import com.techsenger.tabshell.tabs.workertab.AbstractWorkerTabViewModel;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Worker;
import javafx.scene.control.IndexRange;
import org.fxmisc.undo.UndoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Text tab can be used in very different components that's why it doesn't add any optional menu/items.
 *
 * @author Pavel Castornii
 */
public abstract class AbstractViewerTabViewModel extends AbstractWorkerTabViewModel
        implements FileOpenerViewModel, FileSaverViewModel {

    private static final Logger logger = LoggerFactory.getLogger(AbstractViewerTabViewModel.class);

    private final ReadOnlyBooleanWrapper textFocused  = new ReadOnlyBooleanWrapper(false);

    private final BooleanProperty editable  = new SimpleBooleanProperty(false);

    private final BooleanProperty caretVisible = new SimpleBooleanProperty(false);

    /**
     * Although it is a viewer content can be modified, for example, if it a log viewer.
     */
    private final ReadOnlyBooleanWrapper modified = new ReadOnlyBooleanWrapper(false);

    /**
     * Never null. If there is no selection, then empty string.
     */
    private final ReadOnlyStringWrapper selectedText = new ReadOnlyStringWrapper();

    private final ReadOnlyStringWrapper text = new ReadOnlyStringWrapper();

    private final ReadOnlyIntegerWrapper textLength = new ReadOnlyIntegerWrapper();

    private final ReadOnlyObjectWrapper<IndexRange> selection = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyIntegerWrapper caretPosition = new ReadOnlyIntegerWrapper();

    private final ObservableSource<String> content = new SimpleObservableSource<>();

    private final BooleanProperty wrapText = new SimpleBooleanProperty(true);

    private final ObjectProperty<GenericFile> file = new SimpleObjectProperty<>();

    private final ObjectProperty<Charset> charset = new SimpleObjectProperty<>(StandardCharsets.UTF_8);

    private final ReadOnlyBooleanWrapper persisted = new ReadOnlyBooleanWrapper();

    private FindMatchesResetPolicy findMatchesResetPolicy = FindMatchesResetPolicy.AUTOMATIC;

    private DefaultFindPaneViewModel find;

    private UndoManager undoManager;

    /**
     * Constructor.
     *
     */
    public AbstractViewerTabViewModel(TabShellViewModel tabShell, GenericFile file) {
        super(tabShell);
        this.file.set(file);
        addMenuHelpers(new SimpleMenuHelper(EditMenuKeys.EDIT, Boolean.TRUE));
        addMenuItemHelpers(
            //file
            new SimpleMenuItemHelper(FileMenuKeys.OPEN, Boolean.TRUE, Boolean.TRUE) {
                @Override
                public void doOnItemAction() {
                    openFile(DialogScope.TAB);
                }
            },
            new SimpleMenuItemHelper(FileMenuKeys.SAVE, Boolean.TRUE) {
                @Override
                public void doOnItemAction() {
                    writeFile();
                }

                @Override
                public Boolean getItemValid() {
                    return isPersisted();
                }
            },
            new SimpleMenuItemHelper(FileMenuKeys.SAVE_AS, Boolean.TRUE) {

                @Override
                public void doOnItemAction() {
                    saveFile(DialogScope.TAB);
                }

                @Override
                public Boolean getItemValid() {
                    return true;
                }
            },

            //edit
            new SimpleMenuItemHelper(EditMenuKeys.COPY, Boolean.TRUE) {
                @Override
                public Boolean getItemValid() {
                    return isCopyItemValid();
                }
            },
            new SimpleMenuItemHelper(EditMenuKeys.FIND, Boolean.TRUE, Boolean.TRUE),
            new SimpleMenuItemHelper(EditMenuKeys.FIND_SELECTION, Boolean.TRUE) {
                @Override
                public Boolean getItemValid() {
                    return !selectedText.get().isEmpty();
                }
            },
            new SimpleMenuItemHelper(EditMenuKeys.FIND_NEXT, Boolean.TRUE) {
                @Override
                public Boolean getItemValid() {
                    if (find != null) {
                        return find.hasNextMatch();
                    } else {
                        return false;
                    }
                }
            },
            new SimpleMenuItemHelper(EditMenuKeys.FIND_PREVIOUS, Boolean.TRUE) {
                @Override
                public Boolean getItemValid() {
                    if (find != null) {
                        return find.hasPreviousMatch();
                    } else {
                        return false;
                    }
                }
            }
        );
    }

    public void openGoToLineDialog() {
        var viewModel = new GoToLineDialogViewModel(getTabShell().getHistoryManager());
        getComponentHelper().openGoToLineDialog(viewModel);
    }

    /**
     * Creates and sets initial values for find pane. This method can be called many times without removing pane.
     *
     * @param replaceMode
     */
    public void addFindPane(boolean replaceMode) {
        if (this.find == null) {
            this.find = new DefaultFindPaneViewModel(getFindMatchesResetPolicy(), getTabShell().getHistoryManager());
            this.find.closeActionProperty().set(() -> {
                this.removeFindPane();
            });
            this.find.replaceModeProperty().set(replaceMode);
            getComponentHelper().addFindPane(this.find);
        }
    }

    /**
     * Removes and destroys find pane.
     */
    public void removeFindPane() {
        if (this.find != null) {
            getComponentHelper().removeFindPane();
            this.find = null;
        }
    }

    @Override
    public ViewerTabHelper<?> getComponentHelper() {
        return (ViewerTabHelper) super.getComponentHelper();
    }

    @Override
    public GenericFile getFile() {
        return this.file.get();
    }

    @Override
    public void setFile(GenericFile file) {
        this.file.set(file);
    }

    @Override
    public void readFile() {
        var file = getFile();
        if (file == null || file.getUri() == null) {
            return;
        }
        var task = createFileTaskProvider().createFileReader(file);
        task.stateProperty().addListener((ob, oldV, newV) -> {
            if (newV == Worker.State.SUCCEEDED) {
                this.modified.set(false);
                setContent(task.getValue());
                this.persisted.set(true);
            } else if (newV == Worker.State.FAILED) {
                var message = "Error reading file " + file.getUri();
                logger.warn(message, task.getException());
                var alertViewModel = new AlertDialogViewModel(DialogScope.TAB, AlertDialogType.ERROR, message);
                getComponentHelper().openAlertDialog(alertViewModel);
            }
        });
        this.submitWorker(task);
    }

    public void writeFile() {
        var file = getFile();
        if (file == null || file.getUri() == null) {
            return;
        }
        var content = this.getContent();
        var task = createFileTaskProvider().createFileWriter(file, content);
        task.stateProperty().addListener((ob, oldV, newV) -> {
            if (newV == Worker.State.SUCCEEDED) {
                persisted.set(true);
                modified.set(false);
            } else if (newV == Worker.State.FAILED) {
                var message = "Error writing file " + file.getUri();
                logger.warn(message, task.getException());
                var alertViewModel = new AlertDialogViewModel(DialogScope.TAB, AlertDialogType.ERROR, message);
                getComponentHelper().openAlertDialog(alertViewModel);
            }
        });
        this.submitWorker(task);
    }

    public FileTaskProvider<String> createFileTaskProvider() {
        return new TextFileTaskProvider(getCharset());
    }

    public ObjectProperty<GenericFile> fileProperty() {
        return this.file;
    }

    public Charset getCharset() {
        return charset.get();
    }

    public void setCharset(Charset charset) {
        this.charset.set(charset);
    }

    public ObjectProperty<Charset> charsetProperty() {
        return charset;
    }

    public boolean isModified() {
        return this.modified.get();
    }

    public String getContent() {
        this.undoManager.mark();
        return this.text.get();
    }

    public void setContent(String content) {
        this.content.next(content);
    }

    public ReadOnlyBooleanProperty modifiedProperty() {
        return modified.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty textFocusedProperty() {
        return textFocused.getReadOnlyProperty();
    }

    public ReadOnlyStringProperty selectedTextProperty() {
        return selectedText.getReadOnlyProperty();
    }

    public ReadOnlyStringProperty textProperty() {
        return text.getReadOnlyProperty();
    }

    public BooleanProperty wrapTextProperty() {
        return wrapText;
    }

    public BooleanProperty editableProperty() {
        return editable;
    }

    public BooleanProperty caretVisibleProperty() {
        return caretVisible;
    }

    public ReadOnlyObjectProperty<IndexRange> selectionProperty() {
        return selection.getReadOnlyProperty();
    }

    public ReadOnlyIntegerProperty caretPositionProperty() {
        return caretPosition.getReadOnlyProperty();
    }

    public FindPaneViewModel getFind() {
        return find;
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    public ViewerSettings getSettings() {
        return getTabShell().getSettings().getViewer();
    }

    public ReadOnlyBooleanProperty persistedProperty() {
        return persisted.getReadOnlyProperty();
    }

    public boolean isPersisted() {
        return persisted.get();
    }

    protected void doOnCloseRequest() {

    }

    protected boolean isCopyItemValid() {
        return !this.selectedText.get().isEmpty();
    }

    protected ReadOnlyIntegerProperty textLengthProperty() {
        return textLength.getReadOnlyProperty();
    }

    protected FindMatchesResetPolicy getFindMatchesResetPolicy() {
        return findMatchesResetPolicy;
    }

    protected void setFindMatchesResetPolicy(FindMatchesResetPolicy findMatchesResetPolicy) {
        this.findMatchesResetPolicy = findMatchesResetPolicy;
    }

    ObservableSource<String> getContentSource() {
        return content;
    }

    ReadOnlyBooleanWrapper textFocusedWrapper() {
        return textFocused;
    }

    ReadOnlyStringWrapper selectedTextWrapper() {
        return selectedText;
    }

    ReadOnlyStringWrapper textWrapper() {
        return text;
    }

    ReadOnlyBooleanWrapper modifiedWrapper() {
        return modified;
    }

    ReadOnlyIntegerWrapper textLengthWrapper() {
        return textLength;
    }

    ReadOnlyObjectWrapper<IndexRange> selectionWrapper() {
        return selection;
    }

    ReadOnlyIntegerWrapper caretPositionWrapper() {
        return caretPosition;
    }

    void setUndoManager(UndoManager undoManager) {
        this.undoManager = undoManager;
    }
}
