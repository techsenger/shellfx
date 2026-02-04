///*
// * Copyright 2024-2026 Pavel Castornii.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.techsenger.tabshell.dialogs.file;
//
//import com.techsenger.tabshell.storage.GenericFile;
//import javafx.util.StringConverter;
//
//
///**
// *
// * @author Pavel Castornii
// */
//class FileStringConverter extends StringConverter<GenericFile> {
//
//    /**
//     * It is supposed that toString is called always before fromString.
//     */
//    private GenericFile file;
//
//    @Override
//    public String toString(GenericFile object) {
//        this.file = object;
//        return object != null ? object.getName() : "";
//    }
//
//    @Override
//    public GenericFile fromString(String string) {
//        var builder = new GenericFile.Builder();
//        builder.setAllFrom(file);
//        builder.name(string);
//        var builtFile = builder.build();
//        return builtFile;
//    }
//}
