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

import com.techsenger.patternfx.mvp.AbstractChildFxView;
import com.techsenger.tabshell.core.FxViewUtils;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.ShellPort;
import com.techsenger.tabshell.core.dialog.DefaultDialogManager;
import com.techsenger.tabshell.core.dialog.DialogFxView;
import com.techsenger.tabshell.core.dialog.DialogManager;
import com.techsenger.tabshell.core.dialog.DialogPort;
import com.techsenger.tabshell.core.popup.OverlayScope;
import com.techsenger.tabshell.core.popup.PopupFxView;
import com.techsenger.tabshell.core.popup.PopupPort;
import com.techsenger.tabshell.material.Anchors;
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.toolkit.fx.pulse.PulseListenerManager;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import java.util.List;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractTabFxView<P extends TabPresenter<?, ?>>
        extends AbstractChildFxView<P> implements TabFxView<P> {

    public class Composer extends AbstractChildFxView<P>.Composer implements TabFxView.Composer {

        private final AbstractTabFxView<P> view = AbstractTabFxView.this;

        @Override
        public void remove() {
            var parent = view.getParent();
            if (parent != null) {
                ((TabContainerFxView.Composer) parent.getComposer()).removeTab(view);
            }
        }

        @Override
        public ShellPort getShell() {
            return view.getShell().getPresenter().getPort();
        }

        @Override
        public OverlayScope getOverlayScope() {
            return OverlayScope.TAB;
        }

        @Override
        public List<? extends DialogPort> getDialogs() {
            return view.dialogManager.getDialogs().stream().map(v -> v.getPresenter().getPort()).toList();
        }

        @Override
        public void addDialog(DialogFxView<?> dialog) {
            var scope = dialog.getPresenter().getOverlayScope();
            if (scope == getOverlayScope()) {
                view.dialogManager.showDialog(dialog);
                view.getModifiableChildren().add(dialog);
            } else {
                view.getShell().getComposer().addDialog(dialog);
            }
        }

        @Override
        public void removeDialog(DialogFxView<?> dialog) {
            var scope = dialog.getPresenter().getOverlayScope();
            if (scope == getOverlayScope()) {
                view.dialogManager.hideDialog(dialog);
                view.getModifiableChildren().remove(dialog);
                dialog.getPresenter().deinitializeTree();
            } else {
                view.getShell().getComposer().removeDialog(dialog);
            }
        }

        @Override
        public void addPopup(PopupFxView<?> popup, Anchors anchors) {
            var scope = popup.getPresenter().getOverlayScope();
            if (scope == getOverlayScope()) {
                view.dialogManager.showPopup(popup, anchors);
                view.getModifiableChildren().add(popup);
            } else {
                view.getShell().getComposer().addPopup(popup, anchors);
            }
        }

        @Override
        public void removePopup(PopupFxView<?> popup) {
            var scope = popup.getPresenter().getOverlayScope();
            if (scope == getOverlayScope()) {
                view.dialogManager.hidePopup(popup);
                view.getModifiableChildren().remove(popup);
                popup.getPresenter().deinitializeTree();
            } else {
                view.getShell().getComposer().removePopup(popup);
            }
        }

        @Override
        public List<? extends PopupPort> getPopups() {
            return view.dialogManager.getPopups().stream().map(v -> v.getPresenter().getPort()).toList();
        }
    }

    private final ShellFxView<?> shell;

    private final ComponentTab root = new ComponentTab(this);

    private final VBox contentBox = new VBox();

    private final Pane bgPane = new Pane();

    private final StackPane wrapperPane = new StackPane(contentBox);

    private final IconViewBox iconViewBox = new IconViewBox();

    private final DialogManager dialogManager = new DefaultDialogManager(wrapperPane, contentBox);

    private PulseListenerManager pulseListenerManager;

    public AbstractTabFxView(ShellFxView<?> shell) {
        super();
        this.shell = shell;
    }

    @Override
    public ComponentTab getNode() {
        return root;
    }

    @Override
    public String getTooltip() {
        var tooltip = this.root.getTooltip();
        if (tooltip != null) {
            return tooltip.getText();
        } else {
            return null;
        }
    }

    @Override
    public void setTooltip(String tooltip) {
        this.root.setTooltip(new Tooltip(tooltip));
    }

    @Override
    public boolean isClosable() {
        return this.root.isClosable();
    }

    @Override
    public void setClosable(boolean closable) {
        this.root.setClosable(closable);
    }

    @Override
    public void setWaiting(boolean waiting) {
        if (isWaiting() == waiting) {
            return;
        }
        if (waiting) {
            bgPane.setMouseTransparent(false);
            wrapperPane.getChildren().add(bgPane);
            bgPane.setCursor(Cursor.WAIT);
        } else {
            wrapperPane.getChildren().remove(bgPane);
        }
    }

    @Override
    public boolean isWaiting() {
        return bgPane.getParent() != null;
    }

    @Override
    public Icon<?> getIcon() {
        if (this.root.getGraphic() == iconViewBox) {
            return iconViewBox.getIcon();
        } else {
            return null;
        }
    }

    @Override
    public void setIcon(Icon<?> icon) {
        if (icon != null) {
            this.root.setGraphic(iconViewBox);
            iconViewBox.setIcon(icon);
        } else {
            this.root.setGraphic(new Label());
            iconViewBox.setIcon(null);
        }
    }

    @Override
    public String getTitle() {
        return this.root.getText();
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
    public ShellFxView<?> getShell() {
        return shell;
    }

    protected DialogManager getDialogManager() {
        return dialogManager;
    }

    @Override
    protected void initialize() {
        this.pulseListenerManager = new PulseListenerManager(getDescriptor().getFullName(),
                () -> getContentBox().sceneProperty());
        FxViewUtils.setComponent(wrapperPane, this);
        super.initialize();
    }

    protected VBox getContentBox() {
        return contentBox;
    }

    @Override
    protected void build() {
        super.build();
        this.root.setGraphic(new Label());
        this.root.setContent(wrapperPane);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        this.root.selectedProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                NodeUtils.requestFocus(this.contentBox);
            }
            getPresenter().onSelected(newV);
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        getNode().setOnCloseRequest((e) -> getPresenter().close());
        // otherwise scene focus owner doesn't work
        this.contentBox.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getTarget() == this.contentBox) {
                this.contentBox.requestFocus();
                e.consume(); // otherwise the tabpane will become focused
            }
        });
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
