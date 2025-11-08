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

package com.techsenger.tabshell.demos.full.dock;

import com.techsenger.mvvm4fx.core.ComponentDescriptor;
import com.techsenger.mvvm4fx.core.HistoryPolicy;
import com.techsenger.tabshell.core.ShellViewModel;
import com.techsenger.tabshell.core.tab.AbstractShellTabViewModel;
import com.techsenger.tabshell.demos.full.DemoComponentNames;
import com.techsenger.tabshell.layout.dock.DockLayoutViewModel;

/**
 *
 * @author Pavel Castornii
 */
public class DockLayoutTabViewModel extends AbstractShellTabViewModel {

    private final DockLayoutTabHistory history;

    private final DockLayoutViewModel layout;

    private final TextViewerViewModel textViewer = new TextViewerViewModel();

    public DockLayoutTabViewModel(ShellViewModel shell) {
        super(shell);
        setTitle("Dock Layout Tab");
        getDescriptor().setHistoryPolicy(HistoryPolicy.ALL);
        setHistoryProvider(() -> shell.getHistoryManager()
                .getOrCreateHistory(DockLayoutTabHistory.class, DockLayoutTabHistory::new));
        this.history = (DockLayoutTabHistory) getHistoryProvider().provide();
        this.layout = new DockLayoutViewModel(this.history.getDockLayout());
    }

    public DockLayoutViewModel getLayout() {
        return layout;
    }

    public TextViewerViewModel getTextViewer() {
        return textViewer;
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(DemoComponentNames.DEMO_DOCK_LAYOUT_TAB);
    }
}
