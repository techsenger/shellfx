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

package com.techsenger.shellfx.material.menu;

import com.techsenger.toolkit.fx.FxPlatform;

/**
 * Starts the JavaFX toolkit once for the menu tests and runs their code on the FX Application Thread, so a failed
 * assertion inside it fails the test.
 *
 * @author Pavel Castornii
 */
final class MenuTestSupport {

    private static boolean started;

    static synchronized void start() throws InterruptedException {
        if (started) {
            return;
        }
        try {
            System.setProperty("glass.platform", "Headless");
            System.setProperty("prism.order", "sw");
            FxPlatform.start();
        } catch (IllegalStateException alreadyStarted) {
            // toolkit already running in this JVM (e.g. started by another test class); nothing to do
        }
        started = true;
    }

    /**
     * Runs {@code action} on the FX Application Thread and blocks the test thread until it completes; whatever it
     * throws is rethrown as an {@link AssertionError}.
     */
    static void runOnFx(Runnable action) throws InterruptedException {
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

    private MenuTestSupport() {
        // empty
    }
}
