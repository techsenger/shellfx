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

package com.techsenger.shellfx.storage.style;

import com.techsenger.shellfx.material.theme.Theme;
import com.techsenger.shellfx.storage.FileEntryType;
import com.techsenger.shellfx.storage.StorageFile;
import com.techsenger.shellfx.storage.settings.FileStyleScheme;
import com.techsenger.shellfx.storage.settings.FileStyleSettings;
import com.techsenger.toolkit.fx.color.ColorUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Styles the files with the scheme of the current theme: the root of a storage is styled as its storage, any other
 * file by the style of its kind (a link by the kind of the file it points to), over which the masks of a failed and a
 * hidden file are laid in this order. An application that knows more kinds of files and states extends its scheme
 * and overrides {@link #selectStyle} and {@link #applyMasks}.
 *
 * <p>The schemes are kept per theme, so a resolver creates each of them once. It can be called from several
 * threads.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 * @param <S> the type of the scheme
 */
public class DefaultFileStyleResolver<F extends StorageFile, S extends FileStyleScheme>
        implements FileStyleResolver<F> {

    private final Function<Theme, S> schemeFactory;

    private final Map<Theme, S> schemesByTheme = new ConcurrentHashMap<>();

    /**
     * Creates a resolver.
     *
     * @param schemeFactory creates the scheme of a theme, once for every theme
     */
    public DefaultFileStyleResolver(Function<Theme, S> schemeFactory) {
        this.schemeFactory = schemeFactory;
    }

    @Override
    public IconTextStyle resolve(F file, FileDecorationType type, Theme theme) {
        if (file.isRoot()) {
            return file.getStorage().getStyleResolver().resolve(theme);
        }
        var scheme = schemesByTheme.computeIfAbsent(theme, schemeFactory);
        return toIconTextStyle(resolveSettings(file, type, scheme));
    }

    /**
     * Gives the style of a file that is not a root: the style of its kind with the masks of its states over it.
     *
     * @param file the file
     * @param type whether the decoration is provisional or final
     * @param scheme the scheme of the current theme
     * @return the settings of the style
     */
    protected FileStyleSettings resolveSettings(F file, FileDecorationType type, S scheme) {
        return applyMasks(selectStyle(getStyledFile(file), scheme), file, type, scheme);
    }

    /**
     * Lays the masks of the states of the file over the style of its kind: first the failed one, then the hidden
     * one. An application with more states overrides it to lay its own masks before, between or after these.
     *
     * @param style the style of the kind of the file
     * @param file the file
     * @param type whether the decoration is provisional or final
     * @param scheme the scheme of the current theme
     * @return the style with the masks laid over it
     */
    protected FileStyleSettings applyMasks(FileStyleSettings style, F file, FileDecorationType type, S scheme) {
        var result = style;
        if (isError(file, type)) {
            result = overlay(result, scheme.getError());
        }
        if (file.isHidden()) {
            result = overlay(result, scheme.getHidden());
        }
        return result;
    }

    /**
     * Gives the file whose kind styles the file: the target of a link, or the file itself.
     *
     * @param file the file
     * @return the file whose kind to use
     */
    protected StorageFile getStyledFile(F file) {
        var link = file.isLink() ? file.getLink() : null;
        var target = link == null ? null : link.getTarget();
        return target != null ? target : file;
    }

    /**
     * Tells whether the file is shown as erroneous: the default says so of a link that turned out to be broken, once
     * the file is final. An application with more kinds of errors overrides it and keeps the answer of the
     * superclass.
     *
     * @param file the file
     * @param type whether the decoration is provisional or final
     * @return {@code true} if the mask of the error is laid over the file
     */
    protected boolean isError(F file, FileDecorationType type) {
        var link = file.getLink();
        return type == FileDecorationType.FINAL && link != null && link.getTarget() == null;
    }

    /**
     * Selects the style of the kind of the file.
     *
     * @param file the file whose kind to style
     * @param scheme the scheme of the current theme
     * @return the settings of the style of the kind
     */
    protected FileStyleSettings selectStyle(StorageFile file, S scheme) {
        if (file.isDirectory()) {
            return scheme.getDirectory();
        }
        if (file.getEntryType() == FileEntryType.OTHER) {
            return scheme.getOther();
        }
        // a broken link has no kind of its own, the error mask is what shows it
        return scheme.getFile();
    }

    /**
     * Lays a mask over a style: every value the mask sets replaces the one of the style, and the opacity of the mask
     * is multiplied by the opacity of the style.
     *
     * @param base the style to lay the mask over
     * @param mask the mask
     * @return a new style
     */
    protected FileStyleSettings overlay(FileStyleSettings base, FileStyleSettings mask) {
        var result = new FileStyleSettings();
        var baseOpacity = base.getOpacity();
        var maskOpacity = mask.getOpacity();
        if (baseOpacity != null && maskOpacity != null) {
            result.setOpacity(baseOpacity * maskOpacity);
        } else {
            result.setOpacity(maskOpacity != null ? maskOpacity : baseOpacity);
        }
        var maskIconColor = mask.getIcon().getColor();
        result.getIcon().setColor(maskIconColor != null ? maskIconColor : base.getIcon().getColor());
        var maskText = mask.getText();
        var baseText = base.getText();
        var text = result.getText();
        text.setColor(maskText.getColor() != null ? maskText.getColor() : baseText.getColor());
        text.setBold(maskText.getBold() != null ? maskText.getBold() : baseText.getBold());
        text.setItalic(maskText.getItalic() != null ? maskText.getItalic() : baseText.getItalic());
        return result;
    }

    /**
     * Converts the settings of a style to the inline styles of the icon and of the texts.
     *
     * @param style the settings
     * @return the styles
     */
    protected IconTextStyle toIconTextStyle(FileStyleSettings style) {
        // Alpha 0 means "no alpha" for ColorUtils, so a fully transparent style is made almost transparent.
        var opacity = style.getOpacity();
        int alpha = opacity == null ? 255 : Math.max(1, (int) Math.round(opacity * 255));

        var iconColor = style.getIcon().getColor();
        var iconStyle = iconColor == null ? null
                : "-fx-fill: " + ColorUtils.toCssRgba(ColorUtils.toArgb(alpha, iconColor)) + ";";

        var text = style.getText();
        var textColor = text.getColor();
        var bold = text.getBold();
        var italic = text.getItalic();
        var textStyle = new StringBuilder();
        if (textColor != null) {
            textStyle.append("-fx-text-fill: ").append(ColorUtils.toCssRgba(ColorUtils.toArgb(alpha, textColor)))
                    .append(";");
        }
        if (bold != null) {
            textStyle.append("-fx-font-weight: ").append(bold ? "bold" : "normal").append(";");
        }
        if (italic != null) {
            textStyle.append("-fx-font-style: ").append(italic ? "italic" : "normal").append(";");
        }
        return new IconTextStyle(iconStyle, textStyle.isEmpty() ? null : textStyle.toString());
    }
}
