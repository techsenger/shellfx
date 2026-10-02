/*
 * Copyright 2026 Pavel Castornii.
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
import java.nio.file.FileSystem;
import java.nio.file.Files;
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

/**
 * Integration tests for the system file storages ({@link UnixFileStorage} for Linux and macOS,
 * {@link WindowsFileStorage}): how they describe entries - above all links, which must be reported as links, never
 * as the file or directory they point to, so that code walking a directory tree can avoid going through them.
 * Run against a fresh in-memory Jimfs file system of each flavor for every test, the root of which is the root of
 * the storage. What Jimfs can't imitate (Windows junctions, the Windows hidden attribute) is tested separately on
 * the real file system, only on Windows.
 *
 * @author Pavel Castornii
 */
public class SystemFileStorageIT {

    /**
     * Keeps every created Jimfs file system strongly reachable for the lifetime of the test JVM - a storage only
     * keeps the root URI, not the {@link FileSystem} object itself, and Jimfs registers a file system by URI only
     * as long as something still references it; without this, an unlucky GC can collect it mid-test.
     */
    private static final List<FileSystem> RETAINED_FILE_SYSTEMS = new CopyOnWriteArrayList<>();

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

    private static GenericFile find(List<GenericFile> files, String name) {
        return files.stream().filter(file -> file.getName().equals(name)).findFirst().orElseThrow();
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
        assertThat(file.getTargetUri()).isNull();
        var folder = find(files, "folder");
        assertThat(folder.getEntryType()).isEqualTo(FileEntryType.DIRECTORY);
        assertThat(folder.isLink()).isFalse();
        assertThat(folder.getTargetUri()).isNull();
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
        assertThat(Paths.get(link.getTargetUri())).isEqualTo(target);
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
        assertThat(Paths.get(link.getTargetUri())).isEqualTo(target);
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
        assertThat(Paths.get(link.getTargetUri()).normalize()).isEqualTo(real);
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
        assertThat(Paths.get(link.getTargetUri())).isEqualTo(missing);
        assertThat(Files.exists(missing)).isFalse();
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
        assertThat(Paths.get(file.getTargetUri())).isEqualTo(target);
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
        assertThat(Paths.get(link.getTargetUri()).toRealPath()).isEqualTo(target.toRealPath());
        assertThat(directories).extracting(GenericFile::getName).containsExactly("target");
        assertThat(recursively).extracting(GenericFile::getName)
                .containsExactlyInAnyOrder("target", "inside.txt", "junction");
    }
}
