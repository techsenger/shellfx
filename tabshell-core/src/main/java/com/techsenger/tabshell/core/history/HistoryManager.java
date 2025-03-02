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
import com.techsenger.toolkit.core.function.Factory;

/**
 * {@link HistoryManager} is interface and there can be different implementations. For example, it is possible to use
 * different {@link HistoryFile} when one file is used for one layer.
 *
 * @author Pavel Castornii
 */
public interface HistoryManager {

    <T extends ComponentHistory> T getHistory(Class<T> historyClass, Factory<T> factory);
}
