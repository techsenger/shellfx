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

package com.techsenger.tabshell.material.style;

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

    /**
     * This style class is used when it is necessary to make font monospace.
     */
    String MONOSPACE = "monospace";

    /**
     * Partial support (atlantaFX provides "dense").
     */
    String EXTRA_DENSE = "extra-dense";

    /**
     * Class for a cross button.
     */
    String CROSS_BUTTON = "cross";

    /**
     * Button that has only icon and has no text. Iconed button has a square shape.
     */
    String ICONED_BUTTON = "iconed";

    /**
     * Represents a small icon-only button, typically used for auxiliary panel actions such as closing, minimizing,
     * or other management operations.
     *
     * <p>Unlike regular iconed buttons, this button is visually smaller and usually positioned in a panel's corner
     * (commonly top-right) to indicate it controls the panel itself rather than performing primary panel functions.
     */
    String MINI_ICONED_BUTTON = "mini-iconed";

    /**
     * Use this class for TabPanes to hide entire tab-header-area.
     */
    String HIDDEN_TABS = "hidden-tabs";

    /**
     * By default all table cells in all themes have {@code -fx-padding: 0 0.5em 0 0.5em;}. The table with the same
     * spacing sets 0.25em instead of 0.5em, but leaves 0.5em for left padding of the first column and right padding
     * for the last column. So, the `spacing` between columns will be the same.
     */
    String SAME_SPACING_TABLE = "same-spacing-table";

    String SAME_SPACING_TABLE_FIRST_COLUMN = "first";

    String SAME_SPACING_TABLE_LAST_COLUMN = "last";

    /**
     * CSS style class for UI elements that should visually blend with the default background. Applied to elements
     * when their background color needs to match the base container's background color (-color-bg-default), creating
     * a seamless integration between the element and its surrounding environment.
     *
     * <p>Commonly used for toolbars, panels, and containers that should appear as part of the background rather than
     * distinct elevated elements.
     */
    String BLEND = "blend";

    /**
     * Use this class for Hyperlinks to make them look like labels.
     */
    String NO_URL = "no-url";
}
