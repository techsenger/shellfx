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
import com.techsenger.tabshell.terminal.TerminalComponents;
import com.techsenger.tabshell.terminal.TerminalPaletteType;
import java.util.Arrays;

/**
 *
 * @author Pavel Castornii
 */
public class ToolBarPresenter<V extends ToolBarView, C extends AreaComposer>
        extends AbstractAreaPresenter<V, C> {

    protected class Port extends AbstractAreaPresenter<V, C>.Port implements ToolBarPort {

        private ToolBarPresenter<V, C> presenter = ToolBarPresenter.this;

        @Override
        public TerminalPaletteType getPaletteType() {
            return getView().getPaletteType();
        }

        @Override
        public void setCopyDisable(boolean value) {
            getView().setCopyDisable(value);
        }

        @Override
        public void setListener(ToolBarListener listener) {
            presenter.listener = listener;
        }
    }

    private ToolBarListener listener;

    public ToolBarPresenter(V view, ToolBarHistory history) {
        super(view);
        setHistoryPolicy(HistoryPolicy.ALL);
        setHistoryProvider(() -> history);
    }

    @Override
    public Port getPort() {
        return (Port) super.getPort();
    }

    protected void onAddNew() {
        this.listener.onAddNew();
    }

    protected void onClear() {
        this.listener.onClear();
    }

    protected void onCopy() {
        this.listener.onCopy();
    }

    protected void onPaste() {
        this.listener.onPaste();
    }

    protected void onSelectAll() {
        this.listener.onSelectAll();
    }

    protected void onOpenUrl() {

    }

    protected void onFind() {

    }

    protected void onPageUp() {
        this.listener.onPageUp();
    }

    protected void onPageDown() {
        this.listener.onPageDown();
    }

    protected void onLineUp() {
        this.listener.onLineUp();
    }

    protected void onLineDown() {
        this.listener.onLineDown();
    }

    protected void onPaletteTypeChanged(TerminalPaletteType type) {
        this.listener.onPaletteTypeChanged(type);
    }

    @Override
    protected Port createPort() {
        return new ToolBarPresenter.Port();
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(TerminalComponents.TOOL_BAR);
    }

    @Override
    protected void preInitialize() {
        super.preInitialize();
        getView().setPaletteTypes(Arrays.stream(TerminalPaletteType.values()).toList());
        getView().setPaletteType(TerminalPaletteType.THEME_32_LC);
    }

    @Override
    protected ToolBarHistory getHistory() {
        return (ToolBarHistory) super.getHistory();
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
