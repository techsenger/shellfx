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

package com.techsenger.tabshell.layout.dock;

import com.techsenger.patternfx.core.Name;
import com.techsenger.tabshell.core.area.AbstractAreaComponent;
import com.techsenger.tabshell.core.area.AreaComponent;
import com.techsenger.tabshell.layout.LayoutComponentNames;

/**
 *
 * @author Pavel Castornii
 */
public class SplitSpaceComponent<T extends SplitSpaceView<?, ?>> extends AbstractAreaComponent<T> {

    private final DockLayoutComponent<?> layout;

    public SplitSpaceComponent(T view, DockLayoutComponent<?> layout) {
        super(view);
        this.layout = layout;
    }

    @Override
    protected Mediator createMediator() {
        return new AbstractAreaComponent.Mediator() { };
    }

    @Override
    public Name getName() {
        return LayoutComponentNames.SPLIT_SPACE;
    }

    public void addChild(AreaComponent<?> child) {
        getModifiableChildren().add(child);
        getView().addChild(child.getView());
    }

    public void addChild(int index, AreaComponent<?> child) {
        getModifiableChildren().add(index, child);
        getView().addChild(index, child.getView());
    }

    public void removeChild(int index) {
        getModifiableChildren().remove(index);
        getView().removeChild(index);
    }

    public DockLayoutComponent<?> getLayout() {
        return layout;
    }

    void replacePlaceholder(int index, TabDockComponent<?> tabDock) {
        getModifiableChildren().set(index, tabDock);
        getView().removeChild(index);
        getView().addChild(index, tabDock.getView());
    }
}
