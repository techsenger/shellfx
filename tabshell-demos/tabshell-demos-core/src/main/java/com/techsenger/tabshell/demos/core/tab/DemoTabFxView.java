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

package com.techsenger.tabshell.demos.core.tab;

import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.core.shelltab.AbstractShellTabFxView;
import com.techsenger.tabshell.demos.core.dialog.DemoDialogFxView;
import com.techsenger.tabshell.demos.core.dialog.DemoDialogPresenter;
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
public class DemoTabFxView<P extends DemoTabPresenter<?, ?>> extends AbstractShellTabFxView<P> implements DemoTabView {

    public class Composer extends AbstractShellTabFxView.Composer implements DemoTabComposer {

        private final DemoTabFxView<?> view = DemoTabFxView.this;

        @Override
        public void addDemoDialog(DialogScope scope, boolean resizable) {
            var v = new DemoDialogFxView(resizable);
            var p = new DemoDialogPresenter(v, scope);
            p.initialize();
            view.getShell().getComposer().addDialog(v);
        }
    }

    private final CheckBox newValidCheckBox = new CheckBox("New Item Valid");

    private final CheckBox exitIncludedCheckBox = new CheckBox("Exit Item Included");

    private final CheckBox exitValidCheckBox = new CheckBox("Exit Item Valid");

    private final Button shellDialogButton = new Button("Shell Dialog");

    private final Button tabDialogButton = new Button("Tab Dialog");

    private final VBox vBox = new VBox(newValidCheckBox, exitIncludedCheckBox, exitValidCheckBox, shellDialogButton,
            tabDialogButton);

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
        getContentPane().setAlignment(Pos.CENTER);
        getContentPane().getChildren().add(hBox);

    }

    @Override
    protected void bind() {
        super.bind();
        shellDialogButton.prefWidthProperty().bind(vBox.widthProperty());
        tabDialogButton.prefWidthProperty().bind(vBox.widthProperty());
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        this.shellDialogButton.setOnAction(e -> getPresenter().handleDialogOpen(DialogScope.SHELL));
        this.tabDialogButton.setOnAction(e -> getPresenter().handleDialogOpen(DialogScope.TAB));
    }

    @Override
    protected Composer createComposer() {
        return new DemoTabFxView.Composer();
    }
}
