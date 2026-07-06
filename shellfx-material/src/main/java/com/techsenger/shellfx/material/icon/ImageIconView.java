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

package com.techsenger.shellfx.material.icon;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.ImageView;

/**
 *
 * @author Pavel Castornii
 */
public class ImageIconView extends ImageView {

    private final ObjectProperty<ImageIcon<?>> icon = new SimpleObjectProperty<>();

    public ImageIconView(ImageIcon<?> icon) {
        this();
        setIcon(icon);
    }

    public ImageIconView() {
        getStyleClass().addAll("image-icon-view", "icon-view");
        this.icon.addListener((ov, oldV, newV) -> {
            if (oldV != null) {
                if (oldV instanceof StyleImageIcon sii) {
                    getStyleClass().remove(sii.getContent());
                } else {
                    setImage(null);
                }
            }
            if (newV != null) {
                if (newV instanceof StyleImageIcon sii) {
                    getStyleClass().add(sii.getContent());
                } else {
                    var i = (PlainImageIcon) newV;
                    setImage(i.getContent());
                }
            }
        });
    }

    public ObjectProperty<ImageIcon<?>> iconProperty() {
        return this.icon;
    }

    public ImageIcon<?> getIcon() {
        return this.icon.get();
    }

    public void setIcon(ImageIcon<?> icon) {
        this.icon.set(icon);
    }

}
