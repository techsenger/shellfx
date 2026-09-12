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

package com.techsenger.shellfx.demo.ide;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.dialog.FullDialogPort;
import com.techsenger.shellfx.core.popup.AbstractPopupManager;
import com.techsenger.shellfx.core.popup.ContainerPopupPort;
import com.techsenger.shellfx.core.popup.OverlayScope;
import com.techsenger.shellfx.core.popup.PopupContainerView;
import com.techsenger.shellfx.core.popup.PopupManager;
import com.techsenger.shellfx.core.popup.PopupParams;
import com.techsenger.shellfx.core.popup.PopupPort;
import com.techsenger.shellfx.core.popup.PopupView;
import com.techsenger.shellfx.core.tab.AbstractTabView;
import com.techsenger.shellfx.demo.dialog.DemoDialogView;
import com.techsenger.shellfx.demo.dialog.DemoDialogViewModel;
import com.techsenger.shellfx.demo.popup.DemoPopupView;
import com.techsenger.shellfx.demo.popup.DemoPopupViewModel;
import com.techsenger.shellfx.material.Anchors;
import com.techsenger.shellfx.material.style.Spacing;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * This tab is a popup container.
 *
 * @author Pavel Castornii
 */
public class IdeMainTabView<VM extends IdeMainTabViewModel<?>> extends AbstractTabView<VM> implements
        PopupContainerView<VM> {

    public class Composer extends AbstractTabView<VM>.Composer implements IdeMainTabComposer,
            PopupContainerView.Composer {

        private final IdeMainTabView<VM> view = IdeMainTabView.this;

        private final PopupManager popupManager = new AbstractPopupManager(() -> view.getWrapperPane()) {

            @Override
            protected void onContainerBlocked(boolean blocked) {
                if (blocked) {
                    view.getNode().getContent().addEventFilter(InputEvent.ANY, getEventBlocker());
                } else {
                    view.getNode().getContent().removeEventFilter(InputEvent.ANY, getEventBlocker());
                }
                getParent().setTabHeaderBlocked(view.getNode(), blocked);
            }
        };

        @Override
        public FullDialogPort openDemoDialog(boolean resizable, DialogParams params) {
            var viewModel = new DemoDialogViewModel<>(params);
            var dialogView = new DemoDialogView<>(viewModel);
            dialogView.initialize();
            viewModel.setResizable(resizable);
            getShell().getComposer().addDialog(dialogView);
            return viewModel;
        }

        @Override
        public PopupPort openDemoPopup(OverlayScope scope) {
            var params = new PopupParams(false);
            var viewModel = new DemoPopupViewModel<>(params);
            var popupView = new DemoPopupView<>(viewModel);
            popupView.initialize();
            if (scope == OverlayScope.WINDOW) {
                getShell().getComposer().addPopup(popupView, Anchors.topRight(40, 20));
            } else {
                addPopup(popupView, Anchors.bottomRight(20, 20));
            }
            return viewModel;
        }

        @Override
        public void addPopup(PopupView<?> popup, Anchors anchors) {
            getModifiableChildren().add(popup);
            this.popupManager.addPopup(popup, anchors);
        }

        @Override
        public void removePopup(PopupView<?> popup) {
            this.popupManager.removePopup(popup);
            getModifiableChildren().remove(popup);
        }

        @Override
        public void closePopup(PopupView<?> popup) {
            removePopup(popup);
            popup.deinitializeTree();
        }

        @Override
        public @Unmodifiable List<? extends PopupView<?>> getPopups() {
            return popupManager.getPopups();
        }

        @Override
        public @Unmodifiable List<? extends ContainerPopupPort> getPopupPorts() {
            return popupManager.getPopups().stream().map(d -> d.getViewModel()).toList();
        }
    }

    private final CheckBox fooDisabledCheckBox = new CheckBox("Foo Item Disabled");

    private final CheckBox barIncludedCheckBox = new CheckBox("Bar Item Included");

    private final CheckBox barDisabledCheckBox = new CheckBox("Bar Item Disabled");

    private final Button shellDialogButton = new Button("Shell");

    private final HBox dialogHBox = new HBox(shellDialogButton);

    private final Button shellPopupButton = new Button("Shell");

    private final Button tabPopupButton = new Button("Tab");

    private final HBox popupHBox = new HBox(shellPopupButton, tabPopupButton);

    private final VBox vBox = new VBox(new Label("Main Menu -> Extra:"), fooDisabledCheckBox, barIncludedCheckBox,
            barDisabledCheckBox, new Label("Dialogs:"), dialogHBox, new Label("Popups:"), popupHBox);

    private final StackPane stackPane = new StackPane(vBox);

    public IdeMainTabView(VM viewModel, ShellView<?> shell) {
        super(viewModel, shell);
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    public void requestFocus() {

    }

    @Override
    protected void build() {
        super.build();
        StackPane.setAlignment(vBox, Pos.CENTER);
        setupButton(shellDialogButton);
        setupButton(shellPopupButton);
        setupButton(tabPopupButton);
        vBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        vBox.setSpacing(Spacing.getVertical());
        dialogHBox.setSpacing(Spacing.getHorizontal());
        popupHBox.setSpacing(Spacing.getHorizontal());
        VBox.setVgrow(stackPane, Priority.ALWAYS);
        getContentBox().getChildren().add(stackPane);
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        this.fooDisabledCheckBox.selectedProperty().bindBidirectional(viewModel.fooDisabledProperty());
        this.barIncludedCheckBox.selectedProperty().bindBidirectional(viewModel.barIncludedProperty());
        this.barDisabledCheckBox.selectedProperty().bindBidirectional(viewModel.barDisabledProperty());
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        // when a Pane inside a Tab requests focus, it receives it, but immediately loses it because
        // TabPane captures the focus. To fix it we request focus on next pulse.
        this.stackPane.setOnMouseClicked(e -> Platform.runLater(() -> this.stackPane.requestFocus()));
        this.shellDialogButton.setOnAction(e -> getViewModel().onDialogOpen());
        this.shellPopupButton.setOnAction(e -> getViewModel().onPopupOpen(OverlayScope.WINDOW));
        this.tabPopupButton.setOnAction(e -> getViewModel().onPopupOpen(OverlayScope.TAB));
    }

    @Override
    protected Composer createComposer() {
        return new IdeMainTabView.Composer();
    }

    private void setupButton(Button button) {
        HBox.setHgrow(button, Priority.ALWAYS);
        button.setMaxWidth(Double.MAX_VALUE);
    }
}
