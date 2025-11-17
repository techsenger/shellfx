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

import com.techsenger.mvvm4fx.core.AbstractParentViewModel;
import com.techsenger.mvvm4fx.core.ComponentDescriptor;
import com.techsenger.mvvm4fx.core.HistoryPolicy;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.menu.MenuHelper;
import com.techsenger.tabshell.core.menu.MenuItemHelper;
import com.techsenger.tabshell.core.settings.Settings;
import com.techsenger.tabshell.core.tab.ShellTabViewModel;
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.tabshell.material.menu.MenuItemName;
import com.techsenger.tabshell.material.menu.MenuName;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * This class extends ParentViewModel but its interface doesn't because of encapsulation.
 *
 * @author Pavel Castornii
 */
public class DefaultShellViewModel extends AbstractParentViewModel implements ShellViewModel {

    private ShellClosedCallback onClosed;

    private final ReadOnlyObjectWrapper<ShellTabViewModel> selectedTab = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyIntegerWrapper selectedTabIndex = new ReadOnlyIntegerWrapper();

    private ObservableList<ShellTabViewModel> modifiableTabs = FXCollections.observableArrayList();

    private ObservableList<? extends ShellTabViewModel> tabs =
            FXCollections.unmodifiableObservableList(modifiableTabs);

    private ReadOnlyDoubleWrapper width = new ReadOnlyDoubleWrapper();

    private ReadOnlyDoubleWrapper height = new ReadOnlyDoubleWrapper();

    private ReadOnlyBooleanWrapper maximized = new ReadOnlyBooleanWrapper();

    private ReadOnlyIntegerWrapper dialogCount = new ReadOnlyIntegerWrapper();

    private final StringProperty title = new SimpleStringProperty();

    private final ObjectProperty<Icon<?>> icon = new SimpleObjectProperty<>();

    private final Map<MenuName, MenuHelper> menuHelpersByName = new HashMap<>();

    private final Map<MenuItemName, MenuItemHelper> menuItemHelpersByName = new HashMap<>();

    private final ObservableSource<Boolean> closeRequested = new SimpleObservableSource<>();

    private final Settings settings;

    private final HistoryManager historyManager;

    /**
     * The width of the stage if the stage is not maximized.
     */
    private double defaultWidth = 1200;

    /**
     * The height of the stage if the stage is not maximized.
     */
    private double defaultHeight = 800;

    public DefaultShellViewModel(Settings settings, HistoryManager historyManager) {
        super();
        this.settings = settings;
        this.historyManager = historyManager;
        getDescriptor().setHistoryPolicy(HistoryPolicy.APPEARANCE);
        setHistoryProvider(() -> historyManager
                .getOrCreateHistory(DefaultShellHistory.class, DefaultShellHistory::new));
    }

    @Override
    public ShellClosedCallback getOnClosed() {
        return onClosed;
    }

    @Override
    public void setOnClosed(ShellClosedCallback onClose) {
        this.onClosed = onClose;
    }

    @Override
    public ReadOnlyObjectProperty<ShellTabViewModel> selectedTabProperty() {
        return this.selectedTab.getReadOnlyProperty();
    }

    @Override
    public ShellTabViewModel getSelectedTab() {
        return this.selectedTab.get();
    }

    @Override
    public void selectTab(ShellTabViewModel tabViewModel) {
        var tabIndex = modifiableTabs.indexOf(tabViewModel);
        if (tabIndex >= 0) {
            selectTab(tabIndex);
        }
    }

    @Override
    public void selectTab(int tabIndex) {
        if (tabIndex >= 0 && tabIndex < this.modifiableTabs.size())  {
            this.selectedTabIndex.set(tabIndex);
        }
    }

    @Override
    public int getSelectedTabIndex() {
        return this.selectedTabIndex.get();
    }

    @Override
    public ReadOnlyIntegerProperty selectedTabIndexProperty() {
        return this.selectedTabIndex.getReadOnlyProperty();
    }

