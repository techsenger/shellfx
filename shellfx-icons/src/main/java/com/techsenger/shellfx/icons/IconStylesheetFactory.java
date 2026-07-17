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

package com.techsenger.shellfx.icons;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.shellfx.material.style.Stylesheet;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public final class IconStylesheetFactory {

    /**
     * Creates and returns the stylesheet with icons for the 'core' module.
     */
    public static Stylesheet forCore() {
        return new Stylesheet(IconStylesheetFactory.class.getResource("core.css"));
    }

    /**
     * Creates and returns the stylesheet with icons for the 'shared' module.
     */
    public static Stylesheet forShared() {
        return new Stylesheet(IconStylesheetFactory.class.getResource("shared.css"));
    }

    /**
     * Creates and returns the stylesheet with icons for the 'layout' module.
     */
    public static Stylesheet forLayout() {
        return new Stylesheet(IconStylesheetFactory.class.getResource("layout.css"));
    }

    /**
     * Creates and returns the stylesheet with icons for the 'dialogs' module.
     */
    public static Stylesheet forDialogs() {
        return new Stylesheet(IconStylesheetFactory.class.getResource("dialogs.css"));
    }

    /**
     * Creates and returns the stylesheet with icons for the 'devtools' module.
     */
    public static Stylesheet forDevTools() {
        return new Stylesheet(IconStylesheetFactory.class.getResource("devtools.css"));
    }

    /**
     * Creates and returns the stylesheet with icons for the 'storage' module.
     */
    public static Stylesheet forStorage() {
        return new Stylesheet(IconStylesheetFactory.class.getResource("storage.css"));
    }

    /**
     * Creates and returns an unmodifiable list with icon stylesheet for the all modules.
     */
    public static @Unmodifiable List<Stylesheet> forAll() {
        return List.of(
                forCore(),
                forShared(),
                forLayout(),
                forDialogs(),
                forDevTools(),
                forStorage());
    }

    private IconStylesheetFactory() {
        //empty
    }
}
