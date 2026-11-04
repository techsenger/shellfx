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

package com.techsenger.shellfx.core.window;

import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.base.registry.ItemRegistryListener;
import com.techsenger.shellfx.material.style.Stylesheet;
import com.techsenger.shellfx.material.theme.Theme;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages a set of {@link Stylesheet} instances applied to a list of stylesheet URLs, such as the one of a scene or
 * of a node, with automatic theme-specific extension support.
 *
 * <p>The list may have other stylesheets applied to it externally (not managed by this class); this manager only
 * tracks and modifies the stylesheets it owns, leaving unmanaged stylesheets untouched.
 *
 * <p>The manager follows a {@link StylesheetRegistry}: from its creation until {@link #dispose()} the
 * stylesheets of the registry are applied and kept in step with the registry. The registry can change in any thread,
 * and the manager applies the changes in the JavaFX Application Thread.
 *
 * <p>The theme may not be known at construction time. Stylesheets added before the theme is set are queued internally
 * but not applied to the list until {@link #setTheme(Theme)} is called. Once the theme is set, all subsequent
 * {@link #addStylesheets(List)} calls apply immediately.
 *
 * @author Pavel Castornii
 */
final class StylesheetManager {

    private static final Logger logger = LoggerFactory.getLogger(StylesheetManager.class);

    private static String getThemedStylesheetUrl(String url, Theme theme) {
        int lastSlash = url.lastIndexOf('/');
        var fileName = url.substring(lastSlash + 1);
        fileName = fileName.substring(0, fileName.length() - 4);
        fileName = fileName + "-" + theme.name().toLowerCase().replace("_", "-") + ".css";
        var newUrl = url.substring(0, lastSlash + 1) + fileName;
        return newUrl;
    }

    private final ObservableList<String> target;

    private final List<Stylesheet> stylesheets = new ArrayList<>();

    /**
     * Set of stylesheet URLs currently applied to the target by this manager. Used for fast membership checks
     * when removing stylesheets, to avoid touching unmanaged stylesheets that may also be present on the target.
     */
    private final Set<String> managedUrls = new HashSet<>();

    private final String logPrefix;

    private final StylesheetRegistry registry;

    private final ItemRegistryListener<Stylesheet> registryListener = this::onRegistryChanged;

    private @Nullable Theme theme;

    private boolean disposed;

    /**
     * Creates a manager that applies the stylesheets of the registry and follows it until {@link #dispose()}.
     *
     * @param target the list of stylesheet URLs to manage
     * @param logPrefix the prefix of the log messages
     * @param registry the registry to follow
     */
    StylesheetManager(ObservableList<String> target, String logPrefix, StylesheetRegistry registry) {
        this.target = target;
        this.logPrefix = logPrefix;
        this.registry = registry;
        addStylesheets(registry.getStylesheets());
        registry.addListener(registryListener);
    }

    public String getLogPrefix() {
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

    /**
     * Stops following the registry. The owner must call it when it no longer needs the manager, because the registry
     * lives as long as the application. The stylesheets already applied stay on the target.
     */
    public void dispose() {
        this.disposed = true;
        registry.removeListener(registryListener);
    }

    private void onRegistryChanged(List<Stylesheet> added, List<Stylesheet> removed) {
        if (Platform.isFxApplicationThread()) {
            applyRegistryChanges(added, removed);
        } else {
            Platform.runLater(() -> applyRegistryChanges(added, removed));
        }
    }

    private void applyRegistryChanges(List<Stylesheet> added, List<Stylesheet> removed) {
        if (disposed) {
            return;
        }
        if (!removed.isEmpty()) {
            removeStylesheets(removed);
        }
        if (!added.isEmpty()) {
            addStylesheets(added);
        }
    }

    private void addToTarget(Stylesheet sheet, Theme theme) {
        var urlStr = sheet.getUrl();
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
        target.removeAll(managedUrls);
        managedUrls.clear();
        for (var sheet : this.stylesheets) {
            addToTarget(sheet, this.theme);
        }
    }

    private void logStylesheets() {
        var prefix = this.logPrefix;
        if (logger.isDebugEnabled()) {
            if (target.isEmpty()) {
                logger.debug("{} Window stylesheets updated. No stylesheets applied to target", prefix);
            } else {
                var sb = new StringBuilder();
                sb.append("{} Window stylesheets updated. Current target stylesheets:");
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
