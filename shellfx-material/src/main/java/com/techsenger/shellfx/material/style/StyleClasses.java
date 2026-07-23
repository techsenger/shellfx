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

package com.techsenger.shellfx.material.style;

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
     * Represents a medium density spacing mode. This is the default spacing level and provides balanced padding and
     * layout density.
     */
    String DENSITY_M = "density-m";

    /**
     * Represents a slightly more compact spacing mode than {@code DENSITY_M}, reducing padding and spacing for denser
     * layouts.
     */
    String DENSITY_S = "density-s";

    /**
     * Represents the most compact spacing mode, with minimal padding and margins, intended for highly space-constrained
     * UIs.
     */
    String DENSITY_XS = "density-xs";

    /**
     * Represents the extra-extra-large size variant for UI controls.
     *
     * <p>The actual rendered size (including padding, font size, minimum height, and icon size) is dynamically
     * determined by both the global {@code density} setting applied to the scene/root and this component-specific
     * {@code size} value.
     */
    String SIZE_XXL = "size-xxl";

    /**
     * Represents the extra-large size variant for UI controls.
     *
     * <p>The actual rendered size (including padding, font size, minimum height, and icon size) is dynamically
     * determined by both the global {@code density} setting applied to the scene/root and this component-specific
     * {@code size} value.
     */
    String SIZE_XL = "size-xl";

    /**
     * Represents the large size variant for UI controls.
     *
     * <p>The actual rendered size (including padding, font size, minimum height, and icon size) is dynamically
     * determined by both the global {@code density} setting applied to the scene/root and this component-specific
     * {@code size} value.
     */
    String SIZE_L = "size-l";

    /**
     * Represents the medium (default) size variant for UI controls.
     *
     * <p>The actual rendered size (including padding, font size, minimum height, and icon size) is dynamically
     * determined by both the global {@code density} setting applied to the scene/root and this component-specific
     * {@code size} value.
     */
    String SIZE_M = "size-m";

    /**
     * Represents the small size variant for UI controls.
     *
     * <p>The actual rendered size (including padding, font size, minimum height, and icon size) is dynamically
     * determined by both the global {@code density} setting applied to the scene/root and this component-specific
     * {@code size} value.
     */
    String SIZE_S = "size-s";

    /**
     * Represents the extra-small size variant for UI controls.
     *
     * <p>The actual rendered size (including padding, font size, minimum height, and icon size) is
     * dynamically determined by both the global {@code density} setting applied to the scene/root
     * and this component-specific {@code size} value.
     */
    String SIZE_XS = "size-xs";

    /**
     * Represents the extra-extra-small size variant for UI controls. This is the smallest available
     * size in the size scale.
     *
     * <p>The actual rendered size (including padding, font size, minimum height, and icon size) is
     * dynamically determined by both the global {@code density} setting applied to the scene/root
     * and this component-specific {@code size} value.
     *
     * <p>Typical use cases: close button for a collapsed side panel, dismiss button for a compact
     * notification badge.
     */
    String SIZE_XXS = "size-xxs";

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

    /**
     * Style class for a {@code ListView} whose cells are highlighted with the theme's accent color on hover,
     * rather than the default neutral highlight. Intended for menu-like lists where a click immediately selects
     * an item and closes the containing popup (e.g. the {@code LocationBar} segment/storage popups), as opposed
     * to persistent-selection data lists such as file or directory listings.
     */
    String ACCENT_LIST_VIEW = "accent-list-view";
}
