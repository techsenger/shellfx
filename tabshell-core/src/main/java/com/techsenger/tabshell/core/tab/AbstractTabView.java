/*
 * Copyright 2024-2025 Pavel Castornii.
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

import com.techsenger.mvvm4fx.core.AbstractChildView;
import com.techsenger.mvvm4fx.core.ParentView;
import com.techsenger.tabshell.core.CloseScope;
import com.techsenger.tabshell.core.menu.MenuHelper;
import com.techsenger.tabshell.core.menu.MenuItemHelper;
import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.tabshell.material.menu.MenuItemKey;
import com.techsenger.tabshell.material.menu.MenuKey;
import com.techsenger.toolkit.core.ObjectUtils;
import com.techsenger.toolkit.fx.pulse.PulseListenerManager;
import com.techsenger.toolkit.fx.value.ValueUtils;
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
public abstract class AbstractTabView<T extends AbstractTabViewModel> extends AbstractChildView<T>
        implements TabView<T> {

    private final ComponentTab root = new ComponentTab(this);

    private final VBox contentPane = new VBox();

    private final StackPane wrapperPane = new StackPane(contentPane);

    private final PulseListenerManager pulseListenerManager;

    public AbstractTabView(T viewModel) {
        super(viewModel);
        this.pulseListenerManager = new PulseListenerManager(ObjectUtils.getIdentity(this),
                () -> getContentPane().sceneProperty());
    }

    @Override
    public void setParent(ParentView<?> parent) {
        super.setParent(parent);
    }

    @Override
    public void doOnSelected() {
        // empty
    }

    @Override
    public void doOnDeselected() {
        // empty
    }

    @Override
    public ComponentTab getNode() {
        return root;
    }

    @Override
    public void doOnMenuShowing(MenuKey menuKey) {

    }

    @Override
    public void doOnMenuHiding(MenuKey menuKey) {

    }

    @Override
    public MenuHelper getMenuHelper(MenuKey menuKey) {
        return getViewModel().getMenuHelpersByKey().get(menuKey);
    }

    @Override
    public MenuItemHelper getMenuItemHelper(MenuItemKey menuItemKey) {
        return getViewModel().getMenuItemHelpersByKey().get(menuItemKey);
    }

    @Override
    public void close() {
        ((TabContainerView<AbstractTabView<T>>) getParent()).closeTab(this);
    }

    @Override
    public boolean doOnCloseAttempt(CloseScope scope, Runnable retryCallback) {
        if (getViewModel().isReadyToClose()) {
            return true;
        } else {
            getViewModel().prepareForClose(scope, retryCallback);
            return false;
        }
    }

    protected VBox getContentPane() {
        return contentPane;
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        this.root.setGraphic(new Label());
        this.root.setContent(wrapperPane);
    }

    @Override
    protected void bind(T viewModel) {
        super.bind(viewModel);
        this.root.textProperty().bind(viewModel.titleProperty());
        var iconViewBox = new IconViewBox();
        iconViewBox.iconProperty().bind(viewModel.iconProperty());
        this.root.setGraphic(iconViewBox);
        ValueUtils.callAndAddListener(viewModel.tooltipProperty(), (ov, oldV, newV) -> {
            if (newV != null) {
                this.root.setTooltip(new Tooltip(newV));
            } else {
                this.root.setTooltip(null);
            }
        });
        viewModel.selectedWrapper().bind(this.root.selectedProperty());
    }

    @Override
    protected void addListeners(T viewModel) {
        super.addListeners(viewModel);
        viewModel.waitingProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                var bgPane = new Pane();
                bgPane.setMouseTransparent(false);
                wrapperPane.getChildren().add(bgPane);
                bgPane.setCursor(Cursor.WAIT);
            } else {
                wrapperPane.getChildren().remove(wrapperPane.getChildren().size() - 1);
            }
        });
        viewModel.closeSource().addListener((v) -> {
            if (Boolean.TRUE.equals(v)) {
                close();
            }
        });
    }

    protected StackPane getWrapperPane() {
        return wrapperPane;
    }

    protected PulseListenerManager getPulseListenerManager() {
        return pulseListenerManager;
    }
}
