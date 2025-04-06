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

package com.techsenger.tabshell.tabs.workertab;

import com.techsenger.tabshell.tabs.splittab.SplitTabHelper;

/**
 *
 * @author Pavel Castornii
 */
public class WorkerTabHelper<T extends AbstractWorkerTabView<?>> extends SplitTabHelper<T> {

    public WorkerTabHelper(T view) {
        super(view);
    }

    public void openWorkerReportTab(WorkerReportTabViewModel viewModel) {
        var reportView = new WorkerReportTabView(viewModel);
        reportView.initialize();
        getView().getBottomTabManager().openTab(reportView);
    }
}
