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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableDoubleProperty;
import javafx.css.SimpleStyleableIntegerProperty;
import javafx.css.SimpleStyleableStringProperty;
import javafx.css.StyleOrigin;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableIntegerProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.css.StyleableStringProperty;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Text} node that renders a single font-based icon, described by a {@link FontIcon}.
 *
 * <p>The icon's code point, font family and size are exposed as independent CSS-styleable properties
 * ({@code -fx-icon-code}, {@code -fx-icon-family}, {@code -fx-icon-size}) rather than through the inherited
 * {@code fontProperty()}. This avoids conflicts between manually assigned font families and CSS-driven font
 * sizes, since {@code -fx-font-*} sub-properties and this view's icon properties are resolved independently
 * by the CSS engine. Any stylesheet rule that still targets {@code -fx-font-size} on this view (or on the
 * inherited {@code icon-view} style class) will bypass this mechanism entirely and should be migrated to
 * {@code -fx-icon-size}.
 *
 * <p>Two kinds of {@link FontIcon} are supported:
 * <ul>
 *     <li>{@link PlainFontIcon} — carries an explicit code point and, optionally, a font family. Both are
 *     read directly from the icon instance in {@link #updateText()}/{@link #updateFont()}, bypassing the
 *     CSS-driven {@link #iconCode}/{@link #iconFamily} properties entirely.</li>
 *     <li>{@link StyleFontIcon} — contributes only a style class; the code point, font and size are expected
 *     to be defined entirely in a stylesheet rule for that class.</li>
 * </ul>
 *
 * <p>If no font family is resolved for an icon (neither from a {@link PlainFontIcon} nor from CSS), the
 * static {@link #defaultIconFontProperty() default icon font} is used instead.
 *
 * @author Pavel Castornii
 */
public class FontIconView extends Text {

    private static final class Css {
        private static final CssMetaData<FontIconView, Number> ICON_SIZE;
        private static final CssMetaData<FontIconView, String> ICON_FAMILY;
        private static final CssMetaData<FontIconView, Number> ICON_CODE;
        private static final List<CssMetaData<?, ?>> META_DATA;
        static {
            var factory = new StyleablePropertyFactory<FontIconView>(Text.getClassCssMetaData());
            ICON_SIZE = factory.createSizeCssMetaData("-fx-icon-size", s -> s.iconSize, 14.0);
            ICON_FAMILY = factory.createStringCssMetaData("-fx-icon-family", s -> s.iconFamily, null);
            ICON_CODE = factory.createSizeCssMetaData("-fx-icon-code", s -> s.iconCode, 0);
            META_DATA = List.copyOf(factory.getCssMetaData());
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(FontIconView.class);

    /**
     * The application-wide default font family used when an icon does not specify its own font, either via
     * {@link PlainFontIcon#getFont()} or via the {@code -fx-icon-family} CSS property.
     */
    private static final ObjectProperty<String> defaultIconFont =
            new SimpleObjectProperty<>(FontIconView.class, "defaultIconFont");

    /**
     * Returns the property holding the application-wide default icon font family.
     *
     * @return the default icon font property
     */
    public static ObjectProperty<String> defaultIconFontProperty() {
        return defaultIconFont;
    }

    /**
     * Returns the application-wide default icon font family.
     *
     * @return the default font family, or {@code null} if not set
     */
    public static String getDefaultIconFont() {
        return defaultIconFont.get();
    }

    /**
     * Sets the application-wide default icon font family, used by any {@link FontIconView} instance that does
     * not resolve its own font family from an icon or from CSS.
     *
     * @param font the default font family to use
     */
    public static void setDefaultIconFont(String font) {
        defaultIconFont.set(font);
    }

    /**
     * Returns the CSS metadata for this class, including the inherited {@link Text} metadata and the
     * {@code -fx-icon-size}, {@code -fx-icon-family} and {@code -fx-icon-code} properties.
     *
     * @return the list of CSS metadata for this class
     */
    public static List<CssMetaData<?, ?>> getClassCssMetaData() {
        return Css.META_DATA;
    }

    /**
     * Merges a single CSS declaration into an existing inline style string, replacing any prior
     * declaration for the same property while leaving all other declarations intact.
     *
     * @param style    the current inline style string, possibly {@code null} or blank
     * @param property the CSS property name to set (e.g. {@code "-fx-icon-size"})
     * @param value    the value to assign to the property
     * @return the merged inline style string
     */
    private static String normalizeStyle(String style, String property, String value) {
        StringBuilder sb = new StringBuilder();
        if (style != null && !style.isBlank()) {
            for (String part : style.split(";")) {
                String trimmed = part.trim();
                if (trimmed.isEmpty() || trimmed.startsWith(property + ":")) {
                    continue;
                }
                sb.append(trimmed).append("; ");
            }
        }
        sb.append(property).append(": ").append(value).append(";");
        return sb.toString();
    }

    private final StyleableDoubleProperty iconSize =
            new SimpleStyleableDoubleProperty(Css.ICON_SIZE, this, "iconSize", 14.0);

    private final StyleableStringProperty iconFamily =
            new SimpleStyleableStringProperty(Css.ICON_FAMILY, this, "iconFamily", null);

    private final StyleableIntegerProperty iconCode =
            new SimpleStyleableIntegerProperty(Css.ICON_CODE, this, "iconCode", 0);

    private final ObjectProperty<FontIcon<?>> icon = new SimpleObjectProperty<>();

    private final ChangeListener<String> defaultFontListener = (ov, oldV, newV) -> updateFont();

    private final StringProperty units = new SimpleStringProperty(this, "units", "px");

    /**
     * Creates a view and immediately assigns the given icon.
     *
     * @param icon the icon to display
     */
    public FontIconView(FontIcon<?> icon) {
        this();
        setIcon(icon);
    }

    /**
     * Creates an empty view with no icon assigned.
     */
    public FontIconView() {
        getStyleClass().addAll("font-icon-view", "icon-view");
        fontProperty().addListener((ov, oldV, newV) -> checkFontOriginConflict());
        // Only relevant for StyleFontIcon, whose code/font come entirely from a stylesheet rule.
        // PlainFontIcon bypasses these two properties entirely; see updateText()/updateFont().
        iconCode.addListener((ov, oldV, newV) -> updateText());
        iconFamily.addListener((ov, oldV, newV) -> updateFont());
        iconSize.addListener((ov, oldV, newV) -> {
            updateFont();
            freezeResolvedSize(newV.doubleValue());
        });
        defaultIconFont.addListener(new WeakChangeListener<>(defaultFontListener));
        icon.addListener((ov, oldV, newV) -> {
            if (oldV instanceof StyleFontIcon sfi) {
                getStyleClass().remove(sfi.getContent());
            }
            if (newV instanceof StyleFontIcon sfi) {
                getStyleClass().add(sfi.getContent());
            }
            updateText();
            updateFont();
        });
        updateText();
        updateFont();
    }

    /**
     * Returns the property holding the CSS units appended when the resolved icon size is frozen back into an
     * inline style override, in order to break an {@code em}-relative self-recalculation loop that can otherwise
     * occur when the resolved size is computed relative to this view's own font.
     *
     * @return the units property
     */
    public StringProperty unitsProperty() {
        return units;
    }

    /**
     * Returns the CSS units used when freezing the resolved icon size.
     *
     * @return the current units, e.g. {@code "px"}
     */
    public String getUnits() {
        return units.get();
    }

    /**
     * Sets the CSS units used when freezing the resolved icon size. If {@code null} or blank, {@code "px"}
     * is used instead.
     *
     * @param units the units to use when freezing the resolved icon size
     */
    public void setUnits(String units) {
        this.units.set(units);
    }

    /**
     * Returns the CSS metadata for this instance.
     *
     * @return the list of CSS metadata
     */
    @Override
    public List<CssMetaData<?, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    /**
     * Returns the styleable property holding the icon's font size, backed by the {@code -fx-icon-size}
     * CSS property.
     *
     * @return the icon size property
     */
    public DoubleProperty iconSizeProperty() {
        return iconSize;
    }

    /**
     * Returns the icon's font size.
     *
     * @return the current icon size
     */
    public double getIconSize() {
        return iconSize.get();
    }

    /**
     * Sets the icon's font size.
     *
     * @param size the icon size to use
     */
    public void setIconSize(double size) {
        iconSize.set(size);
    }

    /**
     * Returns the styleable property holding the icon's font family, backed by the {@code -fx-icon-family}
     * CSS property. A {@code null} value means the {@link #defaultIconFontProperty() default icon font}
     * is used instead. Only used for a {@link StyleFontIcon}; a {@link PlainFontIcon}'s font is read
     * directly from the icon instance instead.
     *
     * @return the icon font family property
     */
    public StringProperty iconFamilyProperty() {
        return iconFamily;
    }

    /**
     * Returns the icon's font family, as resolved from CSS for a {@link StyleFontIcon}.
     *
     * @return the current font family, or {@code null} if none is explicitly set
     */
    public String getIconFamily() {
        return iconFamily.get();
    }

    /**
     * Sets the icon's font family. Only meaningful for a {@link StyleFontIcon}.
     *
     * @param family the font family to use, or {@code null} to fall back to the default icon font
     */
    public void setIconFamily(String family) {
        iconFamily.set(family);
    }

    /**
     * Returns the styleable property holding the icon's Unicode code point, backed by the
     * {@code -fx-icon-code} CSS property. Only used for a {@link StyleFontIcon}; a {@link PlainFontIcon}'s
     * code point is read directly from the icon instance instead.
     *
     * @return the icon code property
     */
    public IntegerProperty iconCodeProperty() {
        return iconCode;
    }

    /**
     * Returns the icon's Unicode code point, as resolved from CSS for a {@link StyleFontIcon}.
     *
     * @return the current code point, or {@code 0} if none is set
     */
    public int getIconCode() {
        return iconCode.get();
    }

    /**
     * Sets the icon's Unicode code point. Only meaningful for a {@link StyleFontIcon}.
     *
     * @param code the code point to display, or {@code 0} to clear the icon's text
     */
    public void setIconCode(int code) {
        this.iconCode.set(code);
    }

    /**
     * Returns the property holding the {@link FontIcon} currently assigned to this view.
     *
     * @return the icon property
     */
    public ObjectProperty<FontIcon<?>> iconProperty() {
        return this.icon;
    }

    /**
     * Returns the {@link FontIcon} currently assigned to this view.
     *
     * @return the current icon, or {@code null} if none is assigned
     */
    public FontIcon<?> getIcon() {
        return this.icon.get();
    }

    /**
     * Assigns a {@link FontIcon} to this view, updating the style class, code point and font accordingly.
     *
     * @param icon the icon to display, or {@code null} to clear the current icon
     */
    public void setIcon(FontIcon<?> icon) {
        this.icon.set(icon);
    }

    /**
     * Freezes the CSS-resolved icon size as an absolute-unit inline override, preventing the
     * {@code em}-relative {@code -fx-icon-size} declaration from recomputing against this view's own,
     * just-updated font on the next CSS pass (which would otherwise spiral).
     *
     * @param resolvedSize the size, in pixels, that CSS just resolved for {@code -fx-icon-size}
     */
    private void freezeResolvedSize(double resolvedSize) {
        String resolvedUnits = units.get() == null || units.get().isBlank() ? "px" : units.get();
        setStyle(normalizeStyle(getStyle(), "-fx-icon-size", resolvedSize + resolvedUnits));
    }

    /**
     * Resolves the code point to display: read directly from a {@link PlainFontIcon}, or from the
     * CSS-driven {@link #iconCode} for a {@link StyleFontIcon}.
     */
    private void updateText() {
        int cp;
        if (icon.get() instanceof PlainFontIcon pfi) {
            cp = pfi.getContent();
        } else {
            cp = iconCode.get();
        }
        setText(cp != 0 ? new String(Character.toChars(cp)) : null);
    }

    /**
     * Resolves the font to apply: read directly from a {@link PlainFontIcon} (falling back to the default
     * icon font), or from the CSS-driven {@link #iconFamily} for a {@link StyleFontIcon}.
     */
    private void updateFont() {
        String family;
        if (icon.get() instanceof PlainFontIcon pfi) {
            family = pfi.getFont() != null ? pfi.getFont() : defaultIconFont.get();
        } else {
            family = iconFamily.get() != null ? iconFamily.get() : defaultIconFont.get();
        }
        setFont(family != null ? Font.font(family, iconSize.get()) : Font.font(iconSize.get()));
    }

    /**
     * Detects CSS rules that target the inherited {@code -fx-font-*} properties on this view instead of the
     * dedicated {@code -fx-icon-family}/{@code -fx-icon-size} properties, and logs a warning. Such rules
     * silently fight with {@link #updateFont()} on every CSS pass (e.g. on pseudo-class changes like
     * {@code :hover} or {@code :focused}), causing the icon's font family or size to jump unexpectedly.
     */
    private void checkFontOriginConflict() {
        var fontProp = (StyleableObjectProperty<Font>) fontProperty();
        if (fontProp.getStyleOrigin() == StyleOrigin.AUTHOR) {
            logger.warn("FontIconView '{}' (classes: {}): -fx-font-* rules are used instead of -fx-icon-*",
                    this, getStyleClass());
        }
    }
}
