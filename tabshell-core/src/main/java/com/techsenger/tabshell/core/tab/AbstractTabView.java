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

import com.techsenger.patternfx.mvvmx.AbstractChildView;
import com.techsenger.tabshell.core.menu.MenuHelper;
import com.techsenger.tabshell.core.menu.MenuItemHelper;
import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.tabshell.material.menu.MenuItemName;
import com.techsenger.tabshell.material.menu.MenuName;
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
public abstract class AbstractTabView<T extends AbstractTabViewModel<?>, S extends AbstractTabComponent<?>>
        extends AbstractChildView<T, S> implements TabView<T, S> {

    private final ComponentTab root = new ComponentTab(this);

    private final VBox contentPane = new VBox();

    private final StackPane wrapperPane = new StackPane(contentPane);

    private PulseListenerManager pulseListenerManager;

    public AbstractTabView(T viewModel) {
        super(viewModel);
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
    public void doOnMenuShowing(MenuName menuName) {

    }

    @Override
    public void doOnMenuHiding(MenuName menuName) {

    }

    @Override
    public MenuHelper getMenuHelper(MenuName menuName) {
        return getViewModel().getMenuHelpersByName().get(menuName);
    }

    @Override
    public MenuItemHelper getMenuItemHelper(MenuItemName menuItemName) {
        return getViewModel().getMenuItemHelpersByName().get(menuItemName);
    }

    protected VBox getContentPane() {
        return contentPane;
    }

    @Override
    protected void initialize() {
        this.pulseListenerManager = new PulseListenerManager(getComponent().getFullName(),
                () -> getContentPane().sceneProperty());
        super.initialize();
    }

    @Override
    protected void build() {
        super.build();
        this.root.setGraphic(new Label());
        this.root.setContent(wrapperPane);
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
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
        this.root.closableProperty().bind(viewModel.closableProperty());
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        getViewModel().waitingProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                var bgPane = new Pane();
                bgPane.setMouseTransparent(false);
                wrapperPane.getChildren().add(bgPane);
                bgPane.setCursor(Cursor.WAIT);
            } else {
                wrapperPane.getChildren().remove(wrapperPane.getChildren().size() - 1);
            }
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        getNode().setOnCloseRequest((e) -> getViewModel().close());
    }

    protected StackPane getWrapperPane() {
        return wrapperPane;
    }

    protected PulseListenerManager getPulseListenerManager() {
        return pulseListenerManager;
    }
}
