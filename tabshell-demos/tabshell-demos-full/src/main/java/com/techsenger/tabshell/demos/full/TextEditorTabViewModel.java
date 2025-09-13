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

package com.techsenger.tabshell.demos.full;

import com.techsenger.mvvm4fx.core.HistoryPolicy;
import com.techsenger.tabshell.core.ShellViewModel;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.core.history.DefaultClassHistoryProvider;
import com.techsenger.tabshell.core.menu.EditMenuKeys;
import com.techsenger.tabshell.core.menu.SimpleMenuItemHelper;
import com.techsenger.tabshell.core.tab.ShellTabKey;
import com.techsenger.tabshell.dialogs.alert.AlertDialogType;
import com.techsenger.tabshell.dialogs.alert.AlertDialogViewModel;
import com.techsenger.tabshell.dialogs.file.ExtensionFilter;
import com.techsenger.tabshell.dialogs.yesno.YesNoDialogViewModel;
import com.techsenger.tabshell.storage.GenericFile;
import com.techsenger.tabshell.text.editor.AbstractEditorTabViewModel;
import com.techsenger.tabshell.text.style.TextIcons;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public class TextEditorTabViewModel extends AbstractEditorTabViewModel {

    TextEditorTabViewModel(ShellViewModel shell, GenericFile file) {
        super(shell, file);
        //the initial history is created using a factory instead of reflection in the history manager to avoid
        //access issues with hidden packages
        setHistoryPolicy(HistoryPolicy.ALL);
        setHistoryProvider(new DefaultClassHistoryProvider<>(shell.getHistoryManager(),
                TextEditorTabHistory.class, TextEditorTabHistory::new));
        setIcon(TextIcons.EDITOR);

        //these validators will be used when menu is shown
        addMenuItemHelpers(new SimpleMenuItemHelper(EditMenuKeys.REPLACE, null, true));
        addMenuItemHelpers(new SimpleMenuItemHelper(EditMenuKeys.GO_TO_LINE, null, true));
    }

    @Override
    public ShellTabKey getKey() {
        return DemoComponentKeys.EDITOR_TAB;
    }

    void showInfoDialog() {
        var viewModel = new AlertDialogViewModel(DialogScope.SHELL, AlertDialogType.INFO,
                "All done! Time for coffee.");
        viewModel.setPrefWidth(400);
        getBridge().openAlertDialog(viewModel);
    }

    void showWarningDialog() {
        var viewModel = new AlertDialogViewModel(DialogScope.SHELL, AlertDialogType.WARNING,
                "Attention! You shouldn't do it!");
        viewModel.setPrefWidth(400);
        getBridge().openAlertDialog(viewModel);
    }

    void showErrorDialog() {
        var viewModel = new AlertDialogViewModel(DialogScope.SHELL, AlertDialogType.ERROR,
                "Oops! That didn’t work.");
        viewModel.setPrefWidth(400);
        getBridge().openAlertDialog(viewModel);
    }

    void showYesNoDialog() {
        var viewModel = new YesNoDialogViewModel(DialogScope.SHELL,
                "Are you really sure?");
        viewModel.setTitle("Confirm");
        viewModel.setYesText("Yes");
        viewModel.setNoText("No");
        viewModel.setPrefWidth(400);
        getBridge().openYesNoDialog(viewModel);
    }

    @Override
    public List<ExtensionFilter> createOpenExtensionFilters() {
        return List.of(
                new ExtensionFilter("All Files", true, "*.*"),
                new ExtensionFilter("Text Files", true, "*.txt")
        );
    }

    @Override
    public List<ExtensionFilter> createSaveExtensionFilters() {
        return List.of(
                new ExtensionFilter("All Files", true, "*.*"),
                new ExtensionFilter("Text Files", true, "*.txt")
        );
    }
}
