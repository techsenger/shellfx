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

import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableIntegerProperty;
import javafx.css.StyleableIntegerProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.scene.text.Text;

/**
 * Font icon that supports icon size in ems and pixels. It extends {@link Text} (not Label because Label adds extra
 * space, although it supports more CSS properties) and adds only one property = code point (-fx-code-point). Note,
 * the code point in the decimal system.
 *
 * <p>To set the icon size, use the standard -fx-font-size property.
 *
 * @author Pavel Castornii
 */
public class FontIconView extends Text {

    private static final class Css {

        private static final CssMetaData<FontIconView, Number> CODE_POINT;

        private static final List<CssMetaData<?, ?>> META_DATA;

        static {
            var factory = new StyleablePropertyFactory<FontIconView>(Text.getClassCssMetaData());
            CODE_POINT = factory.createSizeCssMetaData("-fx-code-point", s -> s.codePoint);
            META_DATA = List.copyOf(factory.getCssMetaData());
        }
    }

    public static List<CssMetaData<?, ?>> getClassCssMetaData() {
        return Css.META_DATA;
    }

    private final StyleableIntegerProperty codePoint =
            new SimpleStyleableIntegerProperty(Css.CODE_POINT, this, "codePoint");

    private final ObjectProperty<GenericFontIcon<?>> icon = new SimpleObjectProperty<>();

    public FontIconView(GenericFontIcon<?> icon) {
        this();
        setIcon(icon);
    }

    public FontIconView() {
        getStyleClass().addAll("font-icon-view", "icon-view");
        codePoint.addListener((ov, oldV, newV) -> {
            if (newV != null && newV.intValue() != 0) {
                var iconStr = new String(Character.toChars(newV.intValue()));
                setText(iconStr);
            } else {
                setText(null);
            }
        });
        this.icon.addListener((ov, oldV, newV) -> {
            if (oldV != null) {
                if (oldV instanceof StyleFontIcon) {
                    var i = (StyleFontIcon) oldV;
                    getStyleClass().remove(i.getContent());
                } else {
                    setCodePoint(0);
                }
            }
            if (newV != null) {
                if (newV instanceof StyleFontIcon) {
                    var i = (StyleFontIcon) newV;
                    getStyleClass().add(i.getContent());
                } else {
                    var i = (FontIcon) newV;
                    setCodePoint(i.getContent());
                }
            }
        });
    }

    @Override
    public List<CssMetaData<?, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    public IntegerProperty codePointProperty() {
        return codePoint;
    }

    public int getCodePoint() {
        return codePoint.get();
    }

    public void setCodePoint(int codePoint) {
        this.codePoint.set(codePoint);
    }

    public ObjectProperty<GenericFontIcon<?>> iconProperty() {
        return this.icon;
    }

    public GenericFontIcon<?> getIcon() {
        return this.icon.get();
    }

    public void setIcon(GenericFontIcon<?> icon) {
        this.icon.set(icon);
    }
}
