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

package com.techsenger.tabshell.layout.dock;

import com.techsenger.patternfx.core.Name;
import com.techsenger.tabshell.core.area.AbstractAreaComponent;
import com.techsenger.tabshell.layout.LayoutComponentNames;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;

/**
 *
 * @author Pavel Castornii
 */
public class SideBarComponent<T extends SideBarView<?, ?>> extends AbstractAreaComponent<T> {

    protected class Mediator extends AbstractAreaComponent.Mediator implements SideBarMediator {

        @Override
        public ObservableList<? extends TabDockViewModel<?>> getTabDocks() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private final DockLayoutComponent<?> layout;

    // todo: do we need this collection?
    private final ObservableList<TabDockComponent<?>> modifiableTabDocks = FXCollections.observableArrayList();

    private final ObservableList<TabDockComponent<?>> tabDocks =
            FXCollections.unmodifiableObservableList(modifiableTabDocks);

    private final ReadOnlyObjectWrapper<TabPopupComponent<?>> popup = new ReadOnlyObjectWrapper<>();

    public SideBarComponent(T view, DockLayoutComponent<?> layout) {
        super(view);
        this.layout = layout;
    }

    @Override
    public Name getName() {
        return LayoutComponentNames.SIDE_BAR;
    }

    /**
     * Returns an unmodifiable list of minimized tab docks.
     *
     * @return
     */
    public ObservableList<TabDockComponent<?>> getTabDocks() {
        return tabDocks;
    }

    public ReadOnlyObjectProperty<TabPopupComponent<?>> popupProperty() {
        return popup.getReadOnlyProperty();
    }

    public TabPopupComponent<?> getPopup() {
        return popup.get();
    }

    public DockLayoutComponent<?> getLayout() {
        return layout;
    }

    @Override
    protected Mediator createMediator() {
        return new Mediator();
    }

    protected void addPopupToLayout(Side side) {
        var history = getView().getViewModel().getHistory();
        var vm = new TabPopupViewModel<>(side, () -> history.getOrCreatePopup());
        var v = new TabPopupView<>(vm);
        var c = new TabPopupComponent<>(v, this);
        c.initialize();
        setPopup(c);
        this.layout.addTabPopup(c);
    }

    protected void removePopupFromLayout() {
        if (getPopup() != null) {
            this.layout.removeTabPopup(getPopup());
            setPopup(null);
        }
    }

    protected void setPopup(TabPopupComponent<?> value) {
        popup.set(value);
    }

    void addTabDock(TabDockComponent<?> tabDock) {
        modifiableTabDocks.add(tabDock);
        getModifiableChildren().add(tabDock);
    }

    void removeTabDock(TabDockComponent<?> tabDock) {
        modifiableTabDocks.remove(tabDock);
        getModifiableChildren().remove(tabDock);
    }

    TabDockComponent<?> removeTabDock(int index) {
        TabDockComponent<?> tabDock = modifiableTabDocks.remove(index);
        getModifiableChildren().remove(tabDock);
        return tabDock;
    }
}
