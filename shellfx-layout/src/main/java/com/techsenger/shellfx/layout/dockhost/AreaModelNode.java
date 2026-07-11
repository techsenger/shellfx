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

package com.techsenger.shellfx.layout.dockhost;

import com.techsenger.shellfx.core.area.AbstractAreaFxView;

/**
 * A leaf node holding an area.
 * <p>
 * Exactly one node in a docking layout tree must have {@link #isMain()} return {@code true} — the area around which
 * the rest of the layout is positioned.
 *
 * @author Pavel Castornii
 */
public final class AreaModelNode implements ModelNode {

    private final AbstractAreaFxView<?> area;

    private final boolean main;

    private final double proportion;

    /**
     * Creates a new area node.
     *
     * @param area the leaf's area
     * @param main whether this is the layout's main area
     * @param proportion this node's relative size among its siblings,
     *         a value between {@code 0} and {@code 1}, or
     *         {@link ModelNodeBuilder#UNSET_PROPORTION}
     */
    public AreaModelNode(AbstractAreaFxView<?> area, boolean main, double proportion) {
        this.area = area;
        this.main = main;
        this.proportion = proportion;
    }

    /**
     * Returns the area held by this node.
     *
     * @return the leaf's area
     */
    public AbstractAreaFxView<?> getArea() {
        return area;
    }

    /**
     * Returns whether this is the layout's main area — the area around
     * which the rest of the layout is positioned.
     *
     * @return {@code true} if this is the main area
     */
    public boolean isMain() {
        return main;
    }

    @Override
    public double getProportion() {
        return proportion;
    }
}