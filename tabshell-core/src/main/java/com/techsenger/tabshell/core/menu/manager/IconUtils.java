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

package com.techsenger.tabshell.core.menu.manager;

import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.icon.GenericFontIcon;
import com.techsenger.tabshell.material.icon.GenericImageIcon;
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.tabshell.material.icon.ImageIconView;
import javafx.scene.control.MenuItem;

/**
 *
 * @author Pavel Castornii
 */
final class IconUtils {

    static void setIcon(MenuItem item, Icon<?> icon) {
        var graphic = item.getGraphic();
        if (graphic == null) {
            return;
        }
        if (graphic instanceof ImageIconView view) {
            view.setIcon((GenericImageIcon<?>) icon);
        } else if (graphic instanceof FontIconView view) {
            view.setIcon((GenericFontIcon<?>) icon);
        } else if (graphic instanceof IconViewBox box) {
            box.setIcon(icon);
        }
    }

    static Icon<?> getIcon(MenuItem item) {
        var graphic = item.getGraphic();
        if (graphic == null) {
            return null;
        }
        if (graphic instanceof ImageIconView view) {
            return view.getIcon();
        } else if (graphic instanceof FontIconView view) {
            return view.getIcon();
        } else if (graphic instanceof IconViewBox box) {
            return box.getIcon();
        }
        return null;
    }

    private IconUtils() {

    }
}
