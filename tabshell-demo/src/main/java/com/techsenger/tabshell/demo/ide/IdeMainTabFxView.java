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

package com.techsenger.tabshell.demo.ide;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.dialog.DialogParams;
import com.techsenger.tabshell.core.dialog.DialogPort;
import com.techsenger.tabshell.core.popup.AbstractPopupManager;
import com.techsenger.tabshell.core.popup.OverlayScope;
import com.techsenger.tabshell.core.popup.PopupContainerFxView;
import com.techsenger.tabshell.core.popup.PopupFxView;
import com.techsenger.tabshell.core.popup.PopupManager;
import com.techsenger.tabshell.core.popup.PopupParams;
import com.techsenger.tabshell.core.popup.PopupPort;
import com.techsenger.tabshell.core.tab.AbstractTabFxView;
import com.techsenger.tabshell.core.tab.TabContainerFxView;
import com.techsenger.tabshell.core.window.WindowPosition;
import com.techsenger.tabshell.demo.dialogs.DemoDialogFxView;
import com.techsenger.tabshell.demo.dialogs.DemoDialogPresenter;
import com.techsenger.tabshell.demo.main.TestInterface;
import com.techsenger.tabshell.demo.popup.DemoPopupFxView;
import com.techsenger.tabshell.demo.popup.DemoPopupPresenter;
import com.techsenger.tabshell.material.Anchors;
import com.techsenger.tabshell.material.style.Spacing;
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
public class IdeMainTabFxView<P extends IdeMainTabPresenter<?>> extends AbstractTabFxView<P> implements
        IdeMainTabView, PopupContainerFxView<P> {

    public class Composer extends AbstractTabFxView<P>.Composer implements IdeMainTabView.Composer,
            PopupContainerFxView.Composer, TestInterface {

        private final IdeMainTabFxView<P> view = IdeMainTabFxView.this;

        private final PopupManager popupManager = new AbstractPopupManager(() -> view.getWrapperPane()) {

            @Override
            protected void onContainerBlocked(boolean blocked) {
                if (blocked) {
                    view.getNode().getContent().addEventFilter(InputEvent.ANY, getEventBlocker());
                } else {
                    view.getNode().getContent().removeEventFilter(InputEvent.ANY, getEventBlocker());
                }
                getParent(TabContainerFxView.class).setTabHeaderBlocked(view.getNode(), blocked);
            }
        };

        @Override
        public DialogPort openDemoDialog(boolean resizable, DialogParams params) {
            var v = new DemoDialogFxView();
            var p = new DemoDialogPresenter(v, params);
            p.initialize();
            p.setResizable(resizable);
            getShell().getComposer().addWindow(v);
            getShell().getComposer().alignWindowToStage(v, WindowPosition.CENTER);
            return p;
        }

        @Override
        public PopupPort openDemoPopup(OverlayScope scope) {
            var v = new DemoPopupFxView();
            var params = new PopupParams(false);
            var p = new DemoPopupPresenter(v, params);
            p.initialize();
            if (scope == OverlayScope.WINDOW) {
                getShell().getComposer().addPopup(v, Anchors.topRight(40, 20));
            } else {
                addPopup(v, Anchors.bottomRight(20, 20));
            }
            return p;
        }

        @Override
        public void addPopup(PopupFxView<?> popup, Anchors anchors) {
            getModifiableChildren().add(popup);
            this.popupManager.addPopup(popup, anchors);
        }

        @Override
        public void removePopup(PopupFxView<?> popup) {
            this.popupManager.removePopup(popup);
            getModifiableChildren().remove(popup);
        }

        @Override
        public void closePopup(PopupFxView<?> popup) {
            removePopup(popup);
            popup.getPresenter().deinitializeTree();
        }

        @Override
        public @Unmodifiable List<? extends PopupFxView<?>> getPopups() {
            return popupManager.getPopups();
        }

        @Override
        public @Unmodifiable List<? extends PopupPort> getPopupPorts() {
            return popupManager.getPopups().stream().map(d -> d.getPresenter()).toList();
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

    public IdeMainTabFxView(ShellFxView<?> shell) {
        super(shell);
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
    protected void addListeners() {
        super.addListeners();
        this.fooDisabledCheckBox.selectedProperty()
                .addListener((ov, oldV, newV) -> getPresenter().onFooDisabledSelected(newV));
        this.barDisabledCheckBox.selectedProperty()
                .addListener((ov, oldV, newV) -> getPresenter().onBarDisabledSelected(newV));
        this.barIncludedCheckBox.selectedProperty()
                .addListener((ov, oldV, newV) -> getPresenter().onBarIncludedSelected(newV));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        // when a Pane inside a Tab requests focus, it receives it, but immediately loses it because
        // TabPane captures the focus. To fix it we request focus on next pulse.
        this.stackPane.setOnMouseClicked(e -> Platform.runLater(() -> this.stackPane.requestFocus()));
        this.shellDialogButton.setOnAction(e -> getPresenter().onDialogOpen());
        this.shellPopupButton.setOnAction(e -> getPresenter().onPopupOpen(OverlayScope.WINDOW));
        this.tabPopupButton.setOnAction(e -> getPresenter().onPopupOpen(OverlayScope.TAB));
    }

    @Override
    protected Composer createComposer() {
        return new IdeMainTabFxView.Composer();
    }

    private void setupButton(Button button) {
        HBox.setHgrow(button, Priority.ALWAYS);
        button.setMaxWidth(Double.MAX_VALUE);
    }
}
