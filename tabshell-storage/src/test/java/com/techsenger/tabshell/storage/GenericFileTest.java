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

package com.techsenger.tabshell.storage;

import com.techsenger.toolkit.core.os.OsUtils;
import java.net.URI;
import static org.assertj.core.api.Java6Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Pavel Castornii
 */
public class GenericFileTest {

    private static final boolean IS_WINDOWS = OsUtils.isWindows();

    private FileStorage storage;
    private URI rootUri;
    private GenericFile root;
    private GenericFile child;

    @BeforeEach
    void setUp() {
        rootUri = IS_WINDOWS
                ? URI.create("file:///C:/")
                : URI.create("file:///");

        storage = mock(FileStorage.class);

        root = new GenericFile.Builder()
                .storage(storage)
                .type(FileType.DIRECTORY)
                .name("")
                .uri(rootUri)
                .virtual(true)
                .build();

        when(storage.getRootUri()).thenReturn(rootUri);
        when(storage.getRoot()).thenReturn(root);

        URI childUri = IS_WINDOWS
                ? URI.create("file:///C:/home/user/foo/bar")
                : URI.create("file:///home/user/foo/bar");

        child = new GenericFile.Builder()
                .storage(storage)
                .type(FileType.FILE)
                .name("bar")
                .uri(childUri)
                .virtual(true)
                .build();
    }

    @Test
    void getParent_immediateParent_returned() {
        var parent = GenericFile.getParent(child);

        URI expectedUri = IS_WINDOWS
                ? URI.create("file:///C:/home/user/foo")
                : URI.create("file:///home/user/foo");

        assertThat(parent.getUri()).isEqualTo(expectedUri);
        assertThat(parent.getName()).isEqualTo("foo");
        assertThat(parent.isDirectory()).isTrue();
        assertThat(parent.isVirtual()).isTrue();
    }

    @Test
    void getParent_directChildOfRoot_rootReturned() {
        URI directChildUri = IS_WINDOWS
                ? URI.create("file:///C:/home")
                : URI.create("file:///home");

        var directChild = new GenericFile.Builder()
                .storage(storage)
                .type(FileType.DIRECTORY)
                .name("home")
                .uri(directChildUri)
                .virtual(true)
                .build();

        var parent = GenericFile.getParent(directChild);

        assertThat(parent).isSameAs(root);
    }

    @Test
    void getParents_deeplyNestedFile_allParentsReturnedFromImmediateToRoot() {
        var parents = GenericFile.getParents(child);

        URI fooUri = IS_WINDOWS
                ? URI.create("file:///C:/home/user/foo")
                : URI.create("file:///home/user/foo");
        URI userUri = IS_WINDOWS
                ? URI.create("file:///C:/home/user")
                : URI.create("file:///home/user");
        URI homeUri = IS_WINDOWS
                ? URI.create("file:///C:/home")
                : URI.create("file:///home");

        assertThat(parents).hasSize(4);
        assertThat(parents.get(0).getUri()).isEqualTo(fooUri);
        assertThat(parents.get(0).getName()).isEqualTo("foo");
        assertThat(parents.get(1).getUri()).isEqualTo(userUri);
        assertThat(parents.get(1).getName()).isEqualTo("user");
        assertThat(parents.get(2).getUri()).isEqualTo(homeUri);
        assertThat(parents.get(2).getName()).isEqualTo("home");
        assertThat(parents.get(3)).isSameAs(root);
    }

    @Test
    void getParents_allIntermediateParents_virtualDirectories() {
        var parents = GenericFile.getParents(child);

        assertThat(parents.subList(0, parents.size() - 1))
                .allSatisfy(p -> {
                    assertThat(p.isDirectory()).isTrue();
                    assertThat(p.isVirtual()).isTrue();
                });
    }

    @Test
    void getParents_lastElement_isRoot() {
        var parents = GenericFile.getParents(child);

        assertThat(parents.getLast()).isSameAs(root);
        assertThat(parents.getLast().isRoot()).isTrue();
    }

    @Test
    void getParents_directChildOfRoot_onlyRootReturned() {
        URI directChildUri = IS_WINDOWS
                ? URI.create("file:///C:/home")
                : URI.create("file:///home");

        var directChild = new GenericFile.Builder()
                .storage(storage)
                .type(FileType.DIRECTORY)
                .name("home")
                .uri(directChildUri)
                .virtual(true)
                .build();

        var parents = GenericFile.getParents(directChild);

        assertThat(parents).hasSize(1);
        assertThat(parents.getFirst()).isSameAs(root);
    }
}
