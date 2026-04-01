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

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.dialog.DialogPort;
import com.techsenger.tabshell.core.popup.OverlayScope;
import com.techsenger.tabshell.core.popup.PopupPort;
import com.techsenger.tabshell.core.tab.AbstractTabFxView;
import com.techsenger.tabshell.demo.dialogs.DemoDialogFxView;
import com.techsenger.tabshell.demo.dialogs.DemoDialogPresenter;
import com.techsenger.tabshell.demo.main.TestInterface;
import com.techsenger.tabshell.demo.popup.DemoPopupFxView;
import com.techsenger.tabshell.demo.popup.DemoPopupPresenter;
import com.techsenger.tabshell.material.Anchors;
import com.techsenger.tabshell.material.style.Spacing;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class IdeMainTabFxView<P extends IdeMainTabPresenter<?, ?>> extends AbstractTabFxView<P> implements
        IdeMainTabView {

    public class Composer extends AbstractTabFxView<P>.Composer implements IdeMainTabComposer, TestInterface {

        private final IdeMainTabFxView<P> view = IdeMainTabFxView.this;

        @Override
        public DialogPort addDemoDialog(boolean resizable) {
            var v = new DemoDialogFxView();
            var p = new DemoDialogPresenter(v);
            p.initialize();
            p.setResizable(resizable);
            getShell().getComposer().addDialog(v);
            return p;
        }

        @Override
        public PopupPort addDemoPopup(OverlayScope scope) {
            var v = new DemoPopupFxView();
            var p = new DemoPopupPresenter(v, false);
            p.initialize();
            if (scope == OverlayScope.SHELL) {
                getShell().getComposer().addPopup(v, Anchors.topRight(40, 20));
            } else {
                addPopup(v, Anchors.bottomRight(20, 20));
            }
            return p;
        }
    }

    private final CheckBox fooValidCheckBox = new CheckBox("Foo Item Valid");

    private final CheckBox barIncludedCheckBox = new CheckBox("Bar Item Included");

    private final CheckBox barValidCheckBox = new CheckBox("Bar Item Valid");

    private final Button shellDialogButton = new Button("Shell");

    private final HBox dialogHBox = new HBox(shellDialogButton);

    private final Button shellPopupButton = new Button("Shell");

    private final Button tabPopupButton = new Button("Tab");

    private final HBox popupHBox = new HBox(shellPopupButton, tabPopupButton);

    private final VBox vBox = new VBox(new Label("Main Menu -> Extra:"), fooValidCheckBox, barIncludedCheckBox,
            barValidCheckBox, new Label("Dialogs:"), dialogHBox, new Label("Popups:"), popupHBox);

    private final StackPane stackPane = new StackPane(vBox);

    public IdeMainTabFxView(ShellFxView<?> shell) {
        super(shell);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public boolean isFooValid() {
        return this.fooValidCheckBox.isSelected();
    }

    @Override
    public void setFooValid(boolean value) {
        this.fooValidCheckBox.setSelected(value);
    }

    @Override
    public boolean isBarIncluded() {
        return this.barIncludedCheckBox.isSelected();
    }

    @Override
    public void setBarIncluded(boolean value) {
        this.barIncludedCheckBox.setSelected(value);
    }

    @Override
    public boolean isBarValid() {
        return this.barValidCheckBox.isSelected();
    }

    @Override
    public void setBarValid(boolean value) {
        this.barValidCheckBox.setSelected(value);
    }

    @Override
    protected void build() {
        super.build();
        StackPane.setAlignment(vBox, Pos.CENTER);
        setupButton(shellDialogButton);
        setupButton(shellPopupButton);
        setupButton(tabPopupButton);
        vBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        vBox.setSpacing(Spacing.VERTICAL);
        dialogHBox.setSpacing(Spacing.HORIZONTAL);
        popupHBox.setSpacing(Spacing.HORIZONTAL);
        VBox.setVgrow(stackPane, Priority.ALWAYS);
        getContentBox().getChildren().add(stackPane);
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        // when a Pane inside a Tab requests focus, it receives it, but immediately loses it because
        // TabPane captures the focus. To fix it we request focus on next pulse.
        this.stackPane.setOnMouseClicked(e -> Platform.runLater(() -> this.stackPane.requestFocus()));
        this.shellDialogButton.setOnAction(e -> getPresenter().onDialogOpen());
        this.shellPopupButton.setOnAction(e -> getPresenter().onPopupOpen(OverlayScope.SHELL));
        this.tabPopupButton.setOnAction(e -> getPresenter().onPopupOpen(OverlayScope.TAB));
    }

    private void setupButton(Button button) {
        button.getStyleClass().add(Styles.DENSE);
        HBox.setHgrow(button, Priority.ALWAYS);
        button.setMaxWidth(Double.MAX_VALUE);
    }

    @Override
    protected Composer createComposer() {
        return new IdeMainTabFxView.Composer();
    }
}
