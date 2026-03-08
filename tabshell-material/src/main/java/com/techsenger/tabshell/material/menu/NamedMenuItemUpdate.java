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

package com.techsenger.tabshell.material.menu;

import com.techsenger.tabshell.material.icon.Icon;

/**
 * As new value can be null we need *changed fields to detect what properties were changed.
 *
 * @author Pavel Castornii
 */
public class NamedMenuItemUpdate {

    public static class Builder extends AbstractChangeBuilder<NamedMenuItemUpdate, Builder> {

        public Builder() {
            super(new NamedMenuItemUpdate());
        }
    }

    private String text;

    private Icon<?> icon;

    private boolean textChanged;

    private boolean iconChanged;

    protected NamedMenuItemUpdate() {

    }

    public boolean isTextChanged() {
        return textChanged;
    }

    public boolean isIconChanged() {
        return iconChanged;
    }

    public String getText() {
        return text;
    }

    public Icon<?> getIcon() {
        return icon;
    }

    void setTextChanged(boolean textChanged) {
        this.textChanged = textChanged;
    }

    void setIconChanged(boolean iconChanged) {
        this.iconChanged = iconChanged;
    }

    void setText(String text) {
        this.text = text;
    }

    void setIcon(Icon<?> icon) {
        this.icon = icon;
    }
}
