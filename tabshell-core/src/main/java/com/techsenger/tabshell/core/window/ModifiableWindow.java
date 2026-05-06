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

import com.techsenger.tabshell.material.icon.Icon;

/**
 *
 * @author Pavel Castornii
 */
public interface ModifiableWindow {

    /**
     * Sets the title of the component.
     *
     * @param title
     */
    void setTitle(String title);

    /**
     * Sets the icon of the component.
     *
     * @param icon
     */
    void setIcon(Icon<?> icon);

    void setWidth(double value);

    void setHeight(double value);

    void setMaximized(boolean value);

    void setMaximizable(boolean maximizable);

    void setMinimized(boolean minimized);

    void setMinimizable(boolean minimizable);

    void setClosable(boolean closable);
}
