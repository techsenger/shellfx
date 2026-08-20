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

package com.techsenger.shellfx.core.window;

import com.techsenger.annotations.Nullable;
import com.techsenger.patternfx.mvp.ChildView;
import com.techsenger.shellfx.material.icon.Icon;
import com.techsenger.shellfx.material.style.Density;
import com.techsenger.shellfx.material.theme.Theme;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public interface WindowView extends ChildView {

    interface Composer extends ChildView.Composer {

        /**
         * Closes both {@link WindowType#TOP_LEVEL} and {@link WindowType#NESTED} windows.
         */
        void close();

        @Nullable WindowContainerPort getContainerPort();
    }

    @Override
    Composer getComposer();

    /**
     * Makes this window modal.
     *
     * <p>This is a one-time initialization command, not a live state update: modality can only be applied before
     * the window is shown and cannot be changed or unset afterwards. Call this only once, when
     * {@link WindowPort#isModal()} is {@code true}.
     */
    void updateModal();

    void updateAlwaysOnTop(boolean alwaysOnTop);

    void updateTitle(String title);

    void updateIcon(Icon<?> icon);

    void updateWidth(double value);

    void updateHeight(double value);

    void updateMinWidth(double value);

    void updateMinHeight(double value);

    void updateMaxWidth(double value);

    void updateMaxHeight(double value);

    void updateMaximized(boolean value);

    void updateMaximizable(boolean maximizable);

    void updateMinimized(boolean minimized);

    void updateMinimizable(boolean minimizable);

    void updateClosable(boolean closable);

    void updateBlocked(boolean blocked);

    void updateOutOfBoundsAllowed(boolean outOfBoundsAllowed);

    void updateResizable(boolean value);

    void updateX(double x);

    void updateY(double y);

    void updateDensity(@Nullable Density density);

    void updateTheme(Theme theme);

    void updateRegularFont(Font font);

    void updateMonospaceFont(Font font);

    /**
     * Closes the top level window.
     *
     * <p>This method is intended for {@link WindowType#TOP_LEVEL} windows only.
     */
    void closeWindow();
}
