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

package com.techsenger.tabshell.core.popup;

import com.techsenger.tabshell.core.CloseablePresenter;
import com.techsenger.tabshell.core.area.AreaPresenter;

/**
 *
 * @author Pavel Castornii
 */
public interface PopupPresenter<V extends PopupView, C extends PopupComposer>
        extends AreaPresenter<V, C>, CloseablePresenter<V, C> {

    OverlayScope getOverlayScope();

    /**
     * Returns {@code true} if the popup blocks interaction with underlying content (modal) and {@code false} otherwise.
     *
     * @return
     */
    boolean isModal();

    @Override
    PopupPort getPort();
}
