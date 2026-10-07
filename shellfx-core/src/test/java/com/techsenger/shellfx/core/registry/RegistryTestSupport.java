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

package com.techsenger.shellfx.core.registry;

import com.techsenger.patternfx.mvvm.ParentView;
import com.techsenger.toolkit.fx.FxPlatform;
import java.util.function.Function;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

/**
 * Shared fixture of the builder integration tests: starts the JavaFX toolkit, creates the view the builders are
 * given, and creates menu items and menus.
 *
 * @author Pavel Castornii
 */
final class RegistryTestSupport {

    private static boolean started;

    static synchronized void startFxToolkit() {
        if (started) {
            return;
        }
        try {
            System.setProperty("glass.platform", "Headless");
            FxPlatform.start();
        } catch (IllegalStateException alreadyStarted) {
            // toolkit already running in this JVM (e.g. started by another test class); nothing to do
        }
        started = true;
    }

    /**
     * Creates the view the builders are given; they use its class to pick the registrations that apply and its
     * view model to build the log prefix of a component, so the mock answers with mocks.
     */
    static ParentView<?> createView() {
        return mock(ParentView.class, RETURNS_DEEP_STUBS);
    }

    /**
     * Wraps a plain function into the factory of a provider that sets the control it creates; a test that has
     * nothing to unhook does not need a provider class of its own.
     */
    static <C> ControlProviderFactory<ParentView<?>, C> provider(Function<ParentView<?>, C> factory) {
        return () -> new SimpleControlProvider<>() {
            @Override
            public void initialize(ParentView<?> view) {
                super.initialize(view);
                setControl(factory.apply(view));
            }
        };
    }

    /**
     * Creates a menu item, as the registered factories do.
     */
    static MenuItem createItem(String text) {
        return new MenuItem(text);
    }

    /**
     * Creates a menu, as the registered factories do.
     */
    static Menu createMenu(String text) {
        return new Menu(text);
    }

    private RegistryTestSupport() {
        // empty
    }
}
