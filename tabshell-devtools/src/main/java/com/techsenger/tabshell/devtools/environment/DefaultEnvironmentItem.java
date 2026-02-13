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

package com.techsenger.tabshell.devtools.environment;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultEnvironmentItem implements EnvironmentItem {

    private final EnvironmentItemType type;

    private final String name;

    private final String value;

    private final boolean expanded;

    public DefaultEnvironmentItem(EnvironmentItemType type, String name, String value, boolean expanded) {
        this.type = type;
        this.name = name;
        this.value = value;
        this.expanded = expanded;
    }

    @Override
    public EnvironmentItemType getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isExpanded() {
        return this.expanded;
    }

    @Override
    public void setExpanded(boolean expanded) {

    }
}
