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

import com.techsenger.tabshell.core.TabShellViewModel;
import com.techsenger.tabshell.core.tab.TabWorker;
import com.techsenger.tabshell.tabs.CoreComponentKeys;
import com.techsenger.tabshell.tabs.splittab.AbstractSplitTabViewModel;
import com.techsenger.tabshell.tabs.tabmanager.TabManagerViewModel;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractWorkerTabViewModel extends AbstractSplitTabViewModel {

    /**
     * Worker can be either Task or Service.
     */
    private ObservableList<TabWorker<?>> workers = FXCollections.observableArrayList();

    private ReadOnlyIntegerWrapper workerCount = new ReadOnlyIntegerWrapper(0);

    private final TabManagerViewModel bottomTabManager;

    public AbstractWorkerTabViewModel(TabShellViewModel tabShell) {
        super(tabShell);
        this.bottomTabManager = new TabManagerViewModel(CoreComponentKeys.BOTTOM_TABS);
        this.workers.addListener((InvalidationListener) (change) -> this.workerCount.set(workers.size()));
        this.bottomTabManager.getTabs().addListener((InvalidationListener) (change) -> {
            if (this.bottomTabManager.getTabs().size() == 0) {
                this.setBottomPaneVisible(false);
            }
        });
    }

    public void submitWorker(TabWorker<?> worker) {
        this.workers.add(worker);
        worker.stateProperty().addListener((ov, oldV, newV) -> {
            if (newV == Worker.State.SUCCEEDED || newV == Worker.State.FAILED) {
                this.workers.remove(worker);
            }
        });
        if (worker instanceof Service) {
            ((Service<?>) worker).start();
        } else if (worker instanceof Task) {
            new Thread((Task<?>) worker).start();
        }
    }

    public void cancelAllWorkers() {
        var iterator = this.workers.iterator();
        while (iterator.hasNext()) {
            var w = iterator.next();
            w.cancel();
            iterator.remove();
        }
    }

    @Override
    public WorkerTabHelper<?> getComponentHelper() {
        return (WorkerTabHelper) super.getComponentHelper();
    }

    protected TabManagerViewModel getBottomTabManager() {
        return bottomTabManager;
    }

    protected void openWorkerReportTab() {
        //firstly check if such tab is open
        for (var tab : this.bottomTabManager.getTabs()) {
            if (tab.getKey() == CoreComponentKeys.WORKER_REPORT_TAB) {
                return;
            }
        }
        var reportViewModel = new WorkerReportTabViewModel(getWorkers());
        getComponentHelper().openWorkerReportTab(reportViewModel);
        bottomPaneVisibleProperty().set(true);
    }

    ReadOnlyIntegerProperty workerCountProperty() {
        return this.workerCount.getReadOnlyProperty();
    }

    int getWorkerCount() {
        return this.workerCount.get();
    }

    ObservableList<TabWorker<?>> getWorkers() {
        return workers;
    }
}
