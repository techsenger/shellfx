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

package com.techsenger.tabshell.material.icon;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.HBox;

/**
 *
 * @author Pavel Castornii
 */
public class IconViewBox extends HBox {

    private final ObjectProperty<Icon<?>> icon = new SimpleObjectProperty<>();

    public IconViewBox() {
        getStyleClass().add("icon-view-box");
        icon.addListener((ov, oldV, newV) -> {
            getChildren().clear();
            if (newV != null) {
                if (newV instanceof FontIcon) {
                    var view = new FontIconView((FontIcon) newV);
                    getChildren().add(view);
                } else if (newV instanceof ImageIcon)  {
                    var view = new ImageIconView((ImageIcon) newV);
                    getChildren().add(view);
                } else {
                    throw new AssertionError();
                }
            }
        });
    }

    public Icon<?> getIcon() {
        return icon.get();
    }

    public void setIcon(Icon<?> icon) {
        this.icon.set(icon);
    }

    public ObjectProperty<Icon<?>> iconProperty() {
        return icon;
    }
}
