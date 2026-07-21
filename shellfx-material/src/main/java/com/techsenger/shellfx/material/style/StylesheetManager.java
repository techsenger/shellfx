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

package com.techsenger.shellfx.material.style;

import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.shellfx.material.theme.Theme;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages a set of {@link Stylesheet} instances applied to a {@link Parent} node, with automatic theme-specific
 * extension support.
 *
 * <p>The node may have other stylesheets applied to it externally (not managed by this class); this manager only
 * tracks and modifies the stylesheets it owns, leaving unmanaged stylesheets untouched.
 *
 * <p>The theme may not be known at construction time. Stylesheets added before the theme is set are queued internally
 * but not applied to the node until {@link #setTheme(Theme)} is called. Once the theme is set, all subsequent
 * {@link #addStylesheets(List)} calls apply immediately.
 *
 * @author Pavel Castornii
 */
public class StylesheetManager {

    private static final Logger logger = LoggerFactory.getLogger(StylesheetManager.class);

    private static String getThemedStylesheetUrl(String url, Theme theme) {
        int lastSlash = url.lastIndexOf('/');
        var fileName = url.substring(lastSlash + 1);
        fileName = fileName.substring(0, fileName.length() - 4);
        fileName = fileName + "-" + theme.name().toLowerCase().replace("_", "-") + ".css";
        var newUrl = url.substring(0, lastSlash + 1) + fileName;
        return newUrl;
    }

    /**
     * Resolved lazily on every call, not cached — for a TOP_LEVEL window the target is the Scene's stylesheets
     * (so that popups showing over the window, which only inherit Scene-level stylesheets, are styled
     * correctly), and for a NESTED window it is the window's own root node's stylesheets, since a nested window
     * has no Scene of its own.
     */
    private final Supplier<ObservableList<String>> targetSupplier;

    private final List<Stylesheet> stylesheets = new ArrayList<>();

    /**
     * Set of stylesheet URLs currently applied to the target by this manager. Used for fast membership checks
     * when removing stylesheets, to avoid touching unmanaged stylesheets that may also be present on the target.
     */
    private final Set<String> managedUrls = new HashSet<>();

    private final Supplier<@Nullable String> logPrefix;

    private @Nullable Theme theme;

    public StylesheetManager(Supplier<ObservableList<String>> targetSupplier, Supplier<@Nullable String> logPrefix) {
        this.targetSupplier = targetSupplier;
        this.logPrefix = logPrefix;
    }

    public Supplier<@Nullable String> getLogPrefix() {
        return logPrefix;
    }

    public @Nullable Theme getTheme() {
        return theme;
    }

    /**
     * Sets the current theme and applies all queued stylesheets (including their theme-specific
     * extensions) to the target. If stylesheets were added before this call, they are applied now.
     * Subsequent calls to {@link #addStylesheets(List)} will apply immediately.
     *
     * @param theme the theme to apply
     */
    public void setTheme(Theme theme) {
        this.theme = theme;
        if (this.theme != null) {
            updateStylesheets();
            logStylesheets();
        }
    }

    /**
     * Adds the given stylesheets to this manager. If the theme is already set, the stylesheets
     * (and any theme-specific extensions matching the current theme) are applied to the target
     * immediately. If the theme is not yet set, the stylesheets are queued and will be applied
     * when {@link #setTheme(Theme)} is called.
     *
     * @param sheets the stylesheets to add
     */
    public void addStylesheets(List<Stylesheet> sheets) {
        this.stylesheets.addAll(sheets);
        if (this.theme != null) {
            for (var sheet : sheets) {
                addToTarget(sheet, this.theme);
            }
            logStylesheets();
        }
    }

    /**
     * Removes the given stylesheets from this manager and from the target. Only URLs that were
     * applied by this manager are removed; unmanaged stylesheets on the target are not affected.
     *
     * @param sheets the stylesheets to remove
     */
    public void removeStylesheets(List<Stylesheet> sheets) {
        this.stylesheets.removeAll(sheets);
        if (this.theme != null) {
            updateStylesheets();
            logStylesheets();
        }
    }

    /**
     * Returns an unmodifiable view of the stylesheets managed by this instance.
     *
     * @return the managed stylesheets
     */
    public @Unmodifiable List<Stylesheet> getStylesheets() {
        return Collections.unmodifiableList(stylesheets);
    }

    private void addToTarget(Stylesheet sheet, Theme theme) {
        var target = targetSupplier.get();
        var urlStr = sheet.getUrl().toExternalForm();
        if (!managedUrls.contains(urlStr)) {
            target.add(urlStr);
            managedUrls.add(urlStr);
        }
        if (sheet.getExtensionThemes().contains(theme)) {
            var themedUrl = getThemedStylesheetUrl(urlStr, theme);
            if (!managedUrls.contains(themedUrl)) {
                target.add(themedUrl);
                managedUrls.add(themedUrl);
            }
        }
    }

    private void updateStylesheets() {
        var target = targetSupplier.get();
        target.removeAll(managedUrls);
        managedUrls.clear();
        for (var sheet : this.stylesheets) {
            addToTarget(sheet, this.theme);
        }
    }

    private void logStylesheets() {
        var prefix = this.logPrefix.get();
        if (logger.isDebugEnabled() && prefix != null) {
            var target = targetSupplier.get();
            if (target.isEmpty()) {
                logger.debug("{} Stylesheets updated. No stylesheets applied to target", prefix);
            } else {
                var sb = new StringBuilder();
                sb.append("{} Stylesheets updated. Current target stylesheets:");
                for (var s : target) {
                    sb.append(System.lineSeparator());
                    sb.append("    ");
                    sb.append(s);
                }
                logger.debug(sb.toString(), prefix);
            }
        }
    }
}
