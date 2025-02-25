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

package com.techsenger.tabshell.demo.tab;

import com.techsenger.mvvm4fx.core.ComponentHelper;
import com.techsenger.tabshell.core.TabShellView;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.core.style.SizeConstants;
import com.techsenger.tabshell.core.tab.AbstractShellTabView;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class DemoTabView extends AbstractShellTabView<DemoTabViewModel> {

    private final CheckBox newValidCheckBox = new CheckBox("New Item Valid");

    private final CheckBox exitIncludedCheckBox = new CheckBox("Exit Item Included");

    private final CheckBox exitValidCheckBox = new CheckBox("Exit Item Valid");

    private final Button shellDialogButton = new Button("Shell Dialog");

    private final Button tabDialogButton = new Button("Tab Dialog");

    private final VBox vBox = new VBox(newValidCheckBox, exitIncludedCheckBox, exitValidCheckBox, shellDialogButton,
            tabDialogButton);

    private final HBox hBox = new HBox(vBox);

    public DemoTabView(TabShellView<?> tabShell, DemoTabViewModel viewModel) {
        super(tabShell, viewModel);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public boolean doOnCloseRequest() {
        return true;
    }

    @Override
    protected void build(DemoTabViewModel viewModel) {
        super.build(viewModel);
        hBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(SizeConstants.INSET);
        getContentPane().setAlignment(Pos.CENTER);
        getContentPane().getChildren().add(hBox);

    }

    @Override
    protected void bind(DemoTabViewModel viewModel) {
        super.bind(viewModel);
        newValidCheckBox.selectedProperty().bindBidirectional(viewModel.newValidProperty());
        exitIncludedCheckBox.selectedProperty().bindBidirectional(viewModel.exitIncludedProperty());
        exitValidCheckBox.selectedProperty().bindBidirectional(viewModel.exitValidProperty());
        shellDialogButton.prefWidthProperty().bind(vBox.widthProperty());
        tabDialogButton.prefWidthProperty().bind(vBox.widthProperty());
    }

    @Override
    protected void addHandlers(DemoTabViewModel viewModel) {
        super.addHandlers(viewModel);
        this.shellDialogButton.setOnAction(e -> viewModel.openDialog(DialogScope.SHELL));
        this.tabDialogButton.setOnAction(e -> viewModel.openDialog(DialogScope.TAB));
    }

    @Override
    protected ComponentHelper<?> createComponentHelper() {
        return new DemoTabHelper(this);
    }
}
