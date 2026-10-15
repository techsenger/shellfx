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
 * Where a link (see {@link StorageFile#isLink()}) points to: the path it states, the path of the entry at the end of
 * the chain of links, and the file there. A link is resolved in one of two loading phases:
 * <ul>
 * <li>in the first phase, by the storage of the link, which follows the chain in the file system. If that gives the
 * path of the end of the chain, it is the {@linkplain #getResolvedUri() resolved URI}, and the file there is left to
 * the second phase. This is the case for every link but the few that end in something that has no path at all, such
 * as the pipe behind {@code /dev/stderr}; for those the storage reads the attributes of the end itself and gives the
 * {@linkplain #getTarget() target} at once, and there is no resolved URI;</li>
 * <li>in the second phase, by the {@link FileProvider}, which gets the file at the resolved URI from the storage that
 * holds it, which is not necessarily the storage of the link.</li>
 * </ul>
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
     * Returns the URI of the entry at the end of the whole chain of links this link starts, as the storage of the
     * link sees it. It is known as soon as the link is listed, even when the entry lies in another storage, which is
     * what lets that storage be asked for the target in the second phase.
     *
     * @return the URI, or {@code null} if nothing exists at the end of the chain, or if the end exists but has no
     *     path of its own, in which case the target is already known
     */
    @Nullable URI getResolvedUri();

    /**
     * Returns the file at the end of the whole chain of links this link starts, so it is never a link itself.
     * Returns {@code null} if the link is broken: nothing exists at the end of the chain, the chain is a cycle, the
     * file is out of reach or its storage is not available. It is also {@code null} until the second loading phase
     * has got the file at the {@linkplain #getResolvedUri() resolved URI}, so the state of the file tells a link that
     * is not resolved yet from a broken one.
     *
     * @return the file the chain finally ends at, or {@code null} if the link is broken or not resolved yet
     */
    @Nullable StorageFile getTarget();
}
