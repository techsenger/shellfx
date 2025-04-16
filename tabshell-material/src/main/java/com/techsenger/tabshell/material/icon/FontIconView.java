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

import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableIntegerProperty;
import javafx.css.StyleableIntegerProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.scene.control.Label;

/**
 * Font icon that supports icon size in ems and pixels. It extends {@link Label} (not Text because Label supports more
 * CSS properties) and adds only one property = code point (-fx-code-point). Note, the code point in the decimal system.
 *
 * <p>Ikonli is not used because it doesn't support `em` see  https://github.com/kordamp/ikonli/issues/150 . It seems
 * that the reason is in custom property `-fx-icon-size: 1.0em;` that internally sets `-fx-font-size` and it creates
 * endless loop.
 *
 * <p>That's why this class extends {@link Label} and uses its `-fx-font-size` property adding only one custom property.
 *
 * @author Pavel Castornii
 */
public class FontIconView extends Label {

    private static final class Css {

        private static final CssMetaData<FontIconView, Number> CODE_POINT;

        private static final List<CssMetaData<?, ?>> META_DATA;

        static {
            var factory = new StyleablePropertyFactory<FontIconView>(Label.getClassCssMetaData());
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
        getStyleClass().add("font-icon-view");
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
    public List<CssMetaData<?, ?>> getControlCssMetaData() {
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
