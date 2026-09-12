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

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;

/**
 *
 * @author Pavel Castornii
 */
public class InsetEditorDialogViewModel<C extends EditorDialogComposer> extends AbstractEditorDialogViewModel<C> {

    private final StringProperty top = new SimpleStringProperty();

    private final StringProperty right = new SimpleStringProperty();

    private final StringProperty bottom = new SimpleStringProperty();

    private final StringProperty left = new SimpleStringProperty();

    public InsetEditorDialogViewModel(EditorDialogParams params) {
        super(params);
        setOnResult((button) -> {
            if (button == EditorDialogButtons.OK) {
                try {
                    var value = top.get() + "," + right.get() + "," + bottom.get() + "," + left.get();
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

    public String getTop() {
        return top.get();
    }

    public StringProperty topProperty() {
        return top;
    }

    public String getRight() {
        return right.get();
    }

    public StringProperty rightProperty() {
        return right;
    }

    public String getBottom() {
        return bottom.get();
    }

    public StringProperty bottomProperty() {
        return bottom;
    }

    public String getLeft() {
        return left.get();
    }

    public StringProperty leftProperty() {
        return left;
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        Insets insets = (Insets) getTask().getGetter().get();
        if (insets != null) {
            top.set(String.valueOf(insets.getTop()));
            right.set(String.valueOf(insets.getRight()));
            bottom.set(String.valueOf(insets.getBottom()));
            left.set(String.valueOf(insets.getLeft()));
        }
    }
}
