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

package com.techsenger.shellfx.devtools.event;

import com.techsenger.connectorfx.event.ConnectorEvent;
import com.techsenger.shellfx.shared.find.FindBasePort;
import java.util.Set;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;

/**
 * Provides minimal, read-only access to the component's client API.
 *
 * @author Pavel Castornii
 */
public interface EventToolBarPort extends FindBasePort {

    Set<Class<? extends ConnectorEvent>> getSelectedEventTypes();

    boolean isFilterSelected();

    ReadOnlyBooleanProperty filterSelectedProperty();

    boolean isSelectedNodeOnly();

    ReadOnlyBooleanProperty selectedNodeOnlyProperty();

    ReadOnlyBooleanProperty recordSelectedProperty();

    ReadOnlyStringProperty statisticsProperty();
}
