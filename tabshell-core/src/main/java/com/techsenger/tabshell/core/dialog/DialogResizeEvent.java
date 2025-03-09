/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.core.dialog;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author Pavel Castornii
 */
public class DialogResizeEvent extends Event {

    /**
     * Common supertype for all dialog resize event types.
     */
    public static final EventType<DialogResizeEvent> ANY = new EventType<>(Event.ANY, "DIALOG_RESIZE");

    /**
     * This event occurs when user starts resizing a dialog.
     */
    public static final EventType<DialogResizeEvent> DIALOG_RESIZE_STARTED =
            new EventType<>(DialogResizeEvent.ANY, "DIALOG_RESIZE_STARTED");

    /**
     * This event occurs when user finishes resizing a dialog.
     */
    public static final EventType<DialogResizeEvent> DIALOG_RESIZE_FINISHED =
            new EventType<>(DialogResizeEvent.ANY, "DIALOG_RESIZE_FINISHED");

    private final MouseEvent mouseEvent;

    public DialogResizeEvent(EventType<? extends DialogResizeEvent> eventType, MouseEvent mouseEvent) {
        super(eventType);
        this.mouseEvent = mouseEvent;
    }

    public MouseEvent getMouseEvent() {
        return mouseEvent;
    }
}
