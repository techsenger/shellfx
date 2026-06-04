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

package com.techsenger.tabshell.dialogs.progress;

import com.techsenger.tabshell.core.dialog.AbstractDialogFxView;
import com.techsenger.tabshell.material.button.ResultButton;
import com.techsenger.tabshell.material.style.Spacing;
import com.techsenger.toolkit.fx.Spacer;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class ProgressDialogFxView extends AbstractDialogFxView<ProgressDialogPresenter>
        implements ProgressDialogView {

    private final Label messageLabel = new Label();

    private final Label stepLabel = new Label();

    private final HBox textBox = new HBox(messageLabel, new Spacer(Orientation.HORIZONTAL), stepLabel);

    private final ProgressBar progressBar = new ProgressBar();

    private final VBox mainBox = new VBox(Spacing.getVertical() * 3, textBox, progressBar);

    private final ResultButton cancelButton = new ResultButton(ProgressDialogButtons.CANCEL, "Cancel");

    @Override
    public void requestFocus() {

    }

    @Override
    public void setMessage(String text) {
        messageLabel.setText(text);
    }

    @Override
    public void showSteps(int currentStep, int stepCount) {
        var text = "[" + String.valueOf(currentStep) + " / " + String.valueOf(stepCount) + "]";
        stepLabel.setText(text);
    }

    @Override
    public void setShowSteps(boolean value) {
        if (value && stepLabel.getParent() == null) {
            textBox.getChildren().add(stepLabel);
        } else if (!value && stepLabel.getParent() != null) {
            textBox.getChildren().remove(stepLabel);
        }
    }

    @Override
    public void setProgress(double value) {
        progressBar.setProgress(value);
    }

    @Override
    protected void build() {
        super.build();
        progressBar.setMaxWidth(Double.MAX_VALUE);
        getContentBox().getChildren().add(mainBox);
        registerButtons(cancelButton);
    }

    protected Label getMessageLabel() {
        return messageLabel;
    }

    protected Label getStepLabel() {
        return stepLabel;
    }

    protected HBox getTextBox() {
        return textBox;
    }

    protected ProgressBar getProgressBar() {
        return progressBar;
    }

    protected VBox getMainBox() {
        return mainBox;
    }

    protected ResultButton getCancelButton() {
        return cancelButton;
    }
}
