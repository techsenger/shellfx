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

package com.techsenger.shellfx.core.tab;

import com.techsenger.annotations.Nullable;
import com.techsenger.patternfx.mvvm.AbstractChildView;
import com.techsenger.patternfx.mvvm.ViewUtils;
import com.techsenger.shellfx.core.ShellPort;
import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.material.icon.IconViewBox;
import com.techsenger.toolkit.fx.pulse.PulseListenerManager;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.scene.Cursor;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractTabView<VM extends AbstractTabViewModel<?>> extends AbstractChildView<VM>
        implements TabView<VM> {

    public class Composer extends AbstractChildView<VM>.Composer implements TabView.Composer {

        private final AbstractTabView<VM> view = AbstractTabView.this;

        private ShellView<?> shell;

        @Override
        public ShellView<?> getShell() {
            return shell;
        }

        @Override
        public ShellPort getShellPort() {
            return shell == null ? null : shell.getViewModel();
        }

        @Override
        public void close() {
            var parent = getParent();
            if (parent != null) {
                parent.getComposer().closeTab(view);
            }
        }

        @Override
        public @Nullable TabContainerView<?> getParent() {
            return (TabContainerView<?>) super.getParent();
        }

        @Override
        public @Nullable TabContainerPort getParentPort() {
            var container = getParent();
            return container == null ? null : container.getViewModel();
        }

        private void setShell(ShellView<?> shell) {
            this.shell = shell;
        }
    }

    private final Tab root = new Tab();

    private final VBox contentBox = new VBox();

    private final Pane bgPane = new Pane();

    private final StackPane wrapperPane = new StackPane(contentBox);

    private final IconViewBox iconViewBox = new IconViewBox();

    private PulseListenerManager pulseListenerManager;

    public AbstractTabView(VM viewModel, ShellView<?> shell) {
        super(viewModel);
        ViewUtils.setView(root, this);
        ViewUtils.setView(wrapperPane, this);
        this.pulseListenerManager = new PulseListenerManager(getDescriptor().getFullName(),
                () -> getContentBox().sceneProperty());
        getComposer().setShell(shell);
    }

    @Override
    public Tab getNode() {
        return root;
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new AbstractTabView.Composer();
    }

    protected VBox getContentBox() {
        return contentBox;
    }

    protected IconViewBox getIconViewBox() {
        return iconViewBox;
    }

    @Override
    protected void build() {
        super.build();
        this.root.setGraphic(iconViewBox);
        this.root.setContent(wrapperPane);
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        root.closableProperty().bind(viewModel.closableProperty());
        iconViewBox.iconProperty().bind(viewModel.iconProperty());
        root.textProperty().bind(viewModel.titleProperty());
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        viewModel.waitingProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                if (bgPane.getParent() == null) {
                    bgPane.setMouseTransparent(false);
                    wrapperPane.getChildren().add(bgPane);
                    bgPane.setCursor(Cursor.WAIT);
                }
            } else {
                if (bgPane.getParent() != null) {
                    wrapperPane.getChildren().remove(bgPane);
                }
            }
        });
        ValueUtils.callAndAddListener(viewModel.tooltipProperty(), (ov, oldV, newV) -> {
            if (newV != null) {
                this.root.setTooltip(new Tooltip(newV));
            }
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        this.wrapperPane.setFocusTraversable(true);
        getNode().setOnCloseRequest((e) -> getViewModel().onCloseRequest());
    }

    protected StackPane getWrapperPane() {
        return wrapperPane;
    }

    protected PulseListenerManager getPulseListenerManager() {
        return pulseListenerManager;
    }
}
