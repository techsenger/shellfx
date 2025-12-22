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

import com.techsenger.patternfx.core.ComponentState;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.tab.AbstractShellTabViewModel;
import com.techsenger.tabshell.dialogs.file.ExtensionFilter;
import com.techsenger.tabshell.dialogs.file.FileOpenerViewModel;
import com.techsenger.tabshell.dialogs.file.FileSaverViewModel;
import com.techsenger.tabshell.hex.model.HexDocument;
import com.techsenger.tabshell.hex.style.HexIcons;
import com.techsenger.tabshell.storage.GenericFile;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class HexEditorTabViewModel<T extends HexEditorTabMediator> extends AbstractShellTabViewModel<T>
        implements FileOpenerViewModel, FileSaverViewModel {

    private final HexDocument document;

    public HexEditorTabViewModel(GenericFile file) {
        setIcon(HexIcons.EDITOR);
        setTitle("Hex Editor");
        this.document = new HexDocument(file);
    }

    @Override
    public void readFile() {
        var caret = getMediator().getArea().getMediator().getCaret();
        caret.setDisabled(true);
        if (this.document.readFile()) {
            getMediator().getArea().updateOnNewFile();
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
    public CloseCheckResult canClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void initialize() {
        super.initialize();
        getMediator().stateProperty().addListener((ov, oldV, newV) -> {
            if (newV == ComponentState.INITIALIZED) {
                readFile();
            }
        });
    }
}
