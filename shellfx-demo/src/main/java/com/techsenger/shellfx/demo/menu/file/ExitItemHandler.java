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

package com.techsenger.shellfx.demo.menu.file;

import com.techsenger.shellfx.core.ShellFxView;
import com.techsenger.shellfx.core.menu.AbstractMenuItemHandler;
import com.techsenger.shellfx.material.menu.ManagedMenuItem;

/**
 *
 * @author Pavel Castornii
 */
public class ExitItemHandler extends AbstractMenuItemHandler<ShellFxView<?>> {

    public ExitItemHandler(ManagedMenuItem item, ShellFxView<?> component) {
        super(item, component);
    }

    @Override
    public void onAction() {
        getComponent().getPresenter().closeSafely();
    }

}
