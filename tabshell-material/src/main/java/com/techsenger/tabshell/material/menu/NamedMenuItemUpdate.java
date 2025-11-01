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

package com.techsenger.tabshell.material.menu;

import javafx.scene.Node;

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

    private Node graphic;

    private boolean textChanged;

    private boolean graphicChanged;

    protected NamedMenuItemUpdate() {

    }

    public boolean isTextChanged() {
        return textChanged;
    }

    public boolean isGraphicChanged() {
        return graphicChanged;
    }

    public String getText() {
        return text;
    }

    public Node getGraphic() {
        return graphic;
    }

    void setTextChanged(boolean textChanged) {
        this.textChanged = textChanged;
    }

    void setGraphicChanged(boolean graphicChanged) {
        this.graphicChanged = graphicChanged;
    }

    void setText(String text) {
        this.text = text;
    }

    void setGraphic(Node graphic) {
        this.graphic = graphic;
    }
}
