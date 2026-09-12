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

import com.techsenger.patternfx.core.HistoryProvider;
import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.settings.AppearanceSettings;
import com.techsenger.shellfx.demo.shared.DockableTabParams;
import com.techsenger.shellfx.demo.shared.DockableTabView;
import com.techsenger.shellfx.demo.shared.DockableTabViewModel;
import com.techsenger.shellfx.layout.dockhost.DockHostHistory;
import com.techsenger.shellfx.layout.dockhost.DockHostParams;
import com.techsenger.shellfx.layout.dockhost.DockHostView;
import com.techsenger.shellfx.layout.dockhost.DockHostViewModel;
import com.techsenger.shellfx.layout.dockhost.SideBarPolicy;
import com.techsenger.shellfx.layout.dockhost.TabDockView;
import com.techsenger.shellfx.layout.tabhost.ProminentTabHostParams;
import com.techsenger.shellfx.layout.tabhost.ProminentTabHostView;
import com.techsenger.shellfx.layout.tabhost.ProminentTabHostViewModel;

/**
 *
 * @author Pavel Castornii
 */
public final class HostFactory {

    public static ProminentTabHostView<?> createProminentTabHost(AppearanceSettings settings) {
        var params = new ProminentTabHostParams(settings);
        var viewModel = new ProminentTabHostViewModel<>(params);
        var view = new ProminentTabHostView<>(viewModel);
        viewModel.getComposer().tabCountProperty().addListener((ov, oldV, newV) ->
                viewModel.setTabHeaderVisible(newV.intValue() > 0));
        view.initialize();
        viewModel.setTabHeaderVisible(false);
        return view;
    }

    public static DockHostView<?> createDockHost(ShellView<?> shell, HistoryProvider<DockHostHistory> historyProvider) {
        var params = new DockHostParams(historyProvider);
        var viewModel = new DockHostViewModel<>(params);
        var view = new DockHostView<>(viewModel) {
            public class Composer extends DockHostView<?>.Composer {

                @Override
                public TabDockView<?> createTabDock() {
                    var tabDock = super.createTabDock();
                    tabDock.getViewModel().setMinimizable(true);
                    tabDock.getViewModel().setDraggable(true);
                    return tabDock;
                }
            }

            @Override
            protected DockHostView.Composer createComposer() {
                return new Composer();
            }
        };
        view.initialize();
        view.getComposer().setBottomBarPolicy(SideBarPolicy.EXISTS_ALWAYS);
        return view;
    }

    public static TabDockView<?> createLeftTabDock(ShellView<?> shell, DockHostView<?> dockHost) {
        var leftTabDock = dockHost.getComposer().createTabDock();
        leftTabDock.getViewModel().setDraggable(true);
        leftTabDock.getViewModel().setMinimizable(true);
        fillTabs(shell, leftTabDock);
        leftTabDock.getViewModel().selectTab(0);
        return leftTabDock;
    }

    private static void fillTabs(ShellView<?> shell, TabDockView<?> tabDock) {
        for (var i = 0; i < 10; i++) {
            var tabParams = new DockableTabParams(i + 1);
            var tabViewModel = new DockableTabViewModel<>(tabParams);
            var tabView = new DockableTabView(tabViewModel, shell);
            tabView.initialize();
            tabDock.getComposer().addTab(tabView);
        }
    }

    private HostFactory() {
        // empty
    }
}
