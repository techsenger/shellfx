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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class of a configuration: a plain serializable object with what a component writes by itself while it works
 * (dialog sizes, recent queries, view modes), as opposed to settings that the user edits deliberately. Extended by
 * a subclass per level, the way components are. Always used on the UI thread.
 *
 * <p>The key difference between configuration and history is that the configuration is updated while the component is
 * running, together with changes to the component’s own properties, whereas history was updated during component
 * deinitialization.
 *
 * <p>Whoever changes a config calls {@link #notifyListeners()} once after making all its changes, so listeners
 * hear about the batch once and then read the current values; they are not told what changed, except for an
 * optional hint that they must not depend on. A config is saved by its manager, not when it changes.
 *
 * <p>When an older file is read, a field added since then holds the Java zero value, not its initializer's value.
 *
 * @author Pavel Castornii
 */
public abstract class AbstractConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(AbstractConfig.class);

    private transient List<ConfigListener> listeners = new ArrayList<>();

    /**
     * Registers a listener. A config lives as long as the application, so a listener owned by a shorter-lived
     * component must be removed when that component is gone, otherwise the manager reports it as a leak.
     *
     * @param listener the listener to notify.
     */
    public void addListener(ConfigListener listener) {
        Objects.requireNonNull(listener, "Listener can't be null");
        listeners.add(listener);
    }

    /**
     * Unregisters a listener; does nothing if it is not registered.
     *
     * @param listener the listener to remove.
     */
    public void removeListener(ConfigListener listener) {
        listeners.remove(listener);
    }

    /**
     * Tells every listener that this config has changed, without a hint, so they re-read everything.
     */
    public void notifyListeners() {
        notifyListeners(null);
    }

    /**
     * Tells every listener that this config has changed. A listener that fails is logged and does not prevent the
     * others from being notified.
     *
     * @param hint a value passed to the listeners as is, e.g. the caller itself, so it can ignore its own change,
     *     or what was changed; {@code null} means anything may have changed.
     */
    public void notifyListeners(@Nullable Object hint) {
        for (var listener : listeners) {
            if (!listeners.contains(listener)) {
                continue;
            }
            try {
                listener.changed(hint);
            } catch (RuntimeException ex) {
                logger.warn("Config listener failed", ex);
            }
        }
    }

    final List<ConfigListener> getListeners() {
        return List.copyOf(listeners);
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        listeners = new ArrayList<>();
    }
}
