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
import com.techsenger.patternfx.mvp.AbstractChildFxView;
import com.techsenger.patternfx.mvp.FxViewUtils;
import com.techsenger.shellfx.core.ShellFxView;
import com.techsenger.shellfx.core.ShellPort;
import com.techsenger.shellfx.material.icon.Icon;
import com.techsenger.shellfx.material.icon.IconViewBox;
import com.techsenger.toolkit.fx.pulse.PulseListenerManager;
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
public abstract class AbstractTabFxView<P extends AbstractTabPresenter<?>> extends AbstractChildFxView<P>
        implements TabFxView<P> {

    public class Composer extends AbstractChildFxView<P>.Composer implements TabFxView.Composer {

        private final AbstractTabFxView<P> view = AbstractTabFxView.this;

        private ShellFxView<?> shell;

        @Override
        public ShellFxView<?> getShell() {
            return shell;
        }

        @Override
        public ShellPort getShellPort() {
            return shell == null ? null : shell.getPresenter();
        }

        @Override
        public void close() {
            var parent = getParent();
            if (parent != null) {
                ((TabContainerFxView.Composer) parent.getComposer()).closeTab(view);
            }
        }

        @Override
        public @Nullable TabContainerFxView<?> getContainer() {
            return getParent(TabContainerFxView.class);
        }

        @Override
        public @Nullable TabContainerPort getContainerPort() {
            var container = getContainer();
            return container == null ? null : container.getPresenter();
        }

        private void setShell(ShellFxView<?> shell) {
            this.shell = shell;
        }
    }

    private final Tab root = new Tab();

    private final VBox contentBox = new VBox();

    private final Pane bgPane = new Pane();

    private final StackPane wrapperPane = new StackPane(contentBox);

    private final IconViewBox iconViewBox = new IconViewBox();

    private PulseListenerManager pulseListenerManager;

    public AbstractTabFxView(ShellFxView<?> shell) {
        super();
        FxViewUtils.setView(root, this);
        FxViewUtils.setView(wrapperPane, this);
        getComposer().setShell(shell);
    }

    @Override
    public Tab getNode() {
        return root;
    }

    @Override
    public void setTooltip(String tooltip) {
        this.root.setTooltip(new Tooltip(tooltip));
    }

    @Override
    public void setClosable(boolean closable) {
        this.root.setClosable(closable);
    }

    @Override
    public void setWaiting(boolean waiting) {
        if (waiting) {
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
    }

    @Override
    public void setIcon(Icon<?> icon) {
        iconViewBox.setIcon(icon);
    }

    @Override
    public void setTitle(String title) {
        this.root.setText(title);
    }

    @Override
    public boolean isSelected() {
        return this.root.isSelected();
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected void initialize() {
        this.pulseListenerManager = new PulseListenerManager(getDescriptor().getFullName(),
                () -> getContentBox().sceneProperty());
        super.initialize();
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
    protected void addListeners() {
        super.addListeners();
        this.root.selectedProperty().addListener((ov, oldV, newV) -> {
            getPresenter().onSelected(newV);
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        this.wrapperPane.setFocusTraversable(true);
        getNode().setOnCloseRequest((e) -> getPresenter().onCloseRequest());
    }

    protected StackPane getWrapperPane() {
        return wrapperPane;
    }

    protected PulseListenerManager getPulseListenerManager() {
        return pulseListenerManager;
    }

    @Override
    protected Composer createComposer() {
        return new AbstractTabFxView.Composer();
    }
}
