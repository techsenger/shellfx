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
import com.techsenger.tabshell.core.tab.ShellTabComponent;
import com.techsenger.tabshell.dialogs.simple.AbstractSimpleDialogComponent;
import com.techsenger.tabshell.jfx.JfxComponentNames;
import com.techsenger.tabshell.web.WebBrowserTabComponent;
import com.techsenger.tabshell.web.WebBrowserTabView;
import com.techsenger.tabshell.web.WebBrowserTabViewModel;

/**
 *
 * @author Pavel Castornii
 */
public class PropertyDialogComponent<T extends PropertyDialogView<?, ?>> extends AbstractSimpleDialogComponent<T> {

    protected class Mediator extends AbstractSimpleDialogComponent.Mediator implements PropertyDialogMediator {

        @Override
        public void addBrowser(WebBrowserTabViewModel vm) {
            var v = new WebBrowserTabView<>(vm);
            var c = new WebBrowserTabComponent<>(v, shellTab.getShell());
            c.initialize();
            shellTab.getShell().addTab(c);
        }
    }

    private final ShellTabComponent<?> shellTab;

    public PropertyDialogComponent(T view, ShellTabComponent<?> shellTab) {
        super(view);
        this.shellTab = shellTab;
    }

    @Override
    public Name getName() {
        return JfxComponentNames.PROPERTY_DIALOG;
    }

    @Override
    protected Mediator createMediator() {
        return new Mediator();
    }
}
