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
 * Defines the base spacing constants used for padding and gaps throughout the UI.
 *
 * <p>Unlike {@code spacing} and {@code gap}, which can be applied via CSS style classes and automatically respond
 * to the global {@code density} setting, padding cannot be expressed through a finite set of CSS classes — it requires
 * up to four independent values (top, right, bottom, left) that may all differ.
 *
 * <p>A cleaner solution would be CSS custom properties (variables), but JavaFX currently supports lookup values only
 * for colors, not for numeric properties such as {@code -fx-padding}. Until JavaFX adds support for numeric CSS
 * variables, padding must be set programmatically using the constants defined here.
 *
 * @author Pavel Castornii
 */
public final class Spacing {

    private static double vertical = 10.0;

    private static double horizontal = 12.0;

    /**
     * Returns the base vertical spacing value, used as the primary gap between rows
     * or as top/bottom padding.
     *
     * @return the vertical spacing in pixels
     */
    public static double getVertical() {
        return vertical;
    }

    /**
     * Sets the base vertical spacing value. All derived vertical values are calculated
     * from this value automatically.
     *
     * <p>This value should be updated before the UI is constructed, as already-created
     * components will not reflect the change automatically.
     *
     * @param value the vertical spacing in pixels
     */
    public static void setVertical(double value) {
        vertical = value;
    }

    /**
     * Returns half of the base vertical spacing value, used for tighter row gaps
     * or reduced top/bottom padding.
     *
     * @return {@code vertical / 2.0} in pixels
     */
    public static double getVerticalHalf() {
        return vertical / 2.0;
    }

    /**
     * Returns the base horizontal spacing value, used as the primary gap between columns
     * or as left/right padding.
     *
     * @return the horizontal spacing in pixels
     */
    public static double getHorizontal() {
        return horizontal;
    }

    /**
     * Sets the base horizontal spacing value. All derived horizontal values are calculated
     * from this value automatically.
     *
     * <p>This value should be updated before the UI is constructed, as already-created
     * components will not reflect the change automatically.
     *
     * @param value the horizontal spacing in pixels
     */
    public static void setHorizontal(double value) {
        horizontal = value;
    }

    /**
     * Returns half of the base horizontal spacing value, used for tighter column gaps
     * or reduced left/right padding.
     *
     * @return {@code horizontal / 2.0} in pixels
     */
    public static double getHorizontalHalf() {
        return horizontal / 2.0;
    }

    /**
     * Returns one third of the base horizontal spacing value, used for compact inline gaps
     * or minimal left/right padding.
     *
     * @return {@code horizontal / 3.0} in pixels
     */
    public static double getHorizontalThird() {
        return horizontal / 3.0;
    }

    private Spacing() {
        // empty
    }
}
