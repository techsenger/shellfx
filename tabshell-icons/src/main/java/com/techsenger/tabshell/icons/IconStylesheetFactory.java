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

package com.techsenger.tabshell.icons;

import com.techsenger.tabshell.material.style.Stylesheet;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public final class IconStylesheetFactory {

    /**
     * Creates and returns the stylesheet with icons for the 'shared' module.
     *
     * @return
     */
    public static Stylesheet forShared() {
        return new Stylesheet(IconStylesheetFactory.class.getResource("shared.css"));
    }

    /**
     * Creates and returns the stylesheet with icons for the 'layout' module.
     *
     * @return
     */
    public static Stylesheet forLayout() {
        return new Stylesheet(IconStylesheetFactory.class.getResource("layout.css"));
    }

    /**
     * Creates and returns the stylesheet with icons for the 'dialogs' module.
     *
     * @return
     */
    public static Stylesheet forDialogs() {
        return new Stylesheet(IconStylesheetFactory.class.getResource("dialogs.css"));
    }

    /**
     * Creates and returns the stylesheet with icons for the 'devtools' module.
     *
     * @return
     */
    public static Stylesheet forDevTools() {
        return new Stylesheet(IconStylesheetFactory.class.getResource("devtools.css"));
    }

    /**
     * Creates and returns the stylesheet with icons for the all modules.
     *
     * @return
     */
    public static List<Stylesheet> forAll() {
        return List.of(forShared(),
                forLayout(),
                forDialogs(),
                forDevTools());
    }

    private IconStylesheetFactory() {
        //empty
    }
}
