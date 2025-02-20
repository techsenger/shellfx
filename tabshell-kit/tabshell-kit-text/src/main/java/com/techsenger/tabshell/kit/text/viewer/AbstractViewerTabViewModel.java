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

package com.techsenger.tabshell.kit.text.viewer;

import com.techsenger.tabshell.core.MenuItemValidator;
import com.techsenger.tabshell.core.TabShellViewModel;
import com.techsenger.tabshell.kit.core.file.FileInfo;
import com.techsenger.tabshell.kit.core.file.FileTabViewModel;
import com.techsenger.tabshell.kit.core.file.FileTaskProvider;
import com.techsenger.tabshell.kit.core.menu.FileMenuKeys;
import com.techsenger.tabshell.kit.core.settings.Settings;
import com.techsenger.tabshell.kit.core.settings.ViewerSettings;
import com.techsenger.tabshell.kit.core.workertab.AbstractWorkerTabViewModel;
import com.techsenger.tabshell.kit.text.menu.EditMenuKeys;
import com.techsenger.tabshell.material.menu.MenuItemKey;
import com.techsenger.tabshell.material.menu.MenuKey;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
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
        implements FileTabViewModel<String> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractViewerTabViewModel.class);

    private final ReadOnlyBooleanWrapper textFocused  = new ReadOnlyBooleanWrapper(false);

    private final BooleanProperty editable  = new SimpleBooleanProperty(false);

    private final BooleanProperty caretVisible = new SimpleBooleanProperty(false);

    /**
     * Although it is a viewer content can be modified, for example, if it a log viewer.
     */
    private final ReadOnlyBooleanWrapper contentModified = new ReadOnlyBooleanWrapper(false);

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

    private final FileInfo fileInfo;

    private final FileTaskProvider<String> fileTaskProvider;

    private FindMatchesResetPolicy findMatchesResetPolicy = FindMatchesResetPolicy.AUTOMATIC;

    private DefaultFindPaneViewModel find;

    private final Map<MenuItemKey, MenuItemValidator> menuItemValidatorsByKey = new HashMap<>();

    private UndoManager undoManager;

    /**
     * Constructor.
     *
     */
    public AbstractViewerTabViewModel(TabShellViewModel tabShell, FileInfo fileInfo,
            FileTaskProvider<String> fileTaskProvider) {
        super(tabShell);
        this.fileInfo = fileInfo;
        this.fileTaskProvider = fileTaskProvider;
        this.addSupportedMenus(EditMenuKeys.EDIT);
        this.addSupportedMenuItems(FileMenuKeys.FILE,
                FileMenuKeys.OPEN);
        this.addSupportedMenuItems(EditMenuKeys.EDIT,
                EditMenuKeys.COPY,
                EditMenuKeys.FIND,
                EditMenuKeys.FIND_SELECTION,
                EditMenuKeys.FIND_NEXT,
                EditMenuKeys.FIND_PREVIOUS);
        initializeMenuItemValidators();
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
    public FileInfo getFileInfo() {
        return this.fileInfo;
    }

    @Override
    public FileTaskProvider<String> getFileTaskProvider() {
        return this.fileTaskProvider;
    }

    @Override
    public boolean isContentModified() {
        return this.contentModified.get();
    }

    @Override
    public void setContentModified(boolean modified) {
        this.contentModified.set(modified);
    }

    @Override
    public String getContent() {
        this.undoManager.mark();
        return this.text.get();
    }

    @Override
    public void setContent(String content) {
        this.content.next(content);
    }

    @Override
    public boolean isMenuValid(MenuKey menuKey) {
        return true;
    }

    @Override
    public boolean isMenuItemValid(MenuKey menuKey, MenuItemKey itemKey) {
        var validator = this.menuItemValidatorsByKey.get(itemKey);
        if (validator != null) {
            return validator.validate(menuKey, itemKey);
        } else {
            return false;
        }
    }

    public ReadOnlyBooleanProperty contentModifiedProperty() {
        return contentModified.getReadOnlyProperty();
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
        return getTabShell().getSettings(Settings.class).getViewer();
    }

    protected boolean isCopyItemValid() {
        return !this.selectedText.get().isEmpty();
    }

    protected void initializeMenuItemValidators() {
        this.menuItemValidatorsByKey.put(FileMenuKeys.SAVE, (menuKey, itemKey) -> getFileInfo().getPath() != null);
        this.menuItemValidatorsByKey.put(FileMenuKeys.SAVE_AS, (menuKey, itemKey) -> !this.fileInfo.isRemote());
        this.menuItemValidatorsByKey.put(EditMenuKeys.COPY, (menuKey, itemKey) -> isCopyItemValid());
        this.menuItemValidatorsByKey.put(EditMenuKeys.FIND, (menuKey, itemKey) -> true);
        this.menuItemValidatorsByKey.put(EditMenuKeys.FIND_SELECTION, (menuKey, itemKey) ->
                !this.selectedText.get().isEmpty());
        this.menuItemValidatorsByKey.put(EditMenuKeys.FIND_NEXT, (menuKey, itemKey) -> {
            if (this.find != null) {
                return this.find.hasNextMatch();
            } else {
                return false;
            }
        });
        this.menuItemValidatorsByKey.put(EditMenuKeys.FIND_PREVIOUS, (menuKey, itemKey) -> {
            if (this.find != null) {
                return this.find.hasPreviousMatch();
            } else {
                return false;
            }
        });
    }

    protected Map<MenuItemKey, MenuItemValidator> getMenuItemValidatorsByKey() {
        return menuItemValidatorsByKey;
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

    ReadOnlyBooleanWrapper contentModifiedWrapper() {
        return contentModified;
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
