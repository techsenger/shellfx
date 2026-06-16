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

package com.techsenger.shellfx.core.tab;

import javafx.concurrent.Worker;

/**
 *
 * There are two type of workers - daemon workers and user workers. Daemon workers as a rule initiated by component
 * and there can be N workers at any point of time. User workers initiated by user and as a rule there is only one
 * user worker that the user is waiting for. At the same time there can be N user threads, for example, when
 * there are N tabs and user can do different actions in these tabs. That's why we don't distinguish them because
 * it is tab responsibility how how to show progress for user workers. For WorkerTab all workers are equal.
 *
 * @author Pavel Castornii
 */
public interface TabWorker<T> extends Worker<T> {

    boolean usesProgress();

}
