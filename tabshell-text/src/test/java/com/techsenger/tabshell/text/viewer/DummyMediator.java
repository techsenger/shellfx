/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.text.viewer;

import com.techsenger.patternfx.core.ChildViewModel;
import com.techsenger.patternfx.core.ComponentGroup;
import com.techsenger.patternfx.core.ComponentHistory;
import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.patternfx.core.ComponentState;
import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.patternfx.core.ParentViewModel;
import com.techsenger.patternfx.core.TreeIterator;
import com.techsenger.tabshell.shared.find.FindPaneMediator;
import java.util.UUID;
import java.util.function.BiConsumer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class DummyMediator implements FindPaneMediator {

    @Override
    public void deinitialize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ComponentName getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UUID getUuid() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getFullName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLogPrefix() {
        return null;
    }

    @Override
    public ComponentState getState() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ReadOnlyObjectProperty<ComponentState> stateProperty() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ObjectProperty<HistoryPolicy> historyPolicyProperty() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HistoryPolicy getHistoryPolicy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setHistoryPolicy(HistoryPolicy policy) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ComponentHistory<?> getHistory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ComponentGroup getGroup() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setGroup(ComponentGroup value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ObjectProperty<ComponentGroup> groupProperty() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ReadOnlyObjectProperty<ParentViewModel<?>> parentProperty() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ParentViewModel getParent() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ObservableList<ChildViewModel<?>> getChildren() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TreeIterator<ParentViewModel<?>> depthFirstIterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TreeIterator<ParentViewModel<?>> breadthFirstIterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toTreeString() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toTreeString(BiConsumer<ParentViewModel<?>, StringBuilder> componentAppender) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deinitializeTree() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
