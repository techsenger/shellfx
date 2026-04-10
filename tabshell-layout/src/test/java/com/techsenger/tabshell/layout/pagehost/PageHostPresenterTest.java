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

package com.techsenger.tabshell.layout.pagehost;

import com.techsenger.tabshell.core.page.TreePageItem;
import com.techsenger.tabshell.material.icon.Icon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Pavel Castornii
 */
public class PageHostPresenterTest {

    /*
     * Tree structure:
     *
     * root
     * ├── settings
     * │   ├── profile
     * │   └── security
     * ├── dashboard
     * │   ├── analytics
     * │   └── reports
     * └── users
     *     ├── admins
     *     └── guests
     */
    static TestPageItem buildTree() {
        var profile   = new TestPageItem("profile");
        var security  = new TestPageItem("security");
        var analytics = new TestPageItem("analytics");
        var reports   = new TestPageItem("reports");
        var admins    = new TestPageItem("admins");
        var guests    = new TestPageItem("guests");

        var settings  = new TestPageItem("settings",  profile, security);
        var dashboard = new TestPageItem("dashboard", analytics, reports);
        var users     = new TestPageItem("users",     admins, guests);

        return new TestPageItem("root", settings, dashboard, users);
    }

    static Matcher matcher(String text) {
        return Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE).matcher("");
    }

    static FindStatistics newStats() {
        return new FindStatistics();
    }

    static FilteredTreePageItem childByText(FilteredTreePageItem parent, String text) {
        return parent.getChildren().stream()
            .filter(c -> c.getOriginal().getText().equals(text))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Child not found: " + text));
    }

    static class TestPageItem implements TreePageItem {

        private final String text;
        private final List<TestPageItem> children;
        private TestPageItem parent;

        TestPageItem(String text, TestPageItem... children) {
            this.text = text;
            this.children = new ArrayList<>(Arrays.asList(children));

            for (TestPageItem child : children) {
                child.parent = this;
            }
        }

        @Override
        public TestPageItem getParent() {
            return parent;
        }

        @Override
        public List<TestPageItem> getChildren() {
            return children;
        }

        @Override public String getText() {
            return text;
        }

        @Override public Icon<?> getIcon() {
            return null;
        }
    }

    @Test
    void match_singleNodeMatches_returnsNode() {
        var node = new TestPageItem("Hello");
        var stats = newStats();

        var result = TreePageHostPresenter.match(node, matcher("Hello"), stats);

        assertThat(result).isNotNull();
        assertThat(result.getOriginal()).isSameAs(node);
        assertThat(result.getChildren()).isEmpty();

        assertThat(stats.getTotal()).isEqualTo(1);
        assertThat(stats.getMatches()).isEqualTo(1);
    }

    @Test
    void match_singleNodeNoMatch_returnsNull() {
        var node = new TestPageItem("Hello");
        var stats = newStats();

        var result = TreePageHostPresenter.match(node, matcher("xyz"), stats);

        assertThat(result).isNull();

        assertThat(stats.getTotal()).isEqualTo(1);
        assertThat(stats.getMatches()).isEqualTo(0);
    }

    @Test
    void match_caseInsensitive_returnsNode() {
        var node = new TestPageItem("Hello");
        var stats = newStats();

        var result = TreePageHostPresenter.match(node, matcher("hello"), stats);

        assertThat(result).isNotNull();
        assertThat(result.getOriginal()).isSameAs(node);

        assertThat(stats.getTotal()).isEqualTo(1);
        assertThat(stats.getMatches()).isEqualTo(1);
    }

    @Test
    void match_partialTextMatch_returnsNode() {
        var node = new TestPageItem("HelloWorld");
        var stats = newStats();

        var result = TreePageHostPresenter.match(node, matcher("World"), stats);

        assertThat(result).isNotNull();
        assertThat(result.getOriginal()).isSameAs(node);

        assertThat(stats.getTotal()).isEqualTo(1);
        assertThat(stats.getMatches()).isEqualTo(1);
    }

    @Test
    void match_noMatch_returnsNull() {
        var root = buildTree();
        var stats = newStats();

        var result = TreePageHostPresenter.match(root, matcher("xyz"), stats);

        assertThat(result).isNull();

        assertThat(stats.getTotal()).isEqualTo(10);
        assertThat(stats.getMatches()).isEqualTo(0);
    }

    @Test
    void match_leafMatches_wholeChainIncluded() {
        var root = buildTree();
        var stats = newStats();

        var result = TreePageHostPresenter.match(root, matcher("profile"), stats);

        assertThat(result.getOriginal().getText()).isEqualTo("root");

        assertThat(result.getChildren())
            .extracting(f -> f.getOriginal().getText())
            .containsExactly("settings");

        var settings = result.getChildren().get(0);

        assertThat(settings.getChildren())
            .extracting(f -> f.getOriginal().getText())
            .containsExactly("profile");

        assertThat(settings.getChildren().get(0).getChildren()).isEmpty();

        assertThat(stats.getTotal()).isEqualTo(10);
        assertThat(stats.getMatches()).isEqualTo(1);
    }

    @Test
    void match_leafMatches_siblingsExcluded() {
        var root = buildTree();
        var stats = newStats();

        var result = TreePageHostPresenter.match(root, matcher("profile"), stats);

        var settings = childByText(result, "settings");

        assertThat(settings.getChildren())
            .extracting(f -> f.getOriginal().getText())
            .containsExactly("profile");

        assertThat(stats.getTotal()).isEqualTo(10);
        assertThat(stats.getMatches()).isEqualTo(1);
    }

    @Test
    void match_leafMatches_otherBranchesExcluded() {
        var root = buildTree();
        var stats = newStats();

        var result = TreePageHostPresenter.match(root, matcher("profile"), stats);

        assertThat(result.getChildren())
            .extracting(f -> f.getOriginal().getText())
            .doesNotContain("dashboard", "users");

        assertThat(stats.getTotal()).isEqualTo(10);
        assertThat(stats.getMatches()).isEqualTo(1);
    }

    @Test
    void match_intermediateNodeMatches_childrenExcluded() {
        var root = buildTree();
        var stats = newStats();

        var result = TreePageHostPresenter.match(root, matcher("users"), stats);

        var users = childByText(result, "users");

        assertThat(users.getChildren()).isEmpty();

        assertThat(stats.getTotal()).isEqualTo(10);
        assertThat(stats.getMatches()).isEqualTo(1);
    }

    @Test
    void match_rootMatches_childrenExcluded() {
        var root = buildTree();
        var stats = newStats();

        var result = TreePageHostPresenter.match(root, matcher("root"), stats);

        assertThat(result.getOriginal().getText()).isEqualTo("root");
        assertThat(result.getChildren()).isEmpty();

        assertThat(stats.getTotal()).isEqualTo(10);
        assertThat(stats.getMatches()).isEqualTo(1);
    }

    @Test
    void match_manyMatches_statisticsCorrect() {
        var root = buildTree();
        var stats = newStats();

        var result = TreePageHostPresenter.match(root, matcher("s"), stats);

        assertThat(result).isNotNull();
        assertThat(result.getOriginal().getText()).isEqualTo("root");

        assertThat(result.getChildren())
            .extracting(f -> f.getOriginal().getText())
            .containsExactlyInAnyOrder("settings", "dashboard", "users");

        var settings = childByText(result, "settings");
        assertThat(settings.getChildren())
            .extracting(f -> f.getOriginal().getText())
            .containsExactly("security");

        var dashboard = childByText(result, "dashboard");
        assertThat(dashboard.getChildren())
            .extracting(f -> f.getOriginal().getText())
            .containsExactlyInAnyOrder("analytics", "reports");

        var users = childByText(result, "users");
        assertThat(users.getChildren())
            .extracting(f -> f.getOriginal().getText())
            .containsExactlyInAnyOrder("admins", "guests");

        assertThat(stats.getTotal()).isEqualTo(10);
        assertThat(stats.getMatches()).isEqualTo(8);
    }
}
