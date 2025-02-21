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

package com.techsenger.tabshell.kit.demo;

import com.techsenger.tabshell.core.DefaultTabShellView;
import com.techsenger.tabshell.core.DefaultTabShellViewModel;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.core.theme.TabShellTheme;
import com.techsenger.tabshell.demo.history.DemoHistoryManager;
import com.techsenger.tabshell.kit.core.settings.AppearanceSettings;
import com.techsenger.tabshell.kit.core.settings.FontSettings;
import com.techsenger.tabshell.kit.core.settings.Settings;
import com.techsenger.tabshell.kit.core.settings.TabSymbolSettings;
import com.techsenger.tabshell.kit.core.settings.ViewerSettings;
import com.techsenger.tabshell.kit.core.style.CoreIcons;
import com.techsenger.tabshell.kit.core.style.StyleClasses;
import com.techsenger.tabshell.kit.dialog.style.DialogIcons;
import com.techsenger.tabshell.kit.terminal.style.TerminalIcons;
import com.techsenger.tabshell.kit.text.menu.EditMenuRegistrar;
import com.techsenger.tabshell.kit.text.style.TextIcons;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 *
 * @author Pavel Castornii
 */
public class Demo extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        //creating shell
        var viewModel = new DefaultTabShellViewModel(createSettings(), new DemoHistoryManager());
        viewModel.setTitle("TabShell Kit Demo");
        viewModel.setOnClosed(() -> {
            Platform.exit();
        });
        var stylesheets = List.of(
                StyleClasses.class.getResource("base.css").toExternalForm(),
                CoreIcons.class.getResource("icons.css").toExternalForm(),
                TextIcons.class.getResource("icons.css").toExternalForm(),
                DialogIcons.class.getResource("icons.css").toExternalForm(),
                TerminalIcons.class.getResource("icons.css").toExternalForm()
        );
        var view = new DefaultTabShellView(stage, stylesheets, viewModel);
        view.initialize();

        //adding menu
        var controlRegistry = new ControlRegistry();
        var fmr = new DemoFileMenuRegistrar(controlRegistry);
        fmr.register();
        var emr = new EditMenuRegistrar(controlRegistry);
        emr.register();
        view.upgradeMenuBar(controlRegistry);
    }

    private Settings createSettings() {
        var font = new FontSettings();
        font.setSize(14);
        var tabSymbol = new TabSymbolSettings();
        tabSymbol.setSize(4);
        tabSymbol.setUseSpaces(true);
        var appearance = new AppearanceSettings(font);
        appearance.setTheme(TabShellTheme.CUPERTINO_DARK);
        var viewerer = new ViewerSettings(font, tabSymbol);
        var settings = new Settings(appearance, viewerer);
        return settings;
    }
}
