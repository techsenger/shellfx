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

package com.techsenger.tabshell.core;

import com.techsenger.patternfx.mvvmx.ParentComponent;
import com.techsenger.tabshell.core.dialog.DialogContainerComponent;
import com.techsenger.tabshell.core.tab.ShellTabComponent;
import com.techsenger.tabshell.core.tab.TabContainerComponent;

/**
 *
 * @author Pavel Castornii
 */
public interface ShellComponent<T extends ShellView<?, ?>> extends ParentComponent<T>,
        TabContainerComponent<T, ShellTabComponent<?>>, DialogContainerComponent<T> {

}
