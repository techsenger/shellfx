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

package com.techsenger.shellfx.core.close;

/**
 * Contract for components that, in addition to a {@link CloseRequestPort#getOnCloseRequest() close request}
 * handler, also expose a callback for when the component has actually finished closing.
 *
 * @author Pavel Castornii
 */
public interface CloseCallbackPort extends CloseRequestPort {

    /**
     * Returns the callback invoked after this component has been fully closed and deinitialized.
     *
     * @return the callback, or {@code null} if none is set
     */
    Runnable getOnClosed();

    /**
     * Sets the callback invoked after this component has been fully closed and deinitialized.
     *
     * @param runnable the callback to invoke, or {@code null} to clear it
     */
    void setOnClosed(Runnable runnable);
}
