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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public final class IconStylesheetFactory {

    /**
     * Creates and returns the stylesheet with icons for the 'core' module.
     */
    public static Stylesheet forCoreModule() {
        return new Stylesheet(IconStylesheetFactory.class.getResource("core.css").toExternalForm());
    }

    /**
     * Creates and returns the stylesheet with icons for the 'shared' module.
     */
    public static Stylesheet forSharedModule() {
        return new Stylesheet(IconStylesheetFactory.class.getResource("shared.css").toExternalForm());
    }

    /**
     * Creates and returns the stylesheet with icons for the 'layout' module.
     */
    public static Stylesheet forLayoutModule() {
        return new Stylesheet(IconStylesheetFactory.class.getResource("layout.css").toExternalForm());
    }

    /**
     * Creates and returns the stylesheet with icons for the 'material' module.
     */
    public static Stylesheet forMaterialModule() {
        return new Stylesheet(IconStylesheetFactory.class.getResource("material.css").toExternalForm());
    }

    /**
     * Creates and returns the stylesheet with icons for the 'dialogs' module.
     */
    public static Stylesheet forDialogsModule() {
        return new Stylesheet(IconStylesheetFactory.class.getResource("dialogs.css").toExternalForm());
    }

    /**
     * Creates and returns the stylesheet with icons for the 'devtools' module.
     */
    public static Stylesheet forDevToolsModule() {
        return new Stylesheet(IconStylesheetFactory.class.getResource("devtools.css").toExternalForm());
    }

    /**
     * Creates and returns the stylesheet with icons for the 'storage' module.
     */
    public static Stylesheet forStorageModule() {
        return new Stylesheet(IconStylesheetFactory.class.getResource("storage.css").toExternalForm());
    }

    /**
     * Creates and returns an unmodifiable list with icon stylesheet for all modules.
     */
    public static @Unmodifiable List<Stylesheet> forAllModules() {
        return List.of(
                forCoreModule(),
                forSharedModule(),
                forLayoutModule(),
                forMaterialModule(),
                forDialogsModule(),
                forDevToolsModule(),
                forStorageModule());
    }

    /**
     * Creates the stylesheet that makes the family of the given font the default font of every icon. Icons that
     * specify another font in their own rules override it. Only the family of the font is used.
     *
     * @param font the font of the icons used by default, never {@code null}
     */
    public static Stylesheet forDefaultFont(Font font) {
        var css = ".font-icon-view { -sfx-icon-family: '" + font.getFamily() + "'; }";
        return new Stylesheet("data:text/css;base64,"
                + Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8)));
    }

    private IconStylesheetFactory() {
        //empty
    }
}
