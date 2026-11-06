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

import java.util.List;
import javafx.scene.control.MenuItem;

/**
 * Implemented by the view of a component whose menu items have accelerators that must work while the component is in
 * use. The owner of an {@link AcceleratorManager} asks such components for their items every time it updates the
 * accelerators, so a component that has nothing to give at the moment just returns an empty list.
 *
 * <p>If several items have the same accelerator, the item of the component that comes later in the list given to
 * the manager wins.
 *
 * @author Pavel Castornii
 */
public interface AcceleratorAware {

    /**
     * Returns the items whose accelerators are in effect while the component is in use.
     *
     * @return the items, empty if there are none
     */
    List<MenuItem> getAcceleratorItems();
}
