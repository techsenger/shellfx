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

package com.techsenger.tabshell.terminal.area;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import com.techsenger.jeditermfx.core.util.Platform;
import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.area.AbstractAreaPresenter;
import com.techsenger.tabshell.core.settings.SettingsSubscription;
import com.techsenger.tabshell.shared.web.WebBrowser;
import com.techsenger.tabshell.terminal.TerminalComponentNames;
import com.techsenger.tabshell.terminal.TerminalPaletteType;
import com.techsenger.tabshell.terminal.TerminalTabPort;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class TerminalAreaPresenter<V extends TerminalAreaView, C extends TerminalAreaComposer>
        extends AbstractAreaPresenter<V, C> {

    private static final Logger logger = LoggerFactory.getLogger(TerminalAreaPresenter.class);

    protected class Port extends AbstractAreaPresenter.Port implements TerminalAreaPort {

        private final TerminalAreaPresenter<?, ?> presenter = TerminalAreaPresenter.this;

        @Override
        public void addNew() {
            getComposer().addTerminal(presenter.browser.get().getDirectory());
        }

        @Override
        public void clear() {
            getView().clear();
            getView().requestFocus();
        }

        @Override
        public void copy() {
            getView().copy();
            getView().requestFocus();
        }

        @Override
        public void paste() {
            getView().paste();
            getView().requestFocus();
        }

        @Override
        public void selectAll() {
            getView().selectAll();
            getView().requestFocus();
        }

        @Override
        public void scrollPageUp() {
            getView().scrollPageUp();
            getView().requestFocus();
        }

        @Override
        public void scrollPageDown() {
            getView().scrollPageDown();
            getView().requestFocus();
        }

        @Override
        public void scrollLineUp() {
            getView().scrollLineUp();
            getView().requestFocus();
        }

        @Override
        public void scrollLineDown() {
            getView().scrollLineDown();
            getView().requestFocus();
        }

        @Override
        public void setPaletteType(TerminalPaletteType type) {
            presenter.setPaletteType(type);
        }
    }

    private final Supplier<TerminalTabPort> browser;

    private TerminalPalette terminalPalette;

    private PtyProcessTtyConnector ttyConnector;

    private String selectedText;

    private SettingsSubscription themeSubscription;

    public TerminalAreaPresenter(V view, Supplier<TerminalTabPort> browser) {
        super(view);
        this.browser = browser;
    }

    @Override
    public TerminalAreaPort getPort() {
        return (TerminalAreaPort) super.getPort();
    }

    @Override
    protected Port createPort() {
        return new TerminalAreaPresenter.Port();
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(TerminalComponentNames.TERMINAL_AREA);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        this.ttyConnector = createTtyConnector(browser.get().getDirectory());
        getView().createTerminal(createSettingsProvider(), ttyConnector);
        var pt = browser.get().getToolBar().getPaletteType();
        setPaletteType(pt);
        this.themeSubscription = browser.get().getShell().getSettings().getAppearance().observeTheme((oldV, newV) -> {
            terminalPalette.setTheme(newV);
            getView().requestFocus();
        });
    }

    @Override
    protected void postDeinitialize() {
        super.postDeinitialize();
        this.ttyConnector.close();
        this.themeSubscription.unsubscribe();
    }

    protected TerminalSettingsProvider createSettingsProvider() {
        var settings = browser.get().getShell().getSettings();
        this.terminalPalette = new TerminalPalette(settings.getAppearance().getTheme(),
                browser.get().getToolBar().getPaletteType());
        return new TerminalSettingsProvider(settings.getAppearance().getMonospaceFont(), terminalPalette);
    }

    protected PtyProcessTtyConnector createTtyConnector(String directory) {
        try {
            Map<String, String> envs = System.getenv();
            String[] command;
            if (Platform.isWindows()) {
                command = new String[]{"cmd.exe"};
            } else {
                command = new String[]{"/bin/bash", "--login"};
                envs = new HashMap<>(System.getenv());
                envs.put("TERM", "xterm-256color");
            }
            PtyProcess process = new PtyProcessBuilder()
                    .setDirectory(directory)
                    .setCommand(command).setEnvironment(envs).start();
            return new PtyProcessTtyConnector(process, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected void handleTextSelected(String selectedText) {
        this.selectedText = selectedText;
        browser.get().getToolBar().setCopyDisable(selectedText == null);
    }

    protected String getSelectedText() {
        return selectedText;
    }

    protected void handleLinkAction(String url) {
        var settings = browser.get().getShell().getSettings();
        if (settings.getWebBrowser().isUsedByDefault()) {
            getComposer().addWebBrowser(url);
        } else {
            WebBrowser.open(url);
        }
    }

    protected void handleShowFind() {
        if (getComposer().getFindPanel() == null) {
            getComposer().addFindPanel(browser.get().getHistory().getFindPanel());
        }
        var findPane = getComposer().getFindPanel();
        var selectedText = getView().getSelectedText();
        if (selectedText != null) {
            findPane.setFindText(selectedText);
        }
        findPane.requestFocus();
    }

    protected void handleHideFind() {
        if (getComposer().getFindPanel() != null) {
            getComposer().removeFindPanel();
            getView().requestFocus();
        }
    }

    private boolean isTextUrl(String text) {
        if (text != null) {
            try {
                URI uri = new URI(text);
                uri.toURL();
                return true;
            } catch (Exception e) {
                //do nothing
            }
        }
        return false;
    }

    private void setPaletteType(TerminalPaletteType type) {
        terminalPalette.setPaletteType(type);
        getView().requestFocus();
    }
}