    @Override
    public ObservableList<? extends ShellTabViewModel> getTabs() {
        return this.tabs;
    }

    @Override
    public ReadOnlyDoubleProperty widthProperty() {
        return this.width.getReadOnlyProperty();
    }

    @Override
    public double getWidth() {
        return widthProperty().get();
    }

    @Override
    public ReadOnlyDoubleProperty heightProperty() {
        return this.height.getReadOnlyProperty();
    }

    @Override
    public double getHeight() {
        return heightProperty().get();
    }

    @Override
    public StringProperty titleProperty() {
        return this.title;
    }

    @Override
    public String getTitle() {
        return this.title.get();
    }

    @Override
    public void setTitle(String title) {
        this.title.set(title);
    }

    @Override
    public ObjectProperty<Icon<?>> iconProperty() {
        return this.icon;
    }

    @Override
    public Icon<?> getIcon() {
        return this.icon.get();
    }

    @Override
    public void setIcon(Icon<?> icon) {
        this.icon.set(icon);
    }

    @Override
    public Settings getSettings() {
        return settings;
    }

    @Override
    public <T extends Settings> T getSettings(Class<T> settingsClass) {
        return (T) this.settings;
    }

    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    public ReadOnlyBooleanProperty maximizedProperty() {
        return this.maximized.getReadOnlyProperty();
    }

    @Override
    public boolean isMaximized() {
        return maximizedProperty().get();
    }

    @Override
    public ReadOnlyIntegerProperty dialogCountProperty() {
        return this.dialogCount.getReadOnlyProperty();
    }

    @Override
    public int getDialogCount() {
        return this.dialogCount.get();
    }

    @Override
    public void addMenuHelpers(MenuHelper... menuHelpers) {
        for (var h : menuHelpers) {
            this.menuHelpersByName.put(h.getMenuName(), h);
        }
    }

    @Override
    public void removeMenuHelpers(MenuName... menuNames) {
        for (var k : menuNames) {
            this.menuHelpersByName.remove(k);
        }
    }

    @Override
    public void addMenuItemHelpers(MenuItemHelper... itemHelpers) {
        for (var h : itemHelpers) {
            this.menuItemHelpersByName.put(h.getItemName(), h);
        }
    }

    @Override
    public void removeMenuItemHelpers(MenuItemName... itemNames) {
        for (var k : itemNames) {
            this.menuItemHelpersByName.remove(k);
        }
    }

    @Override
    public void requestClose() {
        this.closeRequested.next(Boolean.TRUE);
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(CoreComponentNames.SHELL);
    }

    protected Map<MenuName, MenuHelper> getMenuHelpersByName() {
        return menuHelpersByName;
    }

    protected Map<MenuItemName, MenuItemHelper> getMenuItemHelpersByName() {
        return menuItemHelpersByName;
    }

    ReadOnlyObjectWrapper<ShellTabViewModel> selectedTabWrapper() {
        return this.selectedTab;
    }

    ReadOnlyIntegerWrapper selectedTabIndexWrapper() {
        return selectedTabIndex;
    }

    ReadOnlyDoubleWrapper widthWrapper() {
        return width;
    }

    ReadOnlyDoubleWrapper heightWrapper() {
        return height;
    }

    ReadOnlyBooleanWrapper maximizedWrapper() {
        return maximized;
    }

    ReadOnlyIntegerWrapper dialogCountWrapper() {
        return dialogCount;
    }

    ObservableList<ShellTabViewModel> getModifiableTabs() {
        return modifiableTabs;
    }

    double getDefaultWidth() {
        return defaultWidth;
    }

    void setDefaultWidth(double defaultWidth) {
        this.defaultWidth = defaultWidth;
    }

    double getDefaultHeight() {
        return defaultHeight;
    }

    void setDefaultHeight(double defaultHeight) {
        this.defaultHeight = defaultHeight;
    }

    ObservableSource<Boolean> closeRequestedSource() {
        return closeRequested;
    }
}
