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

/**
 * A minimal, domain-agnostic view of a completed search: how many matches it found, without saying anything about
 * what a match actually is or how to move between them — see {@link NavigableFindResult} for that. A component
 * that runs its own search internally implements this directly on top of whatever result type it already builds,
 * then reports it to {@link AbstractFindViewModel} through {@code setFindResult(FindResult)}.
 *
 * @author Pavel Castornii
 */
public interface FindResult {

    int getTotalMatches();
}
