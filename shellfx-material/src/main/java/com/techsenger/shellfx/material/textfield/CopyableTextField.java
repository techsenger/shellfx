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

package com.techsenger.shellfx.material.textfield;

import atlantafx.base.theme.Styles;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.MaterialIcons;
import com.techsenger.shellfx.material.style.StyleClasses;
import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * A {@link TextField} that reveals a small copy-to-clipboard button at its right edge while the mouse hovers
 * over it, instead of requiring a separate button placed next to the field. The button overlaps whatever text
 * is underneath it while shown &mdash; this field does not reserve permanent padding for it &mdash; so it
 * suits fields that are typically read rather than actively edited while hovered (e.g. read-only display
 * fields).
 *
 * <p>The button sits on an opaque backdrop matching the field's own background (normal or readonly, see
 * {@code copyable-text-field.css}), since otherwise the button's icon would visually merge with whichever text
 * glyphs happen to be underneath it.
 *
 * <p>Clicking the button copies the field's current {@link #getText()} to the system clipboard and swaps the
 * icon to a checkmark for a short confirmation period before reverting back to the copy icon.
 *
 * @author Pavel Castornii
 */
public class CopyableTextField extends TextField {

    private static final PseudoClass READONLY = PseudoClass.getPseudoClass("readonly");

    private static final Duration CONFIRMATION_DURATION = Duration.seconds(1.5);

    private final Button button = new Button(null, new FontIconView(MaterialIcons.COPY));

    private final StackPane buttonPane = new StackPane(button);

    private final BooleanProperty confirming = new SimpleBooleanProperty();

    private final PauseTransition confirmationPause = new PauseTransition(CONFIRMATION_DURATION);

    public CopyableTextField() {
        super();
        var css = CopyableTextField.class.getResource("copyable-text-field.css").toExternalForm();
        getStylesheets().add(css);
        getStyleClass().add("copyable-text-field");

        getChildren().add(buttonPane);
        buttonPane.setManaged(false);
        buttonPane.setCursor(Cursor.DEFAULT);
        buttonPane.getStyleClass().add("button-pane");
        buttonPane.visibleProperty().bind(hoverProperty().or(confirming));
        buttonPane.pseudoClassStateChanged(READONLY, !isEditable());
        editableProperty().addListener((ov, oldV, newV) ->
                buttonPane.pseudoClassStateChanged(READONLY, !newV));

        button.setFocusTraversable(false);
        button.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT, StyleClasses.SIZE_XS);
        button.setOnAction(e -> copyAndConfirm());
        confirmationPause.setOnFinished(e -> {
            confirming.set(false);
            button.setGraphic(new FontIconView(MaterialIcons.COPY));
        });
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        // The skin installs its own content nodes (text, caret, prompt) into this control's children list
        // lazily, after this control's own constructor has already added copyButtonBackdrop — so, left
        // unchecked, those nodes end up in front of it in z-order (both for painting and, more importantly,
        // for mouse picking) even though the backdrop is still visible through their transparent pixels.
        // Re-asserting it as the last child on every layout pass keeps it frontmost so it actually receives
        // hover/click events instead of the skin's content silently swallowing them.
        if (!getChildren().isEmpty() && getChildren().get(getChildren().size() - 1) != buttonPane) {
            getChildren().remove(buttonPane);
            getChildren().add(buttonPane);
        }
        var backdropWidth = buttonPane.prefWidth(-1);
        var backdropHeight = buttonPane.prefHeight(backdropWidth);
        var x = getWidth() - getInsets().getRight() - backdropWidth;
        var y = (getHeight() - backdropHeight) / 2.0;
        buttonPane.resizeRelocate(x, y, backdropWidth, backdropHeight);
    }

    private void copyAndConfirm() {
        var content = new ClipboardContent();
        content.putString(getText());
        Clipboard.getSystemClipboard().setContent(content);
        confirming.set(true);
        button.setGraphic(new FontIconView(MaterialIcons.COPIED));
        confirmationPause.stop();
        confirmationPause.playFromStart();
    }
}
