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

package com.techsenger.shellfx.demo.controls;

import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.demo.browser.BrowserMainTabParams;
import com.techsenger.shellfx.demo.browser.BrowserMainTabView;
import com.techsenger.shellfx.demo.browser.BrowserMainTabViewModel;
import com.techsenger.shellfx.demo.ide.IdeMainTabView;
import com.techsenger.shellfx.demo.ide.IdeMainTabViewModel;
import com.techsenger.shellfx.layout.tabhost.TabHostView;
import com.techsenger.shellfx.material.menu.ManagedMenuItem;

/**
 *
 * @author Pavel Castornii
 */
public class MainTabItemHandler extends AbstractContainerItemHandler {

    public MainTabItemHandler(ShellView<?> component, ManagedMenuItem item) {
        super(component, item);
    }

    @Override
    public void onAction() {
        var shell = getComponent();
        if (shell.getComposer().getWorkspace() instanceof TabHostView<?>) {
            var tabParams = new BrowserMainTabParams(shell.getViewModel().getContext().getHistoryManager());
            var tabViewModel = new BrowserMainTabViewModel<>(tabParams);
            var tabView = new BrowserMainTabView<>(tabViewModel, shell);
            tabView.initialize();
            resolveMainTabContainer().getComposer().addTab(tabView);
        } else {
            var tabViewModel = new IdeMainTabViewModel<>();
            var tabView = new IdeMainTabView<>(tabViewModel, shell);
            tabView.initialize();
            resolveMainTabContainer().getComposer().addTab(tabView);
        }
    }
}
