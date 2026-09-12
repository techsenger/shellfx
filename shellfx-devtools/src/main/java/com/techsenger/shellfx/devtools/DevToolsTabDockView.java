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

package com.techsenger.shellfx.devtools;

import atlantafx.base.theme.Styles;
import com.techsenger.annotations.Nullable;
import com.techsenger.connectorfx.Connector;
import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.window.WindowContainerView;
import com.techsenger.shellfx.devtools.component.ComponentTabParams;
import com.techsenger.shellfx.devtools.component.ComponentTabView;
import com.techsenger.shellfx.devtools.component.ComponentTabViewModel;
import com.techsenger.shellfx.devtools.component.JfxComponentService;
import com.techsenger.shellfx.devtools.environment.EnvironmentTabParams;
import com.techsenger.shellfx.devtools.environment.EnvironmentTabView;
import com.techsenger.shellfx.devtools.environment.EnvironmentTabViewModel;
import com.techsenger.shellfx.devtools.event.EventTabParams;
import com.techsenger.shellfx.devtools.event.EventTabView;
import com.techsenger.shellfx.devtools.event.EventTabViewModel;
import com.techsenger.shellfx.devtools.node.NodeTabParams;
import com.techsenger.shellfx.devtools.node.NodeTabView;
import com.techsenger.shellfx.devtools.node.NodeTabViewModel;
import com.techsenger.shellfx.devtools.style.DevToolsIcons;
import com.techsenger.shellfx.devtools.stylesheet.StylesheetTabParams;
import com.techsenger.shellfx.devtools.stylesheet.StylesheetTabView;
import com.techsenger.shellfx.devtools.stylesheet.StylesheetTabViewModel;
import com.techsenger.shellfx.layout.dockhost.TabDockView;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.material.style.StyleClasses;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 *
 * @author Pavel Castornii
 */
public class DevToolsTabDockView<VM extends DevToolsTabDockViewModel<?>> extends TabDockView<VM> {

    public class Composer extends TabDockView<VM>.Composer {

        @Override
        public void compose() {
            super.compose();

            var nodeTab = createNodeTab();
            var componentTab = createComponentTab();

            addTab(componentTab);
            addTab(nodeTab);

            var eventTab = createEventTab();
            addTab(eventTab);

            var stylesheetTab = createStylesheetTab();
            addTab(stylesheetTab);

            var environmentTab = createEnvironmentTab();
            addTab(environmentTab);

            getViewModel().selectTab(getTabs().indexOf(componentTab));
        }

        protected ComponentTabView<?> createComponentTab() {
            var params = new ComponentTabParams(new JfxComponentService(shell), getViewModel());
            var viewModel = new ComponentTabViewModel<>(params);
            var tabView = new ComponentTabView<>(viewModel, shell, getWindowComposer());
            tabView.initialize();
            return tabView;
        }

        protected NodeTabView<?> createNodeTab() {
            var params = new NodeTabParams(getViewModel());
            var viewModel = new NodeTabViewModel<>(params);
            var tabView = new NodeTabView<>(viewModel, shell, getWindowComposer());
            tabView.initialize();
            return tabView;
        }

        protected EventTabView<?> createEventTab() {
            var params = new EventTabParams(connector, getViewModel().getSelector());
            var viewModel = new EventTabViewModel<>(params);
            var tabView = new EventTabView<>(viewModel, shell);
            tabView.initialize();
            return tabView;
        }

        protected StylesheetTabView<?> createStylesheetTab() {
            var params = new StylesheetTabParams(getViewModel());
            var viewModel = new StylesheetTabViewModel<>(params);
            var tabView = new StylesheetTabView<>(viewModel, shell);
            tabView.initialize();
            return tabView;
        }

        protected EnvironmentTabView<?> createEnvironmentTab() {
            var params = new EnvironmentTabParams(getViewModel());
            var viewModel = new EnvironmentTabViewModel<>(params);
            var tabView = new EnvironmentTabView<>(viewModel, shell, getWindowComposer());
            tabView.initialize();
            return tabView;
        }

        private @Nullable WindowContainerView.Composer getWindowComposer() {
            if (windowContainer != null) {
                return windowContainer.getComposer();
            } else {
                return null;
            }
        }
    }

    private final Button selectButton = new Button(null, new FontIconView(DevToolsIcons.SELECT));

    private final ToggleButton selectionButton = new ToggleButton(null, new FontIconView(DevToolsIcons.SELECTION));

    private final Button optionsButton = new Button(null, new FontIconView(DevToolsIcons.DOTS_VERTICAL));

    private final ShellView<?> shell;

    private final WindowContainerView<?> windowContainer;

    private final Connector connector;

    public DevToolsTabDockView(VM viewModel, ShellView<?> shell, WindowContainerView<?> windowContainer) {
        super(viewModel);
        this.shell = shell;
        this.windowContainer = windowContainer;
        this.connector = getViewModel().getConnector();
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected void build() {
        super.build();
        selectButton.getStyleClass().addAll(StyleClasses.SQUARE, Styles.FLAT, StyleClasses.SIZE_S);
        selectButton.setTooltip(new Tooltip("Select Node"));
        selectionButton.getStyleClass().addAll(StyleClasses.SQUARE, Styles.FLAT, StyleClasses.SIZE_S);
        selectionButton.setTooltip(new Tooltip("Enable/Disable Selection"));
        getTabHeaderFirstBox().getChildren().addAll(selectButton, selectionButton);
        getTabHeaderFirstBox().setSpacing(Spacing.getHorizontalThird());
        var styles = DevToolsTabDockView.class.getResource("devtools-tab-dock.css").toExternalForm();
        getNode().getStylesheets().add(styles);
        getNode().setTabDragEnabled(false);
        getNode().setTabDropEnabled(false);

        optionsButton.getStyleClass().addAll(StyleClasses.SQUARE, Styles.FLAT, StyleClasses.SIZE_S);
        optionsButton.setTooltip(new Tooltip("Options"));
        getTabHeaderLastBox().getChildren().add(0, optionsButton);
        getTabHeaderLastBox().setSpacing(Spacing.getHorizontalThird());
    }

    @Override
    protected void bind() {
        super.bind();
        selectionButton.selectedProperty().bindBidirectional(getViewModel().selectionSelectedProperty());
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        selectButton.setOnAction(e -> getViewModel().onSelect());
        optionsButton.setOnAction(e -> {
            var menu = createOptionsMenu();
            menu.show(optionsButton, Side.BOTTOM, 0, 0);
        });
    }

    protected ContextMenu createOptionsMenu() {
        var menu = new ContextMenu();
        for (var window : Window.getWindows()) {
            if (window instanceof Stage stage) {
                var uid = stage.hashCode();
                var item = new MenuItem(stage.getTitle() + " (uid: " + uid + ")");
                item.setOnAction(e -> getViewModel().onWindowSelected(uid));
                menu.getItems().add(item);
            }
        }
        return menu;
    }

    @Override
    protected Composer createComposer() {
        return new DevToolsTabDockView.Composer();
    }

    protected Button getSelectButton() {
        return selectButton;
    }

    protected ToggleButton getSelectionButton() {
        return selectionButton;
    }

    protected Button getOptionsButton() {
        return optionsButton;
    }
}
