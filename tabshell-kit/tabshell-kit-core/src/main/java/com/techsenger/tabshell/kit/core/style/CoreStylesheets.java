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

package com.techsenger.tabshell.kit.core.style;

import com.techsenger.tabshell.core.style.AbstractStylesheets;
import com.techsenger.tabshell.core.style.Stylesheet;
import com.techsenger.tabshell.core.theme.TabShellTheme;

/**
 *
 * @author Pavel Castornii
 */
public final class CoreStylesheets extends AbstractStylesheets {

    public CoreStylesheets(boolean iconsIncluded) {
        super(iconsIncluded);
        add(new Stylesheet(CoreStylesheets.class.getResource("core.css")),
            new Stylesheet(TabShellTheme.CUPERTINO_DARK,
                    CoreStylesheets.class.getResource("core-cupertino-dark.css")),
            new Stylesheet(TabShellTheme.CUPERTINO_LIGHT,
                    CoreStylesheets.class.getResource("core-cupertino-light.css")),
            new Stylesheet(TabShellTheme.DRACULA,
                    CoreStylesheets.class.getResource("core-dracula.css")),
            new Stylesheet(TabShellTheme.NORD_DARK,
                    CoreStylesheets.class.getResource("core-nord-dark.css")),
            new Stylesheet(TabShellTheme.NORD_LIGHT,
                    CoreStylesheets.class.getResource("core-nord-light.css")),
            new Stylesheet(TabShellTheme.PRIMER_DARK,
                    CoreStylesheets.class.getResource("core-primer-dark.css")),
            new Stylesheet(TabShellTheme.PRIMER_LIGHT,
                    CoreStylesheets.class.getResource("core-primer-light.css")));
        if (iconsIncluded) {
            add(new Stylesheet(CoreIcons.class.getResource("icons.css")));
        }
    }
}
