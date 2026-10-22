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

package com.techsenger.shellfx.test;

import atlantafx.base.theme.PrimerLight;
import com.techsenger.toolkit.fx.FxPlatform;
import java.util.function.Supplier;
import javafx.application.Application;

/**
 * Shared JavaFX Application Thread setup for the tests of all modules: starts the toolkit once per JVM on the
 * headless platform with the software renderer, applies a real AtlantaFX user-agent stylesheet, and runs code on the
 * FX thread so that a failed assertion inside it fails the test.
 *
 * <p>The AtlantaFX stylesheet matters, not just cosmetically: the stylesheets of the platform use lookups like
 * {@code -color-fg-default}, which are AtlantaFX theme variables. Without a real user-agent stylesheet every such
 * lookup fails to resolve and falls back to a default, which would silently mask a real regression in tests that
 * assert on measured height or width.
 *
 * @author Pavel Castornii
 */
public final class FxTestSupport {

    private static boolean started;

    /**
     * Starts the toolkit if it is not started yet; can be called many times. An interruption while the stylesheet is
     * being applied is reported as an {@link IllegalStateException} with the interrupted flag restored.
     */
    public static synchronized void start() {
        if (started) {
            return;
        }
        System.setProperty("glass.platform", "Headless");
        System.setProperty("prism.order", "sw");
        try {
            FxPlatform.start();
        } catch (IllegalStateException alreadyStarted) {
            // toolkit already running in this JVM (e.g. started by another test class); nothing to do
        }
        try {
            FxPlatform.runLaterAndWait(() -> {
                Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
            });
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while starting the JavaFX toolkit", ex);
        }
        started = true;
    }

    /**
     * Runs {@code action} on the FX Application Thread and blocks the test thread until it completes; whatever it
     * throws is rethrown as an {@link AssertionError}.
     *
     * @param action the code to run
     * @throws InterruptedException if the test thread is interrupted while waiting
     */
    public static void runOnFx(Runnable action) throws InterruptedException {
        var error = new Throwable[1];
        FxPlatform.runLaterAndWait(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                error[0] = t;
            }
        });
        if (error[0] != null) {
            throw new AssertionError("Action on FX thread failed: " + error[0], error[0]);
        }
    }

    /**
     * Runs {@code action} on the FX Application Thread and returns its result, blocking the test thread until it
     * completes; whatever it throws is rethrown as an {@link AssertionError}.
     *
     * @param <T> the type of the result
     * @param action the code to run
     * @return the result of the action
     * @throws InterruptedException if the test thread is interrupted while waiting
     */
    public static <T> T onFxThread(Supplier<T> action) throws InterruptedException {
        var box = new Object[1];
        runOnFx(() -> box[0] = action.get());
        @SuppressWarnings("unchecked")
        var result = (T) box[0];
        return result;
    }

    private FxTestSupport() {
        // empty
    }
}
