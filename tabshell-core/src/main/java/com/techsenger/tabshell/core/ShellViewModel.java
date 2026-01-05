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

package com.techsenger.tabshell.core;

import com.techsenger.patternfx.mvvmx.ParentViewModel;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.menu.MenuHelper;
import com.techsenger.tabshell.core.menu.MenuItemHelper;
import com.techsenger.tabshell.core.settings.Settings;
import com.techsenger.tabshell.core.tab.ShellTabViewModel;
import com.techsenger.tabshell.core.tab.TabContainerViewModel;
import com.techsenger.tabshell.material.menu.MenuItemName;
import com.techsenger.tabshell.material.menu.MenuName;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public interface ShellViewModel<T extends ShellMediator> extends ParentViewModel<T>,
        TabContainerViewModel<ShellTabViewModel<?>>, IconedViewModel, CloseableViewModel<T> {

    /**
     * Returns the history manager.
     *
     * @return
     */
    HistoryManager getHistoryManager();

    /**
     * Returns the settings of the shell.
     *
     * @return
     */
    Settings getSettings();

    /**
     * Returns the settings of the shell as an instance of the specified class using type casting.
     *
     * @return
     */
    <T extends Settings> T getSettings(Class<T> settingsClass);

    /**
     * Returns shell current width property.
     *
     * @return
     */
    ReadOnlyDoubleProperty widthProperty();

    /**
     * Returns shell current width.
     *
     * @return
     */
    double getWidth();

    /**
     * Returns shell current height property.
     *
     * @return
     */
    ReadOnlyDoubleProperty heightProperty();

    /**
     * Returns shell current height.
     *
     * @return
     */
    double getHeight();

    /**
     * Returns title property.
     *
     * @return
     */
    StringProperty titleProperty();

    /**
     * Returns title.
     *
     * @return
     */
    String getTitle();

    /**
     * Sets title.
     *
     * @param title
     */
    void setTitle(String title);

    /**
     * Indicates if the shell stage is maximized.
     *
     * @return
     */
    ReadOnlyBooleanProperty maximizedProperty();

    /**
     * Returns true if the shell stage is maximized, otherwise false.
     *
     * @return
     */
    boolean isMaximized();

    /**
     * Property that shows the count of open dialogs.
     *
     * @return
     */
    ReadOnlyIntegerProperty dialogCountProperty();

    /**
     * Returns the count of open dialogs.
     *
     * @return
     */
    int getDialogCount();

    /**
     * Adds menu helpers which are used when there are no tabs.
     *
     * @param menuHelpers
     */
    void addMenuHelpers(MenuHelper... menuHelpers);

    /**
     * Removes menu helpers which are used when there are no tabs.
     *
     * @param menuNames
     */
    void removeMenuHelpers(MenuName... menuNames);

    /**
     * Adds menu item helpers which are used when there are no tabs.
     *
     * @param itemHelpers
     */
    void addMenuItemHelpers(MenuItemHelper... itemHelpers);

    /**
     * Removes menu item helpers which are used when there are no tabs.
     *
     * @param itemNames
     */
    void removeMenuItemHelpers(MenuItemName... itemNames);
}
