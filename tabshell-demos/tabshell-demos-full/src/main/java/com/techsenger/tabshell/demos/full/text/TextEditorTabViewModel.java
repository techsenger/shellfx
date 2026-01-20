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
//package com.techsenger.tabshell.demos.full.text;
//
//import com.techsenger.patternfx.core.HistoryPolicy;
//import com.techsenger.tabshell.core.history.HistoryManager;
//import com.techsenger.tabshell.core.menu.SimpleMenuItemHelper;
//import com.techsenger.tabshell.dialogs.file.ExtensionFilter;
//import com.techsenger.tabshell.shared.menu.EditMenuNames;
//import com.techsenger.tabshell.storage.GenericFile;
//import com.techsenger.tabshell.text.editor.AbstractEditorTabViewModel;
//import com.techsenger.tabshell.text.editor.EditorTabMediator;
//import com.techsenger.tabshell.text.style.TextIcons;
//import java.util.List;
//
///**
// *
// * @author Pavel Castornii
// */
//public class TextEditorTabViewModel extends AbstractEditorTabViewModel<EditorTabMediator> {
//
//    public TextEditorTabViewModel(GenericFile file, HistoryManager historyManager) {
//        super(file);
//        setIcon(TextIcons.EDITOR);
//        setHistoryPolicy(HistoryPolicy.ALL);
//        //the initial history is created using a factory instead of reflection in the history manager to avoid
//        //access issues with hidden packages
//        setHistoryProvider(() -> historyManager
//                .getOrCreateHistory(TextEditorTabHistory.class, TextEditorTabHistory::new));
//        //these validators will be used when menu is shown
//        addMenuItemHelpers(new SimpleMenuItemHelper(EditMenuNames.REPLACE, null, true));
//        addMenuItemHelpers(new SimpleMenuItemHelper(EditMenuNames.GO_TO_LINE, null, true));
//    }
//
//    @Override
//    public List<ExtensionFilter> createOpenExtensionFilters() {
//        return List.of(
//                new ExtensionFilter("All Files", true, "*.*"),
//                new ExtensionFilter("Text Files", true, "*.txt")
//        );
//    }
//
//    @Override
//    public List<ExtensionFilter> createSaveExtensionFilters() {
//        return List.of(
//                new ExtensionFilter("All Files", true, "*.*"),
//                new ExtensionFilter("Text Files", true, "*.txt")
//        );
//    }
//}
