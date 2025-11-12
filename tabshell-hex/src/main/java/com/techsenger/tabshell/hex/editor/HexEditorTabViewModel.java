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

import com.techsenger.tabshell.hex.model.HexDocument;
import com.techsenger.mvvm4fx.core.ComponentDescriptor;
import com.techsenger.tabshell.core.ShellViewModel;
import com.techsenger.tabshell.core.tab.AbstractShellTabViewModel;
import com.techsenger.tabshell.dialogs.StandardDialogMediator;
import com.techsenger.tabshell.dialogs.file.ExtensionFilter;
import com.techsenger.tabshell.dialogs.file.FileOpenerViewModel;
import com.techsenger.tabshell.dialogs.file.FileSaverViewModel;
import com.techsenger.tabshell.hex.HexComponentNames;
import com.techsenger.tabshell.hex.inspector.DataInspectorTabViewModel;
import com.techsenger.tabshell.hex.style.HexIcons;
import com.techsenger.tabshell.layout.dock.DockLayoutHistory;
import com.techsenger.tabshell.layout.dock.DockLayoutViewModel;
import com.techsenger.tabshell.storage.GenericFile;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 *
 * @author Pavel Castornii
 */
public class HexEditorTabViewModel extends AbstractShellTabViewModel
        implements FileOpenerViewModel, FileSaverViewModel {

    private HexToolBarViewModel toolBar;

    private DockLayoutViewModel layout;

    private HexAreaViewModel area;

    private final ReadOnlyObjectWrapper<DataInspectorTabViewModel> dataInspector = new ReadOnlyObjectWrapper<>();

    private final HexDocument document;

    public HexEditorTabViewModel(ShellViewModel shell, GenericFile file) {
        super(shell);
        setIcon(HexIcons.EDITOR);
        setTitle("Hex Editor");
        setHistoryProvider(() -> shell.getHistoryManager()
                .getOrCreateHistory(HexEditorTabHistory.class, HexEditorTabHistory:: new));
        this.document = new HexDocument(file);
    }

    public HexToolBarViewModel getToolBar() {
        return toolBar;
    }

    public DockLayoutViewModel getLayout() {
        return layout;
    }

    public HexAreaViewModel getArea() {
        return area;
    }

    public DataInspectorTabViewModel getDataInspector() {
        return dataInspector.get();
    }

    public ReadOnlyObjectProperty<DataInspectorTabViewModel> dataInspectorProperty() {
        return dataInspector.getReadOnlyProperty();
    }

    @Override
    public void readFile() {
        this.area.getCaret().setDisabled(true);
        if (this.document.readFile()) {
            this.area.updateOnNewFile();
            if (getDataInspector() != null) {
                getDataInspector().updateTypeItems();
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
    public StandardDialogMediator getMediator() {
        return (StandardDialogMediator) super.getMediator();
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(HexComponentNames.HEX_EDITOR_TAB);
    }

    protected void createComponents() {
        HexEditorTabHistory<?> history = (HexEditorTabHistory<?>) getHistoryProvider().provide();
        this.toolBar = createToolBar();
        this.layout = createLayout(history.getDockLayout());
        this.area = createArea();
        this.dataInspector.set(createDataInspector());
    }

    protected HexToolBarViewModel createToolBar() {
        return new HexToolBarViewModel();
    }

    protected DockLayoutViewModel createLayout(DockLayoutHistory<?> history) {
        return new DockLayoutViewModel(history);
    }

    protected HexAreaViewModel createArea() {
        return new HexAreaViewModel(toolBar, getShell().getSettings().getAppearance(), this.document);
    }

    protected DataInspectorTabViewModel createDataInspector() {
        return new DataInspectorTabViewModel(this.document, this.area.getCaret().offsetProperty());
    }
}
