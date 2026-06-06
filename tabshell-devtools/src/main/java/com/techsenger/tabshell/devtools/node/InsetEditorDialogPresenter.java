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
import javafx.geometry.Insets;

/**
 *
 * @author Pavel Castornii
 */
public class InsetEditorDialogPresenter<V extends InsetEditorDialogView> extends AbstractEditorDialogPresenter<V> {

    private String top;

    private String right;

    private String bottom;

    private String left;

    public InsetEditorDialogPresenter(V view, EditorDialogParams params) {
        super(view, params);
        setOnResult((button) -> {
            if (button == EditorDialogButtons.OK) {
                try {
                    var value = top + "," + right + "," + bottom + "," + left;
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
        return top;
    }

    public void setTop(String top) {
        if (Objects.equals(this.top, top)) {
            return;
        }
        this.top = top;
        getView().setTop(top);
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        if (Objects.equals(this.right, right)) {
            return;
        }
        this.right = right;
        getView().setRight(right);
    }

    public String getBottom() {
        return bottom;
    }

    public void setBottom(String bottom) {
        if (Objects.equals(this.bottom, bottom)) {
            return;
        }
        this.bottom = bottom;
        getView().setBottom(bottom);
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        if (Objects.equals(this.left, left)) {
            return;
        }
        this.left = left;
        getView().setLeft(left);
    }

    protected void onTopChanged(String value) {
        this.top = value;
    }

    protected void onRightChanged(String value) {
        this.right = value;
    }

    protected void onBottomChanged(String value) {
        this.bottom = value;
    }

    protected void onLeftChanged(String value) {
        this.left = value;
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        Insets insets = (Insets) getTask().getGetter().get();
        if (insets != null) {
            setTop(String.valueOf(insets.getTop()));
            setRight(String.valueOf(insets.getRight()));
            setBottom(String.valueOf(insets.getBottom()));
            setLeft(String.valueOf(insets.getLeft()));
        }

    }
}
