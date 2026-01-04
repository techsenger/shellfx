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

package com.techsenger.tabshell.jfx.environment;

import com.techsenger.patternfx.core.Name;
import com.techsenger.tabshell.core.tab.ShellTabComponent;
import com.techsenger.tabshell.dialogs.namevalue.NameValueDialogComponent;
import com.techsenger.tabshell.dialogs.namevalue.NameValueDialogView;
import com.techsenger.tabshell.dialogs.namevalue.NameValueDialogViewModel;
import com.techsenger.tabshell.jfx.AbstractSearchableTabComponent;
import com.techsenger.tabshell.jfx.JfxComponentNames;

/**
 *
 * @author Pavel Castornii
 */
public class EnvironmentTabComponent<T extends EnvironmentTabView<?, ?>> extends AbstractSearchableTabComponent<T> {

    protected class Mediator extends AbstractSearchableTabComponent.Mediator implements EnvironmentTabMediator {

        private final EnvironmentTabComponent component = EnvironmentTabComponent.this;

        @Override
        public void addNameValueDialog(NameValueDialogViewModel<?> vm) {
            var v = new NameValueDialogView<>(vm);
            var c = new NameValueDialogComponent<>(v);
            c.initialize();
            component.shellTab.addDialog(c);
        }

    }

    private final ShellTabComponent<?> shellTab;

    public EnvironmentTabComponent(T view, ShellTabComponent<?> shellTab) {
        super(view);
        this.shellTab = shellTab;
    }

    @Override
    protected Mediator createMediator() {
        return new Mediator();
    }

    @Override
    public Name getName() {
        return JfxComponentNames.ENVIRONMENT_TAB;
    }

}
