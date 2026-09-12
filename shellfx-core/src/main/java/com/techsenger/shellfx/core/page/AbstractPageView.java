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

package com.techsenger.shellfx.core.page;

import com.techsenger.shellfx.core.area.AbstractAreaView;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractPageView<VM extends AbstractPageViewModel<?>>
        extends AbstractAreaView<VM> implements PageView<VM> {

    public class Composer extends AbstractAreaView<VM>.Composer implements PageView.Composer {

    }

    public AbstractPageView(VM viewModel) {
        super(viewModel);
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new AbstractPageView<VM>.Composer();
    }
}
