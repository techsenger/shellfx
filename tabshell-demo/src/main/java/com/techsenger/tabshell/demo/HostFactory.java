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

package com.techsenger.tabshell.demo;

import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.patternfx.core.HistoryProvider;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.demo.shared.DockableTabFxView;
import com.techsenger.tabshell.demo.shared.DockableTabPresenter;
import com.techsenger.tabshell.layout.dockhost.DockHostFxView;
import com.techsenger.tabshell.layout.dockhost.DockHostHistory;
import com.techsenger.tabshell.layout.dockhost.DockHostPresenter;
import com.techsenger.tabshell.layout.dockhost.SideBarPolicy;
import com.techsenger.tabshell.layout.dockhost.TabDockFxView;
import com.techsenger.tabshell.layout.tabhost.TabHostFxView;
import com.techsenger.tabshell.layout.tabhost.TabHostPresenter;
import javafx.geometry.Orientation;

/**
 *
 * @author Pavel Castornii
 */
public final class HostFactory {

    public static TabHostFxView<?> createTabHost() {
        var view = new TabHostFxView<>(true);
        var presenter = new TabHostPresenter<>(view);
        presenter.setHistoryPolicy(HistoryPolicy.NONE);
        presenter.initialize();
        return view;
    }

    public static DockHostFxView<?> createDockHost(ShellFxView<?> shell,
            HistoryProvider<DockHostHistory> historyProvider) {
        var view = new DockHostFxView<>();
        var presenter = new DockHostPresenter<>(view, historyProvider);
        presenter.setHistoryPolicy(HistoryPolicy.NONE);
        presenter.initialize();
        view.getComposer().setBottomBarPolicy(SideBarPolicy.EXISTS_ALWAYS);

        var leftTabDock = view.getComposer().createTabDock();
        leftTabDock.getPresenter().initialize();
        leftTabDock.getPresenter().setDraggable(true);
        fillTabs(shell, leftTabDock);
        leftTabDock.selectTab(0);

        var splitSpace = view.getComposer().createSplitSpace();
        splitSpace.getPresenter().initialize();
        splitSpace.getPresenter().setOrientation(Orientation.HORIZONTAL);
        view.getComposer().setRoot(splitSpace);
        splitSpace.getComposer().addChild(leftTabDock);
        return view;
    }

    private static void fillTabs(ShellFxView<?> shell, TabDockFxView<?> tabDock) {
        for (var i = 0; i < 10; i++) {
            var tabView = new DockableTabFxView(shell);
            var tabPresenter = new DockableTabPresenter(tabView, i + 1);
            tabPresenter.initialize();
            tabDock.getComposer().addTab(tabView);
        }
    }

    private HostFactory() {
        // empty
    }
}
