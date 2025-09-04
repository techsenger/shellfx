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

package com.techsenger.tabshell.core.history;

import com.techsenger.mvvm4fx.core.ComponentHistory;
import com.techsenger.mvvm4fx.core.HistoryProvider;
import com.techsenger.toolkit.core.function.Factory;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultClassHistoryProvider<T extends ComponentHistory<?>> implements HistoryProvider {

    private final HistoryManager manager;

    private final Class<T> clazz;

    private final Factory<T> factory;

    public DefaultClassHistoryProvider(HistoryManager manager, Class<T> clazz, Factory<T> factory) {
        this.manager = manager;
        this.clazz = clazz;
        this.factory = factory;
    }

    @Override
    public T provide() {
        var history = this.manager.getHistory(this.clazz);
        if (history == null) {
            history = factory.create();
            history.setDefaultValues();
            this.manager.putHistory(clazz, history);
        }
        return history;
    }
}
