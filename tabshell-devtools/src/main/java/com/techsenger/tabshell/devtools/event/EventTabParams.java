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

package com.techsenger.tabshell.devtools.event;

import com.techsenger.connectorfx.Connector;
import com.techsenger.tabshell.core.tab.TabParams;
import com.techsenger.tabshell.devtools.Selector;
import java.util.Objects;

/**
 *
 * @author Pavel Castornii
 */
public class EventTabParams extends TabParams {

    private final Connector connector;

    private final Selector selector;

    public EventTabParams(Connector connector, Selector selector) {
        this.connector = connector;
        this.selector = selector;
    }

    public Connector getConnector() {
        return connector;
    }

    public Selector getSelector() {
        return selector;
    }

    @Override
    protected void validate() {
        super.validate();
        Objects.requireNonNull(connector);
        Objects.requireNonNull(selector);
    }
}
