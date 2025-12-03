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

package com.techsenger.tabshell.hex.editor;

import com.techsenger.mvvm4fx.core.ComponentDescriptor;
import com.techsenger.tabshell.core.ShellViewModel;
import com.techsenger.tabshell.core.tab.AbstractShellTabViewModel;
import com.techsenger.tabshell.dialogs.file.ExtensionFilter;
import com.techsenger.tabshell.dialogs.file.FileOpenerViewModel;
import com.techsenger.tabshell.dialogs.file.FileSaverViewModel;
import com.techsenger.tabshell.hex.HexComponentNames;
import com.techsenger.tabshell.hex.model.HexDocument;
import com.techsenger.tabshell.hex.style.HexIcons;
import com.techsenger.tabshell.storage.GenericFile;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public class HexEditorTabViewModel extends AbstractShellTabViewModel
        implements FileOpenerViewModel, FileSaverViewModel {

    private final HexDocument document;

    public HexEditorTabViewModel(ShellViewModel shell, GenericFile file) {
        super(shell);
        setIcon(HexIcons.EDITOR);
        setTitle("Hex Editor");
        setHistoryProvider(() -> shell.getHistoryManager()
                .getOrCreateHistory(HexEditorTabHistory.class, HexEditorTabHistory:: new));
        this.document = new HexDocument(file);
    }

    @Override
    public void readFile() {
        var area = getMediator().getArea();
        area.getCaret().setDisabled(true);
        if (this.document.readFile()) {
            area.updateOnNewFile();
            var dataInspector = getMediator().getDataInspector();
            if (dataInspector != null) {
                dataInspector.updateTypeItems();
            }
        }
    }

    @Override
    public void writeFile() {
        this.document.writeFile();
    }

    @Override
    public GenericFile getFile() {
        return this.document.getFile();
    }

    @Override
    public void setFile(GenericFile file) {
        this.document.setFile(file);
    }

    @Override
    public List<ExtensionFilter> createOpenExtensionFilters() {
        return null;
    }

    @Override
    public List<ExtensionFilter> createSaveExtensionFilters() {
        return null;
    }

    public HexDocument getDocument() {
        return document;
    }

    @Override
    public HexEditorTabMediator getMediator() {
        return (HexEditorTabMediator) super.getMediator();
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(HexComponentNames.HEX_EDITOR_TAB);
    }
}
