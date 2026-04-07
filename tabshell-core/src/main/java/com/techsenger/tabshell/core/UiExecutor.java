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

import java.util.concurrent.Executor;
import javafx.application.Platform;

/**
 * Utility class for executing tasks on the UI thread.
 *
 * <p>In a JavaFX MVP architecture, Presenters often receive results from background threads (e.g. via
 * {@link java.util.concurrent.CompletableFuture}) and need to update the View, which must happen on the JavaFX
 * Application Thread. Placing {@code Platform.runLater()} directly in Presenters or View interfaces pollutes
 * those layers with infrastructure concerns and makes unit testing difficult.
 *
 * <p>This class solves the problem by providing a single, replaceable point of dispatch. In production, it delegates
 * to {@code Platform.runLater()}. In unit tests, the executor can be replaced with a synchronous implementation so
 * that no JavaFX runtime is required.
 *
 * @author Pavel Castornii
 */
public final class UiExecutor {

    private static volatile Executor executor = (Runnable r) -> {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    };

    /**
     * Executes the given task on the UI thread.
     *
     * @param r the task to execute, must not be null
     */
    public static void execute(Runnable r) {
        executor.execute(r);
    }

    /**
     * Replaces the underlying executor. Intended for use in unit tests to inject
     * a synchronous executor ({@code Runnable::run}) so that UI updates can be
     * verified without a running JavaFX runtime.
     *
     * @param executor the new executor, must not be null
     */
    public static void setExecutor(Executor executor) {
        UiExecutor.executor = executor;
    }

    private UiExecutor() {
        // empty
    }
}
