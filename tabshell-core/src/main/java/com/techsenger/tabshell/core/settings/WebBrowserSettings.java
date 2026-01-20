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

package com.techsenger.tabshell.core.settings;

/**
 * Settings for the embedded (simple) web browser.
 *
 * @author Pavel Castornii
 */
public interface WebBrowserSettings {

    /**
     * Indicates whether the embedded browser is used by default.
     *
     * <p>If {@code true}, the platform opens links using the embedded browser unless explicitly overridden.
     * If {@code false}, links are delegated to the system browser by default.
     *
     * @return {@code true} if the embedded browser is used by default;
     *         {@code false} if the system browser is used by default
     */
    boolean isUsedByDefault();

    /**
     * Sets whether the embedded web browser should be used by default.
     *
     * @param value {@code true} to use the embedded browser by default;
     *              {@code false} to use the system browser by default
     */
    void setUsedByDefault(boolean value);

    SettingsSubscription observeUsedByDefault(SettingsObserver<Boolean> observer);
}
