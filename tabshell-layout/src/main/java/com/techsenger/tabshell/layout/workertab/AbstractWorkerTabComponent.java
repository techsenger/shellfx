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

package com.techsenger.tabshell.layout.workertab;

import com.techsenger.tabshell.core.ShellComponent;
import com.techsenger.tabshell.layout.splittab.AbstractSplitTabComponent;
import com.techsenger.tabshell.layout.tabhost.TabHostComponent;
import com.techsenger.tabshell.layout.tabhost.TabHostView;
import com.techsenger.tabshell.layout.tabhost.TabHostViewModel;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractWorkerTabComponent<T extends AbstractWorkerTabView<?, ?>>
        extends AbstractSplitTabComponent<T> {

    protected class Mediator extends AbstractSplitTabComponent.Mediator implements WorkerTabMediator {

    }

    public AbstractWorkerTabComponent(T view, ShellComponent<?> shell) {
        super(view, shell);
    }

    protected TabHostComponent<?> createTabHost() {
        var vm = new TabHostViewModel();
        var v = new TabHostView<>(vm);
        var c = new TabHostComponent<>(v);
        c.initialize();
        return c;
    }

    @Override
    protected abstract Mediator createMediator();
}
