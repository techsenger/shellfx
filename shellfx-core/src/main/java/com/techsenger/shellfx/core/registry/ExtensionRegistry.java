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

package com.techsenger.shellfx.core.registry;

/**
 * A registry of the contributions of plugins to the components, which can change at any time. It is not synchronized
 * and promises nothing about threads: its listeners are called in the thread that changed it. How a listener is told
 * about a change depends on the kind of the registry: see {@link StateRegistry} and
 * {@link ItemRegistry}.
 *
 * @author Pavel Castornii
 */
public interface ExtensionRegistry {
    // marker
}
