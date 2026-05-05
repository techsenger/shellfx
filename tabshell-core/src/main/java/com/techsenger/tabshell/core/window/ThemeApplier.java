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

package com.techsenger.tabshell.core.window;

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.material.style.Stylesheet;
import com.techsenger.tabshell.material.theme.Theme;
import java.util.List;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
class ThemeApplier {

    private static String getThemedStylesheetUrl(String url, Theme theme) {
        int lastSlash = url.lastIndexOf('/');
        var fileName = url.substring(lastSlash + 1);
        fileName = fileName.substring(0, fileName.length() - 4);
        fileName = fileName + "-" + theme.name().toLowerCase().replace("_", "-") + ".css";
        var newUrl = url.substring(0, lastSlash + 1) + fileName;
        return newUrl;
    }

    private static final Logger logger = LoggerFactory.getLogger(ThemeApplier.class);

    private final Scene scene;

    private final ObservableList<Stylesheet> stylesheets;

    private final Descriptor descriptor;

    /**
     * Constructor.
     *
     * @param root the root of the scene. We can't get the root from stage.getScene().getRoot() because of custom stage.
     * @param theme
     */
    ThemeApplier(Scene scene, ObservableList<Stylesheet> stylesheets, ObjectProperty<Theme> theme,
            Descriptor descriptor) {
        this.stylesheets = stylesheets;
        this.scene = scene;
        this.descriptor = descriptor;
        addTheme(theme.get(), false);
        logSceneStylesheets();
        theme.addListener((ov, oldV, newV) -> {
            removeTheme(oldV);
            addTheme(newV, true);
            logSceneStylesheets();
        });
        this.stylesheets.addListener((ListChangeListener<Stylesheet>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    addStylesheets(theme.get(), change.getAddedSubList());
                }
                if (change.wasRemoved()) {
                    removeStylesheets(theme.get(), change.getRemoved());
                }
            }
            logSceneStylesheets();
        });
    }

    private void addTheme(Theme theme, boolean setStageColors) {
        if (theme == null) {
            return;
        }
        Application.setUserAgentStylesheet(theme.getStylesheetSupplier().get());
        addStylesheets(theme, stylesheets);
    }

    private void removeTheme(Theme theme) {
        if (theme == null) {
            return;
        }
        removeStylesheets(theme, stylesheets);
    }

    private void addStylesheets(Theme theme, List<? extends Stylesheet> sheets) {
        for (var s : sheets) {
            var url = s.getUrl().toExternalForm();
            this.scene.getStylesheets().add(url);
            if (s.getExtendedThemes().contains(theme)) {
                var themedUrl = getThemedStylesheetUrl(url, theme);
                this.scene.getStylesheets().add(themedUrl);
            }
        }
    }

    private void removeStylesheets(Theme theme, List<? extends Stylesheet> sheets) {
        for (var s : sheets) {
            var url = s.getUrl().toExternalForm();
            this.scene.getStylesheets().remove(url);
            if (s.getExtendedThemes().contains(theme)) {
                var themedUrl = getThemedStylesheetUrl(url, theme);
                this.scene.getStylesheets().remove(themedUrl);
            }
        }
    }

    private void logSceneStylesheets() {
        if (logger.isDebugEnabled()) {
            var sb = new StringBuilder();
            sb.append("{} Scene stylesheets updated. Current stylesheets:");
            for (var s : this.scene.getStylesheets()) {
                sb.append(System.lineSeparator());
                sb.append("    ");
                sb.append(s);
            }
            logger.debug(sb.toString(), descriptor.getLogPrefix());
        }
    }
}
