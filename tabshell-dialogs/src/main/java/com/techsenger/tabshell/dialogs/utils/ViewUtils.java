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

package com.techsenger.tabshell.dialogs.utils;

import com.techsenger.tabshell.core.style.SizeConstants;
import com.techsenger.tabshell.material.icon.FontIconView;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public final class ViewUtils {

    public static void buildIconedMessageBox(FontIconView messageIconView, Label messageLabel, HBox messageBox) {
        messageIconView.setMinWidth(Label.USE_PREF_SIZE);
        DoubleBinding messageLabelWidth = Bindings.createDoubleBinding(
                () -> messageBox.getWidth() - messageIconView.getWidth() - 2 * SizeConstants.INSET, //0.5 + 0.5 + 1
                messageBox.widthProperty(), messageIconView.widthProperty());
        messageLabel.prefWidthProperty().bind(messageLabelWidth);
        messageLabel.minWidthProperty().bind(messageLabelWidth);
        messageLabel.maxWidthProperty().bind(messageLabelWidth);
        messageLabel.setPadding(new Insets(SizeConstants.INSET, 0, 0, 0));

        VBox.setVgrow(messageBox, Priority.ALWAYS);
        messageBox.getStyleClass().add("message-box");
        messageBox.setSpacing(SizeConstants.HALF_INSET);
        messageBox.setPadding(new Insets(0, SizeConstants.INSET, 0, SizeConstants.HALF_INSET));
    }

    private ViewUtils() {
        //empty
    }
}
