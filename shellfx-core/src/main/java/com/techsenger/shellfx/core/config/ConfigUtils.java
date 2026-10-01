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

package com.techsenger.shellfx.core.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.ObjDoubleConsumer;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author Pavel Castornii
 */
public final class ConfigUtils {

    private static final int COLLECTION_MAX_SIZE = 25;

    /**
     * Limits the collection to the specified maximum size. If the collection exceeds the max size, it will be
     * truncated.
     *
     * @param col
     */
    public static void limit(Collection<?> col) {
        if (col.size() > COLLECTION_MAX_SIZE) {
            var iterator = col.iterator();
            var index = 0;
            while (iterator.hasNext()) {
                iterator.next();
                if (index >= COLLECTION_MAX_SIZE) {
                    iterator.remove();
                }
                index++;
            }
        }
    }

    /**
     * Adds value to head of the list and checks if this value is not present in the list somewhere else.
     *
     * @param <T>
     * @param list
     * @param value
     */
    public static <T> void addFirst(List<T> list, T value) {
        list.remove(value);
        list.add(0, value);
    }

    /**
     * Writes every subsequent change of the value into the config through the setter and then notifies the config
     * listeners. The current value is not written; only changes are.
     *
     * @param <T> the type of the value.
     * @param <C> the type of the config.
     * @param value the observed value.
     * @param config the config to write to and notify after each write.
     * @param setter the config setter that receives the config and the new value.
     */
    public static <T, C extends AbstractConfig> void observe(ObservableValue<T> value, C config,
            BiConsumer<C, ? super T> setter) {
        value.addListener((ov, oldV, newV) -> {
            setter.accept(config, newV);
            config.notifyListeners();
        });
    }

    /**
     * Same as {@link #observe(ObservableValue, AbstractConfig, BiConsumer)} for a {@code double} value, so that a
     * setter taking a primitive {@code double} can be passed as is.
     *
     * @param <C> the type of the config.
     * @param value the observed value.
     * @param config the config to write to and notify after each write.
     * @param setter the config setter that receives the config and the new value.
     */
    public static <C extends AbstractConfig> void observe(ObservableDoubleValue value, C config,
            ObjDoubleConsumer<C> setter) {
        value.addListener((ov, oldV, newV) -> {
            setter.accept(config, newV.doubleValue());
            config.notifyListeners();
        });
    }

    /**
     * Returns the classes of the listeners registered on the config, e.g. to report the ones that are still
     * registered when the config is saved.
     *
     * @param config the config to inspect.
     * @return the classes in the order the listeners were registered; one class per listener.
     */
    public static List<Class<?>> getListenerClasses(AbstractConfig config) {
        var classes = new ArrayList<Class<?>>();
        for (var listener : config.getListeners()) {
            classes.add(listener.getClass());
        }
        return classes;
    }

    private ConfigUtils() {
        //empty
    }
}
