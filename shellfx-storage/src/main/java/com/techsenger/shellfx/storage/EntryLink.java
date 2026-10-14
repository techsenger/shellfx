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

package com.techsenger.shellfx.storage;

import com.techsenger.annotations.Nullable;
import java.net.URI;

/**
 * Where a link (see {@link StorageFile#isLink()}) points to: the path it states and the file at the end of the chain
 * of links. It describes the target only as far as the storages known to the link's own storage can see.
 *
 * @author Pavel Castornii
 */
public interface EntryLink {

    /**
     * Returns the URI of the path this link itself states, without following the chain: when it points to another
     * link, this is the URI of that link. Nothing says the entry at this URI exists, see {@link #getTarget()}.
     *
     * @return the URI, never {@code null}
     */
    URI getTargetUri();

    /**
     * Returns the file at the end of the whole chain of links this link starts, so it is never a link itself.
     * Returns {@code null} if the link is broken: nothing exists at the end of the chain, the chain is a cycle, the
     * file is out of reach or its storage is not available.
     *
     * @return the file the chain finally ends at, or {@code null} if the link is broken
     */
    @Nullable StorageFile getTarget();
}
