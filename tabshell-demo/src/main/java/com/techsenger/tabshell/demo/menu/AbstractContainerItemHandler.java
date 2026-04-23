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

package com.techsenger.tabshell.demo.menu;

import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.menu.AbstractMenuItemHandler;
import com.techsenger.tabshell.core.tab.TabContainerFxView;
import com.techsenger.tabshell.layout.dockhost.DockHostFxView;
import com.techsenger.tabshell.layout.tabhost.TabHostFxView;
import com.techsenger.tabshell.material.menu.ManagedMenuItem;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractContainerItemHandler extends AbstractMenuItemHandler<ShellFxView<?>> {

    public AbstractContainerItemHandler(ManagedMenuItem item, ShellFxView<?> component) {
        super(item, component);
    }

    protected TabContainerFxView<?> resolveMainTabContainer() {
        if (getComponent().getComposer().getWorkspace() instanceof TabHostFxView<?> tabHost) {
            return tabHost;
        } else if (getComponent().getComposer().getWorkspace() instanceof DockHostFxView<?> dockHost) {
            return (TabContainerFxView<?>) dockHost.getComposer().getMain();
        }
        return null;
    }
}
