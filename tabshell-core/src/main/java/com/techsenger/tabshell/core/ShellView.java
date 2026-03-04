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

package com.techsenger.tabshell.core;

import com.techsenger.patternfx.mvp.ParentView;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.material.style.Stylesheet;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public interface ShellView extends ParentView, WriteOnlyShell {

    /**
     * The width of the stage if the stage is not maximized.
     */
    double DEFAULT_WIDTH = 1200;

    /**
     * The height of the stage if the stage is not maximized.
     */
    double DEFAULT_HEIGHT = 800;

    /**
     * Returns the control registry. There can be only one registry in the application.
     *
     * @return
     */
    ControlRegistry getControlRegistry();

    /**
     * Clears all existing elements in menu bar and adds new elements build using registry.
     *
     * @param registry
     */
    void upgradeMenuBar();

    /**
     * Forces shell to update the visibility of the elements in menu bar.
     */
    void updateMenuBar();

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
}
