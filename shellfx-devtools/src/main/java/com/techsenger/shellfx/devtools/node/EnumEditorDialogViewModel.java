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

package com.techsenger.shellfx.devtools.node;

import com.techsenger.annotations.Unmodifiable;
import java.util.Arrays;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class EnumEditorDialogViewModel<C extends EditorDialogComposer> extends AbstractEditorDialogViewModel<C> {

    private final StringProperty value = new SimpleStringProperty();

    private final ObservableList<String> modifiableValues = FXCollections.observableArrayList();

    private final ObservableList<String> values = FXCollections.unmodifiableObservableList(modifiableValues);

    public EnumEditorDialogViewModel(EditorDialogParams params) {
        super(params);
        setOnResult((button) -> {
            if (button == EditorDialogButtons.OK) {
                try {
                    applyValue(getTask(), value.get());
                    closeSafely();
                } catch (Exception ex) {
                    openErrorDialog();
                }
            } else {
                closeSafely();
            }
        });
    }

    public @Unmodifiable ObservableList<String> getValues() {
        return values;
    }

    public String getValue() {
        return value.get();
    }

    public StringProperty valueProperty() {
        return value;
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        modifiableValues.setAll(Arrays.stream(getTask().getType().getEnumConstants())
                .map(o -> ((Enum<?>) o).name())
                .collect(Collectors.toList()));
        Enum<?> e = (Enum<?>) getTask().getGetter().get();
        value.set(e.name());
    }
}
