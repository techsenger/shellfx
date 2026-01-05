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

import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.storage.FileStorage;
import com.techsenger.tabshell.storage.GenericFile;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public interface FileOpenerViewModel extends FileOperatorViewModel {

    /**
     * Opens a file by displaying a file chooser dialog and reading the selected file.
     *
     * @param scope the scope of the dialog.
     * @param storages the storages or null.
     */
    default void openFile(DialogScope scope, List<FileStorage> storages) {
        openFile(scope, storages, null, null);
    }

    /**
     * Opens a file by displaying a file chooser dialog and reading the selected file.
     *
     * @param scope the scope of the dialog.
     * @param storages the storages or null.
     * @param okCallback the callback that will be called if the user clicks the OK button.
     * @param cancelCallback the callback that will be called if the user clicks the cancel or close button.
     */
    default void openFile(DialogScope scope, List<FileStorage> storages, Runnable okCallback, Runnable cancelCallback) {
        var file = getFile();
        var viewModel = new FileChooserDialogViewModel<>(scope, FileChooserType.OPEN, getAppearanceSettings(),
                getHistoryManager());
        var filters = createOpenExtensionFilters();
        if (filters != null) {
            viewModel.getExtensionFilters().addAll(filters);
        }
        if (file != null) {
            viewModel.setInitialFileName(file.getName());
            var parent = GenericFile.getParent(file);
            if (parent != null) {
                viewModel.setInitialDirectory(parent.getUri());
            }
        }
        viewModel.getOk().setAction(() -> {
            var resultFile = viewModel.getResultFile();
            if (resultFile != null) {
                viewModel.requestClose();
                setFile(resultFile);
                readFile();
                if (okCallback != null) {
                    okCallback.run();
                }
            }
        });
        Runnable cancelAndCloseAction = () -> {
            viewModel.requestClose();
            if (cancelCallback != null) {
                cancelCallback.run();
            }
        };
        viewModel.getCancel().setAction(cancelAndCloseAction);
        viewModel.closeActionProperty().set(cancelAndCloseAction);
        getMediator().addFileChooserDialog(viewModel);
    }

    /**
     * Returns the list of filters or null.
     *
     * @return
     */
    List<ExtensionFilter> createOpenExtensionFilters();

    void readFile();

    // todo: remove
    AppearanceSettings getAppearanceSettings();

    // todo: remove
    HistoryManager getHistoryManager();
}
