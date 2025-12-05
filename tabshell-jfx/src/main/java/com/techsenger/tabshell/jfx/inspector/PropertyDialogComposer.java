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

import com.techsenger.tabshell.core.dialog.DialogView;
import com.techsenger.tabshell.core.tab.ShellTabView;
import com.techsenger.tabshell.dialogs.base.DefaultBaseDialogComposer;
import com.techsenger.tabshell.web.WebBrowserTabView;
import com.techsenger.tabshell.web.WebBrowserTabViewModel;

/**
 *
 * @author Pavel Castornii
 */
public class PropertyDialogComposer<T extends PropertyDialogView<?>> extends DefaultBaseDialogComposer<T> {

    protected class Mediator extends DefaultBaseDialogComposer.Mediator implements PropertyDialogMediator {

        @Override
        public void openBrowser(WebBrowserTabViewModel vm) {
            var v = new WebBrowserTabView<>(shellTab.getShell(), vm);
            v.initialize();
            shellTab.getShell().openTab(v);
        }
    }

    private final ShellTabView<?> shellTab;

    public PropertyDialogComposer(ShellTabView<?> shellTab, T view) {
        super(view);
        this.shellTab = shellTab;
    }

    @Override
    public PropertyDialogMediator getMediator() {
        return (PropertyDialogMediator) super.getMediator();
    }

    @Override
    protected PropertyDialogMediator createMediator() {
        return new PropertyDialogComposer.Mediator();
    }

    @Override
    public void openDialog(DialogView<?> dialog) {
        getView().getDialogManager().openDialog(dialog);
    }

}
