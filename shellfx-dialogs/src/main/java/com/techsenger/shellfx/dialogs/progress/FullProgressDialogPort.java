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

import com.techsenger.shellfx.core.dialog.FullDialogPort;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

/**
 * Provides full access to the component's client API.
 *
 * @author Pavel Castornii
 */
public interface FullProgressDialogPort extends ProgressDialogPort, FullDialogPort {

    void setMessage(String text);

    @Override
    StringProperty messageProperty();

    void setStepsVisible(boolean value);

    @Override
    BooleanProperty stepsVisibleProperty();

    void setStepCount(int count);

    @Override
    IntegerProperty stepCountProperty();

    void setCurrentStep(int step);

    @Override
    IntegerProperty currentStepProperty();

    void setProgress(double value);

    @Override
    DoubleProperty progressProperty();
}
