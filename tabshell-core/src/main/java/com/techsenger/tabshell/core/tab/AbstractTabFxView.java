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
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.toolkit.fx.pulse.PulseListenerManager;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractTabFxView<P extends TabPresenter<?, ?>>
        extends AbstractChildFxView<P> implements TabFxView<P> {

    public class Composer extends AbstractChildFxView.Composer implements TabFxView.Composer {

        private final AbstractTabFxView<?> view = AbstractTabFxView.this;

        @Override
        public void remove() {
            var parent = view.getParent();
            if (parent != null) {
                ((TabContainerFxView.Composer<TabFxView<?>>) parent.getComposer()).removeTab(view);
            }
        }
    }

    private final ComponentTab root = new ComponentTab(this);

    private final VBox contentBox = new VBox();

    private final Pane bgPane = new Pane();

    private final StackPane wrapperPane = new StackPane(contentBox);

    private final IconViewBox iconViewBox = new IconViewBox();

    private PulseListenerManager pulseListenerManager;

    public AbstractTabFxView() {
        super();
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
        this.root.setGraphic(new Label());
        this.root.setContent(wrapperPane);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        this.root.selectedProperty().addListener((ov, oldV, newV) -> getPresenter().onSelected(newV));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
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
