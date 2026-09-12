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

package com.techsenger.shellfx.core;

/**
 * Contract for components that expose lifecycle callbacks around a close action, independent of whether the
 * component actually closes via a {@link ForceClosePort} or a {@link SafeClosePort} strategy.
 *
 * @author Pavel Castornii
 */
public interface CloseCallbackPort {

    /**
     * Returns the handler invoked when the component decides that a close action should be performed.
     *
     * @return the handler, or {@code null} if none is set
     */
    Runnable getOnCloseRequest();

    /**
     * Sets the handler invoked when the component decides that a close action should be performed.
     *
     * <p>The handler is responsible for deciding whether and how to close the component, for example by calling
     * {@link ForceClosePort#close()} or {@link SafeClosePort#closeSafely()}, or doing nothing to cancel the close.
     *
     * @param runnable the handler to invoke, or {@code null} to clear it
     */
    void setOnCloseRequest(Runnable runnable);

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
