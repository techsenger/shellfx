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

package com.techsenger.tabshell.core.tab;

import com.techsenger.patternfx.mvvmx.AbstractChildComponent;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractTabComponent<T extends AbstractTabView<?, ?>> extends
        AbstractChildComponent<T> implements TabComponent<T> {

    protected class Mediator extends AbstractChildComponent.Mediator implements TabMediator {

        @Override
        public void remove() {
            ((TabContainerComponent<?, TabComponent<?>>) AbstractTabComponent.this.getParent())
                    .removeTab(AbstractTabComponent.this);
        }
    }

    public AbstractTabComponent(T view) {
        super(view);
    }

    @Override
    protected abstract Mediator createMediator();
}
