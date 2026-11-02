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

/**
 * The root of the settings tree. A listener is registered only on the root and is told when anything in the tree
 * has changed.
 *
 * @author Pavel Castornii
 */
public interface ShellSettings {

    AppearanceSettings getAppearance();

    /**
     * Registers a listener that is told when the settings have changed. A listener owned by a shorter-lived
     * component must be removed when that component is gone.
     *
     * @param listener the listener to notify.
     */
    void addListener(SettingsListener listener);

    /**
     * Unregisters a listener; does nothing if it is not registered.
     *
     * @param listener the listener to remove.
     */
    void removeListener(SettingsListener listener);

    /**
     * Tells every listener that the settings have changed. Call it once after changing a batch of values, setters of
     * the settings do not notify by themselves.
     */
    void notifyListeners();
}
