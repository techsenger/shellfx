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

package com.techsenger.shellfx.core;

import javafx.beans.property.ObjectProperty;

/**
 *
 * @author Pavel Castornii
 */
public final class ActionUtils {

    /**
     * Runs an action from the property if the property and the action are not null.
     *
     * @param actionProperty
     */
    public static void runIfExists(ObjectProperty<Runnable> actionProperty) {
        if (actionProperty == null) {
            return;
        }
        var action = actionProperty.get();
        if (action != null) {
            action.run();
        }
    }

    private ActionUtils() {
        // empty
    }
}
