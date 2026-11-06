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

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.core.area.AreaPort;
import com.techsenger.shellfx.core.area.AreaView;
import com.techsenger.shellfx.core.registries.ControlBuilder;
import com.techsenger.shellfx.core.registries.Controls;
import com.techsenger.shellfx.core.window.AbstractHostWindowView;
import com.techsenger.shellfx.material.slot.MenuBarSlot;
import javafx.scene.control.MenuBar;
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
public class DefaultShellView<VM extends DefaultShellViewModel<?>> extends AbstractHostWindowView<VM>
        implements ShellView<VM> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultShellView.class);

    /**
     * Narrows a menu bar slot or its controls, stored with the wildcard of the actual view class, to the type of
     * the shell view this class works with. It is correct as long as the slot was declared for the class of the
     * view or for its superclass, which is the contract of the constructor.
     */
    @SuppressWarnings("unchecked")
    private static <T> T cast(Object value) {
        return (T) value;
    }

    public class Composer extends AbstractHostWindowView<VM>.Composer implements ShellView.Composer {

        private final DefaultShellView<VM> view = DefaultShellView.this;

        private AreaView<?> workspace;

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
            view.getContentBox().getChildren().remove(this.workspace.getNode());
            getModifiableChildren().remove(this.workspace);
            this.workspace = null;
        }

        @Override
        public AreaView<?> getWorkspace() {
            return this.workspace;
        }

        @Override
        public AreaPort getWorkspacePort() {
            return this.workspace == null ? null : this.workspace.getViewModel();
        }
    }

    private final MenuBarSlot<? extends ShellView<?>> menuBarSlot;

    private final ShellViewContext context;

    private @Nullable Controls<? extends ShellView<?>, MenuBar> menuBarControls;

    public DefaultShellView(VM viewModel, ShellViewContext context, MenuBarSlot<? extends ShellView<?>> menuBarSlot) {
        this(viewModel, context, new Stage(), menuBarSlot);
    }

    public DefaultShellView(VM viewModel, ShellViewContext context, Stage stage,
            MenuBarSlot<? extends ShellView<?>> menuBarSlot) {
        super(viewModel, context.getStylesheetRegistry(), stage);
        this.menuBarSlot = menuBarSlot;
        this.context = context;
    }

    @Override
    public void requestFocus() {
        var workspace = getComposer().getWorkspace();
        if (workspace != null) {
            workspace.requestFocus();
        }
    }

    @Override
    public ShellViewContext getContext() {
        return context;
    }

    @Override
    public void upgradeMenuBar() {
        var children = getLeftBox().getChildren();
        var index = children.size();
        if (this.menuBarControls != null) {
            var oldMenuBar = this.menuBarControls.root();
            for (var menu : oldMenuBar.getMenus()) {
                menu.hide();
            }
            index = children.indexOf(oldMenuBar);
            children.remove(oldMenuBar);
            deinitializeMenuBar();
        }
        var builder = new ControlBuilder(context.getSlotRegistry(), context.getControlRegistry());
        this.menuBarControls =
                builder.buildMenuBar(this, DefaultShellView.<MenuBarSlot<ShellView<?>>>cast(menuBarSlot));
        children.add(index, this.menuBarControls.root());
        logger.debug("{} Menu bar upgraded", getDescriptor().getLogPrefix());
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
    }

    @Override
    protected void unbuild() {
        deinitializeMenuBar();
        super.unbuild();
    }

    @Override
    protected HeaderBar getTitleBar() {
        return (HeaderBar) super.getTitleBar();
    }

    protected @Nullable Controls<? extends ShellView<?>, MenuBar> getMenuBarControls() {
        return menuBarControls;
    }

    /**
     * Deinitializes the providers of the menu bar controls, if there are any.
     */
    private void deinitializeMenuBar() {
        if (this.menuBarControls == null) {
            return;
        }
        DefaultShellView.<Controls<ShellView<?>, MenuBar>>cast(this.menuBarControls).deinitializeAll(this);
        this.menuBarControls = null;
    }
}
