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
import com.techsenger.shellfx.material.menu.MenuHandler;
import com.techsenger.shellfx.material.menu.MenuItemHandler;
import com.techsenger.toolkit.fx.FxPlatform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import static org.mockito.Mockito.mock;

/**
 * Shared fixture of the builder integration tests: starts the JavaFX toolkit, creates the view the builders are
 * given, and creates menu items the way the managed controls are made.
 *
 * @author Pavel Castornii
 */
final class RegistryTestSupport {

    private static final class NoOpMenuHandler implements MenuHandler<ParentView<?>> {

        @Override
        public void onUpdate() {
            // empty
        }

        @Override
        public void onShowing() {
            // empty
        }

        @Override
        public void onHiding() {
            // empty
        }
    }

    private static final class NoOpItemHandler implements MenuItemHandler<ParentView<?>> {

        @Override
        public void onAction() {
            // empty
        }

        @Override
        public void onUpdate() {
            // empty
        }

        @Override
        public void onShowing() {
            // empty
        }

        @Override
        public void onHiding() {
            // empty
        }
    }

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
     * Creates the view the builders are given; they only use its class to pick the registrations that apply.
     */
    static ParentView<?> createView() {
        return mock(ParentView.class);
    }

    /**
     * Creates a menu item with a handler, as the registered factories do.
     */
    static MenuItem createItem(String text) {
        var item = new MenuItem(text);
        MenuItemHandler.setHandler(item, new NoOpItemHandler());
        return item;
    }

    /**
     * Creates a menu with a handler, as the registered factories do.
     */
    static Menu createMenu(String text) {
        var menu = new Menu(text);
        MenuHandler.setHandler(menu, new NoOpMenuHandler());
        return menu;
    }

    private RegistryTestSupport() {
        // empty
    }
}
