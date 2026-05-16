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

package com.techsenger.tabshell.layout.pagehost;

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.tabshell.layout.LayoutComponents;
import com.techsenger.tabshell.shared.find.AbstractFindBasePresenter;

/**
 *
 * @author Pavel Castornii
 */
public class FindPanelPresenter<V extends FindPanelView> extends AbstractFindBasePresenter<V> {

    private final PageHostFindPort pageHost;

    public FindPanelPresenter(V view, FindPanelParams params) {
        super(view, params);
        this.pageHost = params.getPageHost();
    }

    @Override
    protected void onFind() {
        pageHost.onFind(getFindText());
    }

    @Override
    protected void onFindCleared() {
        pageHost.onFindCleared();
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(LayoutComponents.PAGE_FIND_PANEL);
    }
}
