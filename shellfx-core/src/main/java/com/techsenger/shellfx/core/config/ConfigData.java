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

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * All configs held by a {@link ConfigManager}, in the form they are written to a file.
 *
 * @author Pavel Castornii
 */
final class ConfigData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<Class<? extends AbstractConfig>, AbstractConfig> configsByClass = new HashMap<>();

    private final Map<UUID, AbstractConfig> configsByUuid = new HashMap<>();

    Map<Class<? extends AbstractConfig>, AbstractConfig> getConfigsByClass() {
        return configsByClass;
    }

    Map<UUID, AbstractConfig> getConfigsByUuid() {
        return configsByUuid;
    }

    @Override
    public String toString() {
        return "ConfigData [configsByClass: " + configsByClass + ", configsByUuid: " + configsByUuid + ']';
    }
}
