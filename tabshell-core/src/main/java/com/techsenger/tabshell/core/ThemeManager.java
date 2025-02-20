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
import com.techsenger.tabshell.core.theme.TabShellTheme;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;

/**
 *
 * @author Pavel Castornii
 */
class ThemeManager {

    private final Scene scene;

    private final List<String> stylesheetUrls;

    private String baseCss;

    private String baseThemeCss;

    /**
     * Constructor.
     *
     * @param root the root of the scene. We can't get the root from stage.getScene().getRoot() because of custom stage.
     * @param theme
     */
    ThemeManager(StandardStageController controller, List<String> stylesheetUrls, AppearanceSettings settings) {
        var theme = settings.themeProperty();
        this.stylesheetUrls = stylesheetUrls;
        this.scene = controller.getStage().getScene();
        addTheme(theme.get(), false);
        theme.addListener((ov, oldV, newV) -> {
            removeTheme(newV);
            addTheme(newV, true);
            //without applying css and layout title bar spacers are not updated
            controller.getTitleBar().applyCss();
            controller.getTitleBar().layout();
        });
    }

    private void addTheme(TabShellTheme theme, boolean setStageColors) {
        if (theme == null) {
            return;
        }
        Application.setUserAgentStylesheet(theme.getStylesheetSupplier().get());
        //2. tabshell base css file
        this.baseCss = ThemeManager.class.getResource("base.css").toExternalForm();
        this.scene.getStylesheets().add(this.baseCss);
        if (theme.getFileName() != null) {
            //3. base theme addons
            this.baseThemeCss = ThemeManager.class.getResource(theme.resolveFileName("base")).toExternalForm();
            this.scene.getStylesheets().add(this.baseThemeCss);
        }
        //4. custom stylesheets
        if (this.stylesheetUrls != null) {
            this.scene.getStylesheets().addAll(this.stylesheetUrls);
        }
    }

    private void removeTheme(TabShellTheme theme) {
        if (theme == null) {
            return;
        }
        if (this.baseCss != null) {
            this.scene.getStylesheets().remove(this.baseCss);
            this.baseCss = null;
        }
        if (this.baseThemeCss != null) {
            this.scene.getStylesheets().remove(this.baseThemeCss);
            this.baseThemeCss = null;
        }
        if (this.stylesheetUrls != null) {
            this.scene.getStylesheets().removeAll(this.stylesheetUrls);
        }
    }
}
