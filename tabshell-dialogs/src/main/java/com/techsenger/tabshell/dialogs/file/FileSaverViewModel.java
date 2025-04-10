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

import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.storage.FileStorage;
import com.techsenger.tabshell.storage.GenericFile;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public interface FileSaverViewModel extends DialogClientViewModel {

    /**
     * Saves a file by displaying a file chooser dialog and writing the selected file.
     *
     * @param scope the scope of dialog
     * @param storages the storages or null.
     */
    default void saveFile(DialogScope scope, List<FileStorage> storages) {
        var file = getFile();
        var viewModel = new FileChooserDialogViewModel(scope, FileChooserType.SAVE_AS,
                getTabShell().getSettings().getAppearance(), storages,
                getTabShell().getHistoryManager());
        var filters = createSaveExtensionFilters();
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
        viewModel.okActionProperty().set(() -> {
            var resultFile = viewModel.getResultFile();
            if (resultFile != null) {
                viewModel.requestClose();
                setFile(resultFile);
                writeFile();
            }
        });
        getComponentHelper().openFileChooserDialog(viewModel);
    }

    List<ExtensionFilter> createSaveExtensionFilters();

    void writeFile();
}
