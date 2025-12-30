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

package com.techsenger.tabshell.jfx.inspector;

import com.techsenger.patternfx.core.Name;
import com.techsenger.tabshell.core.tab.AbstractTabComponent;
import com.techsenger.tabshell.core.tab.ShellTabComponent;
import com.techsenger.tabshell.core.tab.ShellTabViewModel;
import com.techsenger.tabshell.jfx.JfxComponentNames;

/**
 *
 * @author Pavel Castornii
 */
public class JfxInspectorTabComponent<T extends JfxInspectorTabView<?, ?>> extends AbstractTabComponent<T> {

    protected class Mediator extends AbstractTabComponent.Mediator implements JfxInspectorTabMediator {

        @Override
        public void addPropertyDialog(PropertyDialogViewModel vm) {
            var v = new PropertyDialogView<>(vm);
            var c = new PropertyDialogComponent<>(v, shellTab);
            c.initialize();
            shellTab.getShell().addDialog(c);
        }

        @Override
        public ShellTabViewModel getShellTab() {
            return shellTab.getView().getViewModel();
        }

    }

    private final ShellTabComponent<?> shellTab;

    public JfxInspectorTabComponent(T view, ShellTabComponent<?> shellTab) {
        super(view);
        this.shellTab = shellTab;
    }

    @Override
    public Name getName() {
        return JfxComponentNames.JFX_INSPECTOR_TAB;
    }

    public ShellTabComponent<?> getShellTab() {
        return shellTab;
    }

    @Override
    protected Mediator createMediator() {
        return new Mediator();
    }
}
