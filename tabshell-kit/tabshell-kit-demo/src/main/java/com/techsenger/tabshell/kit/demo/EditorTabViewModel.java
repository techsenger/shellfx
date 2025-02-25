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

package com.techsenger.tabshell.kit.demo;

import com.techsenger.mvvm4fx.core.HistoryPolicy;
import com.techsenger.tabshell.core.TabShellViewModel;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.core.menu.SimpleMenuItemHelper;
import com.techsenger.tabshell.core.tab.ShellTabKey;
import com.techsenger.tabshell.kit.core.file.FileInfo;
import com.techsenger.tabshell.kit.core.file.FileTaskProvider;
import com.techsenger.tabshell.kit.dialog.alert.AlertDialogType;
import com.techsenger.tabshell.kit.dialog.alert.AlertDialogViewModel;
import com.techsenger.tabshell.kit.text.editor.AbstractEditorTabViewModel;
import com.techsenger.tabshell.kit.text.menu.EditMenuKeys;
import com.techsenger.tabshell.kit.text.style.TextIcons;
import com.techsenger.tabshell.material.icon.FontIcon;
import java.util.List;
import javafx.stage.FileChooser;

/**
 *
 * @author Pavel Castornii
 */
public class EditorTabViewModel extends AbstractEditorTabViewModel {

    EditorTabViewModel(TabShellViewModel tabShell, FileInfo fileInfo,
            FileTaskProvider<String> fileTaskProvider) {
        super(tabShell, fileInfo, fileTaskProvider);
        //the initial history is created using a factory instead of reflection in the history manager to avoid
        //access issues with hidden packages
        setHistoryPolicy(HistoryPolicy.ALL);
        setHistoryProvider(() -> tabShell.getHistoryManager()
                .getHistory(EditorTabHistory.class, EditorTabHistory::new));
        setIcon(new FontIcon(TextIcons.EDITOR));

        //these validators will be used when menu is shown
        addMenuItemHelpers(new SimpleMenuItemHelper(EditMenuKeys.REPLACE, null, true));
        addMenuItemHelpers(new SimpleMenuItemHelper(EditMenuKeys.GO_TO_LINE, null, true));
    }

    @Override
    public ShellTabKey getKey() {
        return DemoComponentKeys.EDITOR_TAB;
    }

    @Override
    public String getDefaultExtension() {
        return null;
    }

    @Override
    public List<FileChooser.ExtensionFilter> getExtensionFilters() {
        return null;
    }

    @Override
    public String resolveDefaultExtension(FileChooser.ExtensionFilter filter) {
        return null;
    }

    void showInfo() {
        //shell scope!
        var viewModel = new AlertDialogViewModel(DialogScope.SHELL, AlertDialogType.INFO, "That is a message.");
        getComponentHelper().openAlertDialog(viewModel);
    }
}
