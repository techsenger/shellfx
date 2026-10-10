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

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.techsenger.annotations.Nullable;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for the system file storages ({@link UnixFileStorage} for Linux and macOS,
 * {@link WindowsFileStorage}): how they describe entries - above all links, which must be reported as links, never
 * as the file or directory they point to, so that code walking a directory tree can avoid going through them - and
 * the result and the exception of every operation (see the EAFP section of {@link FileStorage}).
 * Run against a fresh in-memory Jimfs file system of each flavor for every test, the root of which is the root of
 * the storage. What Jimfs can't imitate (Windows junctions, the Windows hidden attribute) is tested separately on
 * the real file system, only on Windows. {@link java.nio.file.AccessDeniedException} is not tested: Jimfs does not
 * enforce permissions, so there is no way to make an operation fail with it.
 *
 * @author Pavel Castornii
 */
public class SystemFileStorageIT {

    /**
     * @param linkName the name of the link in the listed folder.
     * @param targetPath the path the link is expected to point to.
     * @param entryType the type the entry at the target is expected to have, {@code null} for a broken link.
     */
    private record ExpectedTarget(String linkName, Path targetPath, @Nullable FileEntryType entryType) {
    }

    /**
     * Keeps every created Jimfs file system strongly reachable for the lifetime of the test JVM - a storage only
     * keeps the root URI, not the {@link FileSystem} object itself, and Jimfs registers a file system by URI only
     * as long as something still references it; without this, an unlucky GC can collect it mid-test.
     */
    private static final List<FileSystem> RETAINED_FILE_SYSTEMS = new CopyOnWriteArrayList<>();

    private static final String CONTENT = "caf\u00e9";

    /**
     * Returns one storage factory per flavor of system: Linux, macOS and Windows. The root of each storage is the
     * root of its own fresh Jimfs file system, which is empty: the working directory Jimfs creates by default is
     * moved into the root itself, so that nothing but the test's own entries is there.
     */
    static Stream<Named<Supplier<FileStorage<GenericFile>>>> flavors() {
        return Stream.of(
                Named.of("linux", () -> createStorage(
                        Configuration.unix().toBuilder().setWorkingDirectory("/").build(), false)),
                Named.of("macos", () -> createStorage(
                        Configuration.osX().toBuilder().setWorkingDirectory("/").build(), false)),
                Named.of("windows", () -> createStorage(
                        Configuration.windows().toBuilder().setWorkingDirectory("C:\\").build(), true)));
    }

    private static FileStorage<GenericFile> createStorage(Configuration configuration, boolean windows) {
        var fileSystem = Jimfs.newFileSystem(configuration);
        RETAINED_FILE_SYSTEMS.add(fileSystem);
        var rootUri = fileSystem.getRootDirectories().iterator().next().toUri();
        return windows
                ? new WindowsFileStorage<GenericFile>(FileStorageType.BASE, "test", rootUri, DefaultGenericFile::new)
                : new UnixFileStorage<GenericFile>(FileStorageType.BASE, "test", rootUri, DefaultGenericFile::new);
    }

    /**
     * Verifies that each of the expected links is listed as a link, with the expected target.
     */
    private static void assertTargets(List<GenericFile> files, ExpectedTarget... expectedTargets) {
        for (var expected : expectedTargets) {
            var link = find(files, expected.linkName());
            assertThat(link.getEntryType()).as(expected.linkName()).isEqualTo(FileEntryType.LINK);
            var target = link.getLinkTarget();
            assertThat(target).as(expected.linkName()).isNotNull();
            assertThat(Paths.get(target.getUri())).as(expected.linkName()).isEqualTo(expected.targetPath());
            assertThat(target.getEntryType()).as(expected.linkName()).isEqualTo(expected.entryType());
        }
    }

    private static GenericFile find(List<GenericFile> files, String name) {
        return files.stream().filter(file -> file.getName().equals(name)).findFirst().orElseThrow();
    }

