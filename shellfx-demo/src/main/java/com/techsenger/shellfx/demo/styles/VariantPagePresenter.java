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

package com.techsenger.shellfx.demo.styles;

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.shellfx.core.page.AbstractPagePresenter;
import com.techsenger.shellfx.core.page.PageParams;
import com.techsenger.shellfx.demo.DemoComponents;

/**
 *
 * @author Pavel Castornii
 */
public class VariantPagePresenter extends AbstractPagePresenter<VariantPageView> {

    public VariantPagePresenter(VariantPageView view, PageParams params) {
        super(view, params);
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(DemoComponents.VARIANT_PAGE);
    }

}
