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
import com.techsenger.shellfx.material.RequestSetter;

/**
 * Provides full access to the component's client API.
 *
 * @author Pavel Castornii
 */
public interface FullPopupPort extends ClosablePopupPort, Waitable {

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
     * @param value the width in pixels
     */
    @RequestSetter
    void setHeight(double value);
}
