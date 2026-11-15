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

package com.techsenger.shellfx.layout.style;

import com.techsenger.shellfx.base.icon.FontIcon;
import com.techsenger.shellfx.material.icon.StyleFontIcon;

/**
 *
 * @author Pavel Castornii
 */
public interface LayoutIcons {

    FontIcon<?> DRAG_VERTICAL = new StyleFontIcon("drag-vertical-icon");

    FontIcon<?> RESTORE_WINDOW = new StyleFontIcon("restore-window-icon");

    FontIcon<?> CHEVRON_DOUBLE_RIGHT = new StyleFontIcon("chevron-double-right-icon");

    FontIcon<?> REMOVE = new StyleFontIcon("remove-icon");

    FontIcon<?> CHEVRON_RIGHT = new StyleFontIcon("chevron-right-icon");

    FontIcon<?> CHEVRON_LEFT = new StyleFontIcon("chevron-left-icon");

    FontIcon<?> CLOSE = new StyleFontIcon("close-icon");
}
