/*
 * Copyright 2026 Pavel Castornii.
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

package com.techsenger.shellfx.core.window;

import com.techsenger.annotations.Nullable;
import com.techsenger.patternfx.mvvm.ParentView;
import java.util.List;
import java.util.function.Supplier;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Puts the accelerators of the menu items into the accelerators of a scene and keeps them in step with the items. A
 * window does not own it: whoever needs it creates it for the scene it manages.
 *
 * <p>The problem is in JavaFX itself: the accelerators of a scene, {@code Scene#getAccelerators()}, are one map from a
 * key combination to one {@code Runnable}. A menu item that has an accelerator puts its entry into the map when it gets
 * into the scene and removes the entry by the key when it leaves the scene, without checking whose entry it is. It
 * works while there is one item per key. When a component is opened in several instances (several tabs with the same
 * menu, a context menu in every area), their items have the same accelerators: the entry of the instance that was
 * attached last wins, the key is removed as soon as any instance goes away, and the item that is fired may belong to an
 * instance the user is not working with. The manager installs exactly one entry per key, taken from the items that
 * matter now, the ones of the instances that are in use. The owner calls {@link #update(List)} every time that set
 * changes or the accelerators of the items change, for example after the user has reassigned the hotkeys.
 *
 * <p>Only the entries of the manager are removed from the map, so the accelerators that other controls of the scene put
 * there, such as the default button of a dialog, stay intact. The manager recognizes its entries by the
 * {@code Runnable} of its own kind that it puts, and it defends them: if JavaFX replaces or removes such an entry, for
 * example when an item gets into the scene or leaves it, the manager puts its entry back. So the items may be installed
 * by JavaFX in the usual way, with a menu bar or {@code Control#setContextMenu}, and the manager corrects the result.
 * It does not check that different actions have different hotkeys: if two items have the same accelerator, the later
 * one wins.
 *
 * @author Pavel Castornii
 */
public final class AcceleratorManager {

    /**
     * A component to ask for items.
     *
     * @param depth the depth of the component in the component tree; it is only used to indent the log
     * @param view the component; it has items only if it implements {@link AcceleratorAware}
     */
    public record Candidate(int depth, ParentView<?> view) {

    }

    /**
     * The entry of a manager in the accelerators of a scene: fires the item. It is the mark by which the manager
     * recognizes its entries.
     */
    private record ItemRunnable(AcceleratorManager manager, MenuItem item) implements Runnable {

        @Override
        public void run() {
            if (!item.isDisable()) {
                item.fire();
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(AcceleratorManager.class);

    private final Supplier<Scene> sceneSupplier;

    private final MapChangeListener<KeyCombination, Runnable> acceleratorsListener = this::restoreEntry;

    private @Nullable ObservableMap<KeyCombination, Runnable> observed;

    private boolean updating;

    /**
     * Creates a manager.
     *
     * @param sceneSupplier supplies the scene whose accelerators are managed
     */
    public AcceleratorManager(Supplier<Scene> sceneSupplier) {
        this.sceneSupplier = sceneSupplier;
    }

    /**
     * Replaces the entries of the previous call with the accelerators of the items of the given components, collected
     * in one pass. An item is fired only if it is not disabled. If debug logging is on, the components with the number
     * of their accelerators are logged as a tree, followed by the accelerators of the components that have any.
     *
     * @param candidates the components to ask for items, in the order of their priority: if several items have the
     *     same accelerator, the one of the component that comes later wins
     */
    public void update(List<Candidate> candidates) {
        var accelerators = sceneSupplier.get().getAccelerators();
        if (observed == null) {
            accelerators.addListener(acceleratorsListener);
            observed = accelerators;
        }
        var debug = logger.isDebugEnabled();
        var tree = debug ? new StringBuilder() : null;
        var details = debug ? new StringBuilder() : null;
        updating = true;
        try {
            clear();
            for (var candidate : candidates) {
                var view = candidate.view();
                var items = view instanceof AcceleratorAware aware ? aware.getAcceleratorItems() : List.<MenuItem>of();
                var name = tree == null ? "" : view.getViewModel().getDescriptor().getFullName();
                var count = 0;
                for (var item : items) {
                    var combination = item.getAccelerator();
                    if (combination != null) {
                        accelerators.put(combination, new ItemRunnable(this, item));
                        count++;
                        if (details != null) {
                            if (count == 1) {
                                details.append(name).append('\n');
                            }
                            details.append("    [").append(combination.getDisplayText()).append("] ")
                                    .append(item.getText().replace("_", "")).append('\n');
                        }
                    }
                }
                if (tree != null) {
                    tree.append("    ".repeat(candidate.depth())).append(name).append(" : ").append(count)
                            .append(" accelerators\n");
                }
            }
        } finally {
            updating = false;
        }
        if (tree != null && details != null) {
            logger.debug("Accelerators of the scene:\n{}", tree.append('\n').append(details));
        }
    }

    /**
     * Removes the entries of the manager from the scene and stops watching the accelerators of the scene.
     */
    public void dispose() {
        var accelerators = observed;
        if (accelerators != null) {
            accelerators.removeListener(acceleratorsListener);
            observed = null;
        }
        updating = true;
        try {
            clear();
        } finally {
            updating = false;
        }
    }

    /**
     * Puts the entry of the manager back if somebody else has replaced or removed it.
     */
    private void restoreEntry(MapChangeListener.Change<? extends KeyCombination, ? extends Runnable> change) {
        var accelerators = observed;
        if (updating || accelerators == null || !change.wasRemoved()) {
            return;
        }
        var removed = change.getValueRemoved();
        if (removed instanceof ItemRunnable own && own.manager() == this
                && accelerators.get(change.getKey()) != removed) {
            updating = true;
            try {
                accelerators.put(change.getKey(), removed);
            } finally {
                updating = false;
            }
        }
    }

    private void clear() {
        sceneSupplier.get().getAccelerators().values()
                .removeIf(runnable -> runnable instanceof ItemRunnable own && own.manager() == this);
    }
}
