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

import com.techsenger.connectorfx.Highlight;
import com.techsenger.connectorfx.HighlightOptions;
import com.techsenger.connectorfx.event.NodeSelectedEvent;
import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.patternfx.core.HistoryProvider;
import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.settings.Settings;
import com.techsenger.tabshell.core.settings.SettingsSubscription;
import com.techsenger.tabshell.layout.dock.TabDockPresenter;
import com.techsenger.tabshell.layout.tabhost.TabHostComposer;
import com.techsenger.tabshell.material.theme.Theme;
import com.techsenger.toolkit.fx.color.ColorUtils;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class DevToolsTabDockPresenter<V extends DevToolsTabDockView, C extends TabHostComposer>
        extends TabDockPresenter<V, C> {

    protected class Port extends TabDockPresenter<V, C>.Port implements DevToolsTabDockPort {

        private final DevToolsTabDockPresenter<V, C> presenter = DevToolsTabDockPresenter.this;

        @Override
        public int getWindowUid() {
            return getView().getWindowUid();
        }

        @Override
        public HighlightOptions getHighlightOptions() {
            return presenter.highlightOptions;
        }

        @Override
        public void setOnSelection(Consumer<Boolean> action) {
            presenter.onSelection = action;
        }
    }

    private final Settings settings;

    private HighlightOptions highlightOptions = new HighlightOptions(true, false, false);

    private final SettingsSubscription themeSubscription;

    private Consumer<Boolean> onSelection;

    public DevToolsTabDockPresenter(V view, Settings settings, HistoryProvider<DevToolsTabDockHistory> hp) {
        super(view);
        this.settings = settings;
        themeSubscription = settings.getAppearance().observeTheme((oldV, newV) -> updateHighlight(newV));
        this.setHistoryProvider(hp);
        this.setHistoryPolicy(HistoryPolicy.APPEARANCE);
    }

    @Override
    public DevToolsTabDockPort getPort() {
        return (DevToolsTabDockPort) super.getPort();
    }

    @Override
    protected Port createPort() {
        return new DevToolsTabDockPresenter.Port();
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DevToolsComponents.TAB_DOCK);
    }

    protected void onSelect() {
        var connector = getView().getConnector();
        var opts = connector.getOptions();
        if (opts.isInspectMode()) {
            opts.setInspectMode(false);
        } else {
            if (!getView().isSelectionSelected()) {
                getView().setSelectionSelected(true);
            }
            connector.clearSelection(getView().getWindowUid());
            opts.setInspectMode(true);
        }
        updateHighlightOptions(getView().isSelectionSelected());
    }

    protected void onSelection(boolean selected) {
        var connector = getView().getConnector();
        if (!selected) {
            connector.getOptions().setInspectMode(false);
        }
        // in any case it is necessary to clear selection, to fire new events
        connector.clearSelection(getView().getWindowUid());
        updateHighlightOptions(selected);
        if (this.onSelection != null) {
            this.onSelection.accept(selected);
        }
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        var history = getHistory();
        if (history.isNew()) {
            getView().setSelectionSelected(true);
        }
        var connector = getView().getConnector();
        connector.start();
        connector.getEventBus().subscribe(NodeSelectedEvent.class, (e) -> {
            // When inspect mode is set to false the selection is removed because for selection
            // and inspect mode the same overlay. So, it is necessary to reselect the node.
            var selected = e.element();
            connector.getOptions().setInspectMode(false);
            if (selected != null) {
                connector.selectNode(getView().getWindowUid(), selected, highlightOptions);
            }
        });
        updateHighlight(this.settings.getAppearance().getTheme());
        updateHighlightOptions(getView().isSelectionSelected());
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
        var v = getView();
        h.setSelectionSelected(v.isSelectionSelected());
    }

    @Override
    protected void restoreAppearance() {
        super.restoreAppearance();
        var h = getHistory();
        var v = getView();
        v.setSelectionSelected(v.isSelectionSelected());
    }

    /**
     * From connector we can get node properties only when this node is selected. So, if we disable selection
     * we can't update node properties when user selects another node. As a workaround we don't disable selection
     * but use {@link HighlightOptions} with all {@code false}.
     */
    private void updateHighlightOptions(boolean selection) {
        if (selection) {
            this.highlightOptions = new HighlightOptions(true, false, false);
        } else {
            this.highlightOptions = new HighlightOptions(false, false, false);
        }
    }

    private void updateHighlight(Theme theme) {
        var highlight = createHighlight(theme);
        getView().getConnector().setHighlight(highlight);
    }
}
