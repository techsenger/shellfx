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

package com.techsenger.shellfx.dialogs.progress;

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.shellfx.core.CloseCheckResult;
import com.techsenger.shellfx.core.ClosePreparationResult;
import com.techsenger.shellfx.core.dialog.AbstractDialogPresenter;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.dialogs.DialogComponents;
import java.util.Objects;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class ProgressDialogPresenter extends AbstractDialogPresenter<ProgressDialogView> implements ProgressDialogPort {

    private boolean stepsVisible;

    private int stepCount;

    private int currentStep;

    private String message;

    private double progress;

    public ProgressDialogPresenter(ProgressDialogView view, DialogParams params) {
        super(view, params);
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(DialogComponents.PLUGIN_PROGRESS_DIALOG);
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> cnsmr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isStepsVisible() {
        return stepsVisible;
    }

    @Override
    public void setStepsVisible(boolean stepsVisible) {
        if (this.stepsVisible == stepsVisible) {
            return;
        }
        this.stepsVisible = stepsVisible;
        getView().updateStepsVisible(stepsVisible);
    }

    @Override
    public int getStepCount() {
        return stepCount;
    }

    @Override
    public void setStepCount(int stepCount) {
        if (this.stepCount == stepCount) {
            return;
        }
        this.stepCount = stepCount;
        getView().updateStepCount(stepCount);
    }

    @Override
    public int getCurrentStep() {
        return currentStep;
    }

    @Override
    public void setCurrentStep(int currentStep) {
        if (this.currentStep == currentStep) {
            return;
        }
        this.currentStep = currentStep;
        getView().updateCurrentStep(currentStep);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(String message) {
        if (Objects.equals(this.message, message)) {
            return;
        }
        this.message = message;
        getView().updateMessage(message);
    }

    @Override
    public double getProgress() {
        return progress;
    }

    @Override
    public void setProgress(double progress) {
        if (this.progress == progress) {
            return;
        }
        this.progress = progress;
        getView().updateProgress(progress);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setWidth(450);
        setTitle("Progress Dialog");
    }
}
