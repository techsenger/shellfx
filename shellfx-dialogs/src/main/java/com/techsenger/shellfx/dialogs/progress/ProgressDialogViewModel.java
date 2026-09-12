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

import com.techsenger.shellfx.core.CloseCheckResult;
import com.techsenger.shellfx.core.ClosePreparationResult;
import com.techsenger.shellfx.core.dialog.AbstractDialogViewModel;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.window.WindowComposer;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public class ProgressDialogViewModel<C extends WindowComposer> extends AbstractDialogViewModel<C>
        implements FullProgressDialogPort {

    private final BooleanProperty stepsVisible = new SimpleBooleanProperty();

    private final IntegerProperty stepCount = new SimpleIntegerProperty();

    private final IntegerProperty currentStep = new SimpleIntegerProperty();

    private final StringProperty message = new SimpleStringProperty();

    private final DoubleProperty progress = new SimpleDoubleProperty();

    public ProgressDialogViewModel(DialogParams params) {
        super(params);
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isStepsVisible() {
        return stepsVisible.get();
    }

    @Override
    public void setStepsVisible(boolean stepsVisible) {
        this.stepsVisible.set(stepsVisible);
    }

    @Override
    public BooleanProperty stepsVisibleProperty() {
        return stepsVisible;
    }

    @Override
    public int getStepCount() {
        return stepCount.get();
    }

    @Override
    public void setStepCount(int stepCount) {
        this.stepCount.set(stepCount);
    }

    @Override
    public IntegerProperty stepCountProperty() {
        return stepCount;
    }

    @Override
    public int getCurrentStep() {
        return currentStep.get();
    }

    @Override
    public void setCurrentStep(int currentStep) {
        this.currentStep.set(currentStep);
    }

    @Override
    public IntegerProperty currentStepProperty() {
        return currentStep;
    }

    @Override
    public String getMessage() {
        return message.get();
    }

    @Override
    public void setMessage(String message) {
        this.message.set(message);
    }

    @Override
    public StringProperty messageProperty() {
        return message;
    }

    @Override
    public double getProgress() {
        return progress.get();
    }

    @Override
    public void setProgress(double progress) {
        this.progress.set(progress);
    }

    @Override
    public DoubleProperty progressProperty() {
        return progress;
    }

    @Override
    protected void applyPersistentState() {
        super.applyPersistentState();
        setWidth(450);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle("Progress Dialog");
    }
}
