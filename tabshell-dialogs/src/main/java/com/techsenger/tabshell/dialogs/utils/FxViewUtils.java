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

package com.techsenger.tabshell.dialogs.utils;

import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.tabshell.material.style.Spacing;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public final class FxViewUtils {

    public static void buildIconedMessageBox(IconViewBox messageIconViewBox, Label messageLabel, HBox messageBox) {
        messageLabel.setPadding(new Insets(Spacing.VERTICAL, 0, 0, 0));

        VBox.setVgrow(messageBox, Priority.ALWAYS);
        messageBox.getStyleClass().add("message-box");
        messageBox.setSpacing(Spacing.HORIZONTAL_HALF);
        messageBox.setPadding(new Insets(0, Spacing.HORIZONTAL, 0, Spacing.HORIZONTAL_HALF));
    }

    private FxViewUtils() {
        //empty
    }
}
