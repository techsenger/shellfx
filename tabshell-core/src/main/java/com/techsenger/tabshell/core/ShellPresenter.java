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

import com.techsenger.patternfx.mvp.ParentPresenter;

/**
 *
 * @author Pavel Castornii
 */
public interface ShellPresenter<V extends ShellView> extends ParentPresenter<V>,
        CloseAwarePresenter<V>, ReadOnlyShell, WriteOnlyShell, ShellPort {

    /**
     * Returns the action to be executed when the shell is closed.
     *
     * @return
     */
    Runnable getOnClose();

    /**
     * Sets the action to be executed when the shell is closed.
     * @param onClose
     */
    void setOnClose(Runnable onClose);
}
