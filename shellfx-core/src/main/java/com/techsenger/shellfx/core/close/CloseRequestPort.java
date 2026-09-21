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
 * Contract for components that expose a handler for signaling that a close action should be performed, without
 * necessarily knowing how to perform it themselves.
 *
 * @author Pavel Castornii
 */
public interface CloseRequestPort {

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
}
