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

package com.techsenger.tabshell.core;

import com.techsenger.patternfx.core.Name;
import com.techsenger.patternfx.mvvmx.AbstractParentComponent;
import com.techsenger.tabshell.core.dialog.DialogComponent;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.core.tab.ComponentTab;
import com.techsenger.tabshell.core.tab.ShellTabComponent;
import com.techsenger.tabshell.core.tab.ShellTabView;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultShellComponent<T extends DefaultShellView<?, ?>> extends AbstractParentComponent<T>
        implements ShellComponent<T> {

    protected class Mediator extends AbstractParentComponent.Mediator implements ShellMediator {

        @Override
        public void deinitialize() {
            DefaultShellComponent.this.deinitialize();
        }
    }

    public DefaultShellComponent(T view) {
        super(view);
    }

    @Override
    protected Mediator createMediator() {
        return new DefaultShellComponent.Mediator();
    }

    @Override
    public Name getName() {
        return CoreComponentNames.SHELL;
    }

    @Override
    public List<? extends ShellTabComponent<?>> getTabs() {
        return getView().getTabPane().getTabs().stream()
                .map(t -> ((ComponentTab) t).getView())
                .map(v -> ((ShellTabView<?, ?>) v).getComponent())
                .toList();
    }

    @Override
    public void addTab(ShellTabComponent<?> tab) {
        getView().getTabPane().getTabs().add(tab.getView().getNode());
        getModifiableChildren().add(tab);
    }

    @Override
    public void removeTab(ShellTabComponent<?> tab) {
        getView().getTabPane().getTabs().remove(tab.getView().getNode());
        getModifiableChildren().remove(tab);
        tab.deinitializeTree();
    }

    @Override
    public DialogScope getSupportedDialogScope() {
        return DialogScope.SHELL;
    }

    @Override
    public void addDialog(DialogComponent<?> dialog) {
        var scope = dialog.getView().getViewModel().getScope();
        if (scope == getSupportedDialogScope()) {
            getView().getDialogManager().showDialog(dialog.getView());
            getModifiableChildren().add(dialog);
        } else {
            var selectedTab = getView().getSelectedTab();
            if (selectedTab != null) {
                selectedTab.getComponent().addDialog(dialog);
            }
        }
    }

    @Override
    public void removeDialog(DialogComponent<?> dialog) {
        var scope = dialog.getView().getViewModel().getScope();
        if (scope == getSupportedDialogScope()) {
            getView().getDialogManager().hideDialog(dialog.getView());
            getModifiableChildren().remove(dialog);
            dialog.deinitializeTree();
        } else {
            var selectedTab = getView().getSelectedTab();
            if (selectedTab != null) {
                selectedTab.getComponent().removeDialog(dialog);
            }
        }
    }

    @Override
    public List<? extends DialogComponent<?>> getDialogs() {
        return getView().getDialogManager().getDialogs().stream().map(d -> d.getComponent()).toList();
    }
}
