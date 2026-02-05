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

package com.techsenger.tabshell.terminal.toolbar;

import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.area.AbstractAreaPresenter;
import com.techsenger.tabshell.core.area.AreaComposer;
import com.techsenger.tabshell.terminal.TerminalPaletteType;
import com.techsenger.tabshell.terminal.area.TerminalAreaPort;
import java.util.Arrays;
import java.util.function.Supplier;
import com.techsenger.tabshell.terminal.TerminalComponents;

/**
 *
 * @author Pavel Castornii
 */
public class TerminalToolBarPresenter<V extends TerminalToolBarView, C extends AreaComposer>
        extends AbstractAreaPresenter<V, C> {

    protected class Port extends AbstractAreaPresenter.Port implements TerminalToolBarPort {

        @Override
        public TerminalPaletteType getPaletteType() {
            return getView().getPaletteType();
        }

        @Override
        public void setCopyDisable(boolean value) {
            getView().setCopyDisable(value);
        }
    }

    private final Supplier<TerminalAreaPort> area;

    public TerminalToolBarPresenter(V view, TerminalToolBarHistory history, Supplier<TerminalAreaPort> area) {
        super(view);
        this.area = area;
        setHistoryPolicy(HistoryPolicy.ALL);
        setHistoryProvider(() -> history);
    }

    @Override
    public Port getPort() {
        return (Port) super.getPort();
    }

    protected void handleNewAction() {
        area.get().addNew();
    }

    protected void handleClearAction() {
        area.get().clear();
    }

    protected void handleCopyAction() {
        area.get().copy();
    }

    protected void handlePasteAction() {
        area.get().paste();
    }

    protected void handleSelectAllAction() {
        area.get().selectAll();
    }

    protected void handleOpenUrlAction() {

    }

    protected void handleFindAction() {

    }

    protected void handlePageUpAction() {
        area.get().scrollPageUp();
    }

    protected void handlePageDownAction() {
        area.get().scrollPageDown();
    }

    protected void handleLineUpAction() {
        area.get().scrollLineUp();
    }

    protected void handleLineDownAction() {
        area.get().scrollLineDown();
    }

    protected void handlePaletteTypeChanged(TerminalPaletteType type) {
        if (area.get() != null) {
            area.get().setPaletteType(type);
        }
    }

    @Override
    protected Port createPort() {
        return new TerminalToolBarPresenter.Port();
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(TerminalComponents.TERMINAL_TOOL_BAR);
    }

    @Override
    protected void preInitialize() {
        super.preInitialize();
        getView().setPaletteTypes(Arrays.stream(TerminalPaletteType.values()).toList());
        getView().setPaletteType(TerminalPaletteType.THEME_32_LC);
    }

    @Override
    protected TerminalToolBarHistory getHistory() {
        return (TerminalToolBarHistory) super.getHistory();
    }

    @Override
    protected void saveAppearance() {
        super.saveAppearance();
        var h = getHistory();
        h.setPaletteType(getView().getPaletteType());
    }

    @Override
    protected void restoreAppearance() {
        super.restoreAppearance();
        var h = getHistory();
        getView().setPaletteType(h.getPaletteType());
    }

}
