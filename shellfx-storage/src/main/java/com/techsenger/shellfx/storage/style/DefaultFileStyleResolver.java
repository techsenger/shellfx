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

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.material.theme.Theme;
import com.techsenger.shellfx.storage.FileEntryType;
import com.techsenger.shellfx.storage.StorageFile;
import com.techsenger.shellfx.storage.settings.FileStyleScheme;
import com.techsenger.shellfx.storage.settings.FileStyleSettings;
import com.techsenger.toolkit.fx.color.ColorUtils;
import java.util.function.Function;

/**
 * Styles the files with the scheme of the current theme: the root of a storage is styled as its storage, any other
 * file by the style of its kind (a link by the kind of the file it points to), over which the masks of a failed and a
 * hidden file are laid in this order. An application that knows more kinds of files and states extends its scheme
 * and overrides {@link #selectSettings} and {@link #applyMasks}.
 *
 * <p>It can be called from several threads.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 * @param <S> the type of the scheme
 */
public class DefaultFileStyleResolver<F extends StorageFile, S extends FileStyleScheme>
        implements FileStyleResolver<F> {

    private final Function<Theme, S> schemeProvider;

    /**
     * Creates a resolver.
     *
     * @param schemeProvider gives the scheme of a theme. It is called for every file, so it must be fast and safe
     *     for several threads, that is, keep the schemes it has created.
     */
    public DefaultFileStyleResolver(Function<Theme, S> schemeProvider) {
        this.schemeProvider = schemeProvider;
    }

    @Override
    public IconTextStyle resolve(F file, FileDecorationType type, Theme theme) {
        if (file.isRoot()) {
            return file.getStorage().getStyleResolver().resolve(theme);
        }
        var scheme = schemeProvider.apply(theme);
        var foreground = theme.getPalette().getDefaultFgColor() & 0xFFFFFF;
        var settings = applyMasks(selectSettings(getStyledFile(file), scheme), file, type, scheme);
        return toIconTextStyle(settings, foreground);
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
        if (isHidden(file)) {
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
     * Tells whether the file is shown as hidden, that is, dimmed. An application overrides it when a state of the
     * file makes it show at full strength.
     *
     * @param file the file
     * @return {@code true} if the mask of the hidden file is laid over the file
     */
    protected boolean isHidden(F file) {
        return file.isHidden();
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
     * Selects the settings of the kind of the file.
     *
     * @param file the file whose kind to style
     * @param scheme the scheme of the current theme
     * @return the settings of the kind
     */
    protected FileStyleSettings selectSettings(StorageFile file, S scheme) {
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
     * Lays a mask over a style: the color of the mask is composited over the color of the style by its alpha, and
     * every other value the mask sets replaces the one of the style.
     *
     * @param base the style to lay the mask over
     * @param mask the mask
     * @return a new style
     */
    protected FileStyleSettings overlay(FileStyleSettings base, FileStyleSettings mask) {
        var result = new FileStyleSettings();
        result.getIcon().setColor(over(mask.getIcon().getColor(), base.getIcon().getColor()));
        var maskText = mask.getText();
        var baseText = base.getText();
        var text = result.getText();
        text.setColor(over(maskText.getColor(), baseText.getColor()));
        text.setBold(maskText.getBold() != null ? maskText.getBold() : baseText.getBold());
        text.setItalic(maskText.getItalic() != null ? maskText.getItalic() : baseText.getItalic());
        return result;
    }

    /**
     * Converts the settings of a style to the inline styles of the icon and of the texts. A color that is not
     * opaque is composited over the main color of the theme.
     *
     * @param style the settings
     * @param foreground the main color of the theme, 0xRRGGBB
     * @return the styles
     */
    protected IconTextStyle toIconTextStyle(FileStyleSettings style, int foreground) {
        var iconColor = style.getIcon().getColor();
        var iconStyle = iconColor == null ? null : "-fx-fill: " + toCssColor(iconColor, foreground) + ";";

        var text = style.getText();
        var textColor = text.getColor();
        var bold = text.getBold();
        var italic = text.getItalic();
        var textStyle = new StringBuilder();
        if (textColor != null) {
            textStyle.append("-fx-text-fill: ").append(toCssColor(textColor, foreground)).append(";");
        }
        if (bold != null) {
            textStyle.append("-fx-font-weight: ").append(bold ? "bold" : "normal").append(";");
        }
        if (italic != null) {
            textStyle.append("-fx-font-style: ").append(italic ? "italic" : "normal").append(";");
        }
        return new IconTextStyle(iconStyle, textStyle.isEmpty() ? null : textStyle.toString());
    }

    private @Nullable Integer over(@Nullable Integer top, @Nullable Integer bottom) {
        if (top == null) {
            return bottom;
        }
        return bottom == null ? top : composite(top, bottom);
    }

    private String toCssColor(int color, int foreground) {
        return ColorUtils.toCssRgba(composite(color, (foreground << 8) | 0xFF) >>> 8);
    }

    private int composite(int top, int bottom) {
        double topAlpha = (top & 0xFF) / 255.0;
        double bottomAlpha = (bottom & 0xFF) / 255.0;
        double alpha = topAlpha + bottomAlpha * (1 - topAlpha);
        if (alpha == 0) {
            return 0;
        }
        int result = (int) Math.round(alpha * 255);
        for (int shift = 24; shift >= 8; shift -= 8) {
            double channel = (((top >>> shift) & 0xFF) * topAlpha
                    + ((bottom >>> shift) & 0xFF) * bottomAlpha * (1 - topAlpha)) / alpha;
            result |= (int) Math.round(channel) << shift;
        }
        return result;
    }
}
