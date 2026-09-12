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

package com.techsenger.shellfx.demo;

import atlantafx.base.theme.Styles;
import com.techsenger.shellfx.core.DefaultShellContext;
import com.techsenger.shellfx.core.DefaultShellParams;
import com.techsenger.shellfx.core.DefaultShellView;
import com.techsenger.shellfx.core.DefaultShellViewModel;
import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.area.AreaView;
import com.techsenger.shellfx.core.registry.ControlRegistry;
import static com.techsenger.shellfx.demo.ApplicationType.BROWSER;
import static com.techsenger.shellfx.demo.ApplicationType.IDE;
import static com.techsenger.shellfx.demo.ApplicationType.MDI;
import static com.techsenger.shellfx.demo.ApplicationType.STYLES_ONLY;
import com.techsenger.shellfx.demo.controls.ModuleControlRegistrar;
import com.techsenger.shellfx.demo.history.DemoHistoryManager;
import com.techsenger.shellfx.demo.settings.DemoSettings;
import com.techsenger.shellfx.demo.styles.StylesTabView;
import com.techsenger.shellfx.demo.styles.StylesTabViewModel;
import com.techsenger.shellfx.icons.Fonts;
import com.techsenger.shellfx.icons.IconStylesheetFactory;
import com.techsenger.shellfx.layout.dockhost.DockHostHistory;
import com.techsenger.shellfx.layout.dockhost.ModelNodeBuilder;
import com.techsenger.shellfx.layout.tabhost.TabHostView;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.IconStylesheets;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.material.style.StyleClasses;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
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
        primaryStage.setTitle("ShellFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private ShellView<?> createShell(ApplicationType appType) {
        // setting icons
        FontIconView.setDefaultIconFont(Fonts.MATERIAL_DESIGN_ICONS.getFamily());
        IconStylesheets.addAll(IconStylesheetFactory.forAll());

        // creating shell component
        var controlRegistry = new ControlRegistry();
        var context = new DefaultShellContext(DemoSettings.createSettings(),
                new DemoHistoryManager(), getHostServices());
        if (appType == ApplicationType.STYLES_ONLY) {
            // Important: To support different density styles, the window density must not be specified.
            context.getSettings().getAppearance().setDensity(null);
        }
        var shellParams = new DefaultShellParams(context);
        var shellViewModel = new DefaultShellViewModel<>(shellParams);
        var shellView = new DefaultShellView<>(shellViewModel, this, null, ShellControls.MAIN_MENU_GROUP,
                controlRegistry) {
            @Override
            protected void build() {
                super.build();
                if (appType == ApplicationType.STYLES_ONLY) {
                    getTitlePane().getStyleClass().add(StyleClasses.DENSITY_S);
                }
            }
        };
        shellView.initialize();
        shellViewModel.setTitle("ShellFX Demo");

        // creating workspace
        var workspace = createWorkspace(appType, shellView);
        if (workspace != null) {
            shellView.getComposer().addWorkspace(workspace);
        }

        // adding menu; register() itself is a no-op for STYLES_ONLY, since no branch there matches it
        var registrar = new ModuleControlRegistrar(appType, shellView);
        registrar.register();

        shellView.upgradeMenuBar();
        shellView.getStage().show();
        return shellView;
    }

    private AreaView<?> createWorkspace(ApplicationType appType, ShellView<?> shellView) {
        AreaView<?> workspace;
        var context = shellView.getViewModel().getContext();
        switch (appType) {
            case BROWSER -> {
                workspace = HostFactory.createProminentTabHost(context.getSettings().getAppearance());
            }
            case IDE -> {
                var dockHost = HostFactory.createDockHost(shellView, () -> context.getHistoryManager()
                        .getOrCreateHistory(DockHostHistory.class, DockHostHistory::new));
                workspace = dockHost;
                var leftTabDock = HostFactory.createLeftTabDock(shellView, dockHost);
                var rightTabDock = dockHost.getComposer().createTabDock();

                var modelRoot = ModelNodeBuilder.root(Orientation.HORIZONTAL, s -> s
                        .area(leftTabDock)
                        .mainArea(rightTabDock));
                dockHost.getComposer().applyModel(modelRoot);

            }
            case MDI -> {
                workspace = null;
            }
            case STYLES_ONLY -> {
                workspace = HostFactory.createProminentTabHost(context.getSettings().getAppearance());
            }
            default -> throw new AssertionError();
        }
        return workspace;
    }

    private void openInitialTab(ShellView<?> shell, ApplicationType appType) {
        if (appType == ApplicationType.STYLES_ONLY) {
            var tabViewModel = new StylesTabViewModel<>();
            var tabView = new StylesTabView<>(tabViewModel, shell);
            tabView.initialize();
            TabHostView<?> workspace = (TabHostView<?>) shell.getComposer().getWorkspace();
            workspace.getComposer().addTab(tabView);
            tabView.requestFocus();
        }
    }
}
