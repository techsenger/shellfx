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

package com.techsenger.tabshell.core;

import com.techsenger.patternfx.mvp.ParentFxView;
import javafx.scene.Node;

/**
 *
 * @author Pavel Castornii
 */
public final class FxViewUtils {

    private static final Object COMPONENT_KEY = new Object();

    /**
     * Associates the given {@link ParentFxView} component with the specified JavaFX {@link Node}.
     * <p>
     * This method stores a reference to the component in the node's property map, allowing the component to be
     * retrieved later by traversing the JavaFX node tree. This is useful in scenarios where only a node is available
     * (e.g., during focus traversal or event handling) and the associated component needs to be identified.
     * <p>
     * This method should be called during component initialization.
     *
     * @param node the JavaFX node to associate with the component; must not be {@code null}
     * @param view the component to associate with the node; must not be {@code null}
     */
    public static void setComponent(Node node, ParentFxView<?> view) {
        node.getProperties().put(COMPONENT_KEY, view);
    }

    /**
     * Returns the {@link ParentFxView} component associated with the given {@link Node}, or {@code null} if no
     * component has been associated with it.
     * <p>
     * This method is intended for use when traversing the JavaFX node tree — for example, when walking up the parent
     * chain from a focused node to find the nearest component boundary.
     *
     * @param node the JavaFX node to look up; must not be {@code null}
     * @return the associated component, or {@code null} if none is associated
     */
    public static ParentFxView<?> getComponent(Node node) {
        return (ParentFxView<?>) node.getProperties().get(COMPONENT_KEY);
    }

    private FxViewUtils() {
        // empty
    }
}
