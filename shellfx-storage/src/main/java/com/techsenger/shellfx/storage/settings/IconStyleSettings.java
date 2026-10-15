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

package com.techsenger.shellfx.storage.settings;

import com.techsenger.annotations.Nullable;

/**
 * Settings of the icon of a file. A {@code null} value means that the value is inherited from the style underneath.
 *
 * @author Pavel Castornii
 */
public final class IconStyleSettings {

    private @Nullable Integer color;

    public @Nullable Integer getColor() {
        return this.color;
    }

    public void setColor(@Nullable Integer color) {
        this.color = color;
    }
}
