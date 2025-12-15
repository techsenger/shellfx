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

package com.techsenger.tabshell.core.dialog;

import com.techsenger.patternfx.core.ParentComponent;
import com.techsenger.patternfx.core.ParentView;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public interface DialogContainerComponent<T extends ParentView<?, ?>> extends ParentComponent<T> {

    /**
     * Returns the dialog scope supported by this container.
     *
     * @return the supported dialog scope
     */
    DialogScope getSupportedDialogScope();

    /**
     * Adds the specified dialog component to the component tree.
     *
     * <p>If the dialog scope of the component does not match this container's scope, the dialog is forwarded to
     * the appropriate container: {@link ShellTabComponent} forwards {@link DialogScope.SHELL} dialogs to
     * {@link ShellComponent}, and {@link ShellComponent} forwards {@link DialogScope.TAB} dialogs to the selected
     * {@link ShellTabComponent}.
     *
     * @param dialog the dialog component to add
     */
    void addDialog(DialogComponent<?> dialog);

    /**
     * Removes the specified dialog component from the component tree.
     *
     * <p>If the dialog scope of the component does not match this container's scope, the dialog is removed from
     * the appropriate container: {@link ShellTabComponent} removes {@link DialogScope.SHELL} dialogs from the
     * enclosing {@link ShellComponent}, and {@link ShellComponent} removes {@link DialogScope.TAB} dialogs from the
     * currently selected {@link ShellTabComponent}.
     *
     * @param dialog the dialog component to remove
     */
    void removeDialog(DialogComponent<?> dialog);

    /**
     * Returns an unmodifiable list of tabs. A new list instance can created on each call.
     *
     * @return
     */
    List<? extends DialogComponent<?>> getDialogs();
}
