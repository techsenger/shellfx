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

package com.techsenger.tabshell.demos.core;

import com.techsenger.tabshell.core.DefaultShellView;
import com.techsenger.tabshell.core.DefaultShellViewModel;
import com.techsenger.tabshell.core.menu.SimpleMenuItemHelper;
import com.techsenger.tabshell.demos.core.history.DemoHistoryManager;
import com.techsenger.tabshell.demos.core.menu.DemoMenuRegistrar;
import com.techsenger.tabshell.demos.core.settings.DemoSettings;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import com.techsenger.tabshell.demos.core.menu.DemoMenuNames;

/**
 *
 * @author Pavel Castornii
 */
public class Demo extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        //creating shell
        var viewModel = new DefaultShellViewModel(DemoSettings.createSettings(), new DemoHistoryManager());
        viewModel.setTitle("TabShell Core Demo");
        viewModel.setOnClosed(() -> {
            Platform.exit();
        });
        //this helper will be used when there are no tabs
        viewModel.addMenuItemHelpers(new SimpleMenuItemHelper(DemoMenuNames.NEW, null, Boolean.TRUE));
        var view = new DefaultShellView(stage, null, viewModel);
        view.initialize();

        //adding menu
        var controlRegistry = view.getControlRegistry();
        var registrar = new DemoMenuRegistrar(controlRegistry);
        registrar.register();
        view.upgradeMenuBar();
    }
}
