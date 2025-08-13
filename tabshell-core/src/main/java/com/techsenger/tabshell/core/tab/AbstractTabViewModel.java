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
import com.techsenger.tabshell.core.CloseScope;
import com.techsenger.tabshell.core.menu.MenuHelper;
import com.techsenger.tabshell.core.menu.MenuItemHelper;
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.tabshell.material.menu.MenuItemKey;
import com.techsenger.tabshell.material.menu.MenuKey;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
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
public abstract class AbstractTabViewModel extends AbstractChildViewModel implements TabViewModel {

    private final StringProperty title = new SimpleStringProperty();

    private final ObjectProperty<Icon<?>> icon = new SimpleObjectProperty();

    private final StringProperty tooltip = new SimpleStringProperty();

    private final Map<MenuKey, MenuHelper> menuHelpersByKey = new HashMap<>();

    private final Map<MenuItemKey, MenuItemHelper> menuItemHelpersByKey = new HashMap<>();

    private final BooleanProperty waiting = new SimpleBooleanProperty(false);

    private final ReadOnlyBooleanWrapper selected = new ReadOnlyBooleanWrapper();

    private final ObservableSource<Boolean> close = new SimpleObservableSource<>();

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
    public void setOnClosed(TabClosedCallback closedCallback) {
        this.onClosed = closedCallback;
    }

    @Override
    public TabClosedCallback getOnClosed() {
        return this.onClosed;
    }

    @Override
    public boolean isReadyToClose() {
        return true;
    }

    /**
     * Default implementation throws UnsupportedOperationException.
     *
     * @param scope
     * @param retryCallback
     */
    @Override
    public void prepareForClose(CloseScope scope, Runnable retryCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public void requestClose() {
        this.close.next(Boolean.TRUE);
    }

    protected Map<MenuKey, MenuHelper> getMenuHelpersByKey() {
        return menuHelpersByKey;
    }

    protected Map<MenuItemKey, MenuItemHelper> getMenuItemHelpersByKey() {
        return menuItemHelpersByKey;
    }

    protected void addMenuHelpers(MenuHelper... menuHelpers) {
        for (var h : menuHelpers) {
            this.menuHelpersByKey.put(h.getMenuKey(), h);
        }
    }

    protected void removeMenuHelpers(MenuKey... menuKeys) {
        for (var k : menuKeys) {
            this.menuHelpersByKey.remove(k);
        }
    }

    protected void addMenuItemHelpers(MenuItemHelper... itemHelpers) {
        for (var h : itemHelpers) {
            this.menuItemHelpersByKey.put(h.getItemKey(), h);
        }
    }

    protected void removeMenuItemHelpers(MenuItemKey... itemKeys) {
        for (var k : itemKeys) {
            this.menuItemHelpersByKey.remove(k);
        }
    }

    ReadOnlyBooleanWrapper selectedWrapper() {
        return selected;
    }

    ObservableSource<Boolean> closeSource() {
        return close;
    }
}
