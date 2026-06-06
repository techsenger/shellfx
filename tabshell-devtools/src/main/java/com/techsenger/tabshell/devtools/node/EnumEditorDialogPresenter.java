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

package com.techsenger.tabshell.devtools.node;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author Pavel Castornii
 */
public class EnumEditorDialogPresenter<V extends EnumEditorDialogView> extends AbstractEditorDialogPresenter<V> {

    private List<String> values;

    private String value;

    public EnumEditorDialogPresenter(V view, EditorDialogParams params) {
        super(view, params);
        setOnResult((button) -> {
            if (button == EditorDialogButtons.OK) {
                try {
                    applyValue(getTask(), value);
                    closeSafely();
                } catch (Exception ex) {
                    openErrorDialog();
                }
            } else {
                closeSafely();
            }
        });
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        List<String> valueList = Arrays.stream(getTask().getType().getEnumConstants())
                .map(o -> ((Enum<?>) o).name())
                .collect(Collectors.toList());
        setValues(valueList);
        Enum<?> e = (Enum<?>) getTask().getGetter().get();
        setValue(e.name());
    }

    protected void onValueSelected(String value) {
        this.value = value;
    }

    protected void setValues(List<String> values) {
        this.values = values;
        getView().setValues(values);
    }

    protected List<String> getValues() {
        return values;
    }

    protected String getValue() {
        return value;
    }

    protected void setValue(String value) {
        if (Objects.equals(this.value, value)) {
            return;
        }
        this.value = value;
        getView().setValue(value);
    }
}
