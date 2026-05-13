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
     * Represents a moderately dense spacing mode. Equivalent to the {@code dense} style class in Atlantafx Styles.
     */
    String DENSE = "dense";

    /**
     * Represents the most compact spacing mode. Typically used when minimal padding and margins are desired.
     */
    String COMPACT = "compact";

    /**
     * Class for a cross button.
     */
    String CROSS_BUTTON = "cross";

    /**
     * Button that has only icon and has no text. Iconed button has a square shape.
     */
    String ICON_BUTTON = "iconed";

    /**
     * Use this class for TabPanes to hide entire tab-header-area.
     */
    String HIDDEN_TABS = "hidden-tabs";

    /**
     * By default all table cells in all themes have {@code -fx-padding: 0 0.5em 0 0.5em;}. The table with the same
     * spacing sets 0.25em instead of 0.5em, but leaves 0.5em for left padding of the first column and right padding
     * for the last column. So, the `spacing` between columns will be the same.
     *
     * This style class can used for table view and column list view.
     */
    String SAME_SPACING_COLUMN = "same-spacing-column";

    String SAME_SPACING_COLUMN_FIRST = "first";

    String SAME_SPACING_COLUMN_LAST = "last";

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

    /**
     * This style class is used in atlantafx.
     */
    String NO_BORDER = "edge-to-edge";

    /**
     * This class adds readonly styles for controls that fully or partially do not support this property
     * (e.g. CheckBox).
     */
    String READ_ONLY = "read-only";
}
