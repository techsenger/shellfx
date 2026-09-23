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

package com.techsenger.shellfx.shared.find;

import javafx.beans.property.ObjectProperty;

/**
 * Provides full access to the client API of a find component that can move between individual matches.
 *
 * @param <R> the kind of {@link NavigableFindResult} this component reports
 * @author Pavel Castornii
 */
public interface FullNavigableFindPort<R extends NavigableFindResult> extends NavigableFindPort<R>, FullFindPort<R> {

    void setMatchesFormat(MatchesFormat matchesFormat);

    @Override
    ObjectProperty<MatchesFormat> matchesFormatProperty();
}
