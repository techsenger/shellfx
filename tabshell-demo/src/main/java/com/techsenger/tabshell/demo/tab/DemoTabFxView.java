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

package com.techsenger.tabshell.demo.tab;

import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.dialog.DialogPort;
import com.techsenger.tabshell.core.popup.OverlayScope;
import com.techsenger.tabshell.core.popup.PopupPort;
import com.techsenger.tabshell.core.tab.AbstractTabFxView;
import com.techsenger.tabshell.demo.dialogs.DemoDialogFxView;
import com.techsenger.tabshell.demo.dialogs.DemoDialogPresenter;
import com.techsenger.tabshell.demo.popup.DemoPopupFxView;
import com.techsenger.tabshell.demo.popup.DemoPopupPresenter;
import com.techsenger.tabshell.material.Anchors;
import com.techsenger.tabshell.material.style.SizeConstants;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class DemoTabFxView<P extends DemoTabPresenter<?, ?>> extends AbstractTabFxView<P> implements DemoTabView {

    public class Composer extends AbstractTabFxView<P>.Composer implements DemoTabComposer {

        private final DemoTabFxView<P> view = DemoTabFxView.this;

        @Override
        public DialogPort addDemoDialog(OverlayScope scope, boolean resizable) {
            var v = new DemoDialogFxView(resizable);
            var p = new DemoDialogPresenter(v, scope);
            p.initialize();
            view.getShell().getComposer().addDialog(v);
            return p.getPort();
        }

        @Override
        public PopupPort addDemoPopup(OverlayScope scope) {
            var v = new DemoPopupFxView();
            var p = new DemoPopupPresenter(v, scope, false);
            p.initialize();
            if (scope == OverlayScope.SHELL) {
                view.getShell().getComposer().addPopup(v, Anchors.topRight(40, 20));
            } else {
                view.getShell().getComposer().addPopup(v, Anchors.bottomRight(20, 20));
            }
            return p.getPort();
        }
    }

    private final CheckBox newValidCheckBox = new CheckBox("New Item Valid");

    private final CheckBox exitIncludedCheckBox = new CheckBox("Exit Item Included");

    private final CheckBox exitValidCheckBox = new CheckBox("Exit Item Valid");

    private final Button shellDialogButton = new Button("Shell Dialog");

    private final Button tabDialogButton = new Button("Tab Dialog");

    private final Button shellPopupButton = new Button("Shell Popup");

    private final Button tabPopupButton = new Button("Tab Popup");

    private final VBox vBox = new VBox(newValidCheckBox, exitIncludedCheckBox, exitValidCheckBox, shellDialogButton,
            tabDialogButton, shellPopupButton, tabPopupButton);

    private final HBox hBox = new HBox(vBox);

    public DemoTabFxView(ShellFxView<?> shell) {
        super(shell);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public boolean isNewValid() {
        return newValidCheckBox.isSelected();
    }

    @Override
    public void setNewValid(boolean value) {
        this.newValidCheckBox.setSelected(value);
    }

    @Override
    public boolean isExitIncluded() {
        return this.exitIncludedCheckBox.isSelected();
    }

    @Override
    public void setExitIncluded(boolean value) {
        this.exitIncludedCheckBox.setSelected(value);
    }

    @Override
    public boolean isExitValid() {
        return this.exitValidCheckBox.isSelected();
    }

    @Override
    public void setExitValid(boolean value) {
        this.exitValidCheckBox.setSelected(value);
    }

    @Override
    protected void build() {
        super.build();
        hBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(SizeConstants.INSET);
        getContentBox().setAlignment(Pos.CENTER);
        getContentBox().getChildren().add(hBox);
    }

    @Override
    protected void bind() {
        super.bind();
        shellDialogButton.prefWidthProperty().bind(vBox.widthProperty());
        tabDialogButton.prefWidthProperty().bind(vBox.widthProperty());
        shellPopupButton.prefWidthProperty().bind(vBox.widthProperty());
        tabPopupButton.prefWidthProperty().bind(vBox.widthProperty());
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        this.shellDialogButton.setOnAction(e -> getPresenter().onDialogOpen(OverlayScope.SHELL));
        this.tabDialogButton.setOnAction(e -> getPresenter().onDialogOpen(OverlayScope.TAB));
        this.shellPopupButton.setOnAction(e -> getPresenter().onPopupOpen(OverlayScope.SHELL));
        this.tabPopupButton.setOnAction(e -> getPresenter().onPopupOpen(OverlayScope.TAB));
    }

    @Override
    protected Composer createComposer() {
        return new DemoTabFxView.Composer();
    }
}
