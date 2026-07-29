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

package com.techsenger.shellfx.material.list;

import atlantafx.base.theme.PrimerLight;
import com.techsenger.toolkit.fx.FxPlatform;
import java.util.function.Supplier;
import javafx.application.Application;

/**
 * Shared JavaFX Application Thread setup for tests in this package that need a real, shown {@code Stage}
 * ({@code ColumnViewUtilsTest}, {@code ColumnListViewTest}, {@code ColumnTileViewTest}) &mdash; one place to
 * start the toolkit, apply a real AtlantaFX user-agent stylesheet, and hop onto the FX thread, instead of each
 * test class duplicating it.
 *
 * <p>The AtlantaFX stylesheet matters, not just cosmetically: {@code column-list-view.css}/
 * {@code column-tile-view.css} both use lookups like {@code -color-fg-default}, which are AtlantaFX theme
 * variables. Without a real user-agent stylesheet applied, every such lookup fails to resolve (logged as a
 * {@code CssStyleHelper} warning per cell) - which is harmless for tests asserting on indices/geometry, but
 * would silently mask a real regression in tests that ever assert on measured height/width, since a failed
 * lookup falls back to a default rather than the value production code would actually see.
 *
 * @author Pavel Castornii
 */
final class FxTestSupport {

    private static volatile boolean started;

    static synchronized void start() throws InterruptedException {
        if (started) {
            return;
        }
        System.setProperty("glass.platform", "Headless");
        System.setProperty("prism.order", "sw");
        FxPlatform.start();
        FxPlatform.runLaterAndWait(() -> {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        });
        started = true;
    }

    /**
     * Runs {@code action} on the FX Application Thread and returns its result, blocking the calling (test)
     * thread until it completes.
     */
    static <T> T onFxThread(Supplier<T> action) throws InterruptedException {
        var box = new Object[1];
        var error = new Throwable[1];
        FxPlatform.runLaterAndWait(() -> {
            try {
                box[0] = action.get();
            } catch (Throwable t) {
                error[0] = t;
            }
        });
        if (error[0] != null) {
            throw new AssertionError("Action on FX thread failed: " + error[0], error[0]);
        }
        @SuppressWarnings("unchecked")
        var result = (T) box[0];
        return result;
    }

    private FxTestSupport() {
        // empty
    }
}
