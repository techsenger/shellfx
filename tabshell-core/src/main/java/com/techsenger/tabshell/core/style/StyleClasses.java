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

package com.techsenger.tabshell.core.style;

/**
 *
 * @author Pavel Castornii
 */
public interface StyleClasses {

    /**
     * A CSS class that applies a border and background radius to all corners of an element,
     * according to the current theme.
     */
    String CORNERS_ALL = "corners-all";

    /**
     * A CSS class that applies a border and background radius to the top corners of an element
     * (top-left and top-right), according to the current theme.
     */
    String CORNERS_TOP = "corners-top";

    /**
     * A CSS class that applies a border and background radius to the bottom corners of an element
     * (bottom-left and bottom-right), according to the current theme.
     */
    String CORNERS_BOTTOM = "corners-bottom";

    /**
     * A CSS class that applies a border and background radius to the left corners of an element
     * (top-left and bottom-left), according to the current theme.
     */
    String CORNERS_LEFT = "corners-left";

    /**
     * A CSS class that applies a border and background radius to the right corners of an element
     * (top-right and bottom-right), according to the current theme.
     */
    String CORNERS_RIGHT = "corners-right";

    /**
     * A CSS class that applies a light shadow effect to floating elements (e.g., dialogs, notifications) according
     * to the current theme.
     */
    String SHADOW = "shadow";

    /**
     * This class is used for controls that allow to select items, for example in ComboBox - item will be selected
     * in model, but selection won't be visible.
     */
    String NO_SELECTED = "no-selection";
}
