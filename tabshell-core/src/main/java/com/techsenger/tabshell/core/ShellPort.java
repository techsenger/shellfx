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

import com.techsenger.tabshell.material.icon.Icon;

/**
 *
 * @author Pavel Castornii
 */
public interface ShellPort extends MenuAwarePort, WindowPort {

    /**
     * Returns the context of the shell.
     *
     * @return
     */
    ShellContext getContext();

    /**
     * Returns the context of the shell as an instance of the specified class using type casting.
     *
     * @return
     */
    <T extends ShellContext> T getContext(Class<T> contextClass);

    /**
     * Returns shell current width.
     *
     * @return
     */
    double getWidth();

    /**
     * Returns shell current height.
     *
     * @return
     */
    double getHeight();

    /**
     * Returns true if the shell stage is maximized, otherwise false.
     *
     * @return
     */
    boolean isMaximized();

    /**
     * Returns the title of the component.
     *
     * @return
     */
    String getTitle();

    /**
     * Returns the icon of the component.
     * @return
     */
    Icon<?> getIcon();

}
