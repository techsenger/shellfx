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

package com.techsenger.tabshell.core.tab;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.mvp.AbstractChildFxView;
import com.techsenger.patternfx.mvp.FxViewUtils;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.ShellPort;
import com.techsenger.tabshell.core.dialog.DefaultDialogManager;
import com.techsenger.tabshell.core.dialog.DialogFxView;
import com.techsenger.tabshell.core.dialog.DialogManager;
import com.techsenger.tabshell.core.dialog.DialogPort;
import com.techsenger.tabshell.core.popup.PopupFxView;
import com.techsenger.tabshell.core.popup.PopupPort;
import com.techsenger.tabshell.material.Anchors;
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.toolkit.fx.pulse.PulseListenerManager;
import java.util.List;
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
public abstract class AbstractTabFxView<P extends TabPresenter<?>> extends AbstractChildFxView<P>
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
        public void remove() {
            var parent = view.getParent();
            if (parent != null) {
                ((TabContainerFxView.Composer) parent.getComposer()).removeTab(view);
            }
        }

        @Override
        public void addDialog(DialogFxView<?> dialog) {
            view.dialogManager.showDialog(dialog);
            view.getModifiableChildren().add(dialog);
        }

        @Override
        public void removeDialog(DialogFxView<?> dialog) {
            view.dialogManager.hideDialog(dialog);
            view.getModifiableChildren().remove(dialog);
            dialog.getPresenter().deinitializeTree();
        }

        @Override
        public @Unmodifiable List<? extends DialogFxView<?>> getDialogs() {
            return view.dialogManager.getDialogs();
        }

        @Override
        public @Unmodifiable List<? extends DialogPort> getDialogPorts() {
            return view.dialogManager.getDialogs().stream().map(v -> v.getPresenter()).toList();
        }

        @Override
        public void addPopup(PopupFxView<?> popup, Anchors anchors) {
            view.dialogManager.showPopup(popup, anchors);
            view.getModifiableChildren().add(popup);
        }

        @Override
        public void removePopup(PopupFxView<?> popup) {
            view.dialogManager.hidePopup(popup);
            view.getModifiableChildren().remove(popup);
            popup.getPresenter().deinitializeTree();
        }

        @Override
        public @Unmodifiable List<? extends PopupFxView<?>> getPopups() {
            return view.dialogManager.getPopups();
        }

        @Override
        public List<? extends PopupPort> getPopupPorts() {
            return view.dialogManager.getPopups().stream().map(v -> v.getPresenter()).toList();
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

    private final DialogManager dialogManager = new DefaultDialogManager(wrapperPane, contentBox);

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

    protected DialogManager getDialogManager() {
        return dialogManager;
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
        getNode().setOnCloseRequest((e) -> getPresenter().close());
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
