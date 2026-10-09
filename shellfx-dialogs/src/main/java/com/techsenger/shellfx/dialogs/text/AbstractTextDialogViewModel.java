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
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.window.WindowComposer;
import com.techsenger.shellfx.material.RequestSetter;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.IndexRange;

/**
 * The part of a text dialog common to all of them: the text, whether it is editable and the part of it to select.
 *
 * @author Pavel Castornii
 */
public abstract class AbstractTextDialogViewModel<C extends WindowComposer> extends AbstractDialogViewModel<C>
        implements TextDialogPort {

    private final StringProperty text = new SimpleStringProperty("");

    private final BooleanProperty editable = new SimpleBooleanProperty(true);

    private final ReadOnlyObjectWrapper<IndexRange> selection = new ReadOnlyObjectWrapper<>();

    private final ObservableSource<@Nullable IndexRange> selectionSource = new SimpleObservableSource<>();

    public AbstractTextDialogViewModel(DialogParams params) {
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
    public String getText() {
        return text.get();
    }

    @Override
    public void setText(String text) {
        this.text.set(text);
    }

    @Override
    public StringProperty textProperty() {
        return text;
    }

    @Override
    public boolean isEditable() {
        return editable.get();
    }

    @Override
    public void setEditable(boolean editable) {
        this.editable.set(editable);
    }

    @Override
    public BooleanProperty editableProperty() {
        return editable;
    }

    @Override
    public IndexRange getSelection() {
        return selection.get();
    }

    @Override
    @RequestSetter
    public void setSelection(@Nullable IndexRange selection) {
        this.selectionSource.next(selection);
    }

    @Override
    public ReadOnlyObjectProperty<IndexRange> selectionProperty() {
        return selection.getReadOnlyProperty();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setRightButtons(TextDialogButtons.CANCEL, TextDialogButtons.OK);
        setButtonDefault(TextDialogButtons.OK, true);
    }

    ReadOnlyObjectWrapper<IndexRange> selectionWrapper() {
        return selection;
    }

    ObservableSource<@Nullable IndexRange> selectionSource() {
        return selectionSource;
    }
}
