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

package com.techsenger.tabshell.demo.menu;

import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.demo.styles.StylesTabFxView;
import com.techsenger.tabshell.demo.styles.StylesTabPresenter;
import com.techsenger.tabshell.material.menu.ManagedMenuItem;

/**
 *
 * @author Pavel Castornii
 */
public class StylesItemHandler extends AbstractContainerItemHandler {

    public StylesItemHandler(ManagedMenuItem item, ShellFxView<?> component) {
        super(item, component);
    }

    @Override
    public void onAction() {
        var shell = getComponent();
        var tabView = new StylesTabFxView(shell);
        var tabPresenter = new StylesTabPresenter(tabView);
        tabPresenter.initialize();
        resolveMainTabContainer().getComposer().addTab(tabView);
        tabView.requestFocus();
    }

}
