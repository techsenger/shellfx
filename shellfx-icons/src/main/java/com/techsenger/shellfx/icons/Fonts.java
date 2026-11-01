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

import com.techsenger.fontjars.mdi.Mdi;
import com.techsenger.fontjars.techsengericons.TechsengerIcons;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public final class Fonts {

    public static final Font MATERIAL_DESIGN_ICONS =
            Font.loadFont(Mdi.WEBFONT.getUrl().toExternalForm(), 12);

    public static final Font TECHSENGER_ICONS_MDI =
            Font.loadFont(TechsengerIcons.MDI.getUrl().toExternalForm(), 12);

    private Fonts() {
        // empty
    }
}
