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

/**
 *
 * @author Pavel Castornii
 */
public interface StyleClasses extends com.techsenger.tabshell.core.style.StyleClasses {

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
}
