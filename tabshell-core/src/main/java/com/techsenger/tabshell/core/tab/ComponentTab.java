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

package com.techsenger.tabshell.core.tab;

import javafx.scene.Node;
import javafx.scene.control.Tab;

/**
 *
 * @author Pavel Castornii
 */
public class ComponentTab extends Tab  {

    private final TabFxView<?> view;

    public ComponentTab(TabFxView<?> view) {
        this.view = view;
    }

    public ComponentTab(TabFxView<?> view, String string) {
        super(string);
        this.view = view;
    }

    public ComponentTab(TabFxView<?> view, String string, Node node) {
        super(string, node);
        this.view = view;
    }

    public TabFxView<?> getView() {
        return view;
    }
}
