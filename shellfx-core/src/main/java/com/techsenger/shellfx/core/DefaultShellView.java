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

package com.techsenger.shellfx.core;

import com.techsenger.patternfx.mvvm.ChildView;
import com.techsenger.patternfx.mvvm.ParentView;
import com.techsenger.shellfx.core.area.AreaPort;
import com.techsenger.shellfx.core.area.AreaView;
import com.techsenger.shellfx.core.registry.ControlBuilder;
import com.techsenger.shellfx.core.registry.ControlRegistry;
import com.techsenger.shellfx.core.window.AbstractHostWindowView;
import com.techsenger.shellfx.material.menu.MenuBarManager;
import com.techsenger.shellfx.material.menu.MenuGroupName;
import com.techsenger.shellfx.material.style.Stylesheet;
import java.util.List;
import java.util.Objects;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.MenuBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * There can be only one instance of Shell in VirtualMachine.
 *
 * @author Pavel Castornii
 */
public class DefaultShellView<VM extends DefaultShellViewModel<?>>
        extends AbstractHostWindowView<VM> implements ShellView<VM> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultShellView.class);

    public class Composer extends AbstractHostWindowView<VM>.Composer implements ShellView.Composer {

        private final ReadOnlyObjectWrapper<ParentView<?>> menuAware = new ReadOnlyObjectWrapper<>();

        private final DefaultShellView<VM> view = DefaultShellView.this;

        private AreaView<?> workspace;

        public Composer() {
            this.menuAware.addListener((ov, oldV, newV) -> {
                logger.debug("{} Menu aware component: {}", getDescriptor().getLogPrefix(),
                        (newV == null) ? null : newV.getViewModel().getDescriptor().getFullName());
                updateMenuBar();
            });
        }

        @Override
        public void addWorkspace(AreaView<?> workspace) {
            this.workspace = workspace;
            getModifiableChildren().add(workspace);
            VBox.setVgrow(workspace.getNode(), Priority.ALWAYS);
            view.getContentBox().getChildren().add(workspace.getNode());
        }

        @Override
        public void removeWorkspace() {
            if (this.workspace == null) {
                return;
            }
            getModifiableChildren().remove(this.workspace);
            view.getContentBox().getChildren().remove(this.workspace.getNode());
        }

        @Override
        public AreaView<?> getWorkspace() {
            return this.workspace;
        }

        @Override
        public AreaPort getWorkspacePort() {
            return this.workspace == null ? null : this.workspace.getViewModel();
        }

        @Override
        public ReadOnlyObjectProperty<ParentView<?>> menuAwareProperty() {
            return this.menuAware.getReadOnlyProperty();
        }

        @Override
        public ParentView<?> getMenuAware() {
            return this.menuAware.get();
        }

        @Override
        protected void onFocusPauseFinished() {
            super.onFocusPauseFinished();
            var newNode = view.getStage().getScene().getFocusOwner();
            if (newNode == null) {
                setMenuAware(view);
                return;
            }
            resolvedMenuAware();
        }

        private void setMenuAware(ParentView<?> menuAware) {
            this.menuAware.set(menuAware);
        }

        private void resolvedMenuAware() {
            ParentView<?> focused = getFocused();
            if (focused == null) {
                setMenuAware(view);
                return;
            }
            if (focused == this) { // when user clicks on shell main menu, the previous  menu aware is used
                return;
            }
            ParentView<?> currentComponent = focused;
            while (true) {
                var port = currentComponent.getViewModel();
                if (port instanceof MenuAwarePort menuAware) {
                    setMenuAware(currentComponent);
                    return;
                }
                if (currentComponent instanceof ChildView<?> child) {
                    currentComponent = child.getComposer().getParent();
                    if (currentComponent == null) {
                        logger.warn("{} Child {} has no parent", getDescriptor().getLogPrefix(),
                                child.getViewModel().getDescriptor().getFullName());
                        setMenuAware(view);
                        return;
                    }
                } else {
                    setMenuAware(view);
                    return;
                }
            }
        }
    }

    private final Application application;

    private final MenuBar menuBar = new MenuBar();

    private final MenuBarManager menuBarManager;

    private final MenuGroupName<?> menuBarGroup;

    private final ControlRegistry controlRegistry;

    public DefaultShellView(VM viewModel, Application application, List<Stylesheet> stylesheets,
            MenuGroupName<?> menuBarGroup, ControlRegistry controlRegistry) {
        this(viewModel, application, new Stage(), stylesheets, menuBarGroup, controlRegistry);
    }

    public DefaultShellView(VM viewModel, Application application, Stage stage, List<Stylesheet> stylesheets,
            MenuGroupName<?> menuBarGroup, ControlRegistry controlRegistry) {
        super(viewModel, stage, stylesheets);
        Objects.requireNonNull(application, "Application can't be null");
        this.application = application;
        this.menuBarManager = new MenuBarManager(this.menuBar);
        this.menuBarGroup = menuBarGroup;
        this.controlRegistry = controlRegistry;
        getComposer().setMenuAware(this);
    }

    @Override
    public void requestFocus() {
        var workspace = getComposer().getWorkspace();
        if (workspace != null) {
            workspace.requestFocus();
        }
    }

    @Override
    public ControlRegistry getControlRegistry() {
        return controlRegistry;
    }

    @Override
    public void upgradeMenuBar() {
        this.menuBar.getMenus().clear();
        var builder = new ControlBuilder(controlRegistry);
        var menus = builder.buildMenus(menuBarGroup, this);
        this.menuBar.getMenus().addAll(menus);
        logger.debug("{} Menu bar upgraded", getDescriptor().getLogPrefix());
        updateMenuBar();
    }

    @Override
    public void updateMenuBar() {
        this.menuBarManager.updateMenuBar();
        logger.debug("{} Menu bar updated", getDescriptor().getLogPrefix());
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new DefaultShellView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        getLeftBox().getChildren().remove(getTitleLabel());
        getLeftBox().getChildren().add(menuBar);
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        getStage().getScene().addEventFilter(MouseEvent.MOUSE_CLICKED,
                e -> menuBarManager.setLastMouseClickTime(System.nanoTime()));
    }

    @Override
    protected HeaderBar getTitleBar() {
        return (HeaderBar) super.getTitleBar();
    }

    @Override
    protected void fixAcceleratorKeyPressed(KeyEvent e) {
        menuBarManager.setLastKeyPressedTime(System.nanoTime());
        super.fixAcceleratorKeyPressed(e);
    }
}
