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

package com.techsenger.tabshell.jfx;

import com.techsenger.tabshell.core.tab.ShellTabView;
import com.techsenger.tabshell.layout.dock.DockLayoutView;
import com.techsenger.tabshell.layout.dock.TabDockView;

/**
 *
 * @author Pavel Castornii
 */
public class JfxTabDockView<T extends JfxTabDockViewModel> extends TabDockView<T> {

    private final ShellTabView<?> shellTab;

    public JfxTabDockView(ShellTabView<?> shellTab, DockLayoutView<?> layout, T viewModel) {
        super(layout, viewModel);
        this.shellTab = shellTab;
    }

    @Override
    public JfxTabDockComposer<?> getComposer() {
        return (JfxTabDockComposer<?>) super.getComposer();
    }

    @Override
    protected JfxTabDockComposer<?> createComposer() {
        return new JfxTabDockComposer<>(shellTab, this);
    }

    @Override
    protected void postInitialize(T viewModel) {
        super.postInitialize(viewModel);
        getComposer().addTabToDock(getComposer().getInpectorTab()); // todo: This is a temp solution, open via composer
        getComposer().addTabToDock(getComposer().getEventLogTab());
        viewModel.selectTab(0);
    }

    protected ShellTabView<?> getShellTab() {
        return shellTab;
    }
}
