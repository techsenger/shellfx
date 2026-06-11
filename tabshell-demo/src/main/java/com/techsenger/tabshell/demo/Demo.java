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

package com.techsenger.tabshell.demo;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.DefaultShellContext;
import com.techsenger.tabshell.core.DefaultShellFxView;
import com.techsenger.tabshell.core.DefaultShellParams;
import com.techsenger.tabshell.core.DefaultShellPresenter;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.area.AreaFxView;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.demo.history.DemoHistoryManager;
import com.techsenger.tabshell.demo.menu.extra.ExtraMenuRegistrar;
import com.techsenger.tabshell.demo.menu.file.FileMenuRegistrar;
import com.techsenger.tabshell.demo.menu.window.WindowMenuRegistrar;
import com.techsenger.tabshell.demo.settings.DemoSettings;
import com.techsenger.tabshell.demo.styles.StylesTabFxView;
import com.techsenger.tabshell.demo.styles.StylesTabPresenter;
import com.techsenger.tabshell.icons.IconStylesheetFactory;
import com.techsenger.tabshell.layout.dockhost.DockHostHistory;
import com.techsenger.tabshell.layout.tabhost.TabHostFxView;
import com.techsenger.tabshell.material.style.Spacing;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class Demo extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Demo.class);

    private final Label label = new Label("Select Application:");

    private final ObservableList<ApplicationType> apps = FXCollections.observableArrayList(ApplicationType.values());

    private final ListView<ApplicationType> typeListView = new ListView<>(apps);

    private final VBox root = new VBox(label, typeListView);

    @Override
    public void start(Stage primaryStage) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
            logger.error("Uncaught exception in {}", thread.getName(), throwable)
        );
        typeListView.setCellFactory(lv -> {
            ListCell<ApplicationType> cell = new ListCell<>() {
                @Override
                protected void updateItem(ApplicationType item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.toString());
                }
            };

            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty() && event.getClickCount() == 2) {
                    var shell = createShell(cell.getItem());
                    openInitialTab(shell, cell.getItem());
                }
            });
            return cell;
        });
        typeListView.getStyleClass().add(Styles.DENSE);
        root.setSpacing(Spacing.getVertical());
        root.setPadding(new Insets(Spacing.getVertical(), Spacing.getHorizontal(),
                Spacing.getVertical(), Spacing.getHorizontal()));
        var scene = new Scene(root, 300, 200);
        primaryStage.setTitle("TabShell");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private ShellFxView<?> createShell(ApplicationType appType) {
        //creating shell
        var shellView = new DefaultShellFxView<>(this, IconStylesheetFactory.forAll(), new ControlRegistry());
        var context = new DefaultShellContext(DemoSettings.createSettings(),
                new DemoHistoryManager(), getHostServices());
        if (appType == ApplicationType.STYLES_ONLY) {
            // Important: To support different density styles, the window density must not be specified.
            context.getSettings().getAppearance().setDensity(null);
        }
        var shellParams = new DefaultShellParams(context);
        var shellPresenter = new DefaultShellPresenter<>(shellView, shellParams);
        shellPresenter.initialize();
        shellView.setTitle("TabShell Demo");

        // creating workspace
        AreaFxView<?> workspace;
        switch (appType) {
            case BROWSER -> {
                var tabHost = HostFactory.createTabHost();
                workspace = tabHost;
            }
            case IDE -> {
                var dockHost = HostFactory.createDockHost(shellView, () -> context.getHistoryManager()
                        .getOrCreateHistory(DockHostHistory.class, DockHostHistory::new));
                var rightTabDock = dockHost.getComposer().createTabDock();
                dockHost.getComposer().getRoot().getComposer().addChild(rightTabDock);
                dockHost.getComposer().setMain(rightTabDock);
                workspace = dockHost;
            }
            case MDI -> {
                workspace = null;
            }
            case STYLES_ONLY -> {
                var tabHost = HostFactory.createTabHost();
                workspace = tabHost;
            }
            default -> throw new AssertionError();
        }
        if (workspace != null) {
            shellView.getComposer().addWorkspace(workspace);
        }

        // adding menu
        var controlRegistry = shellView.getControlRegistry();

        if (appType != ApplicationType.STYLES_ONLY) {
            var fmr = new FileMenuRegistrar(controlRegistry, appType, shellView);
            fmr.register();
        }

        if (appType == ApplicationType.BROWSER || appType == ApplicationType.IDE) {
            var dmr = new ExtraMenuRegistrar(controlRegistry);
            dmr.register();
        }

        if (appType == ApplicationType.MDI) {
            var wmr = new WindowMenuRegistrar(controlRegistry, shellView);
            wmr.register();
        }

        shellView.upgradeMenuBar();
        shellView.getStage().show();
        return shellView;
    }

    private void openInitialTab(ShellFxView<?> shell, ApplicationType appType) {
        if (appType == ApplicationType.STYLES_ONLY) {
            var tabView = new StylesTabFxView(shell);
            var tabPresenter = new StylesTabPresenter(tabView);
            tabPresenter.initialize();
            TabHostFxView<?> workspace = (TabHostFxView<?>) shell.getComposer().getWorkspace();
            workspace.getComposer().addTab(tabView);
            tabView.requestFocus();
        }
    }
}
