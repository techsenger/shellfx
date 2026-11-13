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

package com.techsenger.shellfx.core.popup;

import com.techsenger.shellfx.core.traits.Waitable;
import com.techsenger.shellfx.core.window.WindowPort;
import javafx.beans.property.BooleanProperty;

/**
 * Provides full access to the client API of a popup: a bare window without a title bar, which never takes the focus
 * itself, and can be shown and hidden without being closed.
 *
 * @author Pavel Castornii
 */
public interface PopupPort extends WindowPort, Waitable {

    /**
     * Returns whether the popup hides itself when the user presses the mouse or scrolls outside it, presses Escape or
     * leaves the window. The popup is only made invisible, never closed, so the client watches {@code visible}. The
     * default is {@code false}.
     *
     * @return {@code true} if the popup hides itself
     */
    boolean isAutoHide();

    void setAutoHide(boolean autoHide);

    BooleanProperty autoHideProperty();

}
