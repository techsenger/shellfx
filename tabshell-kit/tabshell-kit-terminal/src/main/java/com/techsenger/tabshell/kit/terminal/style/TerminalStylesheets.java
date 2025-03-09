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

package com.techsenger.tabshell.kit.terminal.style;

import com.techsenger.tabshell.core.style.AbstractStylesheets;
import com.techsenger.tabshell.core.style.Stylesheet;

/**
 *
 * @author Pavel Castornii
 */
public final class TerminalStylesheets extends AbstractStylesheets {

    public TerminalStylesheets(boolean iconsIncluded) {
        super(iconsIncluded);
        if (iconsIncluded) {
            add(new Stylesheet(TerminalIcons.class.getResource("icons.css")));
        }
    }
}
