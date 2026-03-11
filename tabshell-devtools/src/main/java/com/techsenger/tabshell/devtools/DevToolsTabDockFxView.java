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

package com.techsenger.tabshell.devtools;

import atlantafx.base.theme.Styles;
import com.techsenger.connectorfx.Connector;
import com.techsenger.connectorfx.LocalConnector;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.dialog.DialogContainerFxView;
import com.techsenger.tabshell.devtools.component.ComponentTabFxView;
import com.techsenger.tabshell.devtools.component.ComponentTabPresenter;
import com.techsenger.tabshell.devtools.component.JfxComponentService;
import com.techsenger.tabshell.devtools.environment.EnvironmentTabFxView;
import com.techsenger.tabshell.devtools.environment.EnvironmentTabPresenter;
import com.techsenger.tabshell.devtools.event.EventTabFxView;
import com.techsenger.tabshell.devtools.event.EventTabPresenter;
import com.techsenger.tabshell.devtools.node.NodeTabFxView;
import com.techsenger.tabshell.devtools.node.NodeTabPort;
import com.techsenger.tabshell.devtools.node.NodeTabPresenter;
import com.techsenger.tabshell.devtools.style.DevToolsIcons;
import com.techsenger.tabshell.devtools.stylesheet.StylesheetTabFxView;
import com.techsenger.tabshell.devtools.stylesheet.StylesheetTabPresenter;
import com.techsenger.tabshell.layout.dockhost.TabDockFxView;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.style.SharedIcons;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;

/**
 *
 * @author Pavel Castornii
 */
public class DevToolsTabDockFxView<P extends DevToolsTabDockPresenter<?, ?>> extends TabDockFxView<P>
        implements DevToolsTabDockView {

    public class Composer extends TabDockFxView<P>.Composer {

        @Override
        public void compose() {
            super.compose();

            var nodeTab = createNodeTab();
            nodeTab.getPresenter().initialize();
            var componentTab = createComponentTab(nodeTab.getPresenter());
            componentTab.getPresenter().initialize();

            addTab(componentTab);
            addTab(nodeTab);

            var eventTab = createEventTab(nodeTab.getPresenter());
            eventTab.getPresenter().initialize();
            addTab(eventTab);

            var stylesheetTab = createStylesheetTab(nodeTab.getPresenter());
            stylesheetTab.getPresenter().initialize();
            addTab(stylesheetTab);

            var environmentTab = createEnvironmentTab();
            environmentTab.getPresenter().initialize();
            addTab(environmentTab);

            selectTab(0);
        }

        protected ComponentTabFxView<?> createComponentTab(NodeTabPort nodeTab) {
            var view = new ComponentTabFxView<>(shell, dialogContainer.getComposer());
            var presenter = new ComponentTabPresenter<>(view, new JfxComponentService(shell), getPresenter());
            return view;
        }

        protected NodeTabFxView<?> createNodeTab() {
            var view = new NodeTabFxView<>(shell, dialogContainer.getComposer());
            var presenter = new NodeTabPresenter<>(view, connector, getPresenter());
            return view;
        }

        protected EventTabFxView<?> createEventTab(NodeTabPort nodeTab) {
            var view = new EventTabFxView<>(shell);
            var presenter = new EventTabPresenter<>(view, connector, nodeTab);
            return view;
        }

        protected StylesheetTabFxView<?> createStylesheetTab(NodeTabPort nodeTab) {
            var view = new StylesheetTabFxView<>(shell);
            var windowUid = shell.getStage().hashCode();
            var presenter = new StylesheetTabPresenter<>(view, connector, getPresenter(), nodeTab);
            return view;
        }

        protected EnvironmentTabFxView<?> createEnvironmentTab() {
            var view = new EnvironmentTabFxView<>(shell, dialogContainer.getComposer());
            var presenter = new EnvironmentTabPresenter<>(view, connector);
            return view;
        }
    }

    private final Button selectButton = new Button(null, new FontIconView(DevToolsIcons.SELECT));

    private final ToggleButton selectionButton = new ToggleButton(null, new FontIconView(DevToolsIcons.SELECTION));

    private final Button settingsButton = new Button(null, new FontIconView(SharedIcons.SETTINGS));

    private final ShellFxView<?> shell;

    private final DialogContainerFxView dialogContainer;

    private final Connector connector;

    public DevToolsTabDockFxView(ShellFxView<?> shell, DialogContainerFxView<?> dialogContainer) {
        super();
        this.shell = shell;
        this.dialogContainer = dialogContainer;
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
    public int getWindowUid() {
        return shell.getStage().hashCode();
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected void build() {
        super.build();
        selectButton.getStyleClass().addAll(Styles.FLAT, "dock-button");
        selectButton.setTooltip(new Tooltip("Select Node"));
        selectionButton.getStyleClass().addAll(Styles.FLAT, "dock-button");
        selectionButton.setTooltip(new Tooltip("Enable/Disable Selection"));
        getTabHeaderFirstBox().getChildren().addAll(selectButton, selectionButton);
        var styles = DevToolsTabDockFxView.class.getResource("devtools-tab-dock.css").toExternalForm();
        getNode().getStylesheets().add(styles);
        getNode().setTabDragEnabled(false);
        getNode().setTabDropEnabled(false);

        settingsButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT, StyleClasses.COMPACT);
        settingsButton.setTooltip(new Tooltip("Settings"));
        getTabHeaderLastBox().getChildren().add(0, settingsButton);
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        selectButton.setOnAction(e -> getPresenter().onSelect());
        selectionButton.setOnAction(e -> getPresenter().onSelection(selectionButton.isSelected()));
    }

    @Override
    protected Composer createComposer() {
        return new DevToolsTabDockFxView.Composer();
    }

    protected Button getSelectButton() {
        return selectButton;
    }

    protected Button getSettingsButton() {
        return settingsButton;
    }
}
