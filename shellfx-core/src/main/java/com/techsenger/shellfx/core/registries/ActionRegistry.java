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

package com.techsenger.shellfx.core.registries;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.shellfx.core.action.Action;
import com.techsenger.shellfx.core.action.ActionName;
import com.techsenger.shellfx.base.registry.AbstractItemRegistry;
import com.techsenger.shellfx.base.registry.Registration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of the actions a user can do, whose hotkeys the user can reassign. A module registers the actions it
 * declares as one contribution, each with the hotkey the user has already chosen (if any) applied, and controls find
 * an action by its name and bind to its hotkey. The registry holds the very objects the module gives it, so a change
 * of a hotkey through the registry is seen by the module.
 *
 * <p>The registry tells its listeners which actions have been added and removed. It does not save the choices of the
 * user: the module applies the saved choices before the registration and saves them after the registration is undone.
 *
 * <p>It can be changed from any thread.
 *
 * @author Pavel Castornii
 */
public final class ActionRegistry extends AbstractItemRegistry<Action> {

    private final class ActionRegistration implements Registration {

        private final List<Action> actions;

        ActionRegistration(List<Action> actions) {
            this.actions = actions;
        }

        @Override
        public void unregister() {
            var removed = new ArrayList<Action>();
            synchronized (ActionRegistry.this) {
                for (var action : actions) {
                    if (actionsByName.get(action.getName()) == action) {
                        actionsByName.remove(action.getName());
                        removed.add(action);
                    }
                }
            }
            notifyChanged(List.of(), List.copyOf(removed));
        }
    }

    private final Map<ActionName, Action> actionsByName = new LinkedHashMap<>();

    /**
     * Registers an action.
     *
     * @param action the action
     * @return a {@link Registration} that can be used to undo this registration
     * @throws IllegalStateException if an action with the same name is already registered
     */
    public Registration register(Action action) {
        return register(List.of(action));
    }

    /**
     * Registers actions as one contribution, undone as a whole. Either all of them are registered or, if the name of
     * one is already taken, none.
     *
     * @param actions the actions
     * @return a {@link Registration} that can be used to undo this contribution
     * @throws IllegalStateException if an action with the same name is already registered or two of the actions have
     *     the same name
     */
    public Registration register(Collection<Action> actions) {
        var added = List.copyOf(actions);
        synchronized (this) {
            var names = new HashSet<ActionName>();
            for (var action : added) {
                if (actionsByName.containsKey(action.getName()) || !names.add(action.getName())) {
                    throw new IllegalStateException("Action is already registered: " + action.getName());
                }
            }
            for (var action : added) {
                actionsByName.put(action.getName(), action);
            }
        }
        // the listeners are told after the registration is undoable, so a listener may undo it
        var registration = new ActionRegistration(added);
        notifyChanged(added, List.of());
        return registration;
    }

    /**
     * Returns the registered actions by their names, in the order of registration.
     *
     * @return an unmodifiable snapshot of the registry, never {@code null}
     */
    public synchronized @Unmodifiable Map<ActionName, Action> getActionsByName() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(actionsByName));
    }
}
