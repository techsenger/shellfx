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

import com.techsenger.patternfx.core.AbstractChildViewModel;
import com.techsenger.tabshell.core.menu.MenuHelper;
import com.techsenger.tabshell.core.menu.MenuItemHelper;
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.tabshell.material.menu.MenuItemName;
import com.techsenger.tabshell.material.menu.MenuName;
import java.util.HashMap;
import java.util.Map;
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
public abstract class AbstractTabViewModel<T extends TabMediator> extends AbstractChildViewModel<T>
        implements TabViewModel<T> {

    private final StringProperty title = new SimpleStringProperty();

    private final ObjectProperty<Icon<?>> icon = new SimpleObjectProperty();

    private final StringProperty tooltip = new SimpleStringProperty();

    private final Map<MenuName, MenuHelper> menuHelpersByName = new HashMap<>();

    private final Map<MenuItemName, MenuItemHelper> menuItemHelpersByName = new HashMap<>();

    private final BooleanProperty waiting = new SimpleBooleanProperty(false);

    private final ReadOnlyBooleanWrapper selected = new ReadOnlyBooleanWrapper();

    private final BooleanProperty closable = new SimpleBooleanProperty(true);

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
    public boolean isReadyToClose() {
        return true;
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

    @Override
    public void close() {
        getMediator().remove();
    }

    @Override
    public BooleanProperty closableProperty() {
        return closable;
    }

    @Override
    public boolean isClosable() {
        return closable.get();
    }

    @Override
    public void setClosable(boolean closable) {
        this.closable.set(closable);
    }

    @Override
    protected AbstractTabHistory getHistory() {
        return (AbstractTabHistory) super.getHistory();
    }

    protected Map<MenuName, MenuHelper> getMenuHelpersByName() {
        return menuHelpersByName;
    }

    protected Map<MenuItemName, MenuItemHelper> getMenuItemHelpersByName() {
        return menuItemHelpersByName;
    }

    protected void addMenuHelpers(MenuHelper... menuHelpers) {
        for (var h : menuHelpers) {
            this.menuHelpersByName.put(h.getMenuName(), h);
        }
    }

    protected void removeMenuHelpers(MenuName... menuNames) {
        for (var k : menuNames) {
            this.menuHelpersByName.remove(k);
        }
    }

    protected void addMenuItemHelpers(MenuItemHelper... itemHelpers) {
        for (var h : itemHelpers) {
            this.menuItemHelpersByName.put(h.getItemName(), h);
        }
    }

    protected void removeMenuItemHelpers(MenuItemName... itemNames) {
        for (var k : itemNames) {
            this.menuItemHelpersByName.remove(k);
        }
    }

    ReadOnlyBooleanWrapper selectedWrapper() {
        return selected;
    }
}
