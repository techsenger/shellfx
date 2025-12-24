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

package com.techsenger.tabshell.layout.tabhost;

import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.tabshell.core.area.AbstractAreaComponent;
import com.techsenger.tabshell.core.tab.ComponentTab;
import com.techsenger.tabshell.core.tab.TabComponent;
import com.techsenger.tabshell.core.tab.TabContainerComponent;
import com.techsenger.tabshell.layout.LayoutComponentNames;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public class TabHostComponent<T extends TabHostView<?, ?>> extends AbstractAreaComponent<T>
        implements TabContainerComponent<T, TabComponent<?>>  {

    protected class Mediator extends AbstractAreaComponent.Mediator implements TabHostMediator {

    }

    public TabHostComponent(T view) {
        super(view);
    }

    @Override
    public ComponentName getName() {
        return LayoutComponentNames.TAB_HOST;
    }

    @Override
    public List<? extends TabComponent<?>> getTabs() {
        return getView().getNode().getTabs().stream()
                .map(t -> ((ComponentTab) t).getView().getComponent())
                .toList();
    }

    @Override
    public void addTab(TabComponent<?> tab) {
        getView().getNode().getTabs().add(tab.getView().getNode());
        getModifiableChildren().add(tab);
    }

    @Override
    public void removeTab(TabComponent<?> tab) {
        getView().getNode().getTabs().remove(tab.getView().getNode());
        getModifiableChildren().remove(tab);
        tab.deinitializeTree();
    }

    @Override
    public TabComponent<?> getSelectedTab() {
        var tab = getView().getSelectedTab();
        if (tab != null) {
            return tab.getComponent();
        }
        return null;
    }

    @Override
    protected Mediator createMediator() {
        return new Mediator();
    }
}
