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

package com.techsenger.tabshell.demo.menu.file;

import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.demo.Density;
import com.techsenger.tabshell.demo.browser.BrowserMainTabFxView;
import com.techsenger.tabshell.demo.browser.BrowserMainTabParams;
import com.techsenger.tabshell.demo.browser.BrowserMainTabPresenter;
import com.techsenger.tabshell.demo.ide.IdeMainTabFxView;
import com.techsenger.tabshell.demo.ide.IdeMainTabPresenter;
import com.techsenger.tabshell.demo.menu.AbstractContainerItemHandler;
import com.techsenger.tabshell.layout.tabhost.TabHostFxView;
import com.techsenger.tabshell.material.menu.ManagedMenuItem;

/**
 *
 * @author Pavel Castornii
 */
public class MainTabItemHandler extends AbstractContainerItemHandler {

    public MainTabItemHandler(ManagedMenuItem item, ShellFxView<?> component) {
        super(item, component);
    }

    @Override
    public void onAction() {
        var shell = getComponent();
        if (shell.getComposer().getWorkspace() instanceof TabHostFxView<?>) {
            var tabView = new BrowserMainTabFxView(shell) {
                {
                    getWrapperPane().getStyleClass().add(Density.STYLE_CLASS); // see Density javadoc
                }
            };
            var tabParams = new BrowserMainTabParams(shell.getPresenter().getContext().getHistoryManager());
            var tabPresenter = new BrowserMainTabPresenter(tabView, tabParams);
            tabPresenter.initialize();
            resolveMainTabContainer().getComposer().addTab(tabView);
        } else {
            var tabView = new IdeMainTabFxView<>(shell) {
                {
                    getWrapperPane().getStyleClass().add(Density.STYLE_CLASS); // see Density javadoc
                }
            };
            var tabPresenter = new IdeMainTabPresenter<>((IdeMainTabFxView<?>) tabView);
            tabPresenter.initialize();
            resolveMainTabContainer().getComposer().addTab(tabView);
        }
    }

}
