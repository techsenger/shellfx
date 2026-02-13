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
import com.techsenger.tabshell.core.shelltab.ShellTabFxView;
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
import com.techsenger.tabshell.layout.dock.TabDockFxView;
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

            var componentTab = createComponentTab();
            componentTab.getPresenter().initialize();
            addTab(componentTab);

            var nodeTab = createNodeTab();
            nodeTab.getPresenter().initialize();
            addTab(nodeTab);

            var eventTab = createEventTab(nodeTab.getPresenter().getPort());
            eventTab.getPresenter().initialize();
            addTab(eventTab);

            var stylesheetTab = createStylesheetTab();
            stylesheetTab.getPresenter().initialize();
            addTab(stylesheetTab);

            var environmentTab = createEnvironmentTab();
            environmentTab.getPresenter().initialize();
            addTab(environmentTab);

            selectTab(0);
        }

        protected ComponentTabFxView<?> createComponentTab() {
            var view = new ComponentTabFxView<>();
            var presenter = new ComponentTabPresenter<>(view, new JfxComponentService(shellTab.getShell()));
            return view;
        }

        protected NodeTabFxView<?> createNodeTab() {
            var view = new NodeTabFxView<>(shellTab);
            var presenter = new NodeTabPresenter<>(view, connector, getPresenter().getPort());
            return view;
        }

        protected EventTabFxView<?> createEventTab(NodeTabPort nodeTab) {
            var view = new EventTabFxView<>();
            var presenter = new EventTabPresenter<>(view, connector, nodeTab);
            return view;
        }

        protected StylesheetTabFxView<?> createStylesheetTab() {
            var view = new StylesheetTabFxView<>();
            var windowUid = shellTab.getShell().getStage().hashCode();
            var presenter = new StylesheetTabPresenter<>(view, connector, getPresenter().getPort());
            return view;
        }

        protected EnvironmentTabFxView<?> createEnvironmentTab() {
            var view = new EnvironmentTabFxView<>(shellTab.getComposer());
            var presenter = new EnvironmentTabPresenter<>(view, connector);
            return view;
        }
    }

    private final Button selectButton = new Button(null, new FontIconView(DevToolsIcons.SELECT));

    private final ToggleButton selectionButton = new ToggleButton(null, new FontIconView(DevToolsIcons.SELECTION));

    private final Button settingsButton = new Button(null, new FontIconView(SharedIcons.SETTINGS));

    private final ShellTabFxView<?> shellTab;

    private final Connector connector;

    public DevToolsTabDockFxView(ShellTabFxView<?> shellTab) {
        super();
        this.shellTab = shellTab;
        this.connector = new LocalConnector(shellTab.getShell().getStage(), null);
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
    public boolean isSelectionSelected() {
        return this.selectionButton.isSelected();
    }

    @Override
    public int getWindowUid() {
        return shellTab.getShell().getStage().hashCode();
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

        settingsButton.getStyleClass().addAll(StyleClasses.MINI_ICONED_BUTTON, Styles.FLAT);
        settingsButton.setTooltip(new Tooltip("Settings"));
        getTabHeaderLastBox().getChildren().add(0, settingsButton);
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        selectButton.setOnAction(e -> getPresenter().handleSelect());
        selectionButton.setOnAction(e -> getPresenter().handleSelection(selectionButton.isSelected()));
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
