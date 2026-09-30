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

package com.techsenger.shellfx.devtools.stylesheet;

import com.techsenger.shellfx.core.tab.TabConfig;
import com.techsenger.shellfx.devtools.shared.ToolBarConfig;
import java.io.Serial;

/**
 *
 * @author Pavel Castornii
 */
public class StylesheetTabConfig extends TabConfig {

    @Serial
    private static final long serialVersionUID = 1L;

    private ToolBarConfig toolBar = new ToolBarConfig();

    public ToolBarConfig getToolBar() {
        return toolBar;
    }

    public void setToolBar(ToolBarConfig toolBar) {
        this.toolBar = toolBar;
    }
}
