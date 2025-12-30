///*
// * Copyright 2024-2025 Pavel Castornii.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.techsenger.tabshell.layout.workertab;
//
//import com.techsenger.patternfx.mvvmx.ComponentDescriptor;
//import com.techsenger.tabshell.core.tab.AbstractTabViewModel;
//import com.techsenger.tabshell.core.tab.TabWorker;
//import com.techsenger.tabshell.shared.SharedComponentNames;
//import javafx.collections.ObservableList;
//
///**
// *
// * @author Pavel Castornii
// */
//public class WorkerReportTabViewModel extends AbstractTabViewModel {
//
//    private final ObservableList<TabWorker<?>> workers;
//
//    public WorkerReportTabViewModel(ObservableList<TabWorker<?>> workers) {
//        super();
//        this.workers = workers;
//        this.titleProperty().set("Tab Workers");
//    }
//
//    @Override
//    protected ComponentDescriptor createDescriptor() {
//        return new ComponentDescriptor(SharedComponentNames.WORKER_REPORT_TAB);
//    }
//
//    protected ObservableList<TabWorker<?>> getWorkers() {
//        return workers;
//    }
//}
