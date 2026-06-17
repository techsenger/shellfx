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

package com.techsenger.shellfx.material.button;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps all registered buttons at the same preferred width. The width is calculated as the maximum width among all
 * buttons currently attached to a scene.
 *
 * @author Pavel Castornii
 */
public final class ButtonWidthGroup {

    private static final Logger logger = LoggerFactory.getLogger(ButtonWidthGroup.class);

    private final Parent container;

    private final Set<Button> buttons = new HashSet<>();

    private final Supplier<String> logPrefix;

    private boolean enabled = true;

    /**
     * Creates a new button width group.
     *
     * @param container container used for CSS and layout calculations
     * @param logPrefix log message prefix supplier
     */
    public ButtonWidthGroup(Parent container, Supplier<String> logPrefix) {
        this.container = Objects.requireNonNull(container);
        this.logPrefix = Objects.requireNonNull(logPrefix);

        this.container.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && enabled) {
                refresh();
            }
        });
    }

    /**
     * Adds buttons to the group. Width recalculation is not triggered automatically.
     * Call {@link #refresh()} explicitly when required.
     *
     * @param buttons buttons to add
     */
    public void add(Button... buttons) {
        for (var button : buttons) {
            Objects.requireNonNull(button);
            this.buttons.add(button);
        }
    }

    /**
     * Removes buttons from the group. Width recalculation is not triggered automatically.
     * Call {@link #refresh()} explicitly when required.
     *
     * @param buttons buttons to remove
     */
    public void remove(Button... buttons) {
        for (var button : buttons) {
            Objects.requireNonNull(button);

            if (this.buttons.remove(button)) {
                button.setPrefWidth(Region.USE_COMPUTED_SIZE);
                button.setMinWidth(Region.USE_COMPUTED_SIZE);
                button.setMaxWidth(Region.USE_COMPUTED_SIZE);
            }
        }
    }

    /**
     * Returns whether width synchronization is enabled.
     *
     * @return {@code true} if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enables or disables width synchronization.
     *
     * @param enabled new enabled state
     */
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;
        refresh();
    }

    /**
     * Recalculates button widths and applies the maximum width to all buttons currently attached to a scene.
     * If synchronization is disabled, previously applied widths are cleared.
     */
    public void refresh() {
        if (!enabled) {
            for (var button : buttons) {
                button.setPrefWidth(Region.USE_COMPUTED_SIZE);
                button.setMinWidth(Region.USE_COMPUTED_SIZE);
                button.setMaxWidth(Region.USE_COMPUTED_SIZE);
            }

            return;
        }

        if (container.getScene() == null) {
            return;
        }

        var participating = buttons.stream()
                .filter(button -> button.getScene() != null)
                .toList();

        if (participating.isEmpty()) {
            return;
        }

        for (var button : participating) {
            button.setPrefWidth(Region.USE_COMPUTED_SIZE);
        }

        container.applyCss();
        container.layout();

        double maxWidth = 0;

        for (var button : participating) {
            maxWidth = Math.max(maxWidth, button.prefWidth(-1));
        }

        for (var button : participating) {
            button.setPrefWidth(maxWidth);
        }

        container.layout();

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "{} Applied equal button width {} to {} buttons: {}",
                    logPrefix.get(),
                    maxWidth,
                    participating.size(),
                    participating.stream()
                            .map(Button::getText)
                            .toList());
        }
    }
}
