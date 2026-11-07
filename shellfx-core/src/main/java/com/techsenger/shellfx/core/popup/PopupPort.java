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

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.core.area.AreaPort;
import com.techsenger.shellfx.core.close.CloseAwarePort;
import com.techsenger.shellfx.core.traits.Waitable;
import com.techsenger.shellfx.material.RequestSetter;
import javafx.beans.property.BooleanProperty;

/**
 * Provides full access to the component's client API.
 *
 * @author Pavel Castornii
 */
public interface PopupPort extends AreaPort, CloseAwarePort, Waitable {

    interface ComposerAccess extends AreaPort.ComposerAccess {

        @Override
        @Nullable PopupContainerPort getParentPort();
    }

    @Override
    ComposerAccess getComposerAccess();

    /**
     * Returns {@code true} if the popup blocks interaction with underlying content (modal) and {@code false} otherwise.
     *
     * @return
     */
    boolean isModal();

    /**
     * Returns whether the popup is visible. A popup is created invisible, so its content can be filled in before it
     * is shown. An invisible popup is a part of the component tree, but takes no part in z-order, focus and modality.
     *
     * @return {@code true} if the popup is visible, {@code false} otherwise
     */
    boolean isVisible();

    /**
     * Makes the popup visible or invisible. Becoming invisible is not closing: the popup stays in the component tree,
     * and {@link #close()} still has to be called to remove it. A modal popup blocks the container only while it is
     * visible. A popup never takes the focus itself: call {@link #requestFocus()} or let the user click it.
     *
     * @param value {@code true} to make the popup visible, {@code false} to make it invisible
     */
    void setVisible(boolean value);

    BooleanProperty visibleProperty();

    /**
     * Sets the width of the popup. Using this method is optional because, by default, the popup width is
     * based on the preferred width of its content.
     *
     * <p>This is a request, not a guarantee — the platform may adjust or ignore it; observe
     * {@link #widthProperty()} for the value actually applied.
     *
     * @param value the width in pixels
     */
    @RequestSetter
    void setWidth(double value);

    /**
     * Sets the height of the popup. Using this method is optional because, by default, the popup height is
     * based on the preferred height of its content.
     *
     * <p>This is a request, not a guarantee — the platform may adjust or ignore it; observe
     * {@link #heightProperty()} for the value actually applied.
     *
     * @param value the height in pixels
     */
    @RequestSetter
    void setHeight(double value);
}
