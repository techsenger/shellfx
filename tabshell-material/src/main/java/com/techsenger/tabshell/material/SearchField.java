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

package com.techsenger.tabshell.material;

import com.techsenger.tabshell.material.style.SizeConstants;
import com.techsenger.tabshell.material.style.StyleClasses;
import java.util.function.Consumer;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 *
 * @author Pavel Castornii
 */
public class SearchField extends StackPane {

    public enum SearchMode {

        /**
         * Real-time incremental search. The search handler is triggered automatically as the user types, with
         * configurable debounce delay and minimum character length.
         */
        AUTO,

        /**
         * Manual search. The search handler is triggered only on explicit user action (Enter key or selecting
         * from dropdown), not during typing.
         */
        MANUAL
    }

    private final SearchMode mode;

    private final ComboBox<String> textComboBox = new ComboBox<>();

    private final Button clearButton = new Button();

    private Consumer<String> handler;

    /**
     * Debounce duration in milliseconds.
     */
    private int debounceMillis = 300;

    /**
     * Minimum characters to trigger incremental search (default 3).
     */
    private int minSearchLength = 3;

    /**
     * PauseTransition used to implement debounce on the JavaFX thread.
     */
    private final PauseTransition debouncePause;

    public SearchField(SearchMode searchType) {
        this.mode = searchType;
        build();

        if (searchType == SearchMode.AUTO) {
            debouncePause = new PauseTransition(Duration.millis(debounceMillis));
            debouncePause.setOnFinished(e -> {
                String text = textComboBox.getEditor().getText();
                invokeHandler(text);
            });
        } else {
            debouncePause = null;
        }

        textComboBox.getEditor().textProperty().addListener((ov, oldV, newV) -> {
            if (clearButton.getParent() == null) {
                if (newV != null && !newV.isEmpty()) {
                    getChildren().add(clearButton);
                }
            } else {
                if (newV == null || newV.isEmpty()) {
                    getChildren().remove(clearButton);
                }
            }
            if (this.mode == SearchMode.AUTO) {
                debouncePause.stop();
                if (newV != null && newV.length() >= minSearchLength) {
                    // restart debounce timer
                    debouncePause.playFromStart();
                }
            }
        });

        // Enter key: immediate invocation (useful for manual search and also useful to allow
        // immediate search in incremental mode)
        textComboBox.getEditor().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                invokeHandler(textComboBox.getEditor().getText());
            }
        });

        // Combobox value change: keep existing behavior (invokes handler on selection)
        textComboBox.valueProperty().addListener((ov, oldV, newV) -> invokeHandler(newV));

        clearButton.setOnAction(e -> textComboBox.getEditor().setText(null));
    }

    public SearchMode getMode() {
        return mode;
    }

    public ComboBox<String> getTextComboBox() {
        return textComboBox;
    }

    public Button getClearButton() {
        return clearButton;
    }

    public Consumer<String> getHandler() {
        return handler;
    }

    public void setHandler(Consumer<String> handler) {
        this.handler = handler;
    }

    /**
     * Returns debounce time in milliseconds.
     */
    public int getDebounceMillis() {
        return debounceMillis;
    }

    /**
     * Sets debounce time in milliseconds. Updates the internal debounce timer immediately.
     */
    public void setDebounceMillis(int debounceMillis) {
        if (debounceMillis < 0) {
            throw new IllegalArgumentException("debounceMillis must be >= 0");
        }
        this.debounceMillis = debounceMillis;
        if (debouncePause != null) {
            debouncePause.setDuration(Duration.millis(debounceMillis));
        }
    }

    /**
     * Returns minimum number of characters required to start incremental search.
     */
    public int getMinSearchLength() {
        return minSearchLength;
    }

    /**
     * Sets minimum number of characters required to start incremental search.
     */
    public void setMinSearchLength(int minSearchLength) {
        if (minSearchLength < 1) {
            throw new IllegalArgumentException("minSearchLength must be >= 1");
        }
        this.minSearchLength = minSearchLength;
    }

    private void build() {
        clearButton.getStyleClass().add(StyleClasses.CROSS_BUTTON);
        clearButton.setFocusTraversable(false);
        StackPane.setMargin(this.clearButton, new Insets(0, SizeConstants.INSET * 2, 0, 0));
        textComboBox.getStyleClass().add(StyleClasses.EXTRA_DENSE);
        textComboBox.setEditable(true);
        textComboBox.maxWidthProperty().bind(widthProperty().subtract(1));
        setAlignment(Pos.CENTER_RIGHT);
        getChildren().add(textComboBox);
        getStyleClass().addAll("search-field", StyleClasses.EXTRA_DENSE);
        var styles = SearchField.class.getResource("search-field.css").toExternalForm();
        getStylesheets().add(styles);
    }

    private void invokeHandler(String text) {
        if (this.handler != null && text != null && !text.isEmpty()) {
            if (this.mode == SearchMode.AUTO && text.length() < minSearchLength) {
                return;
            }
            this.handler.accept(text);
        }
    }
}
