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

package com.techsenger.tabshell.demos.full.dock;

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.tab.AbstractTabPresenter;
import com.techsenger.tabshell.core.tab.TabComposer;
import com.techsenger.tabshell.core.tab.TabView;
import java.util.function.Consumer;
import com.techsenger.tabshell.demos.full.DemoComponents;

/**
 *
 * @author Pavel Castornii
 */
public class DockableTabPresenter extends AbstractTabPresenter<TabView, TabComposer> {

    private final int index;

    public DockableTabPresenter(TabView view, int index) {
        super(view);
        this.index = index;
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DemoComponents.DOCKABLE_TAB);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        getView().setTitle("Tab " + index);
    }
}
