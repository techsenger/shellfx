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

/**
 * Identifies why space is being resolved for a TabDock, passed to {@link SpaceResolver} and used throughout
 * {@code Transformer} for logging.
 * <p>
 * Every value has a fixed {@link #getDirection()} — resolvers that only care whether the TabDock is donating
 * or receiving space can branch on that alone, without enumerating every specific reason.
 * <p>
 * <b>{@link SpaceResolver} is not consulted for every operation.</b> Specifically, {@link #ADD} and
 * {@link #ARRIVE} are also used to label insertions whose position was already determined by a drag-and-drop
 * drop indicator ({@code DropPosition}) — dragging a single Tab out creates a brand-new TabDock ({@link #ADD}),
 * dragging an existing TabDock relocates it ({@link #ARRIVE}). In both of those drag-and-drop cases the donor
 * is fully determined by the indicator geometry already shown to the user before the drop, so the resolver is
 * never called — the operation value is used purely for logging there. {@link #ADD} issued through the
 * explicit anchor-based API ({@link DockHostView.Composer#addTabDock}) has no such prior visual indication and
 * does consult the resolver normally. See {@link SpaceResolver} for the full rule.
 *
 * @author Pavel Castornii
 */
public enum TabDockOperation {

    /**
     * A new TabDock is being added — either through the explicit anchor-based API (resolver consulted), or as
     * a brand-new TabDock created by dragging a single Tab out via drag-and-drop (resolver NOT consulted; the
     * position and donor were already fixed by the drop indicator).
     */
    ADD(Direction.DONATE),

    /**
     * An existing TabDock is being inserted at the destination of a drag-and-drop move. The donor is always
     * determined by the drop indicator geometry already shown to the user — the resolver is never consulted
     * for this operation.
     */
    ARRIVE(Direction.DONATE),

    /**
     * A previously minimized TabDock is being brought back into the live tree. No prior visual indication is
     * involved — the resolver is consulted normally.
     */
    RESTORE(Direction.DONATE),

    /**
     * A TabDock is being permanently closed — not as part of a move. The resolver is consulted normally.
     */
    REMOVE(Direction.RECEIVE),

    /**
     * A TabDock is being removed at the source of a drag-and-drop move. Unlike {@link #ARRIVE}, the old
     * position has no visual indicator of its own during the drag, so the resolver is consulted normally.
     */
    DEPART(Direction.RECEIVE),

    /**
     * A TabDock is being minimized into a SideBar. The resolver is consulted normally.
     */
    MINIMIZE(Direction.RECEIVE);

    /**
     * Whether a TabDock donates space to its siblings, or receives space freed up by them, for a given
     * {@link TabDockOperation}.
     */
    public enum Direction {
        DONATE, RECEIVE
    }

    private final Direction direction;

    TabDockOperation(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }
}
