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

package com.techsenger.tabshell.core;

import com.techsenger.patternfx.mvp.ChildFxView;
import com.techsenger.patternfx.mvp.ParentFxView;
import com.techsenger.tabshell.core.area.AreaFxView;
import com.techsenger.tabshell.core.area.AreaPort;
import com.techsenger.tabshell.core.menu.manager.MenuManager;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.core.registry.MenuBuilder;
import com.techsenger.tabshell.core.window.AbstractHostWindowFxView;
import com.techsenger.tabshell.material.style.Stylesheet;
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
 * This class extends ParentView but its interface doesn't because of encapsulation.
 *
 * @author Pavel Castornii
 */
public class DefaultShellFxView<P extends DefaultShellPresenter<?>>
        extends AbstractHostWindowFxView<P> implements ShellFxView<P> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultShellFxView.class);

    public class Composer extends AbstractHostWindowFxView<P>.Composer implements ShellFxView.Composer {

        private final ReadOnlyObjectWrapper<ParentFxView<?>> menuAware = new ReadOnlyObjectWrapper<>();

        private final DefaultShellFxView<P> view = DefaultShellFxView.this;

        private AreaFxView<?> workspace;

        public Composer() {
            this.menuAware.addListener((ov, oldV, newV) -> {
                logger.debug("{} Menu aware component: {}", getDescriptor().getLogPrefix(),
                        (newV == null) ? null : newV.getDescriptor().getFullName());
                updateMenuBar();
            });
        }

        @Override
        public void addWorkspace(AreaFxView<?> workspace) {
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
        public AreaFxView<?> getWorkspace() {
            return this.workspace;
        }

        @Override
        public AreaPort getWorkspacePort() {
            return this.workspace == null ? null : this.workspace.getPresenter();
        }

        @Override
        public ReadOnlyObjectProperty<ParentFxView<?>> menuAwareProperty() {
            return this.menuAware.getReadOnlyProperty();
        }

        @Override
        public ParentFxView<?> getMenuAware() {
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

        private void setMenuAware(ParentFxView<?> menuAware) {
            this.menuAware.set(menuAware);
        }

        private void resolvedMenuAware() {
            ParentFxView<?> focused = getFocused();
            if (focused == null) {
                setMenuAware(view);
                return;
            }
            if (focused == this) { // when user clicks on shell main menu, the previous  menu aware is used
                return;
            }
            ParentFxView<?> currentComponent = focused;
            while (true) {
                var port = currentComponent.getPresenter();
                if (port instanceof MenuAwarePort menuAware) {
                    setMenuAware(currentComponent);
                    return;
                }
                if (currentComponent instanceof ChildFxView<?> child) {
                    currentComponent = child.getComposer().getParent();
                    if (currentComponent == null) {
                        logger.warn("{} Child {} has no parent", getDescriptor().getLogPrefix(),
                                child.getDescriptor().getFullName());
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

    private final MenuManager menuManager;

    private final ControlRegistry controlRegistry;

    public DefaultShellFxView(Application application, List<Stylesheet> stylesheets, ControlRegistry controlRegistry) {
        this(application, new Stage(), stylesheets, controlRegistry);
    }

    public DefaultShellFxView(Application application, Stage stage, List<Stylesheet> stylesheets,
            ControlRegistry controlRegistry) {
        super(stage, stylesheets);
        Objects.requireNonNull(application, "Application can't be null");
        this.application = application;
        this.menuManager = new MenuManager(this, this.menuBar);
        this.controlRegistry = controlRegistry;
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
        var builder = new MenuBuilder(controlRegistry);
        var menus = builder.buildMainMenus(this);
        this.menuBar.getMenus().addAll(menus);
        logger.debug("{} Menu bar upgraded", getDescriptor().getLogPrefix());
        updateMenuBar();
    }

    @Override
    public void updateMenuBar() {
        this.menuManager.updateMenuBar((MenuAwarePort) getComposer().getMenuAware().getPresenter());
        logger.debug("{} Menu bar updated", getDescriptor().getLogPrefix());
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new DefaultShellFxView.Composer();
    }

    @Override
    protected void initialize() {
        super.initialize();
        getComposer().setMenuAware(this);
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
                e -> menuManager.setLastMouseClickTime(System.nanoTime()));
    }

    @Override
    protected HeaderBar getTitleBar() {
        return (HeaderBar) super.getTitleBar();
    }

    @Override
    protected void fixAcceleratorKeyPressed(KeyEvent e) {
        menuManager.setLastKeyPressedTime(System.nanoTime());
        super.fixAcceleratorKeyPressed(e);
    }
}
