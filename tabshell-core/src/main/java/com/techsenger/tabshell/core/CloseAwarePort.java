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

import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public interface CloseAwarePort {

    int CLOSE_REQUEST_MAX_ATTEMPTS = 5;

    /**
     * Closes this component using a force close strategy.
     *
     * <p>This method immediately deinitializes the component and all its descendants and removes this component
     * from the component tree, without performing any readiness checks or preparation steps. This method should only
     * in exceptional situations where a forced shutdown is required.
     *
     */
    void close();

    /**
     * Initiates a close request for this component using a gentle close strategy.
     *
     * <p>This method coordinates a close operation over the component subtree, preparing components for closing if
     * necessary, and ensures that the system remains in a consistent state even in a live UI where components may
     * change during preparation.
     *
     * <p>The algorithm works as follows:
     * <ol>
     *     <li>Traverse this component and all its descendants in breadth-first order.</li>
     *     <li>During the traversal, {@link #isReadyToClose()} is called for each component to obtain a snapshot of its
     *         current close state.</li>
     *     <li>If any component returns {@link CloseCheckResult#NOT_READY}, the traversal stops immediately and the
     *         close request is aborted.</li>
     *     <li>If no {@code NOT_READY} components are found, the algorithm remembers the first component (if any)
     *         that returned {@link CloseCheckResult#PREPARATION_REQUIRED} while still completing the traversal
     *         to ensure that no {@code NOT_READY} components exist in the subtree.</li>
     *     <li>If a component requiring preparation was found, {@link #prepareToClose(Callback)} is invoked for that
     *         single component, and the close request is suspended until the preparation callback completes.</li>
     *     <li>After a successful preparation, the algorithm restarts from the beginning with a fresh traversal of
     *         {@link #isReadyToClose()} over the entire component subtree.</li>
     *     <li>When a full traversal completes with all components reporting
     *         {@link CloseCheckResult#READY}, {@link #close()} is invoked to perform the actual close operation.</li>
     * </ol>
     *
     * <p>Important: During a close request, the system may be live: new components can appear, existing components
     * may change state, and user actions may continue. The iterative check ensures that the close operation only
     * proceeds when the entire component subtree is truly ready.
     *
     * <p>The result of the close request is provided asynchronously via the given {@link Consumer}. The consumer will
     * be invoked once the request completes, is cancelled, or fails due to a component being not ready after all
     * attempts.
     *
     * <p>This method never force-closes components. If any component cannot be closed, the request is cancelled and
     * no component is deinitialized.
     *
     * @param maxAttempts maximum number of iterations over the component tree. If this limit is exceeded, the close
     *        request is aborted
     * @param resultConsumer a {@link Consumer} that receives the {@link RequestCloseResult} when the close
     *        request finishes
     */
    void requestClose(int maxAttempts, Consumer<CloseRequestResult> resultConsumer);

    /**
     * Initiates a close request using the default maximum number of iterations ({@link #CLOSE_REQUEST_MAX_ATTEMPTS}).
     *
     * <p>Behaves the same as {@link #requestClose(int, Consumer)} but uses the predefined default limit.
     *
     * @param resultCallback a {@link Consumer} that receives the {@link RequestCloseResult} when
     *                       the close request finishes;
     */
    default void requestClose(Consumer<CloseRequestResult> resultCallback) {
        requestClose(CLOSE_REQUEST_MAX_ATTEMPTS, resultCallback);
    }

    /**
     * Initiates a close request using the default maximum number of iterations ({@link #CLOSE_REQUEST_MAX_ATTEMPTS})
     * without a result callback.
     *
     * <p>Behaves the same as {@link #requestClose(Consumer)} but does not notify any consumer.
     */
    default void requestClose() {
        requestClose(CLOSE_REQUEST_MAX_ATTEMPTS, null);
    }

    /**
     * Checks whether this component can be closed.
     *
     * <p>This method must not perform any destructive actions or release resources. It is a pure readiness check.
     *
     * @return {@link CloseCheckResult#READY} if the component is ready to close,
     *         {@link CloseCheckResult#PREPARATION_REQUIRED} if preparation is required,
     *         or {@link CloseCheckResult#NOT_READY} if closing is disallowed
     */
    CloseCheckResult isReadyToClose();

    /**
     * Prepares the component for closing.
     * <p>
     * This method is invoked only if {@link #isReadyToClose()} returned {@link CloseCheckResult#PREPARATION_REQUIRED}.
     * Implementations may perform actions such as saving data, stopping background tasks, or waiting for
     * asynchronous operations to complete. Once the component is fully prepared and guaranteed to be closable,
     * the provided callback must be invoked.
     *
     * @param resultCallback the callback to be called when preparation is complete, receiving a
     *                       {@link ClosePreparationResult} that describes the result
     */
    void prepareToClose(Consumer<ClosePreparationResult> resultCallback);

    /**
     * Returns the callback to be executed after the component has been fully closed and deinitialized.
     *
     * @return the callback, or {@code null} if no callback is set
     */
    Runnable getOnClosed();

    /**
     * Sets the callback to be executed after the component has been fully closed and deinitialized.
     *
     * @param runnable the callback to execute, or {@code null} to clear it
     */
    void setOnClosed(Runnable runnable);
}
