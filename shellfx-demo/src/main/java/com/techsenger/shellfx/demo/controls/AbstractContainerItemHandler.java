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

package com.techsenger.shellfx.demo.controls;

import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.material.menu.AbstractMenuItemHandler;
import com.techsenger.shellfx.core.tab.TabContainerView;
import com.techsenger.shellfx.layout.dockhost.DockHostView;
import com.techsenger.shellfx.layout.tabhost.TabHostView;
import com.techsenger.shellfx.material.menu.ManagedMenuItem;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractContainerItemHandler extends AbstractMenuItemHandler<ShellView<?>, ManagedMenuItem> {

    public AbstractContainerItemHandler(ShellView<?> component, ManagedMenuItem item) {
        super(component, item);
    }

    protected TabContainerView<?> resolveMainTabContainer() {
        if (getComponent().getComposer().getWorkspace() instanceof TabHostView<?> tabHost) {
            return tabHost;
        } else if (getComponent().getComposer().getWorkspace() instanceof DockHostView<?> dockHost) {
            return (TabContainerView<?>) dockHost.getComposer().getMain();
        }
        return null;
    }
}
