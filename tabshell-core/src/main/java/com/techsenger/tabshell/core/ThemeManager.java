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

package com.techsenger.tabshell.core;

import com.techsenger.stagepro.core.StandardStageController;
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.core.style.Stylesheet;
import com.techsenger.tabshell.core.theme.ShellTheme;
import java.util.List;
import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
class ThemeManager {

    private static final Logger logger = LoggerFactory.getLogger(ThemeManager.class);

    private final Scene scene;

    private final ObservableList<Stylesheet> stylesheets;

    /**
     * Constructor.
     *
     * @param root the root of the scene. We can't get the root from stage.getScene().getRoot() because of custom stage.
     * @param theme
     */
    ThemeManager(StandardStageController controller, ObservableList<Stylesheet> stylesheets,
            AppearanceSettings settings) {
        var theme = settings.themeProperty();
        this.stylesheets = stylesheets;
        this.scene = controller.getStage().getScene();
        addTheme(theme.get(), false);
        logSceneStylesheets();
        theme.addListener((ov, oldV, newV) -> {
            removeTheme(oldV);
            addTheme(newV, true);
            //without applying css and layout title bar spacers are not updated
            controller.getTitleBar().applyCss();
            controller.getTitleBar().layout();
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

    private void addTheme(ShellTheme theme, boolean setStageColors) {
        if (theme == null) {
            return;
        }
        Application.setUserAgentStylesheet(theme.getStylesheetSupplier().get());
        addStylesheets(theme, stylesheets);
    }

    private void removeTheme(ShellTheme theme) {
        if (theme == null) {
            return;
        }
        removeStylesheets(theme, stylesheets);
    }

    private void addStylesheets(ShellTheme theme, List<? extends Stylesheet> sheets) {
        for (var s : sheets) {
            if (s.getTheme() == null || s.getTheme().equals(theme)) {
                var url = s.getUrl().toExternalForm();
                this.scene.getStylesheets().add(url);
            }
        }
    }

    private void removeStylesheets(ShellTheme theme, List<? extends Stylesheet> sheets) {
        for (var s : sheets) {
            if (s.getTheme() == null || s.getTheme().equals(theme)) {
                var url = s.getUrl().toExternalForm();
                this.scene.getStylesheets().remove(url);
            }
        }
    }

    private void logSceneStylesheets() {
        if (logger.isDebugEnabled()) {
            var sb = new StringBuilder();
            sb.append("Scene stylesheets updated. Current stylesheets:");
            for (var s : this.scene.getStylesheets()) {
                sb.append(System.lineSeparator());
                sb.append("    ");
                sb.append(s);
            }
            logger.debug(sb.toString());
        }
    }
}
