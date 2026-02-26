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

import com.techsenger.jeditermfx.ui.DefaultHyperlinkFilter;
import com.techsenger.jeditermfx.ui.TerminalPanel;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.area.AbstractAreaFxView;
import com.techsenger.tabshell.core.tab.TabContainerFxView;
import com.techsenger.tabshell.shared.find.FindPanelHistory;
import com.techsenger.tabshell.terminal.TerminalTabFxView;
import com.techsenger.tabshell.terminal.TerminalTabPresenter;
import com.techsenger.tabshell.terminal.find.FindPanelFxView;
import com.techsenger.tabshell.terminal.find.FindPanelPort;
import com.techsenger.tabshell.terminal.find.FindPanelPresenter;
import com.techsenger.tabshell.web.WebBrowserTabFxView;
import com.techsenger.tabshell.web.WebBrowserTabPresenter;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class TerminalAreaFxView<P extends TerminalAreaPresenter<?, ?>> extends AbstractAreaFxView<P>
        implements TerminalAreaView {

    public class Composer extends AbstractAreaFxView<P>.Composer implements TerminalAreaComposer {

        private final TerminalAreaFxView<P> view = TerminalAreaFxView.this;

        @Override
        public void addWebBrowser(String url) {
            var browser = createWebBrowser(url);
            browser.getPresenter().initialize();
            tabContainer.getComposer().addTab(browser);
        }

        @Override
        public void addTerminal(String directory) {
            var terminal = createTerminal(directory);
            terminal.getPresenter().initialize();
            tabContainer.getComposer().addTab(terminal);
        }

        @Override
        public void addFindPanel(FindPanelHistory history) {
            if (view.findPanel != null) {
                return;
            }
            view.findPanel = createFindPanel(history);
            view.findPanel.getPresenter().initialize();
            view.getModifiableChildren().add(findPanel);
            view.node.getChildren().add(view.findPanel.getNode());
        }

        @Override
        public void removeFindPanel() {
            if (view.findPanel == null) {
                return;
            }
            view.getModifiableChildren().remove(findPanel);
            view.node.getChildren().remove(view.findPanel.getNode());
            view.findPanel.getPresenter().deinitialize();
            view.findPanel = null;
        }

        @Override
        public FindPanelPort getFindPanel() {
            return view.findPanel == null ? null : view.findPanel.getPresenter().getPort();
        }

        protected WebBrowserTabFxView<?> createWebBrowser(String url) {
            var view = new WebBrowserTabFxView<>(shell);
            var presenter = new WebBrowserTabPresenter<>(view, url);
            return view;
        }

        protected TerminalTabFxView<?> createTerminal(String directory) {
            var view = new TerminalTabFxView<>(shell, tabContainer);
            var presenter = new TerminalTabPresenter<>(view, directory, shell.getPresenter().getHistoryManager());
            return view;
        }

        protected FindPanelFxView<?> createFindPanel(FindPanelHistory history) {
            var view = new FindPanelFxView<>(widget);
            var presenter = new FindPanelPresenter<>(view, () -> history, widget.getTerminalTextBuffer(),
                    () -> getPresenter().onHideFind());
            return view;
        }
    }

    private TabJediTermFxWidget widget;

    private final ShellFxView<?> shell;

    private final TabContainerFxView<?> tabContainer;

    private FindPanelFxView<?> findPanel;

    private final VBox node = new VBox();

    public TerminalAreaFxView(ShellFxView<?> shell, TabContainerFxView<?> tabContainer) {
        this.shell = shell;
        this.tabContainer = tabContainer;
    }

    @Override
    public void requestFocus() {
        NodeUtils.requestFocus(widget.getTerminalPanel().getCanvas());
    }

    @Override
    public Region getNode() {
        return node;
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    public void createTerminal(TerminalSettingsProvider settingsProvider, PtyProcessTtyConnector ttyConnector) {
        this.widget = new TabJediTermFxWidget(80, 24, settingsProvider, () -> getPresenter().onShowFind());
        widget.setTtyConnector(ttyConnector);
        widget.addHyperlinkFilter(new DefaultHyperlinkFilter() {

            @Override
            protected void open(String url) {
                getPresenter().onOpenLink(url);
            }
        });
        VBox.setVgrow(widget.getPane(), Priority.ALWAYS);
        widget.start();
        getTerminalPanel().selectedTextProperty()
                .addListener((ov, oldV, newV) -> getPresenter().onTextSelected(newV));
        this.node.getChildren().add(widget.getPane());
    }

    @Override
    public void clear() {
        getTerminalPanel().clearBuffer();
    }

    @Override
    public void copy() {
        getTerminalPanel().handleCopy(false, false);
    }

    @Override
    public void paste() {
        getTerminalPanel().handlePaste();
    }

    @Override
    public void selectAll() {
        getTerminalPanel().selectAll();
    }

    @Override
    public void scrollPageUp() {
        getTerminalPanel().pageUp();
    }

    @Override
    public void scrollPageDown() {
        getTerminalPanel().pageDown();
    }

    @Override
    public void scrollLineUp() {
        getTerminalPanel().scrollUp();
    }

    @Override
    public void scrollLineDown() {
        getTerminalPanel().scrollDown();
    }

    @Override
    public String getSelectedText() {
        return getTerminalPanel().getSelectedText();
    }

    @Override
    protected void deinitialize() {
        widget.close();
        super.deinitialize();
    }

    @Override
    protected Composer createComposer() {
        return new TerminalAreaFxView.Composer();
    }

    protected TabJediTermFxWidget getWidget() {
        return widget;
    }

    protected FindPanelFxView<?> getFindPanel() {
        return findPanel;
    }

    @Override
    protected void build() {
        super.build();
        VBox.setVgrow(node, Priority.ALWAYS);
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        node.addEventFilter(KeyEvent.KEY_PRESSED, (e) -> {
            if (this.findPanel != null && e.getCode() == KeyCode.ESCAPE) {
                getPresenter().onHideFind();
                e.consume();
            }
        });
    }

    private TerminalPanel getTerminalPanel() {
        return this.widget.getTerminalPanel();
    }
}
