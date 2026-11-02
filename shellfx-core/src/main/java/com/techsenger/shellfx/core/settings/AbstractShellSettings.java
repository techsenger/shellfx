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

package com.techsenger.shellfx.core.settings;

import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class of the root of the settings tree. It keeps the listeners of the whole tree; the nodes of the tree know
 * nothing about them.
 *
 * @author Pavel Castornii
 */
public abstract class AbstractShellSettings implements ShellSettings {

    private static final Logger logger = LoggerFactory.getLogger(AbstractShellSettings.class);

    private final List<SettingsListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void addListener(SettingsListener listener) {
        Objects.requireNonNull(listener, "Listener can't be null");
        listeners.add(listener);
        logListeners("added");
    }

    @Override
    public void removeListener(SettingsListener listener) {
        if (listeners.remove(listener)) {
            logListeners("removed");
        }
    }

    @Override
    public void notifyListeners() {
        for (var listener : listeners) {
            try {
                listener.changed();
            } catch (Exception ex) {
                logger.error("Settings listener failed", ex);
            }
        }
    }

    private void logListeners(String action) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        var total = 0;
        var countsByName = new TreeMap<String, Integer>();
        for (var listener : listeners) {
            total++;
            var name = listener.getClass().getName();
            var slash = name.indexOf('/');
            if (slash >= 0) {
                name = name.substring(0, slash);
            }
            countsByName.merge(name.substring(name.lastIndexOf('.') + 1), 1, Integer::sum);
        }
        var message = new StringBuilder("Settings listener ").append(action)
                .append(", total: ").append(total);
        countsByName.forEach((name, count) -> message.append("\n    ").append(name).append(" x ").append(count));
        logger.debug(message.toString());
    }
}
