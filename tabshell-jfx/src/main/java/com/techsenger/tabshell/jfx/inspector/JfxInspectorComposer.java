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

import com.techsenger.mvvm4fx.core.ComponentMediator;
import com.techsenger.tabshell.core.tab.AbstractTabComposer;
import com.techsenger.tabshell.core.tab.ShellTabView;

/**
 *
 * @author Pavel Castornii
 */
public class JfxInspectorComposer<T extends JfxInspectorTabView<?>> extends AbstractTabComposer<T> {

    protected class Mediator extends AbstractTabComposer.Mediator implements JfxInspectorMediator {

        @Override
        public void openPropertyDialog(PropertyDialogViewModel vm) {
            var v = new PropertyDialogView<>(shellTab, vm);
            v.initialize();
            shellTab.getDialogManager().openDialog(v);
        }

    }

    private final ShellTabView<?> shellTab;

    public JfxInspectorComposer(ShellTabView<?> shellTab, T view) {
        super(view);
        this.shellTab = shellTab;
    }

    @Override
    protected ComponentMediator createMediator() {
        return new JfxInspectorComposer.Mediator();
    }

}
