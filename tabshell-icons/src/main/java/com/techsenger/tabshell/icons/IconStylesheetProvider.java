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

package com.techsenger.tabshell.icons;

import com.techsenger.tabshell.core.style.Stylesheet;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public final class IconStylesheetProvider {

    /**
     * Creates and returns the stylesheet with icons for the 'core' module.
     *
     * @return
     */
    public static Stylesheet createForCore() {
        return new Stylesheet(IconStylesheetProvider.class.getResource("core.css"));
    }

    /**
     * Creates and returns the stylesheet with icons for the 'dialogs' module.
     *
     * @return
     */
    public static Stylesheet createForDialogs() {
        return new Stylesheet(IconStylesheetProvider.class.getResource("dialogs.css"));
    }

    /**
     * Creates and returns the stylesheet with icons for the 'terminal' module.
     *
     * @return
     */
    public static Stylesheet createForTerminal() {
        return new Stylesheet(IconStylesheetProvider.class.getResource("terminal.css"));
    }

    /**
     * Creates and returns the stylesheet with icons for the 'text' module.
     *
     * @return
     */
    public static Stylesheet createForText() {
        return new Stylesheet(IconStylesheetProvider.class.getResource("text.css"));
    }

    /**
     * Creates and returns the stylesheet with icons for the 'hex' module.
     *
     * @return
     */
    public static Stylesheet createForHex() {
        return new Stylesheet(IconStylesheetProvider.class.getResource("hex.css"));
    }

    /**
     * Creates and returns the stylesheet with icons for the all modules.
     *
     * @return
     */
    public static List<Stylesheet> createForAll() {
        return List.of(createForCore(),
                createForDialogs(),
                createForTerminal(),
                createForText(),
                createForHex());
    }

    private IconStylesheetProvider() {
        //empty
    }
}
