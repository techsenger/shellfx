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
public class DefaultGenericFileTest {

    private static final boolean IS_WINDOWS = OsUtils.isWindows();

    private FileStorage storage;
    private URI rootUri;
    private DefaultGenericFile root;
    private DefaultGenericFile child;

    @BeforeEach
    void setUp() {
        rootUri = IS_WINDOWS
                ? URI.create("file:///C:/")
                : URI.create("file:///");
        storage = mock(FileStorage.class);

        root = new DefaultGenericFile();
        root.setStorage(storage);
        root.setEntryType(FileEntryType.DIRECTORY);
        root.setName("");
        root.setUri(rootUri);
        root.setVirtual(true);

        when(storage.getRootUri()).thenReturn(rootUri);
        when(storage.getRoot()).thenReturn(root);

        URI childUri = IS_WINDOWS
                ? URI.create("file:///C:/home/user/foo/bar")
                : URI.create("file:///home/user/foo/bar");

        child = new DefaultGenericFile();
        child.setStorage(storage);
        child.setEntryType(FileEntryType.FILE);
        child.setName("bar");
        child.setUri(childUri);
        child.setVirtual(true);
    }

    @Test
    void getParent_immediateParent_returned() {
        var parent = child.getParent();

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

        var directChild = new DefaultGenericFile();
        directChild.setStorage(storage);
        directChild.setEntryType(FileEntryType.DIRECTORY);
        directChild.setName("home");
        directChild.setUri(directChildUri);
        directChild.setVirtual(true);

        var parent = directChild.getParent();

        assertThat(parent).isSameAs(root);
    }

    @Test
    void getParents_deeplyNestedFile_allParentsReturnedFromImmediateToRoot() {
        var parents = child.getParents();

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
        var parents = child.getParents();

        assertThat(parents.subList(0, parents.size() - 1))
                .allSatisfy(p -> {
                    assertThat(p.isDirectory()).isTrue();
                    assertThat(p.isVirtual()).isTrue();
                });
    }

    @Test
    void getParents_lastElement_isRoot() {
        var parents = child.getParents();

        assertThat(parents.getLast()).isSameAs(root);
        assertThat(parents.getLast().isRoot()).isTrue();
    }

    @Test
    void getParents_directChildOfRoot_onlyRootReturned() {
        URI directChildUri = IS_WINDOWS
                ? URI.create("file:///C:/home")
                : URI.create("file:///home");

        var directChild = new DefaultGenericFile();
        directChild.setStorage(storage);
        directChild.setEntryType(FileEntryType.DIRECTORY);
        directChild.setName("home");
        directChild.setUri(directChildUri);
        directChild.setVirtual(true);

        var parents = directChild.getParents();

        assertThat(parents).hasSize(1);
        assertThat(parents.getFirst()).isSameAs(root);
    }
}
