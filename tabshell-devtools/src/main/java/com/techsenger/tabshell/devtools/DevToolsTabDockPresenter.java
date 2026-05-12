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

package com.techsenger.tabshell.devtools;

import com.techsenger.connectorfx.Connector;
import com.techsenger.connectorfx.Highlight;
import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.settings.SettingsSubscription;
import com.techsenger.tabshell.core.settings.ShellSettings;
import static com.techsenger.tabshell.devtools.DevToolsHostType.WINDOW;
import com.techsenger.tabshell.layout.dockhost.TabDockPresenter;
import com.techsenger.tabshell.material.theme.Theme;
import com.techsenger.toolkit.fx.color.ColorUtils;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public class DevToolsTabDockPresenter<V extends DevToolsTabDockView>
        extends TabDockPresenter<V> implements DevToolsTabDockPort {

    private final ShellSettings settings;

    private final SettingsSubscription themeSubscription;

    private boolean selectionSelected;

    private final Selector selector;

    private DevToolsHostType hostType;

    public DevToolsTabDockPresenter(V view, DevToolsHostType hostType, ShellSettings settings,
            HistoryManager historyManager) {
        super(view);
        this.hostType = hostType;
        this.settings = settings;
        themeSubscription = settings.getAppearance().onThemeChanged((oldV, newV) -> updateHighlight(newV));
        this.selector = new Selector(view.getConnector());
        this.setHistoryProvider(() -> historyManager
                .getOrCreateHistory(DevToolsTabDockHistory.class, DevToolsTabDockHistory::new));
        this.setHistoryPolicy(HistoryPolicy.APPEARANCE);
    }

    public boolean isSelectionSelected() {
        return selectionSelected;
    }

    public void setSelectionSelected(boolean selectionSelected) {
        this.selectionSelected = selectionSelected;
        getView().setSelectionSelected(selectionSelected);
        this.selector.setSelectionVisible(selectionSelected);
    }

    @Override
    public int getWindowUid() {
        return getView().getWindowUid();
    }

    @Override
    public Connector getConnector() {
        return getView().getConnector();
    }

    @Override
    public Selector getSelector() {
        return this.selector;
    }

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

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DevToolsComponents.TAB_DOCK);
    }

    protected void onSelect() {
        var connector = getView().getConnector();
        var opts = connector.getOptions();
        if (!opts.isInspectMode()) {
            if (!isSelectionSelected()) {
                setSelectionSelected(true);
            }
            this.selector.clearSelection(getView().getWindowUid());
            opts.setInspectMode(true); // enable the mode only after clearing selection
        }
    }

    protected void onSelection(boolean selected) {
        this.selectionSelected = selected;
        this.selector.setSelectionVisible(selected);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setHostType(hostType);
        var connector = getView().getConnector();
        connector.start();
        updateHighlight(this.settings.getAppearance().getTheme());
    }

    @Override
    protected void preDeinitialize() {
        super.preDeinitialize();
        getView().getConnector().stop();
        themeSubscription.unsubscribe();
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
    protected DevToolsTabDockHistory getHistory() {
        return (DevToolsTabDockHistory) super.getHistory();
    }

    @Override
    protected void saveAppearance() {
        super.saveAppearance();
        var h = getHistory();
        h.setSelectionSelected(isSelectionSelected());
    }

    @Override
    protected void restoreAppearance() {
        super.restoreAppearance();
        var h = getHistory();
        setSelectionSelected(h.isSelectionSelected());
    }

    private void updateHighlight(Theme theme) {
        var highlight = createHighlight(theme);
        getView().getConnector().setHighlight(highlight);
    }
}
