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

package com.techsenger.tabshell.demos.full.dock;

import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.tabs.docktab.AbstractDockTabView;

/**
 *
 * @author Pavel Castornii
 */
public class DockTabView extends AbstractDockTabView<DockTabViewModel> {

    public DockTabView(ShellView<?> shell, DockTabViewModel viewModel) {
        super(shell, viewModel);
    }

    @Override
    public void requestFocus() {

    }
}
