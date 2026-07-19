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

package com.techsenger.shellfx.core.menu;

/**
 * Represents behavior attached to a managed menu or menu item, kept as a plain object rather than being expressed
 * as methods on the control itself. This separation exists for two reasons.
 *
 * <p>First, it decouples behavior from the JavaFX control hierarchy. {@code Menu}, {@code MenuItem},
 * {@code CheckMenuItem} and {@code RadioMenuItem} form a fixed single-inheritance hierarchy that managed types
 * ({@code ManagedMenu}, {@code ManagedMenuItem}, {@code ManagedCheckMenuItem}, etc.) must extend directly, leaving
 * no room to also extend a common behavioral base class. Because a {@code Handler} is a separate object, its own
 * class hierarchy is free of that constraint, so shared behavior (e.g. confirming before an action runs) can be
 * reused across otherwise unrelated managed item types.
 *
 * <p>Second, it avoids colliding with JavaFX's own {@code onShowing}/{@code onHiding}/{@code onAction} properties
 * on {@code Menu} and {@code MenuItem}. Those properties are already used internally by {@code MenuManager} to
 * wire dispatch to the registered {@code Handler}; if handler logic were expressed through the same properties,
 * any code that called {@code setOnShowing}/{@code setOnAction} directly on a managed control — a plugin author,
 * for instance — would silently override that wiring instead of failing loudly.
 *
 * @author Pavel Castornii
 */
public interface Handler {

    /**
     * Called when a menu or menu item needs to update its state (visibility, enabled/disabled state, etc.). This
     * may occur when the menu is shown or when an accelerator key is pressed.
     */
    void onUpdate();

    /**
     * Called on menu showing.
     */
    void onShowing();

    /**
     * Called on menu hiding.
     */
    void onHiding();
}
