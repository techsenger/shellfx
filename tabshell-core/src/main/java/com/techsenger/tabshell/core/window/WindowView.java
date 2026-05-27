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

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.mvp.ParentView;
import com.techsenger.tabshell.core.dialog.DialogContainerView;
import com.techsenger.tabshell.material.style.Stylesheet;
import com.techsenger.tabshell.material.theme.Theme;
import java.util.List;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public interface WindowView extends ParentView, WindowShared {

    interface Composer extends ParentView.Composer, DialogContainerView.Composer {

    }

    @Override
    Composer getComposer();

    /**
     * Adds stylesheets to TabShell.
     *
     * @param sheets
     */
    void addStylesheets(List<Stylesheet> sheets);

    /**
     * Removes stylesheets from Shell.
     *
     * @param sheets
     */
    void removeStylesheets(List<Stylesheet> sheets);

    /**
     * Returns an unmodifiable list of stylesheets.
     */
    @Unmodifiable List<Stylesheet> getStylesheets();

    /**
     * Closes the window.
     */
    void closeWindow();

    /**
     * Sets theme.
     */
    void setTheme(Theme theme);

    /**
     * Sets regular font.
     */
    void setRegularFont(Font font);

    /**
     * Sets monospace font.
     */
    void setMonospaceFont(Font font);
}
