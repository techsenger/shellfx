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

package com.techsenger.shellfx.material;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ButtonBase;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

/**
 * Solves the problems that occur when a {@code Node} triggers an overlay (a popup, a nested dialog, a
 * nested window, or any other overlay-producing component) that ends up covering it, both with the node's
 * own animation and with its pseudo-classes:
 *
 * <ul>
 * <li>if the overlay is added to the scene graph immediately on action, it can cover the node before its
 * native press/armed animation has had a chance to play, so the user never sees it;</li>
 * <li>JavaFX only recomputes a node's pseudo-classes (such as {@code :hover}) in response to a genuine
 * mouse move/enter/exit event — adding a node on top of another one does not by itself trigger such an
 * event. As a result, once the overlay covers the node, the node can stay visually stuck in the hover (or
 * armed) state, sometimes indefinitely, until the user happens to move the mouse away and back.</li>
 * </ul>
 *
 * <p>This is meant to be used primarily by classes that create popups, nested dialogs, nested windows, and
 * other components that require an overlay on top of the {@code ButtonBase} that triggered them.
 *
 * @author Pavel Castornii
 */
public final class OverlayUtils {

    private static final Duration DELAY = Duration.millis(200);

    /**
     * Waits {@link #DELAY} (so that the button's own press/armed animation has time to finish), clears the
     * button's pseudo-classes by firing a synthetic {@link MouseEvent#MOUSE_EXITED} on it, and then invokes
     * {@code onFinished} — typically the code that creates and shows the overlay.
     *
     * @param button     the button whose state should be cleared before the overlay appears
     * @param onFinished the action to run once the button's state has been cleared
     */
    public static void runMouseAction(ButtonBase button, EventHandler<ActionEvent> onFinished) {
        var pause = new PauseTransition(DELAY);
        pause.setOnFinished(e -> {
            Event.fireEvent(button, new MouseEvent(MouseEvent.MOUSE_EXITED,
                    0, 0, 0, 0, MouseButton.NONE, 0,
                    false, false, false, false,
                    false, false, false,
                    true, false, false, null));
            onFinished.handle(e);
        });
        pause.playFromStart();
    }

    private OverlayUtils() {
        // empty
    }
}
