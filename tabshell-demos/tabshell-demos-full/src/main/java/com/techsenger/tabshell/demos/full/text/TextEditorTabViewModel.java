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

package com.techsenger.tabshell.demos.full.text;

import com.techsenger.mvvm4fx.core.ComponentDescriptor;
import com.techsenger.mvvm4fx.core.HistoryPolicy;
import com.techsenger.tabshell.core.ShellViewModel;
import com.techsenger.tabshell.core.menu.EditMenuNames;
import com.techsenger.tabshell.core.menu.SimpleMenuItemHelper;
import com.techsenger.tabshell.demos.full.DemoComponentNames;
import com.techsenger.tabshell.dialogs.file.ExtensionFilter;
import com.techsenger.tabshell.storage.GenericFile;
import com.techsenger.tabshell.text.editor.AbstractEditorTabViewModel;
import com.techsenger.tabshell.text.style.TextIcons;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public class TextEditorTabViewModel extends AbstractEditorTabViewModel {

    public TextEditorTabViewModel(ShellViewModel shell, GenericFile file) {
        super(shell, file);
        //the initial history is created using a factory instead of reflection in the history manager to avoid
        //access issues with hidden packages
        getDescriptor().setHistoryPolicy(HistoryPolicy.ALL);
        setHistoryProvider(() -> (shell.getHistoryManager().getOrCreateHistory(TextEditorTabHistory.class,
                TextEditorTabHistory::new)));
        setIcon(TextIcons.EDITOR);

        //these validators will be used when menu is shown
        addMenuItemHelpers(new SimpleMenuItemHelper(EditMenuNames.REPLACE, null, true));
        addMenuItemHelpers(new SimpleMenuItemHelper(EditMenuNames.GO_TO_LINE, null, true));
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(DemoComponentNames.EDITOR_TAB);
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
