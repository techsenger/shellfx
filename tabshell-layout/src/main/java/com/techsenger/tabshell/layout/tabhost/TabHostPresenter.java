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

package com.techsenger.tabshell.layout.tabhost;

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.area.AbstractAreaPresenter;
import com.techsenger.tabshell.core.tab.TabContainerPresenter;
import com.techsenger.tabshell.layout.LayoutComponents;

/**
 *
 * @author Pavel Castornii
 */
public class TabHostPresenter<V extends TabHostView, C extends TabHostComposer> extends AbstractAreaPresenter<V, C>
        implements TabContainerPresenter<V, C> {

    private boolean tabHeaderAutoHide;

    private boolean tabHeaderVisible;

    public TabHostPresenter(V view) {
        super(view);
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(LayoutComponents.TAB_HOST);
    }

    @Override
    public void onSelectedTabChanged(int index) {
        // empty
    }

    public boolean isTabHeaderAutoHide() {
        return tabHeaderAutoHide;
    }

    public void setTabHeaderAutoHide(boolean tabHeaderAutoHide) {
        this.tabHeaderAutoHide = tabHeaderAutoHide;
        getView().setTabHeaderAutoHide(tabHeaderAutoHide);
    }

    public boolean isTabHeaderVisible() {
        return tabHeaderVisible;
    }

    public void setTabHeaderVisible(boolean tabHeaderVisible) {
        this.tabHeaderVisible = tabHeaderVisible;
        getView().setTabHeaderVisible(tabHeaderVisible);
    }
}
