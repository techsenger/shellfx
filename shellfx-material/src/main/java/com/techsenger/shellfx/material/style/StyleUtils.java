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

package com.techsenger.shellfx.material.style;

import com.techsenger.annotations.Nullable;
import java.util.Objects;
import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 *
 * @author Pavel Castornii
 */
public final class StyleUtils {

    public static String toStyle(Font font) {
        if (font != null) {
            var result = "-fx-font-size:" + Math.round(font.getSize()) + "px;-fx-font-family:'"
                    + font.getFamily() + "';";
            return result;
        } else {
            return "";
        }
    }

    /**
     * Returns the width and height of a single character when rendered with the specified monospace font.
     *
     * <p>Note: The character height is not equal to the line height.
     *
     * @param font the font to measure (must be monospace for accurate results)
     * @return a {@code Dimension2D} representing character width and height
     */
    public static Dimension2D getMonospaceCharSize(Font font) {
        final Text text = new Text("W");
        text.setFont(font);
        var bounds = text.getBoundsInLocal();
        return new Dimension2D(bounds.getWidth(), bounds.getHeight());
    }

    /**
     * Looks up the currently active {@link Density} for the given window, walking up the owner-window chain
     * if the window itself has no density style class set — which is the case for any non-top-level window,
     * since density is only ever set on top-level windows. Owner lookup differs by window type: {@link Stage}
     * exposes it via {@link Stage#getOwner()}, while {@link PopupWindow} (e.g. {@link javafx.stage.Popup})
     * exposes it via {@link PopupWindow#getOwnerWindow()} — {@link Window} itself declares neither. Returns
     * {@code null} if no window in the chain has a density style class, has no {@link Scene} yet, or if the
     * chain ends in a window type that exposes no owner (e.g. an unowned {@code Stage}).
     *
     * @param window the window to inspect
     * @return the active density, or {@code null} if none was found in the owner-window chain
     */
    public static @Nullable Density getDensity(Window window) {
        Objects.requireNonNull(window, "window");
        var current = window;
        while (current != null) {
            var scene = current.getScene();
            if (scene != null) {
                var styleClasses = scene.getRoot().getStyleClass();
                for (var density : Density.values()) {
                    if (styleClasses.contains(density.getStyleClass())) {
                        return density;
                    }
                }
            }
            current = getOwner(current);
        }
        return null;
    }

    private static @Nullable Window getOwner(Window window) {
        if (window instanceof Stage stage) {
            return stage.getOwner();
        } else if (window instanceof PopupWindow popupWindow) {
            return popupWindow.getOwnerWindow();
        } else {
            return null;
        }
    }

    private StyleUtils() {

    }
}
