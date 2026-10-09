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
import com.techsenger.shellfx.core.dialog.DialogPort;
import com.techsenger.shellfx.material.RequestSetter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.IndexRange;

/**
 * Provides full access to the component's client API. The other text dialogs extend it with their own members.
 *
 * @author Pavel Castornii
 */
public interface TextDialogPort extends DialogPort {

    String getText();

    void setText(String text);

    StringProperty textProperty();

    boolean isEditable();

    void setEditable(boolean editable);

    BooleanProperty editableProperty();

    IndexRange getSelection();

    /**
     * Requests selecting the given part of the text; the actual selection is reported by the property. Without a
     * request nothing is selected.
     *
     * @param selection the start and the end of the part to select, or {@code null} to clear the selection.
     */
    @RequestSetter
    void setSelection(@Nullable IndexRange selection);

    ReadOnlyObjectProperty<IndexRange> selectionProperty();
}
