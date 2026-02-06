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

package com.techsenger.tabshell.material.button;

import javafx.scene.Node;
import javafx.scene.control.Button;

/**
 *
 * @author Pavel Castornii
 */
public class ResultButton extends Button {

    private final ResultButtonName name;

    public ResultButton(ResultButtonName name) {
        this(name, null);
    }

    public ResultButton(ResultButtonName name, String string) {
        this(name, string, null);
    }

    public ResultButton(ResultButtonName name, String string, Node node) {
        super(string, node);
        this.name = name;
    }

    public ResultButtonName getName() {
        return name;
    }
}
