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

package com.techsenger.tabshell.core.tab;

import com.techsenger.patternfx.mvp.ChildPort;
import com.techsenger.tabshell.core.CloseAwarePort;
import com.techsenger.tabshell.core.ShellPort;
import com.techsenger.tabshell.core.dialog.DialogContainerPort;
import com.techsenger.tabshell.material.icon.Icon;

/**
 *
 * @author Pavel Castornii
 */
public interface TabPort extends ChildPort, CloseAwarePort, DialogContainerPort {

    /**
     * Returns whether the tab can be closed.
     *
     * @return true if the tab can be closed, false otherwise
     */
    boolean isClosable();

    boolean isWaiting();

    boolean isSelected();

    String getTitle();

    Icon<?> getIcon();

    String getTooltip();

    ShellPort getShell();
}
