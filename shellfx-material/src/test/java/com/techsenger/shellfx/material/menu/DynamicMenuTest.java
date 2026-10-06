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

import java.lang.ref.WeakReference;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests of {@link DynamicMenu}: its visibility follows its items and its conditions all the time, not only when it
 * is shown, and it does not stay reachable from what its conditions observe.
 *
 * @author Pavel Castornii
 */
class DynamicMenuTest {

    @BeforeAll
    static void startFx() throws InterruptedException {
        MenuTestSupport.start();
    }

    @Test
    void emptyMenu_isNotVisible() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicMenu("Menu");

            assertThat(menu.isVisible()).isFalse();
        });
    }

    @Test
    void menuWithVisibleItem_isVisible() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicMenu("Menu");

            menu.getItems().add(new MenuItem("a"));

            assertThat(menu.isVisible()).isTrue();
        });
    }

    @Test
    void hidingAndShowingTheOnlyItem_updatesTheMenuWithoutShowingIt() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicMenu("Menu");
            var item = new MenuItem("a");
            menu.getItems().add(item);

            item.setVisible(false);
            assertThat(menu.isVisible()).isFalse();

            item.setVisible(true);
            assertThat(menu.isVisible()).isTrue();
        });
    }

    @Test
    void itemsThatAreHiddenOrSeparators_doNotMakeTheMenuVisible() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicMenu("Menu");
            var hidden = new MenuItem("a");
            hidden.setVisible(false);

            menu.getItems().addAll(hidden, new SeparatorMenuItem());

            assertThat(menu.isVisible()).isFalse();
        });
    }

    @Test
    void addingAndRemovingItems_updatesVisibility() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicMenu("Menu");
            var first = new MenuItem("a");
            var second = new MenuItem("b");
            menu.getItems().addAll(first, second);

            menu.getItems().remove(first);
            assertThat(menu.isVisible()).isTrue();

            menu.getItems().remove(second);
            assertThat(menu.isVisible()).isFalse();
        });
    }

    @Test
    void nestedDynamicMenu_isVisibleOnlyWhenItHasVisibleItems() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var parent = new DynamicMenu("Parent");
            var child = new DynamicMenu("Child");
            parent.getItems().add(child);
            assertThat(parent.isVisible()).isFalse();

            var item = new MenuItem("a");
            child.getItems().add(item);
            assertThat(child.isVisible()).isTrue();
            assertThat(parent.isVisible()).isTrue();

            item.setVisible(false);
            assertThat(parent.isVisible()).isFalse();
        });
    }

    @Test
    void visibleProperty_isBoundAndCannotBeSet() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicMenu("Menu");

            assertThat(menu.visibleProperty().isBound()).isTrue();
            assertThatThrownBy(() -> menu.setVisible(true)).isInstanceOf(RuntimeException.class);
        });
    }

    @Test
    void falseCondition_hidesMenuWithVisibleItem() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicMenu("Menu");
            menu.getItems().add(new MenuItem("a"));

            menu.addVisibleCondition(new SimpleBooleanProperty(false));

            assertThat(menu.isVisible()).isFalse();
        });
    }

    @Test
    void trueCondition_doesNotShowMenuWithoutVisibleItems() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicMenu("Menu");

            menu.addVisibleCondition(new SimpleBooleanProperty(true));

            assertThat(menu.isVisible()).isFalse();
        });
    }

    @Test
    void changingCondition_updatesVisibility() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicMenu("Menu");
            menu.getItems().add(new MenuItem("a"));
            var condition = new SimpleBooleanProperty(true);
            menu.addVisibleCondition(condition);
            assertThat(menu.isVisible()).isTrue();

            condition.set(false);
            assertThat(menu.isVisible()).isFalse();

            condition.set(true);
            assertThat(menu.isVisible()).isTrue();
        });
    }

    @Test
    void severalConditions_allMustBeTrue() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicMenu("Menu");
            menu.getItems().add(new MenuItem("a"));
            var first = new SimpleBooleanProperty(true);
            var second = new SimpleBooleanProperty(true);
            menu.addVisibleCondition(first);
            menu.addVisibleCondition(second);
            assertThat(menu.isVisible()).isTrue();

            second.set(false);
            assertThat(menu.isVisible()).isFalse();

            first.set(false);
            second.set(true);
            assertThat(menu.isVisible()).isFalse();

            first.set(true);
            assertThat(menu.isVisible()).isTrue();
        });
    }

    @Test
    void nullValueOfCondition_countsAsFalse() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicMenu("Menu");
            menu.getItems().add(new MenuItem("a"));
            var condition = new SimpleObjectProperty<Boolean>(null);

            menu.addVisibleCondition(condition);
            assertThat(menu.isVisible()).isFalse();

            condition.set(true);
            assertThat(menu.isVisible()).isTrue();
        });
    }

    @Test
    void removingCondition_makesTheMenuIndependentOfIt() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicMenu("Menu");
            menu.getItems().add(new MenuItem("a"));
            var condition = new SimpleBooleanProperty(false);
            menu.addVisibleCondition(condition);
            assertThat(menu.isVisible()).isFalse();

            menu.removeVisibleCondition(condition);
            assertThat(menu.isVisible()).isTrue();

            condition.set(false);
            assertThat(menu.isVisible()).isTrue();
        });
    }

    @Test
    void removingUnknownCondition_doesNothing() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicMenu("Menu");
            menu.getItems().add(new MenuItem("a"));

            menu.removeVisibleCondition(new SimpleBooleanProperty(false));

            assertThat(menu.isVisible()).isTrue();
        });
    }

    @Test
    void addingSameConditionTwice_countsItOnce() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicMenu("Menu");
            menu.getItems().add(new MenuItem("a"));
            var condition = new SimpleBooleanProperty(false);

            menu.addVisibleCondition(condition);
            menu.addVisibleCondition(condition);
            menu.removeVisibleCondition(condition);

            assertThat(menu.isVisible()).isTrue();
        });
    }

    @Test
    void conditionFromMap_followsItsSourceAndCanBeRemoved() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicMenu("Menu");
            menu.getItems().add(new MenuItem("a"));
            var source = new SimpleObjectProperty<String>();
            var condition = source.map(value -> "on".equals(value));

            menu.addVisibleCondition(condition);
            assertThat(menu.isVisible()).isFalse();

            source.set("on");
            assertThat(menu.isVisible()).isTrue();

            source.set("off");
            assertThat(menu.isVisible()).isFalse();

            menu.removeVisibleCondition(condition);
            assertThat(menu.isVisible()).isTrue();
        });
    }

    @Test
    void showingMenu_hidesSeparatorsAroundEmptyGroups() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicMenu("Menu");
            var hiddenFirst = new MenuItem("a");
            hiddenFirst.setVisible(false);
            var visible = new MenuItem("b");
            var hiddenLast = new MenuItem("c");
            hiddenLast.setVisible(false);
            var firstSeparator = new SeparatorMenuItem();
            var secondSeparator = new SeparatorMenuItem();
            menu.getItems().addAll(hiddenFirst, firstSeparator, visible, secondSeparator, hiddenLast);

            Event.fireEvent(menu, new Event(Menu.ON_SHOWING));

            assertThat(firstSeparator.isVisible()).isFalse();
            assertThat(secondSeparator.isVisible()).isFalse();
        });
    }

    @Test
    void showingMenu_keepsSeparatorBetweenTwoVisibleGroups() throws InterruptedException {
        MenuTestSupport.runOnFx(() -> {
            var menu = new DynamicMenu("Menu");
            var separator = new SeparatorMenuItem();
            menu.getItems().addAll(new MenuItem("a"), separator, new MenuItem("b"));

            Event.fireEvent(menu, new Event(Menu.ON_SHOWING));

            assertThat(separator.isVisible()).isTrue();
        });
    }

    @Test
    void menuWithCondition_canBeCollectedWithoutRemovingTheCondition() throws InterruptedException {
        var source = new SimpleBooleanProperty(true);
        var reference = new WeakReference<DynamicMenu>(createMenuWithCondition(source));

        for (var i = 0; i < 20 && reference.get() != null; i++) {
            System.gc();
            Thread.sleep(50);
        }

        assertThat(reference.get()).isNull();
        assertThat(source.get()).isTrue();
    }

    private DynamicMenu createMenuWithCondition(SimpleBooleanProperty source) throws InterruptedException {
        var menu = new DynamicMenu[1];
        MenuTestSupport.runOnFx(() -> {
            menu[0] = new DynamicMenu("Menu");
            menu[0].getItems().add(new MenuItem("a"));
            menu[0].addVisibleCondition(source);
        });
        return menu[0];
    }
}
