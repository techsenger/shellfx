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

package com.techsenger.shellfx.material.pane;

import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * A pane that is laid over the content of a component while the component is waiting for something: it takes the
 * mouse events and shows the cursor of waiting, and the keyboard events that go to the content are consumed, so the
 * content below does not respond.
 *
 * @author Pavel Castornii
 */
public class WaitingPane extends Pane {

    /**
     * Makes the container show a waiting pane over its content while the property is {@code true}. The keys pressed
     * while the focus is inside the container do not reach its content either.
     *
     * @param container the container, whose content is below the pane
     * @param waiting the property that tells whether the component is waiting
     */
    public static void install(StackPane container, ReadOnlyBooleanProperty waiting) {
        var pane = new WaitingPane();
        EventHandler<KeyEvent> keyBlocker = KeyEvent::consume;
        Consumer<Boolean> update = isWaiting -> {
            if (isWaiting) {
                if (pane.getParent() == null) {
                    container.getChildren().add(pane);
                    container.addEventFilter(KeyEvent.ANY, keyBlocker);
                }
            } else if (container.getChildren().remove(pane)) {
                container.removeEventFilter(KeyEvent.ANY, keyBlocker);
            }
        };
        waiting.addListener((ov, oldV, newV) -> update.accept(newV));
        update.accept(waiting.get());
    }

    /**
     * Creates a pane that takes the mouse events.
     */
    public WaitingPane() {
        setMouseTransparent(false);
        setCursor(Cursor.WAIT);
    }
}
