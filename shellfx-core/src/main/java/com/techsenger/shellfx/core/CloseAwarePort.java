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

import java.util.function.Consumer;

/**
 * Lifecycle contract for components that support controlled closing.
 *
 * <p>The typical close flow is:
 * <ol>
 *     <li>The user or the system triggers a close action, which causes {@link #onCloseRequest()} to be called.</li>
 *     <li>{@link #onCloseRequest()} invokes the registered {@code onCloseRequest} runnable (if any),
 *         or falls back to the default behavior, which calls {@link #close()}.</li>
 *     <li>The runnable decides whether and how to close: it may call {@link #close()} for an immediate
 *         forced close, {@link #closeSafely(Consumer)} for a coordinated close, or do nothing to cancel.</li>
 *     <li>If the component is closed, the registered {@code onClosed} runnable (if any) is invoked.</li>
 * </ol>
 *
 * @author Pavel Castornii
 */
public interface CloseAwarePort {

    int CLOSE_SAFELY_MAX_ATTEMPTS = 5;

    /**
     * Returns the runnable to be invoked when a close action is triggered.
     *
     * @return the runnable, or {@code null} if no custom handler is set
     * @see #onCloseRequest()
     */
    Runnable getOnCloseRequest();

    /**
     * Sets the runnable to be invoked when a close action is triggered via {@code onCloseRequest()}.
     *
     * <p>The runnable is responsible for deciding whether and how to close the component. It may call
     * {@link #close()} for an immediate close, {@link #closeSafely()} for a coordinated close, or do nothing
     * to cancel the close. If set to {@code null}, the default behavior ({@link #closeSafely()}) is restored.
     *
     * @param runnable the runnable to invoke, or {@code null} to restore the default behavior
     */
    void setOnCloseRequest(Runnable runnable);

    /**
     * Closes this component immediately using a force close strategy.
     *
     * <p>This method immediately deinitializes the component and all its descendants and removes this component
     * from the component tree, without performing any readiness checks or preparation steps. This method should
     * only be used in exceptional situations where a forced shutdown is required.
     */
    void close();

    /**
     * Initiates a coordinated close of this component using a gentle close strategy.
     *
     * <p>This method traverses the component subtree, checks readiness, performs preparation if needed,
     * and only calls {@link #close()} when the entire subtree is ready. The algorithm works as follows:
     * <ol>
     *     <li>Traverse this component and all its descendants in breadth-first order.</li>
     *     <li>During the traversal, {@link #isReadyToClose()} is called for each component to obtain a snapshot
     *         of its current close state.</li>
     *     <li>If any component returns {@link CloseCheckResult#NOT_READY}, the traversal stops immediately and
     *         the close is aborted.</li>
     *     <li>If no {@code NOT_READY} components are found, the algorithm remembers the first component (if any)
     *         that returned {@link CloseCheckResult#PREPARATION_REQUIRED} while still completing the traversal
     *         to ensure that no {@code NOT_READY} components exist in the subtree.</li>
     *     <li>If a component requiring preparation was found, {@link #prepareToClose(Consumer)} is invoked for
     *         that single component, and the process is suspended until the preparation callback completes.</li>
     *     <li>After a successful preparation, the algorithm restarts from the beginning with a fresh traversal.</li>
     *     <li>When a full traversal completes with all components reporting {@link CloseCheckResult#READY},
     *         {@link #close()} is invoked to perform the actual close.</li>
     * </ol>
     *
     * <p>During a close operation the system may be live: new components can appear, existing components may
     * change state, and user actions may continue. The iterative check ensures that the close proceeds only
     * when the entire component subtree is truly ready.
     *
     * <p>This method never force-closes components. If any component cannot be closed after all attempts,
     * the operation is cancelled and no component is deinitialized.
     *
     * @param maxAttempts    maximum number of traversal iterations; if exceeded, the operation is aborted
     * @param resultConsumer a {@link Consumer} that receives the {@link CloseRequestResult} when the operation
     *                       finishes, is cancelled, or fails; may be {@code null}
     */
    void closeSafely(int maxAttempts, Consumer<CloseRequestResult> resultConsumer);

    /**
     * Initiates a coordinated close using the default maximum number of attempts
     * ({@link #CLOSE_SAFELY_MAX_ATTEMPTS}).
     *
     * @param resultConsumer a {@link Consumer} that receives the {@link CloseRequestResult}; may be {@code null}
     * @see #closeSafely(int, Consumer)
     */
    default void closeSafely(Consumer<CloseRequestResult> resultConsumer) {
        closeSafely(CLOSE_SAFELY_MAX_ATTEMPTS, resultConsumer);
    }

    /**
     * Initiates a coordinated close using the default maximum number of attempts, without a result callback.
     *
     * @see #closeSafely(int, Consumer)
     */
    default void closeSafely() {
        closeSafely(CLOSE_SAFELY_MAX_ATTEMPTS, null);
    }

    /**
     * Checks whether this component is ready to be closed.
     *
     * <p>This method must not perform any destructive actions or release resources. It is a pure readiness check.
     *
     * @return {@link CloseCheckResult#READY} if the component is ready to close,
     *         {@link CloseCheckResult#PREPARATION_REQUIRED} if preparation is needed before closing,
     *         or {@link CloseCheckResult#NOT_READY} if closing is currently disallowed
     */
    CloseCheckResult isReadyToClose();

    /**
     * Prepares this component for closing.
     *
     * <p>This method is invoked only if {@link #isReadyToClose()} returned
     * {@link CloseCheckResult#PREPARATION_REQUIRED}. Implementations may perform actions such as saving data,
     * stopping background tasks, or waiting for asynchronous operations to complete. Once the component is
     * fully prepared, the provided callback must be invoked.
     *
     * @param resultCallback the callback to invoke when preparation is complete, receiving a
     *                       {@link ClosePreparationResult} that describes the outcome
     */
    void prepareToClose(Consumer<ClosePreparationResult> resultCallback);

    /**
     * Returns the callback to be executed after this component has been fully closed and deinitialized.
     *
     * @return the callback, or {@code null} if none is set
     */
    Runnable getOnClosed();

    /**
     * Sets the callback to be executed after this component has been fully closed and deinitialized.
     *
     * @param runnable the callback to execute, or {@code null} to clear it
     */
    void setOnClosed(Runnable runnable);
}
