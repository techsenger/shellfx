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

package com.techsenger.shellfx.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Schedules a single delayed action, cancelling and rescheduling it on every call to {@link #schedule(Runnable)} —
 * the classic debounce pattern used to wait for a pause in a burst of events (e.g. keystrokes) before reacting to
 * the last one.
 *
 * <p>Unlike {@link UiExecutor}, an instance of this class is not shared, each independent debounce site (e.g. one
 * per find field) needs its own instance, since the pending/cancelled state is per instance. The delayed action
 * itself always runs through {@link UiExecutor#execute(Runnable)}, so callers can rely on it landing on the UI
 * thread regardless of which thread the scheduler runs on; tests needing to avoid a running JavaFX runtime can
 * swap {@link UiExecutor} instead, the same way they would for any other UI-thread dispatch.
 *
 * @author Pavel Castornii
 */
public final class Debouncer {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor((r) -> {
        var thread = new Thread(r, "Debouncer");
        thread.setDaemon(true);
        return thread;
    });

    private final long delayMillis;

    private volatile ScheduledFuture<?> pending;

    public Debouncer(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    /**
     * Cancels any action scheduled by a previous call and schedules {@code action} to run, on the UI thread, once
     * the configured delay elapses without a further call to this method.
     *
     * @param action the task to run once the delay elapses
     */
    public void schedule(Runnable action) {
        cancel();
        pending = scheduler.schedule(() -> UiExecutor.execute(action), delayMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Cancels the currently scheduled action, if any; does nothing if none is pending.
     */
    public void cancel() {
        var future = pending;
        if (future != null) {
            future.cancel(false);
            pending = null;
        }
    }
}
