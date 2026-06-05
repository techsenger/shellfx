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

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class TextEditorDialogPresenter<V extends TextEditorDialogView> extends AbstractEditorDialogPresenter<V> {

    private static final Logger logger = LoggerFactory.getLogger(TextEditorDialogPresenter.class);

    private String value;

    public TextEditorDialogPresenter(V view, EditorDialogParams params) {
        super(view, params);
        setResultAction((button) -> {
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

    protected void setValue(String value) {
        if (Objects.equals(this.value, value)) {
            return;
        }
        this.value = value;
        getView().setValue(value);
    }

    protected String getValue() {
        return value;
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setValue(String.valueOf(getTask().getGetter().get()));
    }

    protected void onValueChanged(String value) {
        this.value = value;
    }
}
