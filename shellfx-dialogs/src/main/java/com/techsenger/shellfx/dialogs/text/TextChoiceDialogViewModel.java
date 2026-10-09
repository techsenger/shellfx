/*
 * Copyright 2026 Pavel Castornii.
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

package com.techsenger.shellfx.dialogs.text;

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.dialog.AbstractDialogViewModel;
import com.techsenger.shellfx.core.window.WindowComposer;
import java.util.function.Consumer;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Lets the user choose one of the given texts; unlike the texts dialog, nothing can be typed.
 *
 * @author Pavel Castornii
 */
public class TextChoiceDialogViewModel<C extends WindowComposer> extends AbstractDialogViewModel<C>
        implements TextChoiceDialogPort {

    private final StringProperty text = new SimpleStringProperty();

    private final ObservableList<String> texts = FXCollections.observableArrayList();

    public TextChoiceDialogViewModel(TextChoiceDialogParams params) {
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
    public @Nullable String getText() {
        return text.get();
    }

    @Override
    public void setText(@Nullable String text) {
        this.text.set(text);
    }

    @Override
    public StringProperty textProperty() {
        return text;
    }

    @Override
    public ObservableList<String> getTexts() {
        return texts;
    }

    @Override
    protected TextChoiceDialogConfig getConfig() {
        return (TextChoiceDialogConfig) super.getConfig();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle("Text Choice");
        setRightButtons(TextDialogButtons.CANCEL, TextDialogButtons.OK);
        setButtonDefault(TextDialogButtons.OK, true);
    }
}
