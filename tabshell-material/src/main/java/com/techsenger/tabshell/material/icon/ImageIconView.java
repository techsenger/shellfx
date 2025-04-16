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
import javafx.scene.image.ImageView;

/**
 *
 * @author Pavel Castornii
 */
public class ImageIconView extends ImageView {

    private final ObjectProperty<GenericImageIcon<?>> icon = new SimpleObjectProperty<>();

    public ImageIconView(GenericImageIcon<?> icon) {
        this();
        setIcon(icon);
    }

    public ImageIconView() {
        getStyleClass().add("image-icon-view");
        this.icon.addListener((ov, oldV, newV) -> {
            if (oldV != null) {
                if (oldV instanceof StyleImageIcon) {
                    var i = (StyleImageIcon) oldV;
                    getStyleClass().remove(i.getContent());
                } else {
                    setImage(null);
                }
            }
            if (newV != null) {
                if (newV instanceof StyleImageIcon) {
                    var i = (StyleImageIcon) newV;
                    getStyleClass().add(i.getContent());
                } else {
                    var i = (ImageIcon) newV;
                    setImage(i.getContent());
                }
            }
        });
    }

    public ObjectProperty<GenericImageIcon<?>> iconProperty() {
        return this.icon;
    }

    public GenericImageIcon<?> getIcon() {
        return this.icon.get();
    }

    public void setIcon(GenericImageIcon<?> icon) {
        this.icon.set(icon);
    }

}
