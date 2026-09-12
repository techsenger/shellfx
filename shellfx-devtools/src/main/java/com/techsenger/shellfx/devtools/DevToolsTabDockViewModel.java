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

package com.techsenger.shellfx.devtools;

import com.techsenger.connectorfx.Connector;
import com.techsenger.connectorfx.Highlight;
import com.techsenger.shellfx.core.history.HistoryManager;
import com.techsenger.shellfx.core.settings.SettingsSubscription;
import com.techsenger.shellfx.core.settings.ShellSettings;
import com.techsenger.shellfx.layout.dockhost.TabDockComposer;
import com.techsenger.shellfx.layout.dockhost.TabDockViewModel;
import com.techsenger.shellfx.material.theme.Theme;
import com.techsenger.toolkit.fx.color.ColorUtils;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Pavel Castornii
 */
public class DevToolsTabDockViewModel<C extends TabDockComposer> extends TabDockViewModel<C>
        implements FullDevToolsTabDockPort {

    private final BooleanProperty selectionSelected = new SimpleBooleanProperty();

    private final ShellSettings settings;

    private final HistoryManager historyManager;

    private final Connector connector;

    private final int shellWindowUid;

    private final Selector selector;

    private SettingsSubscription themeSubscription;

    private DevToolsHostType hostType;

    public DevToolsTabDockViewModel(DevToolsTabDockParams params) {
        super(params);
        this.hostType = params.getHostType();
        this.settings = params.getSettings();
        this.historyManager = params.getHistoryManager();
        this.connector = params.getConnector();
        this.shellWindowUid = params.getShellWindowUid();
        this.selector = new Selector(this.connector, getDescriptor().getLogPrefix());
        this.selectionSelected.addListener((ov, oldV, newV) -> this.selector.setSelectionVisible(newV));
    }

    @Override
    public boolean isSelectionSelected() {
        return selectionSelected.get();
    }

    public void setSelectionSelected(boolean selectionSelected) {
        this.selectionSelected.set(selectionSelected);
    }

    @Override
    public BooleanProperty selectionSelectedProperty() {
        return selectionSelected;
    }

    @Override
    public Connector getConnector() {
        return connector;
    }

    @Override
    public Selector getSelector() {
        return this.selector;
    }

    @Override
    public HistoryManager getHistoryManager() {
        return this.historyManager;
    }

    @Override
    public DevToolsHostType getHostType() {
        return hostType;
    }

    public void setHostType(DevToolsHostType hostType) {
        this.hostType = hostType;
        switch (hostType) {
            case SPLIT_SPACE -> {
                setClosable(true);
                setMinimizable(true);
            }
            case WINDOW -> {
                setClosable(false);
                setMinimizable(false);
            }
            case OTHER -> {
                setClosable(true);
                setMinimizable(false);
            }
            default -> throw new AssertionError();
        }
    }

    protected void onSelect() {
        var opts = connector.getOptions();
        if (!opts.isInspectMode()) {
            if (!isSelectionSelected()) {
                setSelectionSelected(true);
            }
            this.selector.clearSelection();
            opts.setInspectMode(true); // enable the mode only after clearing selection
        }
    }

    protected void onWindowSelected(int uid) {
        this.selector.selectWindow(uid);
    }

    protected Highlight createHighlight(Theme theme) {
        var pal = theme.getPalette();
        var baseBounds = Highlight.BoundsHighlight.builder()
                .fill(ColorUtils.toHex(pal.getSelectionBgColor()))
                .stroke(ColorUtils.toHex(pal.getSelectionBorderColor()))
                .strokeType("INSIDE")
                .opacity(0.5)
                .strokeDashArray(List.of(3.0, 3.0))
                .build();
        var inParentBounds = Highlight.BoundsHighlight.builder()
                .stroke(ColorUtils.toHex(pal.getSelectionBorderColor()))
                .strokeType("INSIDE")
                .opacity(0.8)
                .build();
        var baseline = Highlight.BaselineHighlight.builder()
                .stroke(ColorUtils.toHex(pal.getSelectionBorderColor()))
                .opacity(0.75)
                .build();
        return new Highlight(baseBounds, baseBounds, inParentBounds, baseline);
    }

    @Override
    protected void postInitialize() {
        // must happen before super.postInitialize(), since it composes the child tabs, and some of them
        // synchronously query the connector/selector while initializing
        setHostType(hostType);
        this.connector.start();
        updateHighlight(this.settings.getAppearance().getTheme());
        this.themeSubscription = this.settings.getAppearance().onThemeChanged((oldV, newV) -> updateHighlight(newV));
        // we start with shell window
        this.selector.setSelectedWindowUid(shellWindowUid);
        super.postInitialize();
    }

    @Override
    protected void postDeinitialize() {
        super.postDeinitialize();
        this.connector.stop();
        this.themeSubscription.unsubscribe();
    }

    @Override
    protected DevToolsTabDockHistory getHistory() {
        return (DevToolsTabDockHistory) super.getHistory();
    }

    @Override
    protected void savePersistentState() {
        super.savePersistentState();
        var h = getHistory();
        h.setSelectionSelected(isSelectionSelected());
    }

    @Override
    protected void restorePersistentState() {
        super.restorePersistentState();
        var h = getHistory();
        setSelectionSelected(h.isSelectionSelected());
    }

    private void updateHighlight(Theme theme) {
        var highlight = createHighlight(theme);
        this.connector.setHighlight(highlight);
    }
}
