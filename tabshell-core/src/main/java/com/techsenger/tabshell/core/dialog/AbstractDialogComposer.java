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

package com.techsenger.tabshell.core.dialog;

import com.techsenger.tabshell.core.area.AbstractAreaComposer;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractDialogComposer<T extends AbstractDialogView<?>> extends AbstractAreaComposer<T>
        implements DialogComposer<T> {

    protected abstract class Mediator extends AbstractAreaComposer.Mediator implements DialogMediator {

    }

    public AbstractDialogComposer(T view) {
        super(view);
    }

    @Override
    public DialogMediator getMediator() {
        return (DialogMediator) super.getMediator();
    }
}
