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

import com.techsenger.patternfx.core.HistoryProvider;
import com.techsenger.patternfx.core.Name;
import com.techsenger.tabshell.core.ShellComponent;
import com.techsenger.tabshell.core.tab.AbstractShellTabComponent;
import com.techsenger.tabshell.demos.full.DemoComponentNames;
import com.techsenger.tabshell.layout.dock.DockLayoutComponent;
import com.techsenger.tabshell.layout.dock.DockLayoutHistory;
import com.techsenger.tabshell.layout.dock.DockLayoutView;
import com.techsenger.tabshell.layout.dock.DockLayoutViewModel;
import com.techsenger.tabshell.layout.dock.SideBarPolicy;
import com.techsenger.tabshell.layout.dock.TabDockComponent;
import javafx.geometry.Orientation;

/**
 *
 * @author Pavel Castornii
 */
public class DockLayoutTabComponent extends AbstractShellTabComponent<DockLayoutTabView> {

    private final DockLayoutComponent<?> layout;

    private final TextViewerComponent textViewer;

    public DockLayoutTabComponent(DockLayoutTabView view, ShellComponent<?> shell) {
        super(view, shell);
        this.layout = createLayout(() -> view.getViewModel().getHistory().getDockLayout());
        this.getModifiableChildren().add(this.layout);
        this.textViewer = createTextViewer();
    }

    @Override
    public Name getName() {
        return DemoComponentNames.DEMO_DOCK_LAYOUT_TAB;
    }

    public DockLayoutComponent<?> getLayout() {
        return layout;
    }

    public TextViewerComponent getTextViewer() {
        return textViewer;
    }

    @Override
    protected void preInitialize() {
        super.preInitialize();
        this.layout.initialize();
        this.textViewer.initialize();
        this.layout.setMain(this.textViewer);

        var splitSpace = layout.createSplitSpace(Orientation.HORIZONTAL);
        splitSpace.initialize();
        layout.setRoot(splitSpace);
        splitSpace.addChild(textViewer);

        var tabDock = layout.createTabDock();
        tabDock.initialize();
        fillTabs(tabDock);
        splitSpace.addChild(tabDock);
    }

    @Override
    protected Mediator createMediator() {
        return new AbstractShellTabComponent.Mediator() { };
    }

    protected TextViewerComponent createTextViewer() {
        var vm = new TextViewerViewModel();
        var v = new TextViewerView(vm);
        var c = new TextViewerComponent(v);
        return c;
    }

    protected DockLayoutComponent<?> createLayout(HistoryProvider<DockLayoutHistory> provider) {
        var vm = new DockLayoutViewModel<>(provider);
        vm.setBottomBarPolicy(SideBarPolicy.EXISTS_ALWAYS);
        var v = new DockLayoutView<>(vm);
        var c = new DockLayoutComponent<>(v);
        return c;
    }

    private void fillTabs(TabDockComponent<?> tabDock) {
        for (var i = 0; i < 10; i++) {
            var tabViewModel = new DockableTabViewModel(i);
            var tabView = new DockableTabView(tabViewModel);
            var tabComponent = new DockableTabComponent(tabView);
            tabComponent.initialize();
            tabDock.addTab(tabComponent);
        }
    }
}
