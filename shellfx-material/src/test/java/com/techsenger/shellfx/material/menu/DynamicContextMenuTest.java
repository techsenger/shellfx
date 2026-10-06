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

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.WindowEvent;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests of {@link DynamicContextMenu}: showing is refused when no item is visible or a condition is false, and the
 * separators around empty groups are hidden. The event is fired the way the window does it; whether the popup is
 * really kept from showing is not observable without a screen, so the tests look at whether the handler of the menu
 * consumed the event, which a handler registered after it can see.
 *
 * @author Pavel Castornii
 */
class DynamicContextMenuTest {

    @BeforeAll
    static void startFx() throws InterruptedException {
        MenuTestSupport.start();
    }

    /**
     * Fires the showing event at the menu and tells whether the menu refused to be shown, that is whether its own
     * handler consumed the event.
     */
    private static boolean isShowingRefused(DynamicContextMenu menu) {
        var consumed = new boolean[1];
        menu.addEventHandler(WindowEvent.WINDOW_SHOWING, e -> consumed[0] = e.isConsumed());
        Event.fireEvent(menu, new WindowEvent(menu, WindowEvent.WINDOW_SHOWING));
        return consumed[0];
    }

    @Test
    void menuWithoutItems_refusesToBeShown() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicContextMenu();

            assertThat(isShowingRefused(menu)).isTrue();
        });
    }

    @Test
    void menuWithVisibleItem_isShown() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicContextMenu(new MenuItem("a"));

            assertThat(isShowingRefused(menu)).isFalse();
        });
    }

    @Test
    void menuWithOnlyHiddenItemsAndSeparators_refusesToBeShown() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var hidden = new MenuItem("a");
            hidden.setVisible(false);
            var menu = new DynamicContextMenu(hidden, new SeparatorMenuItem());

            assertThat(isShowingRefused(menu)).isTrue();
        });
    }

    @Test
    void visibilityOfItems_isReadEveryTimeTheMenuIsShown() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var item = new MenuItem("a");
            var menu = new DynamicContextMenu(item);
            assertThat(isShowingRefused(menu)).isFalse();

            item.setVisible(false);
            assertThat(isShowingRefused(menu)).isTrue();

            item.setVisible(true);
            assertThat(isShowingRefused(menu)).isFalse();
        });
    }

    @Test
    void falseCondition_refusesShowingAndTrueConditionAllowsIt() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicContextMenu(new MenuItem("a"));
            var condition = new SimpleBooleanProperty(false);
            menu.addVisibleCondition(condition);
            assertThat(isShowingRefused(menu)).isTrue();

            condition.set(true);
            assertThat(isShowingRefused(menu)).isFalse();
        });
    }

    @Test
    void trueCondition_doesNotAllowMenuWithoutVisibleItems() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicContextMenu();
            menu.addVisibleCondition(new SimpleBooleanProperty(true));

            assertThat(isShowingRefused(menu)).isTrue();
        });
    }

    @Test
    void severalConditions_allMustBeTrue() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicContextMenu(new MenuItem("a"));
            var first = new SimpleBooleanProperty(true);
            var second = new SimpleBooleanProperty(false);
            menu.addVisibleCondition(first);
            menu.addVisibleCondition(second);
            assertThat(isShowingRefused(menu)).isTrue();

            second.set(true);
            assertThat(isShowingRefused(menu)).isFalse();

            first.set(false);
            assertThat(isShowingRefused(menu)).isTrue();
        });
    }

    @Test
    void nullValueOfCondition_countsAsFalse() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicContextMenu(new MenuItem("a"));
            menu.addVisibleCondition(new SimpleObjectProperty<Boolean>(null));

            assertThat(isShowingRefused(menu)).isTrue();
        });
    }

    @Test
    void removingCondition_makesTheMenuIndependentOfIt() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicContextMenu(new MenuItem("a"));
            var condition = new SimpleBooleanProperty(false);
            menu.addVisibleCondition(condition);
            assertThat(isShowingRefused(menu)).isTrue();

            menu.removeVisibleCondition(condition);

            assertThat(isShowingRefused(menu)).isFalse();
        });
    }

    @Test
    void removingUnknownCondition_doesNothing() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicContextMenu(new MenuItem("a"));

            menu.removeVisibleCondition(new SimpleBooleanProperty(false));

            assertThat(isShowingRefused(menu)).isFalse();
        });
    }

    @Test
    void addingSameConditionTwice_countsItOnce() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicContextMenu(new MenuItem("a"));
            var condition = new SimpleBooleanProperty(false);

            menu.addVisibleCondition(condition);
            menu.addVisibleCondition(condition);
            menu.removeVisibleCondition(condition);

            assertThat(isShowingRefused(menu)).isFalse();
        });
    }

    @Test
    void showingMenu_hidesSeparatorsAroundEmptyGroups() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var hiddenFirst = new MenuItem("a");
            hiddenFirst.setVisible(false);
            var visible = new MenuItem("b");
            var hiddenLast = new MenuItem("c");
            hiddenLast.setVisible(false);
            var firstSeparator = new SeparatorMenuItem();
            var secondSeparator = new SeparatorMenuItem();
            var menu = new DynamicContextMenu(hiddenFirst, firstSeparator, visible, secondSeparator, hiddenLast);

            isShowingRefused(menu);

            assertThat(firstSeparator.isVisible()).isFalse();
            assertThat(secondSeparator.isVisible()).isFalse();
        });
    }

    @Test
    void showingMenu_keepsSeparatorBetweenTwoVisibleGroups() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var separator = new SeparatorMenuItem();
            var menu = new DynamicContextMenu(new MenuItem("a"), separator, new MenuItem("b"));

            isShowingRefused(menu);

            assertThat(separator.isVisible()).isTrue();
        });
    }
}
