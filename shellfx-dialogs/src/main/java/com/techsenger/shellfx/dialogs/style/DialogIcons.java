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

package com.techsenger.shellfx.dialogs.style;

import com.techsenger.shellfx.material.icon.FontIcon;
import com.techsenger.shellfx.material.icon.StyleFontIcon;

/**
 *
 * @author Pavel Castornii
 */
public interface DialogIcons {

    FontIcon<?> INFO = new StyleFontIcon("info-icon");

    FontIcon<?> ERROR = new StyleFontIcon("error-icon");

    FontIcon<?> WARNING = new StyleFontIcon("warning-icon");

    FontIcon<?> QUESTION = new StyleFontIcon("question-icon");

    FontIcon<?> DIRECTORY_UP = new StyleFontIcon("directory-up-icon");

    FontIcon<?> HOME = new StyleFontIcon("home-icon");

    FontIcon<?> ADD_DIRECTORY = new StyleFontIcon("add-directory-icon");

    FontIcon<?> LIST_VIEW = new StyleFontIcon("list-view-icon");

    FontIcon<?> GRID_VIEW = new StyleFontIcon("grid-view-icon");

    FontIcon<?> OPEN = new StyleFontIcon("open-icon");

    FontIcon<?> SAVE_AS = new StyleFontIcon("save-as-icon");
}



