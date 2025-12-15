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

package com.techsenger.tabshell.demos.core.tab;

import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.tabshell.core.ShellComponent;
import com.techsenger.tabshell.core.tab.AbstractShellTabComponent;
import com.techsenger.tabshell.demos.core.DemoComponentNames;
import com.techsenger.tabshell.demos.core.dialog.DemoDialogComponent;
import com.techsenger.tabshell.demos.core.dialog.DemoDialogView;
import com.techsenger.tabshell.demos.core.dialog.DemoDialogViewModel;

/**
 *
 * @author Pavel Castornii
 */
public class DemoTabComponent<T extends DemoTabView<?, ?>> extends AbstractShellTabComponent<T> {

    protected class Mediator extends AbstractShellTabComponent.Mediator implements DemoTabMediator {

        @Override
        public void addDemoDialog(DemoDialogViewModel dialog) {
            DemoTabComponent.this.openDemoDialog(dialog); // magic
        }
    }

    public DemoTabComponent(T view, ShellComponent<?> shell) {
        super(view, shell);
    }

    public void openDemoDialog(DemoDialogViewModel dialogVM) {
        var view = new DemoDialogView(dialogVM);
        var component = new DemoDialogComponent(view);
        component.initialize(); //dont' forget!
        addDialog(component);
    }

    @Override
    public ComponentName getName() {
        return DemoComponentNames.DEMO_TAB;
    }

    @Override
    protected DemoTabComponent.Mediator createMediator() {
        return new DemoTabComponent.Mediator();
    }
}