    private static Path rootOf(FileStorage<GenericFile> storage) {
        return Paths.get(storage.getUri());
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFiles_regularEntries_haveTheirOwnTypeAndSizeAndNoTarget(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = Paths.get(storage.getUri());
        Files.writeString(root.resolve("file.txt"), "hello");
        Files.createDirectory(root.resolve("folder"));

        var files = storage.getFiles(root.toUri());

        assertThat(files).extracting(GenericFile::getName).containsExactlyInAnyOrder("file.txt", "folder");
        var file = find(files, "file.txt");
        assertThat(file.getEntryType()).isEqualTo(FileEntryType.FILE);
        assertThat(file.getSize()).isEqualTo(5L);
        assertThat(file.isLink()).isFalse();
        assertThat(file.getLinkTarget()).isNull();
        var folder = find(files, "folder");
        assertThat(folder.getEntryType()).isEqualTo(FileEntryType.DIRECTORY);
        assertThat(folder.isLink()).isFalse();
        assertThat(folder.getLinkTarget()).isNull();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFiles_symbolicLinkToFile_listedAsLinkPointingToTheFile(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = Paths.get(storage.getUri());
        var target = Files.writeString(root.resolve("target.txt"), "hello");
        Files.createSymbolicLink(root.resolve("link"), target);

        var files = storage.getFiles(root.toUri());

        var link = find(files, "link");
        assertThat(link.getEntryType()).isEqualTo(FileEntryType.LINK);
        assertThat(link.isLink()).isTrue();
        assertThat(link.isFile()).isFalse();
        assertThat(link.isDirectory()).isFalse();
        assertThat(Paths.get(link.getLinkTarget().getUri())).isEqualTo(target);
        assertThat(link.getLinkTarget().getEntryType()).isEqualTo(FileEntryType.FILE);
        assertThat(find(files, "target.txt").isLink()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFiles_symbolicLinkToFolder_listedAsLinkNotAsFolder(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = Paths.get(storage.getUri());
        var target = Files.createDirectory(root.resolve("target"));
        Files.createSymbolicLink(root.resolve("link"), target);

        var files = storage.getFiles(root.toUri());

        var link = find(files, "link");
        assertThat(link.getEntryType()).isEqualTo(FileEntryType.LINK);
        assertThat(link.isLink()).isTrue();
        assertThat(link.isDirectory()).isFalse();
        assertThat(Paths.get(link.getLinkTarget().getUri())).isEqualTo(target);
        assertThat(link.getLinkTarget().getEntryType()).isEqualTo(FileEntryType.DIRECTORY);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFiles_relativeSymbolicLink_targetResolvedAgainstTheFolderOfTheLink(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = Paths.get(storage.getUri());
        var real = Files.writeString(root.resolve("real.txt"), "hello");
        var subFolder = Files.createDirectory(root.resolve("sub"));
        var relativeTarget = subFolder.getFileSystem().getPath("..", "real.txt");
        Files.createSymbolicLink(subFolder.resolve("link"), relativeTarget);

        var files = storage.getFiles(subFolder.toUri());

        var link = find(files, "link");
        assertThat(link.isLink()).isTrue();
        assertThat(Paths.get(link.getLinkTarget().getUri())).isEqualTo(real);
        assertThat(link.getLinkTarget().getEntryType()).isEqualTo(FileEntryType.FILE);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFiles_linkStatingDotSlashPath_targetPathIsNormalized(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = Paths.get(storage.getUri());
        var folder = Files.createDirectory(root.resolve("tmp"));
        Files.createSymbolicLink(root.resolve("link"), root.getFileSystem().getPath(".", "tmp"));

        var files = storage.getFiles(root.toUri());

        assertTargets(files, new ExpectedTarget("link", folder, FileEntryType.DIRECTORY));
        assertThat(find(files, "link").getLinkTarget().getUri().toString()).doesNotContain("/./");
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFiles_brokenSymbolicLink_stillListedAsLinkWithItsTargetPath(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = Paths.get(storage.getUri());
        var missing = root.resolve("missing.txt");
        Files.createSymbolicLink(root.resolve("link"), missing);

        var files = storage.getFiles(root.toUri());

        var link = find(files, "link");
        assertThat(link.getEntryType()).isEqualTo(FileEntryType.LINK);
        assertThat(link.isLink()).isTrue();
        assertThat(link.getLinkTarget()).isNotNull();
        assertThat(Paths.get(link.getLinkTarget().getUri())).isEqualTo(missing);
        assertThat(link.getLinkTarget().getEntryType()).isNull();
        assertThat(Files.exists(missing)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFiles_chainOfThreeLinksToFile_eachLinkPointsToTheNextOneAndTheLastToTheFile(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = Paths.get(storage.getUri());
        var target = Files.writeString(root.resolve("target.txt"), "hello");
        var link3 = Files.createSymbolicLink(root.resolve("link3"), target);
        var link2 = Files.createSymbolicLink(root.resolve("link2"), link3);
        Files.createSymbolicLink(root.resolve("link1"), link2);

        var files = storage.getFiles(root.toUri());

        assertTargets(files,
                new ExpectedTarget("link1", link2, FileEntryType.LINK),
                new ExpectedTarget("link2", link3, FileEntryType.LINK),
                new ExpectedTarget("link3", target, FileEntryType.FILE));
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFiles_chainOfThreeLinksToFolder_eachLinkPointsToTheNextOneAndTheLastToTheFolder(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = Paths.get(storage.getUri());
        var target = Files.createDirectory(root.resolve("target"));
        var link3 = Files.createSymbolicLink(root.resolve("link3"), target);
        var link2 = Files.createSymbolicLink(root.resolve("link2"), link3);
        Files.createSymbolicLink(root.resolve("link1"), link2);

        var files = storage.getFiles(root.toUri());

        assertTargets(files,
                new ExpectedTarget("link1", link2, FileEntryType.LINK),
                new ExpectedTarget("link2", link3, FileEntryType.LINK),
                new ExpectedTarget("link3", target, FileEntryType.DIRECTORY));
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFiles_chainOfThreeLinksEndingNowhere_onlyTheLastLinkIsBroken(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = Paths.get(storage.getUri());
        var missing = root.resolve("missing.txt");
        var link3 = Files.createSymbolicLink(root.resolve("link3"), missing);
        var link2 = Files.createSymbolicLink(root.resolve("link2"), link3);
        Files.createSymbolicLink(root.resolve("link1"), link2);

        var files = storage.getFiles(root.toUri());

        assertTargets(files,
                new ExpectedTarget("link1", link2, FileEntryType.LINK),
                new ExpectedTarget("link2", link3, FileEntryType.LINK),
                new ExpectedTarget("link3", missing, null));
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFiles_linksFormingACycle_eachPointsToTheOtherLink(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = Paths.get(storage.getUri());
        var a = root.resolve("a");
        var b = root.resolve("b");
        Files.createSymbolicLink(a, b);
        Files.createSymbolicLink(b, a);

        var files = storage.getFiles(root.toUri());

        assertTargets(files,
                new ExpectedTarget("a", b, FileEntryType.LINK),
                new ExpectedTarget("b", a, FileEntryType.LINK));
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFile_symbolicLink_returnedAsLink(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = Paths.get(storage.getUri());
        var target = Files.createDirectory(root.resolve("target"));
        var link = Files.createSymbolicLink(root.resolve("link"), target);

        var file = storage.getFile(link.toUri());

        assertThat(file.getEntryType()).isEqualTo(FileEntryType.LINK);
        assertThat(file.isLink()).isTrue();
        assertThat(Paths.get(file.getLinkTarget().getUri())).isEqualTo(target);
        assertThat(file.getLinkTarget().getEntryType()).isEqualTo(FileEntryType.DIRECTORY);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getDirectories_symbolicLinkToFolder_notListedAsDirectory(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = Paths.get(storage.getUri());
        var target = Files.createDirectory(root.resolve("target"));
        Files.createSymbolicLink(root.resolve("link"), target);

        var directories = storage.getDirectories(root.toUri());

        assertThat(directories).extracting(GenericFile::getName).containsExactly("target");
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFilesRecursively_symbolicLinkToFolder_listedButNotEntered(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = Paths.get(storage.getUri());
        var target = Files.createDirectory(root.resolve("target"));
        Files.writeString(target.resolve("inside.txt"), "hello");
        Files.createSymbolicLink(root.resolve("link"), target);

        var files = storage.getFilesRecursively(root.toUri());

        assertThat(files).extracting(GenericFile::getName).containsExactlyInAnyOrder("target", "inside.txt", "link");
        assertThat(find(files, "link").isLink()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFiles_nameStartingWithDot_hiddenOnlyOnLinuxAndMacos(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = Paths.get(storage.getUri());
        Files.writeString(root.resolve(".dotfile"), "hello");
        Files.writeString(root.resolve("visible"), "hello");

        var files = storage.getFiles(root.toUri());

        assertThat(find(files, ".dotfile").isHidden()).isEqualTo(!(storage instanceof WindowsFileStorage<?>));
        assertThat(find(files, "visible").isHidden()).isFalse();
    }

    @Test
    public void refersToStorage_unixStorage_trueOnlyForFileScheme() {
        var storage = new UnixFileStorage<GenericFile>(FileStorageType.BASE, "/", URI.create("file:///"),
                DefaultGenericFile::new);

        assertThat(storage.refersToStorage(URI.create("file:///home/user/a.txt"))).isTrue();
        assertThat(storage.refersToStorage(URI.create("FILE:///home/user/a.txt"))).isTrue();
        assertThat(storage.refersToStorage(URI.create("http://host/a.txt"))).isFalse();
    }

    @Test
    public void refersToStorage_windowsStorage_trueOnlyForTheSameDriveIgnoringCase() {
        var storage = new WindowsFileStorage<GenericFile>(FileStorageType.BASE, "C:", URI.create("file:///C:/"),
                DefaultGenericFile::new);

        assertThat(storage.refersToStorage(URI.create("file:///C:/dir/a.txt"))).isTrue();
        assertThat(storage.refersToStorage(URI.create("file:///c:/dir/a.txt"))).isTrue();
        assertThat(storage.refersToStorage(URI.create("file:///D:/dir/a.txt"))).isFalse();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void properties_storageOfBaseType_reportWhatItWasCreatedWith(
            Supplier<FileStorage<GenericFile>> storageFactory) {
        var storage = storageFactory.get();

        assertThat(storage.getType()).isEqualTo(FileStorageType.BASE);
        assertThat(storage.getDisplayName()).isEqualTo("test");
        assertThat(storage.getUri()).isNotNull();
        assertThat(storage.getIcon()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getRootDirectory_always_virtualDirectoryWithTheUriAndNameOfTheStorage(
            Supplier<FileStorage<GenericFile>> storageFactory) {
        var storage = storageFactory.get();

        var root = storage.getRootDirectory();

        assertThat(root.isVirtual()).isTrue();
        assertThat(root.getEntryType()).isEqualTo(FileEntryType.DIRECTORY);
        assertThat(root.getUri()).isEqualTo(storage.getUri());
        assertThat(root.getName()).isEqualTo("test");
        assertThat(root.getStorage()).isSameAs(storage);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void createVirtual_withUri_virtualEntryOfTheGivenTypeNameAndUri(
            Supplier<FileStorage<GenericFile>> storageFactory) {
        var storage = storageFactory.get();
        var uri = rootOf(storage).resolve("pending").toUri();

        var file = storage.createVirtual(FileEntryType.DIRECTORY, "pending", uri);

        assertThat(file.isVirtual()).isTrue();
        assertThat(file.getEntryType()).isEqualTo(FileEntryType.DIRECTORY);
        assertThat(file.getName()).isEqualTo("pending");
        assertThat(file.getUri()).isEqualTo(uri);
        assertThat(file.getStorage()).isSameAs(storage);
        assertThat(Files.exists(rootOf(storage).resolve("pending"))).isFalse();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void createVirtual_withoutUri_virtualEntryWithoutUri(
            Supplier<FileStorage<GenericFile>> storageFactory) {
        var storage = storageFactory.get();

        var file = storage.createVirtual(FileEntryType.FILE, "new.txt", null);

        assertThat(file.isVirtual()).isTrue();
        assertThat(file.getEntryType()).isEqualTo(FileEntryType.FILE);
        assertThat(file.getName()).isEqualTo("new.txt");
        assertThat(file.getUri()).isNull();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getDirectories_foldersAndFiles_onlyFoldersListed(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = rootOf(storage);
        Files.createDirectory(root.resolve("a"));
        Files.createDirectory(root.resolve("b"));
        Files.writeString(root.resolve("file.txt"), CONTENT);

        var directories = storage.getDirectories(root.toUri());

        assertThat(directories).extracting(GenericFile::getName).containsExactlyInAnyOrder("a", "b");
        assertThat(directories).allMatch(GenericFile::isDirectory);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getDirectories_emptyFolder_emptyList(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();

        assertThat(storage.getDirectories(rootOf(storage).toUri())).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getDirectories_missingFolder_noSuchFileException(
            Supplier<FileStorage<GenericFile>> storageFactory) {
        var storage = storageFactory.get();
        var missing = rootOf(storage).resolve("missing").toUri();

        assertThatThrownBy(() -> storage.getDirectories(missing)).isInstanceOf(NoSuchFileException.class);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getDirectories_regularFile_notDirectoryException(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var file = Files.writeString(rootOf(storage).resolve("file.txt"), CONTENT);

        assertThatThrownBy(() -> storage.getDirectories(file.toUri())).isInstanceOf(NotDirectoryException.class);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFiles_foldersAndFiles_everyDirectChildListedButNotTheDeeperOnes(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = rootOf(storage);
        var folder = Files.createDirectory(root.resolve("folder"));
        Files.writeString(folder.resolve("deep.txt"), CONTENT);
        Files.writeString(root.resolve("file.txt"), CONTENT);

        var files = storage.getFiles(root.toUri());

        assertThat(files).extracting(GenericFile::getName).containsExactlyInAnyOrder("folder", "file.txt");
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFiles_emptyFolder_emptyList(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();

        assertThat(storage.getFiles(rootOf(storage).toUri())).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFiles_missingFolder_noSuchFileException(
            Supplier<FileStorage<GenericFile>> storageFactory) {
        var storage = storageFactory.get();
        var missing = rootOf(storage).resolve("missing").toUri();

        assertThatThrownBy(() -> storage.getFiles(missing)).isInstanceOf(NoSuchFileException.class);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFiles_regularFile_notDirectoryException(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var file = Files.writeString(rootOf(storage).resolve("file.txt"), CONTENT);

        assertThatThrownBy(() -> storage.getFiles(file.toUri())).isInstanceOf(NotDirectoryException.class);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFilesRecursively_nestedTree_allDescendantsWithFolderBeforeItsContent(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = rootOf(storage);
        var outer = Files.createDirectory(root.resolve("outer"));
        var inner = Files.createDirectory(outer.resolve("inner"));
        Files.writeString(inner.resolve("deep.txt"), CONTENT);
        Files.writeString(outer.resolve("middle.txt"), CONTENT);
        Files.writeString(root.resolve("top.txt"), CONTENT);

        var files = storage.getFilesRecursively(root.toUri());

        var names = files.stream().map(GenericFile::getName).toList();
        assertThat(names).containsExactlyInAnyOrder("outer", "inner", "deep.txt", "middle.txt", "top.txt");
        assertThat(names.indexOf("outer")).isLessThan(names.indexOf("inner"));
        assertThat(names.indexOf("inner")).isLessThan(names.indexOf("deep.txt"));
        assertThat(names.indexOf("outer")).isLessThan(names.indexOf("middle.txt"));
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFilesRecursively_subFolder_theFolderItselfNotIncluded(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var folder = Files.createDirectory(rootOf(storage).resolve("folder"));
        Files.writeString(folder.resolve("inside.txt"), CONTENT);

        var files = storage.getFilesRecursively(folder.toUri());

        assertThat(files).extracting(GenericFile::getName).containsExactly("inside.txt");
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFilesRecursively_emptyFolder_emptyList(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();

        assertThat(storage.getFilesRecursively(rootOf(storage).toUri())).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFilesRecursively_missingFolder_noSuchFileException(
            Supplier<FileStorage<GenericFile>> storageFactory) {
        var storage = storageFactory.get();
        var missing = rootOf(storage).resolve("missing").toUri();

        assertThatThrownBy(() -> storage.getFilesRecursively(missing)).isInstanceOf(NoSuchFileException.class);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFile_regularFile_describedWithNameTypeAndSize(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = Files.write(rootOf(storage).resolve("file.txt"), new byte[] {1, 2, 3});

        var file = storage.getFile(path.toUri());

        assertThat(file.getName()).isEqualTo("file.txt");
        assertThat(file.getEntryType()).isEqualTo(FileEntryType.FILE);
        assertThat(file.getSize()).isEqualTo(3L);
        assertThat(file.isVirtual()).isFalse();
        assertThat(file.getStorage()).isSameAs(storage);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFile_folder_describedAsDirectory(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = Files.createDirectory(rootOf(storage).resolve("folder"));

        var file = storage.getFile(path.toUri());

        assertThat(file.getName()).isEqualTo("folder");
        assertThat(file.getEntryType()).isEqualTo(FileEntryType.DIRECTORY);
        assertThat(file.isVirtual()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFile_rootUri_theRootDirectory(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();

        var file = storage.getFile(storage.getUri());

        assertThat(file.isVirtual()).isTrue();
        assertThat(file.getUri()).isEqualTo(storage.getUri());
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getFile_missingFile_noSuchFileException(
            Supplier<FileStorage<GenericFile>> storageFactory) {
        var storage = storageFactory.get();
        var missing = rootOf(storage).resolve("missing.txt").toUri();

        assertThatThrownBy(() -> storage.getFile(missing)).isInstanceOf(NoSuchFileException.class);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getParent_rootUri_null(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();

        assertThat(storage.getParent(storage.getUri())).isNull();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getParent_entryInRoot_theRootDirectory(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = Files.writeString(rootOf(storage).resolve("file.txt"), CONTENT);

        var parent = storage.getParent(path.toUri());

        assertThat(parent).isNotNull();
        assertThat(parent.isVirtual()).isTrue();
        assertThat(parent.getUri()).isEqualTo(storage.getUri());
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getParent_nestedEntry_theEnclosingFolder(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var folder = Files.createDirectory(rootOf(storage).resolve("folder"));
        var path = Files.writeString(folder.resolve("file.txt"), CONTENT);

        var parent = storage.getParent(path.toUri());

        assertThat(parent).isNotNull();
        assertThat(parent.getName()).isEqualTo("folder");
        assertThat(parent.getEntryType()).isEqualTo(FileEntryType.DIRECTORY);
        assertThat(parent.isVirtual()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getParent_file_sameAsParentOfItsUri(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var folder = Files.createDirectory(rootOf(storage).resolve("folder"));
        var path = Files.writeString(folder.resolve("file.txt"), CONTENT);
        var file = storage.getFile(path.toUri());

        var parent = storage.getParent(file);

        assertThat(parent).isNotNull();
        assertThat(parent.getName()).isEqualTo("folder");
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getParent_missingParentFolder_noSuchFileException(
            Supplier<FileStorage<GenericFile>> storageFactory) {
        var storage = storageFactory.get();
        var orphan = rootOf(storage).resolve("missing").resolve("file.txt").toUri();

        assertThatThrownBy(() -> storage.getParent(orphan)).isInstanceOf(NoSuchFileException.class);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getHierarchy_rootUri_onlyVirtualRoot(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();

        var hierarchy = storage.getHierarchy(storage.getUri());

        assertThat(hierarchy).hasSize(1);
        assertThat(hierarchy.get(0).isVirtual()).isTrue();
        assertThat(hierarchy.get(0).isRoot()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getHierarchy_nestedFile_chainFromRootToTheFile(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var first = Files.createDirectory(rootOf(storage).resolve("first"));
        var second = Files.createDirectory(first.resolve("second"));
        var path = Files.writeString(second.resolve("file.txt"), CONTENT);

        var hierarchy = storage.getHierarchy(path.toUri());

        assertThat(hierarchy).extracting(GenericFile::getName)
                .containsExactly(storage.getDisplayName(), "first", "second", "file.txt");
        assertThat(hierarchy).extracting(GenericFile::getEntryType).containsExactly(FileEntryType.DIRECTORY,
                FileEntryType.DIRECTORY, FileEntryType.DIRECTORY, FileEntryType.FILE);
        assertThat(hierarchy.get(0).getUri()).isEqualTo(storage.getUri());
        assertThat(hierarchy.get(0).isVirtual()).isTrue();
        assertThat(hierarchy.get(3).getUri()).isEqualTo(path.toUri());
        assertThat(hierarchy.subList(1, 4)).allMatch(f -> !f.isVirtual());
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getHierarchy_directory_chainEndsWithTheDirectory(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var first = Files.createDirectory(rootOf(storage).resolve("first"));
        var second = Files.createDirectory(first.resolve("second"));

        var hierarchy = storage.getHierarchy(second.toUri());

        assertThat(hierarchy).extracting(GenericFile::getName)
                .containsExactly(storage.getDisplayName(), "first", "second");
        assertThat(hierarchy.get(2).getUri()).isEqualTo(second.toUri());
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void getHierarchy_missingEntry_noSuchFileException(
            Supplier<FileStorage<GenericFile>> storageFactory) {
        var storage = storageFactory.get();
        var missing = rootOf(storage).resolve("missing").resolve("file.txt").toUri();

        assertThatThrownBy(() -> storage.getHierarchy(missing)).isInstanceOf(NoSuchFileException.class);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void createDirectory_freeName_folderCreated(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = rootOf(storage).resolve("folder");

        storage.createDirectory(path.toUri());

        assertThat(Files.isDirectory(path)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void createDirectory_missingParent_noSuchFileExceptionAndNothingCreated(
            Supplier<FileStorage<GenericFile>> storageFactory) {
        var storage = storageFactory.get();
        var parent = rootOf(storage).resolve("missing");
        var path = parent.resolve("folder");

        assertThatThrownBy(() -> storage.createDirectory(path.toUri())).isInstanceOf(NoSuchFileException.class);
        assertThat(Files.exists(parent)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void createDirectory_existingFolder_fileAlreadyExistsException(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = Files.createDirectory(rootOf(storage).resolve("folder"));

        assertThatThrownBy(() -> storage.createDirectory(path.toUri()))
                .isInstanceOf(FileAlreadyExistsException.class);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void createDirectory_existingFile_fileAlreadyExistsException(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = Files.writeString(rootOf(storage).resolve("entry"), CONTENT);

        assertThatThrownBy(() -> storage.createDirectory(path.toUri()))
                .isInstanceOf(FileAlreadyExistsException.class);
        assertThat(Files.isRegularFile(path)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void createFile_freeName_emptyFileCreatedAndReturned(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = rootOf(storage).resolve("new.txt");

        var file = storage.createFile("new.txt", path.toUri());

        assertThat(Files.isRegularFile(path)).isTrue();
        assertThat(Files.size(path)).isZero();
        assertThat(file.getName()).isEqualTo("new.txt");
        assertThat(file.getEntryType()).isEqualTo(FileEntryType.FILE);
        assertThat(file.isVirtual()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void createFile_missingParent_noSuchFileExceptionAndNothingCreated(
            Supplier<FileStorage<GenericFile>> storageFactory) {
        var storage = storageFactory.get();
        var parent = rootOf(storage).resolve("missing");
        var path = parent.resolve("new.txt");

        assertThatThrownBy(() -> storage.createFile("new.txt", path.toUri()))
                .isInstanceOf(NoSuchFileException.class);
        assertThat(Files.exists(parent)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void createFile_existingFile_fileAlreadyExistsExceptionAndContentKept(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = Files.writeString(rootOf(storage).resolve("file.txt"), CONTENT);

        assertThatThrownBy(() -> storage.createFile("file.txt", path.toUri()))
                .isInstanceOf(FileAlreadyExistsException.class);
        assertThat(Files.readString(path)).isEqualTo(CONTENT);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void createFile_existingFolder_fileAlreadyExistsException(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = Files.createDirectory(rootOf(storage).resolve("entry"));

        assertThatThrownBy(() -> storage.createFile("entry", path.toUri()))
                .isInstanceOf(FileAlreadyExistsException.class);
        assertThat(Files.isDirectory(path)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void renameFile_regularFile_renamedInTheSameFolder(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var folder = Files.createDirectory(rootOf(storage).resolve("folder"));
        var path = Files.writeString(folder.resolve("old.txt"), CONTENT);

        storage.renameFile(path.toUri(), "new.txt");

        assertThat(Files.exists(path)).isFalse();
        assertThat(Files.readString(folder.resolve("new.txt"))).isEqualTo(CONTENT);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void renameFile_folder_renamedTogetherWithItsContent(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = rootOf(storage);
        var folder = Files.createDirectory(root.resolve("old"));
        Files.writeString(folder.resolve("inside.txt"), CONTENT);

        storage.renameFile(folder.toUri(), "new");

        assertThat(Files.exists(folder)).isFalse();
        assertThat(Files.readString(root.resolve("new").resolve("inside.txt"))).isEqualTo(CONTENT);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void renameFile_missingEntry_noSuchFileException(
            Supplier<FileStorage<GenericFile>> storageFactory) {
        var storage = storageFactory.get();
        var missing = rootOf(storage).resolve("missing.txt");

        assertThatThrownBy(() -> storage.renameFile(missing.toUri(), "new.txt"))
                .isInstanceOf(NoSuchFileException.class);
        assertThat(Files.exists(rootOf(storage).resolve("new.txt"))).isFalse();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void renameFile_nameTakenByAnotherEntry_fileAlreadyExistsExceptionAndBothKept(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var root = rootOf(storage);
        var source = Files.writeString(root.resolve("source.txt"), "source");
        var taken = Files.writeString(root.resolve("taken.txt"), "taken");

        assertThatThrownBy(() -> storage.renameFile(source.toUri(), "taken.txt"))
                .isInstanceOf(FileAlreadyExistsException.class);
        assertThat(Files.readString(source)).isEqualTo("source");
        assertThat(Files.readString(taken)).isEqualTo("taken");
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void writeFileBytes_missingFile_fileCreatedWithTheContent(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = rootOf(storage).resolve("data.bin");

        storage.writeFile(path.toUri(), new byte[] {1, 2, 3});

        assertThat(Files.readAllBytes(path)).containsExactly(1, 2, 3);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void writeFileBytes_existingFile_oldContentReplaced(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = Files.write(rootOf(storage).resolve("data.bin"), new byte[] {9, 9, 9, 9, 9});

        storage.writeFile(path.toUri(), new byte[] {1, 2});

        assertThat(Files.readAllBytes(path)).containsExactly(1, 2);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void writeFileBytes_missingParent_noSuchFileException(
            Supplier<FileStorage<GenericFile>> storageFactory) {
        var storage = storageFactory.get();
        var path = rootOf(storage).resolve("missing").resolve("data.bin");

        assertThatThrownBy(() -> storage.writeFile(path.toUri(), new byte[] {1}))
                .isInstanceOf(NoSuchFileException.class);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void writeFileBytes_folder_ioExceptionAndFolderKept(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var folder = Files.createDirectory(rootOf(storage).resolve("folder"));

        assertThatThrownBy(() -> storage.writeFile(folder.toUri(), new byte[] {1})).isInstanceOf(IOException.class);
        assertThat(Files.isDirectory(folder)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void writeFileText_missingFile_fileCreatedWithTheContent(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = rootOf(storage).resolve("text.txt");

        storage.writeFile(path.toUri(), CONTENT, StandardCharsets.UTF_8);

        assertThat(Files.readString(path, StandardCharsets.UTF_8)).isEqualTo(CONTENT);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void writeFileText_existingFile_oldContentReplacedAndCharsetUsed(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = Files.writeString(rootOf(storage).resolve("text.txt"), "a much longer old content");

        storage.writeFile(path.toUri(), CONTENT, StandardCharsets.ISO_8859_1);

        assertThat(Files.readAllBytes(path)).containsExactly(0x63, 0x61, 0x66, 0xE9);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void writeFileText_missingParent_noSuchFileException(
            Supplier<FileStorage<GenericFile>> storageFactory) {
        var storage = storageFactory.get();
        var path = rootOf(storage).resolve("missing").resolve("text.txt");

        assertThatThrownBy(() -> storage.writeFile(path.toUri(), CONTENT, StandardCharsets.UTF_8))
                .isInstanceOf(NoSuchFileException.class);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void writeFileText_folder_ioExceptionAndFolderKept(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var folder = Files.createDirectory(rootOf(storage).resolve("folder"));

        assertThatThrownBy(() -> storage.writeFile(folder.toUri(), CONTENT, StandardCharsets.UTF_8))
                .isInstanceOf(IOException.class);
        assertThat(Files.isDirectory(folder)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void readFileBytes_existingFile_wholeContent(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = Files.write(rootOf(storage).resolve("data.bin"), new byte[] {1, 2, 3});

        assertThat(storage.readFile(path.toUri())).containsExactly(1, 2, 3);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void readFileBytes_emptyFile_emptyArray(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = Files.createFile(rootOf(storage).resolve("empty.bin"));

        assertThat(storage.readFile(path.toUri())).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void readFileBytes_missingFile_noSuchFileException(
            Supplier<FileStorage<GenericFile>> storageFactory) {
        var storage = storageFactory.get();
        var missing = rootOf(storage).resolve("missing.bin").toUri();

        assertThatThrownBy(() -> storage.readFile(missing)).isInstanceOf(NoSuchFileException.class);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void readFileBytes_folder_ioException(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var folder = Files.createDirectory(rootOf(storage).resolve("folder"));

        assertThatThrownBy(() -> storage.readFile(folder.toUri())).isInstanceOf(IOException.class);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void readFileText_existingFile_contentDecodedWithTheCharset(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var path = Files.writeString(rootOf(storage).resolve("text.txt"), CONTENT, StandardCharsets.ISO_8859_1);

        assertThat(storage.readFile(path.toUri(), StandardCharsets.ISO_8859_1)).isEqualTo(CONTENT);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void readFileText_missingFile_noSuchFileException(
            Supplier<FileStorage<GenericFile>> storageFactory) {
        var storage = storageFactory.get();
        var missing = rootOf(storage).resolve("missing.txt").toUri();

        assertThatThrownBy(() -> storage.readFile(missing, StandardCharsets.UTF_8))
                .isInstanceOf(NoSuchFileException.class);
    }

    @ParameterizedTest
    @MethodSource("flavors")
    public void readFileText_folder_ioException(
            Supplier<FileStorage<GenericFile>> storageFactory) throws Exception {
        var storage = storageFactory.get();
        var folder = Files.createDirectory(rootOf(storage).resolve("folder"));

        assertThatThrownBy(() -> storage.readFile(folder.toUri(), StandardCharsets.UTF_8))
                .isInstanceOf(IOException.class);
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void getFiles_fileWithHiddenAttribute_hiddenOnWindows(@TempDir Path directory) throws Exception {
        var hidden = Files.writeString(directory.resolve("hidden.txt"), "hello");
        Files.setAttribute(hidden, "dos:hidden", true);
        Files.writeString(directory.resolve("visible.txt"), "hello");
        var storage = new WindowsFileStorage<GenericFile>(FileStorageType.BASE, "test", directory.toUri(),
                DefaultGenericFile::new);

        var files = storage.getFiles(directory.toUri());

        assertThat(find(files, "hidden.txt").isHidden()).isTrue();
        assertThat(find(files, "visible.txt").isHidden()).isFalse();
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void getFiles_junction_listedAsLinkAndNeverEntered(@TempDir Path directory) throws Exception {
        var target = Files.createDirectory(directory.resolve("target"));
        Files.writeString(target.resolve("inside.txt"), "hello");
        var junction = directory.resolve("junction");
        var process = new ProcessBuilder("cmd", "/c", "mklink", "/J", junction.toString(), target.toString())
                .inheritIO().start();
        assertThat(process.waitFor()).as("mklink /J").isZero();
        var storage = new WindowsFileStorage<GenericFile>(FileStorageType.BASE, "test", directory.toUri(),
                DefaultGenericFile::new);

        var files = storage.getFiles(directory.toUri());
        var directories = storage.getDirectories(directory.toUri());
        var recursively = storage.getFilesRecursively(directory.toUri());

        var link = find(files, "junction");
        assertThat(link.getEntryType()).isEqualTo(FileEntryType.LINK);
        assertThat(link.isLink()).isTrue();
        assertThat(link.isDirectory()).isFalse();
        assertThat(Paths.get(link.getLinkTarget().getUri()).toRealPath()).isEqualTo(target.toRealPath());
        assertThat(link.getLinkTarget().getEntryType()).isEqualTo(FileEntryType.DIRECTORY);
        assertThat(directories).extracting(GenericFile::getName).containsExactly("target");
        assertThat(recursively).extracting(GenericFile::getName)
                .containsExactlyInAnyOrder("target", "inside.txt", "junction");
    }
}
