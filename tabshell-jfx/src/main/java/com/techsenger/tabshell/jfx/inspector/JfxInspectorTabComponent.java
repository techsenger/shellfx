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

package com.techsenger.tabshell.jfx.inspector;

import com.techsenger.patternfx.core.Name;
import com.techsenger.tabshell.core.tab.AbstractTabComponent;
import com.techsenger.tabshell.core.tab.ShellTabComponent;
import com.techsenger.tabshell.core.tab.ShellTabViewModel;
import com.techsenger.tabshell.jfx.JfxComponentNames;
import com.techsenger.tabshell.jfx.SearchPanelComponent;
import com.techsenger.tabshell.jfx.SearchPanelView;
import com.techsenger.tabshell.jfx.SearchPanelViewModel;

/**
 *
 * @author Pavel Castornii
 */
public class JfxInspectorTabComponent<T extends JfxInspectorTabView<?, ?>> extends AbstractTabComponent<T> {

    protected class Mediator extends AbstractTabComponent.Mediator implements JfxInspectorTabMediator {

        private final JfxInspectorTabComponent<?> component = JfxInspectorTabComponent.this;

        @Override
        public SearchPanelViewModel<?> getNodeSearchPanel() {
            return component.nodeSearchPanel.getView().getViewModel();
        }

        @Override
        public SearchPanelViewModel<?> getPropertySearchPanel() {
            return component.propertySearchPanel.getView().getViewModel();
        }

        @Override
        public void addPropertyDialog(PropertyDialogViewModel vm) {
            var v = new PropertyDialogView<>(vm);
            var c = new PropertyDialogComponent<>(v, shellTab);
            c.initialize();
            component.shellTab.getShell().addDialog(c);
        }

        @Override
        public ShellTabViewModel getShellTab() {
            return component.shellTab.getView().getViewModel();
        }
    }

    private final ShellTabComponent<?> shellTab;

    private final SearchPanelComponent<?> nodeSearchPanel;

    private final SearchPanelComponent<?> propertySearchPanel;

    public JfxInspectorTabComponent(T view, ShellTabComponent<?> shellTab) {
        super(view);
        this.shellTab = shellTab;
        this.nodeSearchPanel = createSearchPanel();
        getModifiableChildren().add(this.nodeSearchPanel);
        this.propertySearchPanel = createSearchPanel();
        getModifiableChildren().add(this.propertySearchPanel);
    }

    @Override
    public Name getName() {
        return JfxComponentNames.JFX_INSPECTOR_TAB;
    }

    public ShellTabComponent<?> getShellTab() {
        return shellTab;
    }

    @Override
    protected void preInitialize() {
        super.preInitialize();
        this.nodeSearchPanel.initialize();
        this.propertySearchPanel.initialize();
    }

    @Override
    protected Mediator createMediator() {
        return new Mediator();
    }

    protected SearchPanelComponent<?> createSearchPanel() {
        var vm = new SearchPanelViewModel<>();
        var v = new SearchPanelView<>(vm);
        var c = new SearchPanelComponent<>(v);
        return c;
    }

    protected SearchPanelComponent<?> getNodeSearchPanel() {
        return nodeSearchPanel;
    }

    protected SearchPanelComponent<?> getPropertySearchPanel() {
        return propertySearchPanel;
    }
}
