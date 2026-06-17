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
import com.techsenger.connectorfx.LocalConnector;
import com.techsenger.shellfx.core.ShellFxView;
import com.techsenger.shellfx.core.window.WindowContainerFxView;
import com.techsenger.shellfx.devtools.component.ComponentTabFxView;
import com.techsenger.shellfx.devtools.component.ComponentTabParams;
import com.techsenger.shellfx.devtools.component.ComponentTabPresenter;
import com.techsenger.shellfx.devtools.component.JfxComponentService;
import com.techsenger.shellfx.devtools.environment.EnvironmentTabFxView;
import com.techsenger.shellfx.devtools.environment.EnvironmentTabParams;
import com.techsenger.shellfx.devtools.environment.EnvironmentTabPresenter;
import com.techsenger.shellfx.devtools.event.EventTabFxView;
import com.techsenger.shellfx.devtools.event.EventTabParams;
import com.techsenger.shellfx.devtools.event.EventTabPresenter;
import com.techsenger.shellfx.devtools.node.NodeTabFxView;
import com.techsenger.shellfx.devtools.node.NodeTabParams;
import com.techsenger.shellfx.devtools.node.NodeTabPresenter;
import com.techsenger.shellfx.devtools.style.DevToolsIcons;
import com.techsenger.shellfx.devtools.stylesheet.StylesheetTabFxView;
import com.techsenger.shellfx.devtools.stylesheet.StylesheetTabParams;
import com.techsenger.shellfx.devtools.stylesheet.StylesheetTabPresenter;
import com.techsenger.shellfx.layout.dockhost.TabDockFxView;
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
public class DevToolsTabDockFxView<P extends DevToolsTabDockPresenter<?>> extends TabDockFxView<P>
        implements DevToolsTabDockView {

    public class Composer extends TabDockFxView<P>.Composer {

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

            selectTab(0);
        }

        protected ComponentTabFxView<?> createComponentTab() {
            var view = new ComponentTabFxView<>(shell, getWindowComposer());
            var params = new ComponentTabParams(new JfxComponentService(shell), getPresenter());
            var presenter = new ComponentTabPresenter<>(view, params);
            presenter.initialize();
            return view;
        }

        protected NodeTabFxView<?> createNodeTab() {
            var view = new NodeTabFxView<>(shell, getWindowComposer());
            var params = new NodeTabParams(getPresenter());
            var presenter = new NodeTabPresenter<>(view, params);
            presenter.initialize();
            return view;
        }

        protected EventTabFxView<?> createEventTab() {
            var view = new EventTabFxView<>(shell);
            var params = new EventTabParams(connector, getPresenter().getSelector());
            var presenter = new EventTabPresenter<>(view, params);
            presenter.initialize();
            return view;
        }

        protected StylesheetTabFxView<?> createStylesheetTab() {
            var view = new StylesheetTabFxView<>(shell);
            var params = new StylesheetTabParams(getPresenter());
            var presenter = new StylesheetTabPresenter<>(view, params);
            presenter.initialize();
            return view;
        }

        protected EnvironmentTabFxView<?> createEnvironmentTab() {
            var view = new EnvironmentTabFxView<>(shell, getWindowComposer());
            var params = new EnvironmentTabParams(getPresenter());
            var presenter = new EnvironmentTabPresenter<>(view, params);
            presenter.initialize();
            return view;
        }

        private @Nullable WindowContainerFxView.Composer getWindowComposer() {
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

    private final ShellFxView<?> shell;

    private final WindowContainerFxView<?> windowContainer;

    private final Connector connector;

    public DevToolsTabDockFxView(ShellFxView<?> shell, WindowContainerFxView<?> windowContainer) {
        super();
        this.shell = shell;
        this.windowContainer = windowContainer;
        this.connector = new LocalConnector(shell.getStage(), null);
    }

    @Override
    public Connector getConnector() {
        return this.connector;
    }

    @Override
    public void setSelectionSelected(boolean value) {
        this.selectionButton.setSelected(value);
    }

    @Override
    public int getShellWindowUid() {
        return shell.getStage().hashCode();
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected void build() {
        super.build();
        selectButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT, StyleClasses.SIZE_S);
        selectButton.setTooltip(new Tooltip("Select Node"));
        selectionButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT, StyleClasses.SIZE_S);
        selectionButton.setTooltip(new Tooltip("Enable/Disable Selection"));
        getTabHeaderFirstBox().getChildren().addAll(selectButton, selectionButton);
        getTabHeaderFirstBox().setSpacing(Spacing.getHorizontalThird());
        var styles = DevToolsTabDockFxView.class.getResource("devtools-tab-dock.css").toExternalForm();
        getNode().getStylesheets().add(styles);
        getNode().setTabDragEnabled(false);
        getNode().setTabDropEnabled(false);

        optionsButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT, StyleClasses.SIZE_S);
        optionsButton.setTooltip(new Tooltip("Options"));
        getTabHeaderLastBox().getChildren().add(0, optionsButton);
        getTabHeaderLastBox().setSpacing(Spacing.getHorizontalThird());
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        selectButton.setOnAction(e -> getPresenter().onSelect());
        selectionButton.setOnAction(e -> getPresenter().onSelection(selectionButton.isSelected()));
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
                item.setOnAction(e -> getPresenter().onWindowSelected(uid));
                menu.getItems().add(item);
            }
        }
        return menu;
    }

    @Override
    protected Composer createComposer() {
        return new DevToolsTabDockFxView.Composer();
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
