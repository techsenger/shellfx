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
 * What a link (see {@link GenericFile#isLink()}) points to: the path the link states and, when something exists
 * there, the type of that entry. It describes the target only as far as the link's own storage can see; it is not
 * an entry of a storage and can't be navigated from.
 *
 * @author Pavel Castornii
 */
public interface LinkTarget {

    /**
     * Returns the URI of the path the link points to. Nothing says the entry at this URI exists, see
     * {@link #getEntryType()}; the URI may also belong to another storage.
     *
     * @return the URI, never {@code null}
     */
    URI getUri();

    /**
     * Returns the type of the entry at the target, as it is: when the link points to another link, that is
     * {@link FileEntryType#LINK} - a chain of links is not followed. Returns {@code null} if that couldn't be
     * determined: nothing exists at the target or it is out of reach. A link with such a target is called broken.
     *
     * @return the type of the entry at the target, or {@code null} if the link is broken
     */
    @Nullable FileEntryType getEntryType();
}
