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

package com.techsenger.tabshell.dialogs.yesno;

import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.tabshell.core.dialog.AbstractDialogComponent;
import com.techsenger.tabshell.dialogs.DialogComponentNames;

/**
 *
 * @author Pavel Castornii
 */
public class YesNoDialogComponent<T extends YesNoDialogView<?, ?>> extends AbstractDialogComponent<T> {

    protected class Mediator extends AbstractDialogComponent.Mediator implements YesNoDialogMediator {

    }

    public YesNoDialogComponent(T view) {
        super(view);
    }

    @Override
    public ComponentName getName() {
        return DialogComponentNames.YES_NO_DIALOG;
    }

    @Override
    protected Mediator createMediator() {
        return new Mediator();
    }
}
