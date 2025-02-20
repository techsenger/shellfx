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

package com.techsenger.tabshell.core.tab;

import com.techsenger.mvvm4fx.core.AbstractChildViewModel;
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.tabshell.material.menu.KeyedMenuItemState;
import com.techsenger.tabshell.material.menu.KeyedMenuItemUpdate;
import com.techsenger.tabshell.material.menu.KeyedMenuState;
import com.techsenger.tabshell.material.menu.KeyedMenuUpdate;
import com.techsenger.tabshell.material.menu.MenuItemKey;
import com.techsenger.tabshell.material.menu.MenuKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractTabViewModel extends AbstractChildViewModel implements TabViewModel {

    private final StringProperty title = new SimpleStringProperty();

    private final ObjectProperty<Icon<?>> icon = new SimpleObjectProperty();

    private final StringProperty tooltip = new SimpleStringProperty();

    private final Set<MenuKey> supportedMenus = new HashSet<>();

    private final Map<MenuKey, Set<MenuItemKey>> supportedMenuItemsByMenu = new HashMap<>();

    private final BooleanProperty waiting = new SimpleBooleanProperty(false);

    private final ReadOnlyBooleanWrapper selected = new ReadOnlyBooleanWrapper();

    private TabClosedCallback onClosed;

    public AbstractTabViewModel() {
        super();
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
        return icon;
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
    public StringProperty tooltipProperty() {
        return tooltip;
    }

    @Override
    public String getTooltip() {
        return this.tooltip.get();
    }

    @Override
    public void setTooltip(String tooltip) {
        this.tooltip.set(tooltip);
    }

    @Override
    public ReadOnlyBooleanProperty selectedProperty() {
        return selected.getReadOnlyProperty();
    }

    @Override
    public boolean isSelected() {
        return selectedProperty().get();
    }

    @Override
    public void doOnMenuShowing(MenuKey menuKey) {

    }

    @Override
    public void doOnMenuHiding(MenuKey menuKey) {

    }

    @Override
    public boolean isMenuSupported(MenuKey menuKey) {
        return this.supportedMenus.contains(menuKey);
    }

    @Override
    public boolean isMenuItemSupported(MenuKey menuKey, MenuItemKey itemKey) {
        var itemKeys = this.getSupportedMenuItemsByMenu().get(menuKey);
        if (itemKeys != null) {
            return itemKeys.contains(itemKey);
        } else {
            return false;
        }
    }

    /**
     * This method is used very rarely.
     *
     * @param menuKey
     * @param menuState
     * @return
     */
    @Override
    public KeyedMenuUpdate updateMenu(MenuKey menuKey, KeyedMenuState menuState) {
        return null;
    }

    /**
     * This method is used very rarely.
     *
     * @param menuKey
     * @param itemKey
     * @param itemState
     * @return
     */
    @Override
    public KeyedMenuItemUpdate updateMenuItem(MenuKey menuKey, MenuItemKey itemKey, KeyedMenuItemState itemState) {
        return null;
    }

    @Override
    public void doOnSharedMenuItemAction(MenuKey menuKey, MenuItemKey itemKey) {
        //empty
    }

    @Override
    public void setOnClosed(TabClosedCallback closedCallback) {
        this.onClosed = closedCallback;
    }

    @Override
    public TabClosedCallback getOnClosed() {
        return this.onClosed;
    }

    public BooleanProperty waitingProperty() {
        return waiting;
    }

    public void setWaiting(boolean waiting) {
        this.waiting.set(waiting);
    }

    public boolean getWaiting() {
        return waiting.get();
    }

    protected Set<MenuKey> getSupportedMenus() {
        return supportedMenus;
    }

    protected Map<MenuKey, Set<MenuItemKey>> getSupportedMenuItemsByMenu() {
        return supportedMenuItemsByMenu;
    }

    protected void addSupportedMenus(MenuKey... menuKeys) {
        this.supportedMenus.addAll(Arrays.asList(menuKeys));
    }

    protected void addSupportedMenuItems(MenuKey menuKey, MenuItemKey... itemKeys) {
        Set<MenuItemKey> itemKeysSet = this.supportedMenuItemsByMenu.get(menuKey);
        if (itemKeysSet == null) {
            itemKeysSet = new HashSet<>();
            this.supportedMenuItemsByMenu.put(menuKey, itemKeysSet);
        }
        itemKeysSet.addAll(Arrays.asList(itemKeys));
    }

    ReadOnlyBooleanWrapper selectedWrapper() {
        return selected;
    }
}
