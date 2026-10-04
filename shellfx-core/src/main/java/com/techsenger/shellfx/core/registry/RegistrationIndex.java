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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Files registrations under the class of the component they target, and resolves the registrations that apply to
 * an actual component instance by walking that instance's own class, its superclasses, and its interfaces, so a
 * registration filed under a base view type is picked up by every subtype.
 *
 * <p>The resolved set is cached by {@link Class#getName()} rather than by the {@link Class} object itself: a plugin
 * module is typically loaded through its own {@code ClassLoader}/JPMS layer, and a {@code Class} holds a strong
 * reference to its defining loader (and, transitively, to everything else that loader defined) — so keeping
 * {@code Class} objects themselves in a cache that outlives the plugin would leak the whole layer. The cache is
 * invalidated on every registration change.
 *
 * @param <R> the type of the registrations
 * @author Pavel Castornii
 */
final class RegistrationIndex<R extends AbstractRegistration> {

    private final Map<Class<?>, Set<R>> registrationsByClass = new ConcurrentHashMap<>();

    private final Map<String, Set<R>> resolvedByClassName = new ConcurrentHashMap<>();

    /**
     * Files {@code registration} under {@code componentClass} and makes it undoable.
     */
    void add(Class<?> componentClass, R registration) {
        registrationsByClass.compute(componentClass, (k, registrations) -> {
            var result = registrations == null ? ConcurrentHashMap.<R>newKeySet() : registrations;
            result.add(registration);
            return result;
        });
        resolvedByClassName.clear();
        registration.setUnregister(() -> {
            registrationsByClass.computeIfPresent(componentClass, (k, registrations) -> {
                registrations.remove(registration);
                return registrations.isEmpty() ? null : registrations;
            });
            resolvedByClassName.clear();
        });
    }

    /**
     * Returns the registrations filed exactly under {@code componentClass}.
     */
    Set<R> get(Class<?> componentClass) {
        return registrationsByClass.getOrDefault(componentClass, Set.of());
    }

    /**
     * Returns every registration applicable to {@code instance}; recomputed lazily the first time a given class is
     * seen after a registry change.
     */
    Set<R> resolve(Object instance) {
        var type = instance.getClass();
        return resolvedByClassName.computeIfAbsent(type.getName(), n -> {
            var result = new HashSet<R>();
            collect(type, result, new HashSet<>());
            return result;
        });
    }

    private void collect(Class<?> type, Set<R> result, Set<Class<?>> visited) {
        if (type == null || !visited.add(type)) {
            return;
        }
        result.addAll(registrationsByClass.getOrDefault(type, Set.of()));
        collect(type.getSuperclass(), result, visited);
        for (var iface : type.getInterfaces()) {
            collect(iface, result, visited);
        }
    }
}
