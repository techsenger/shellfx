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

package com.techsenger.tabshell.demos.full;

import com.techsenger.tabshell.core.DefaultShellView;
import com.techsenger.tabshell.core.DefaultShellViewModel;
import com.techsenger.tabshell.demos.core.history.DemoHistoryManager;
import com.techsenger.tabshell.demos.core.settings.DemoSettings;
import com.techsenger.tabshell.demos.full.menu.DemoFileMenuRegistrar;
import com.techsenger.tabshell.demos.full.menu.DemoMenuRegistrar;
import com.techsenger.tabshell.icons.IconStylesheetProvider;
import com.techsenger.tabshell.registrars.EditMenuRegistrar;
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
        var viewModel = new DefaultShellViewModel(DemoSettings.createSettings(), new DemoHistoryManager());
        viewModel.setTitle("TabShell Full Demo");
        viewModel.setOnClosed(() -> {
            Platform.exit();
        });
        var view = new DefaultShellView(this, stage, IconStylesheetProvider.createForAll(), viewModel);
        view.initialize();

        //adding menu
        var controlRegistry = view.getControlRegistry();
        var fmr = new DemoFileMenuRegistrar(controlRegistry);
        fmr.register();
        var emr = new EditMenuRegistrar(controlRegistry);
        emr.register();
        var dmr = new DemoMenuRegistrar(controlRegistry);
        dmr.register();
        view.upgradeMenuBar();
    }
}
