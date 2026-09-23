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

import com.techsenger.patternfx.mvvm.ChildComposer;
import com.techsenger.shellfx.shared.find.AbstractFindViewModel;
import com.techsenger.shellfx.shared.find.FindTrigger;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author Pavel Castornii
 */
public class FindPanelViewModel<C extends ChildComposer> extends AbstractFindViewModel<C, PageFindResult> {

    private final FindPageHostPort pageHost;

    public FindPanelViewModel(FindPanelParams params) {
        super(params, FindTrigger.ON_TYPE);
        this.pageHost = params.getPageHost();
    }

    @Override
    protected CompletableFuture<PageFindResult> onFind() {
        return pageHost.onFind(getFindText());
    }

    @Override
    protected void onFindCleared() {
        pageHost.onFindCleared();
    }
}
