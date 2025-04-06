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

package com.techsenger.tabshell.dialogs;

import com.techsenger.tabshell.core.dialog.AbstractDialogViewModel;
import com.techsenger.tabshell.core.dialog.DialogScope;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractSimpleDialogViewModel extends AbstractDialogViewModel {

    private final ObjectProperty<Runnable> okAction = new SimpleObjectProperty<>(this.closeActionProperty().get());

    private final ObjectProperty<Runnable> cancelAction = new SimpleObjectProperty<>(this.closeActionProperty().get());

    private final BooleanProperty okDisable = new SimpleBooleanProperty();

    private final BooleanProperty cancelDisable = new SimpleBooleanProperty();

    public AbstractSimpleDialogViewModel(DialogScope scope, boolean resizable) {
        super(scope, resizable);
    }

    public ObjectProperty<Runnable> okActionProperty() {
        return okAction;
    }

    public ObjectProperty<Runnable> cancelActionProperty() {
        return cancelAction;
    }

    public BooleanProperty okDisableProperty() {
        return okDisable;
    }

    public BooleanProperty cancelDisableProperty() {
        return cancelDisable;
    }

    @Override
    public StandardDialogHelper<?> getComponentHelper() {
        return (StandardDialogHelper<?>) super.getComponentHelper();
    }
}
