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

import com.techsenger.annotations.Nullable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

/**
 * An {@link HBox} that shows the view of the given icon. The icon is optional: a box without an icon
 * ({@code null}) is empty and gets the {@code empty} pseudo class.
 *
 * @author Pavel Castornii
 */
public class IconViewBox extends HBox {

    private static final PseudoClass EMPTY_PSEUDO_CLASS = PseudoClass.getPseudoClass("empty");

    private final ObjectProperty<@Nullable Icon<?>> icon = new SimpleObjectProperty<>();

    public IconViewBox() {
        this(null);
    }

    public IconViewBox(@Nullable Icon<?> i) {
        getChildren().addListener((ListChangeListener<Node>) change -> {
            pseudoClassStateChanged(EMPTY_PSEUDO_CLASS, getChildren().isEmpty());
        });
        pseudoClassStateChanged(EMPTY_PSEUDO_CLASS, true);
        getStyleClass().add("icon-view-box");
        icon.addListener((ov, oldV, newV) -> {
            getChildren().clear();
            if (newV != null) {
                if (newV instanceof FontIcon fi) {
                    var view = new FontIconView(fi);
                    getChildren().add(view);
                } else if (newV instanceof ImageIcon ii)  {
                    var view = new ImageIconView(ii);
                    getChildren().add(view);
                } else {
                    throw new AssertionError();
                }
            }
        });
        setIcon(i);
    }

    public @Nullable Icon<?> getIcon() {
        return icon.get();
    }

    public void setIcon(@Nullable Icon<?> icon) {
        this.icon.set(icon);
    }

    public ObjectProperty<@Nullable Icon<?>> iconProperty() {
        return icon;
    }
}
