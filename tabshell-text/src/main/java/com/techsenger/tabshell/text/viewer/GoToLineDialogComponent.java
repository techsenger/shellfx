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

package com.techsenger.tabshell.text.viewer;

import com.techsenger.patternfx.core.Name;
import com.techsenger.tabshell.dialogs.simple.AbstractSimpleDialogComponent;
import com.techsenger.tabshell.text.TextComponentNames;

/**
 *
 * @author Pavel Castornii
 */
public class GoToLineDialogComponent<T extends GoToLineDialogView<?, ?>> extends AbstractSimpleDialogComponent<T> {

    public GoToLineDialogComponent(T view) {
        super(view);
    }

    @Override
    protected Mediator createMediator() {
        return new AbstractSimpleDialogComponent.Mediator() { };
    }

    @Override
    public Name getName() {
        return TextComponentNames.GO_TO_LINE_DIALOG;
    }

}
