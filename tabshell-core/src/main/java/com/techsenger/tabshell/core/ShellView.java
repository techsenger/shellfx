/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.core;

import com.techsenger.mvvm4fx.core.View;
import com.techsenger.tabshell.core.dialog.DialogManager;
import com.techsenger.tabshell.core.menu.MenuAware;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.core.style.Stylesheet;
import com.techsenger.tabshell.core.tab.ShellTabView;
import com.techsenger.tabshell.core.tab.TabContainerView;
import java.util.List;

/**
 * There can be only one instance of Shell in VirtualMachine.
 *
 * @author Pavel Castornii
 */
public interface ShellView<T extends ShellViewModel> extends View<T>, TabContainerView<ShellTabView<?>>, MenuAware,
        CloseableView {

    @Override
    void openTab(ShellTabView<?> tabView);

    @Override
    void closeTab(ShellTabView<?> tabView);

    @Override
    ShellTabView<?> getSelectedTab();

    /**
     * Returns the control registry. There can be only one registry in the application.
     *
     * @return
     */
    ControlRegistry getControlRegistry();

    /**
     * Clears all existing elements in menu bar and adds new elements build using registry.
     *
     * @param registry
     */
    void upgradeMenuBar();

    /**
     * Forces shell to update the visibility of the elements in menu bar.
     */
    void updateMenuBar();

    /**
     * Returns dialog manager.
     *
     * @return
     */
    DialogManager getDialogManager();

    /**
     * Adds stylesheets to TabShell.
     *
     * @param sheets
     */
    void addStylesheets(List<Stylesheet> sheets);

    /**
     * Removes stylesheets from TabShell.
     *
     * @param sheets
     */
    void removeStylesheets(List<Stylesheet> sheets);

    @Override
    void close();
}
