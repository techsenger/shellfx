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

package com.techsenger.shellfx.layout.pagehost;

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.core.area.AreaParams;
import com.techsenger.shellfx.shared.find.FindPanelConfig;
import java.util.Objects;

/**
 *
 * @author Pavel Castornii
 */
public class FindPanelParams extends AreaParams {

    private final FindPageHostPort pageHost;

    public FindPanelParams(@Nullable FindPanelConfig config, FindPageHostPort pageHost) {
        super(config);
        this.pageHost = pageHost;
    }

    public FindPageHostPort getPageHost() {
        return pageHost;
    }

    @Override
    public @Nullable FindPanelConfig getConfig() {
        return (FindPanelConfig) super.getConfig();
    }

    @Override
    public void validate() {
        super.validate();
        Objects.requireNonNull(pageHost);
    }
}
