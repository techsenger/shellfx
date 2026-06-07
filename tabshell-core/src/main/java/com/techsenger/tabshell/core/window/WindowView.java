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

package com.techsenger.tabshell.core.window;

import com.techsenger.annotations.Nullable;
import com.techsenger.patternfx.mvp.ChildView;
import com.techsenger.tabshell.material.theme.Theme;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public interface WindowView extends ChildView, WindowShared {

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
     * Sets whether this window is modal.
     *
     * @param modal {@code true} to make the window modal; {@code false} otherwise
     */
    void setModal(boolean modal);

    /**
     * Sets the density of this window.
     *
     * <p>This method is intended for {@link WindowType#TOP_LEVEL} windows only.
     *
     * @param density the density to apply
     */
    void setDensity(@Nullable String density);

    /**
     * Sets the theme of this window.
     *
     * <p>This method is intended for {@link WindowType#TOP_LEVEL} windows only.
     *
     * @param theme the theme to apply
     */
    void setTheme(Theme theme);

    /**
     * Sets the regular font of this window.
     *
     * <p>This method is intended for {@link WindowType#TOP_LEVEL} windows only.
     *
     * @param font the regular font to apply
     */
    void setRegularFont(Font font);

    /**
     * Sets the monospace font of this window.
     *
     * <p>This method is intended for {@link WindowType#TOP_LEVEL} windows only.
     *
     * @param font the monospace font to apply
     */
    void setMonospaceFont(Font font);

    /**
     * Closes the top level window.
     *
     * <p>This method is intended for {@link WindowType#TOP_LEVEL} windows only.
     */
    void closeWindow();
}
