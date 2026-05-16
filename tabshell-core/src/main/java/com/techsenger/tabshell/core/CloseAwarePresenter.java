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

package com.techsenger.tabshell.core;

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.patternfx.mvp.ParentPresenter;
import com.techsenger.patternfx.mvp.ParentView;
import java.util.Objects;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public interface CloseAwarePresenter<V extends ParentView> extends ParentPresenter<V>, CloseAwarePort {

    ComponentDescriptor getDescriptor();

    @Override
    default void requestClose(int maxAttempts, Consumer<CloseRequestResult> resultConsumer) {
        class Requester {

            private int attemptCount = -1;

            private void run() {
                attemptCount++;
                logger().debug("{} Close requested; attempt: {}", getDescriptor().getLogPrefix(), attemptCount);
                CloseCheckResult checkResult = null;
                CloseAwarePort notReadyComponent = null;
                CloseAwarePort prepRequiredComponent = null;
                var iterator = getView().getComposer().breadthFirstIterator();
                while (iterator.hasNext()) {
                    var parent = iterator.next();
                    if (parent instanceof CloseAwarePort closeable) {
                        checkResult = closeable.isReadyToClose();
                        Objects.requireNonNull(checkResult, "Preparation result can't be null");
                        if (checkResult == CloseCheckResult.NOT_READY) {
                            notReadyComponent = closeable;
                            break;
                        } else if (checkResult == CloseCheckResult.PREPARATION_REQUIRED
                                && prepRequiredComponent == null) {
                            prepRequiredComponent = closeable;
                        }
                    }
                }
                if (checkResult != CloseCheckResult.READY && logger().isDebugEnabled()) {
                    final var finalCanClose = checkResult;
                    final var finalSavedCloseable = notReadyComponent;
                    var tree = getView().getComposer().toTreeString((c, b) -> {
                        b.append(c.getDescriptor().getFullName());
                        if (c == finalSavedCloseable) {
                            b.append(" <-- ");
                            b.append(finalCanClose.name());
                        }
                    });
                    logger().debug("{} Not all components are ready to be closed:\n{}",
                            getDescriptor().getLogPrefix(), tree);
                }
                if (checkResult == CloseCheckResult.NOT_READY) {
                    acceptResult(CloseRequestResult.NOT_READY_TO_CLOSE);
                } else if (prepRequiredComponent != null) {
                    prepRequiredComponent.prepareToClose(this::onResultPrepared);
                } else {
                    logger().debug("{} All components are ready to be closed; performing close",
                            getDescriptor().getLogPrefix());
                    close();
                    acceptResult(CloseRequestResult.SUCCESS);
                }
            }

            private void onResultPrepared(ClosePreparationResult prepResult) {
                Objects.requireNonNull(prepResult, "Preparation result can't be null");
                if (prepResult == ClosePreparationResult.CANCELLED) {
                    acceptResult(CloseRequestResult.PREPARATION_CANCELLED);
                    logger().debug("{} Close request canceled", getDescriptor().getLogPrefix());
                } else {
                    if (attemptCount == maxAttempts - 1) {
                        acceptResult(CloseRequestResult.MAX_ATTEMPTS_REACHED);
                        logger().debug("{} Close request aborted; maximum attempts reached",
                                getDescriptor().getLogPrefix());
                    } else {
                        run();
                    }
                }
            }

            private void acceptResult(CloseRequestResult result) {
                if (resultConsumer != null) {
                    resultConsumer.accept(result);
                }
            }
        }
        new Requester().run();
    }

    private static Logger logger() {
        final class LogHolder {
            private static final Logger logger = LoggerFactory.getLogger(LogHolder.class);
        }
        return LogHolder.logger;
    }
}
