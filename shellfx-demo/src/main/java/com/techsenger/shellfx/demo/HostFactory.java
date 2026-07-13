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

package com.techsenger.shellfx.demo;

import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.patternfx.core.HistoryProvider;
import com.techsenger.shellfx.core.ShellFxView;
import com.techsenger.shellfx.core.area.AreaParams;
import com.techsenger.shellfx.demo.shared.DockableTabFxView;
import com.techsenger.shellfx.demo.shared.DockableTabParams;
import com.techsenger.shellfx.demo.shared.DockableTabPresenter;
import com.techsenger.shellfx.layout.dockhost.DockHostFxView;
import com.techsenger.shellfx.layout.dockhost.DockHostHistory;
import com.techsenger.shellfx.layout.dockhost.DockHostParams;
import com.techsenger.shellfx.layout.dockhost.DockHostPresenter;
import com.techsenger.shellfx.layout.dockhost.SideBarPolicy;
import com.techsenger.shellfx.layout.dockhost.TabDockFxView;
import com.techsenger.shellfx.layout.tabhost.TabHostFxView;
import com.techsenger.shellfx.layout.tabhost.TabHostPresenter;

/**
 *
 * @author Pavel Castornii
 */
public final class HostFactory {

    public static TabHostFxView<?> createTabHost() {
        var view = new TabHostFxView<>(true);
        var params = new AreaParams();
        params.setHistoryPolicy(HistoryPolicy.NONE);
        var presenter = new TabHostPresenter<>(view, params);
        presenter.initialize();
        return view;
    }

    public static DockHostFxView<?> createDockHost(ShellFxView<?> shell,
            HistoryProvider<DockHostHistory> historyProvider) {
        var view = new DockHostFxView<>() {
            public class Composer extends DockHostFxView<?>.Composer {

                @Override
                public TabDockFxView<?> createTabDock() {
                    var tabDock = super.createTabDock();
                    tabDock.getPresenter().setMinimizable(true);
                    tabDock.getPresenter().setDraggable(true);
                    return tabDock;
                }
            }

            @Override
            protected DockHostFxView.Composer createComposer() {
                return new Composer();
            }
        };
        var params = new DockHostParams(historyProvider);
        params.setHistoryPolicy(HistoryPolicy.NONE);
        var presenter = new DockHostPresenter<>(view, params);
        presenter.initialize();
        view.getComposer().setBottomBarPolicy(SideBarPolicy.EXISTS_ALWAYS);
        return view;
    }

    public static TabDockFxView<?> createLeftTabDock(ShellFxView<?> shell, DockHostFxView<?> dockHost) {
        var leftTabDock = dockHost.getComposer().createTabDock();
        leftTabDock.getPresenter().setDraggable(true);
        leftTabDock.getPresenter().setMinimizable(true);
        fillTabs(shell, leftTabDock);
        leftTabDock.selectTab(0);
        return leftTabDock;
    }

    private static void fillTabs(ShellFxView<?> shell, TabDockFxView<?> tabDock) {
        for (var i = 0; i < 10; i++) {
            var tabView = new DockableTabFxView(shell);
            var tabParams = new DockableTabParams(i + 1);
            var tabPresenter = new DockableTabPresenter(tabView, tabParams);
            tabPresenter.initialize();
            tabDock.getComposer().addTab(tabView);
        }
    }

    private HostFactory() {
        // empty
    }
}
