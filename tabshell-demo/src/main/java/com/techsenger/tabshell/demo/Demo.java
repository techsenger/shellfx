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

import com.techsenger.tabshell.core.DefaultShellFxView;
import com.techsenger.tabshell.core.DefaultShellPresenter;
import com.techsenger.tabshell.core.area.AreaFxView;
import com.techsenger.tabshell.core.tab.TabContainerFxView;
import com.techsenger.tabshell.demo.history.DemoHistoryManager;
import com.techsenger.tabshell.demo.menu.DemoFileMenuRegistrar;
import com.techsenger.tabshell.demo.menu.DemoMenuRegistrar;
import com.techsenger.tabshell.demo.settings.DemoSettings;
import com.techsenger.tabshell.icons.IconStylesheetFactory;
import com.techsenger.tabshell.layout.dockhost.DockHostHistory;
import com.techsenger.tabshell.material.style.SizeConstants;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Pavel Castornii
 */
public class Demo extends Application {

    private enum WorkspaceType {

        TAB_HOST, TAB_DOCK
    }

    private final Label label = new Label("Select Workspace:");

    private final ObservableList<WorkspaceType> types = FXCollections.observableArrayList(WorkspaceType.values());

    private final ListView<WorkspaceType> typeListView = new ListView<>(types);

    private final VBox root = new VBox(label, typeListView);

    @Override
    public void start(Stage stage) throws Exception {
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
        root.setSpacing(SizeConstants.INSET);
        root.setPadding(new Insets(SizeConstants.INSET));
        var scene = new Scene(root, 300, 200);
        stage.setTitle("TabShell");
        stage.setScene(scene);
        stage.show();
    }

    private void createShell(WorkspaceType workspaceType) {
        //creating shell
        var stage = new Stage();
        var shellV = new DefaultShellFxView<>(this, stage, IconStylesheetFactory.forAll());
        var shellP = new DefaultShellPresenter<>(shellV, DemoSettings.createSettings(), new DemoHistoryManager());
        shellP.initialize();
        shellP.setOnClose(() -> Platform.exit());
        shellV.setTitle("TabShell Full Demo");

        // creating workspace
        TabContainerFxView<?> tabContainer;
        AreaFxView<?> workspace;
        switch (workspaceType) {
            case TAB_HOST -> {
                var tabHost = HostFactory.createTabHost();
                tabContainer = tabHost;
                workspace = tabHost;
            }
            case TAB_DOCK -> {
                var dockHost = HostFactory.createDockHost(shellV, () -> shellP.getHistoryManager()
                        .getOrCreateHistory(DockHostHistory.class, DockHostHistory::new));
                var rightTabDock = dockHost.getComposer().createTabDock();
                tabContainer = rightTabDock;
                dockHost.getRoot().getComposer().addChild(rightTabDock);
                workspace = dockHost;
            }
            default -> throw new AssertionError();
        }
        shellV.getComposer().addWorkspace(workspace);

        //adding menu
        var controlRegistry = shellV.getControlRegistry();
        var fmr = new DemoFileMenuRegistrar(controlRegistry);
        fmr.register();
//        var emr = new EditMenuRegistrar(controlRegistry);
//        emr.register();
        var dmr = new DemoMenuRegistrar(controlRegistry, tabContainer);
        dmr.register();
        shellV.upgradeMenuBar();
    }
}
