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
import com.techsenger.tabshell.core.area.AreaFxView;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.demo.history.DemoHistoryManager;
import com.techsenger.tabshell.demo.menu.ExtraMenuRegistrar;
import com.techsenger.tabshell.demo.menu.FileMenuRegistrar;
import com.techsenger.tabshell.demo.settings.DemoSettings;
import com.techsenger.tabshell.icons.IconStylesheetFactory;
import com.techsenger.tabshell.layout.dockhost.DockHostHistory;
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

    private final Label label = new Label("Select Workspace:");

    private final ObservableList<WorkspaceType> types = FXCollections.observableArrayList(WorkspaceType.values());

    private final ListView<WorkspaceType> typeListView = new ListView<>(types);

    private final VBox root = new VBox(label, typeListView);

    @Override
    public void start(Stage stage) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
            logger.error("Uncaught exception in {}", thread.getName(), throwable)
        );
        typeListView.getStyleClass().add(Styles.DENSE);
        typeListView.setCellFactory(lv -> {
            ListCell<WorkspaceType> cell = new ListCell<>() {
                @Override
                protected void updateItem(WorkspaceType item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.toString());
                }
            };

            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty() && event.getClickCount() == 2) {
                    createShell(cell.getItem());
                }
            });
            return cell;
        });
        root.setSpacing(Spacing.VERTICAL);
        root.setPadding(new Insets(Spacing.VERTICAL, Spacing.HORIZONTAL, Spacing.VERTICAL, Spacing.HORIZONTAL));
        var scene = new Scene(root, 300, 200);
        stage.setTitle("TabShell");
        stage.setScene(scene);
        stage.show();
    }

    private void createShell(WorkspaceType workspaceType) {
        //creating shell
        var shellView = new DefaultShellFxView<>(this, IconStylesheetFactory.forAll(), new ControlRegistry());
        var context = new DefaultShellContext(DemoSettings.createSettings(),
                new DemoHistoryManager(), getHostServices());
        var shellParams = new DefaultShellParams(context);
        var shellPresenter = new DefaultShellPresenter<>(shellView, shellParams);
        shellPresenter.initialize();
        shellView.setTitle("TabShell Demo");

        // creating workspace
        AreaFxView<?> workspace;
        switch (workspaceType) {
            case BROWSER_LIKE -> {
                var tabHost = HostFactory.createTabHost();
                workspace = tabHost;
            }
            case IDE_LIKE -> {
                var dockHost = HostFactory.createDockHost(shellView, () -> context.getHistoryManager()
                        .getOrCreateHistory(DockHostHistory.class, DockHostHistory::new));
                var rightTabDock = dockHost.getComposer().createTabDock();
                dockHost.getComposer().getRoot().getComposer().addChild(rightTabDock);
                dockHost.getComposer().setMain(rightTabDock);
                workspace = dockHost;
            }
            default -> throw new AssertionError();
        }
        shellView.getComposer().addWorkspace(workspace);

        // adding menu
        var controlRegistry = shellView.getControlRegistry();
        var fmr = new FileMenuRegistrar(controlRegistry, shellView);
        fmr.register();
        var dmr = new ExtraMenuRegistrar(controlRegistry);
        dmr.register();
        shellView.upgradeMenuBar();

        // showing the window
        shellView.getWindow().show();
    }
}
