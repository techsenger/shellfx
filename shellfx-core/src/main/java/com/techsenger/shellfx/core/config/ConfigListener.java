/*
 * Copyright 2026 Pavel Castornii.
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

package com.techsenger.shellfx.core.config;

import com.techsenger.annotations.Nullable;

/**
 * Told that a config has changed. It does not say what changed: the listener reads the current state of the config
 * it is registered on.
 *
 * @author Pavel Castornii
 */
@FunctionalInterface
public interface ConfigListener {

    /**
     * Called once per announced batch of changes.
     *
     * @param hint something from whoever changed the config, e.g. itself or what it changed; it is only an
     *     optimization, {@code null} means anything may have changed and everything must be re-read.
     */
    void changed(@Nullable Object hint);
}
