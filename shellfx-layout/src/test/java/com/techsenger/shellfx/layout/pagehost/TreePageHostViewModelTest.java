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

package com.techsenger.shellfx.layout.pagehost;

import com.techsenger.shellfx.core.page.TreePageItem;
import com.techsenger.shellfx.material.icon.Icon;
import com.techsenger.shellfx.shared.find.TextMatcherFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Pavel Castornii
 */
public class TreePageHostViewModelTest {

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
        return TextMatcherFactory.create(text, false);
    }

    static MatchCounts newMatchCounts() {
        return new MatchCounts();
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
        var counts = newMatchCounts();

        var result = TreePageHostViewModel.match(node, matcher("Hello"), counts);

        assertThat(result).isNotNull();
        assertThat(result.getOriginal()).isSameAs(node);
        assertThat(result.getChildren()).isEmpty();

        assertThat(counts.getTotalItems()).isEqualTo(1);
        assertThat(counts.getTotalMatches()).isEqualTo(1);
    }

    @Test
    void match_singleNodeNoMatch_returnsNull() {
        var node = new TestPageItem("Hello");
        var counts = newMatchCounts();

        var result = TreePageHostViewModel.match(node, matcher("xyz"), counts);

        assertThat(result).isNull();

        assertThat(counts.getTotalItems()).isEqualTo(1);
        assertThat(counts.getTotalMatches()).isEqualTo(0);
    }

    @Test
    void match_caseInsensitive_returnsNode() {
        var node = new TestPageItem("Hello");
        var counts = newMatchCounts();

        var result = TreePageHostViewModel.match(node, matcher("hello"), counts);

        assertThat(result).isNotNull();
        assertThat(result.getOriginal()).isSameAs(node);

        assertThat(counts.getTotalItems()).isEqualTo(1);
        assertThat(counts.getTotalMatches()).isEqualTo(1);
    }

    @Test
    void match_partialTextMatch_returnsNode() {
        var node = new TestPageItem("HelloWorld");
        var counts = newMatchCounts();

        var result = TreePageHostViewModel.match(node, matcher("World"), counts);

        assertThat(result).isNotNull();
        assertThat(result.getOriginal()).isSameAs(node);

        assertThat(counts.getTotalItems()).isEqualTo(1);
        assertThat(counts.getTotalMatches()).isEqualTo(1);
    }

    @Test
    void match_noMatch_returnsNull() {
        var root = buildTree();
        var counts = newMatchCounts();

        var result = TreePageHostViewModel.match(root, matcher("xyz"), counts);

        assertThat(result).isNull();

        assertThat(counts.getTotalItems()).isEqualTo(10);
        assertThat(counts.getTotalMatches()).isEqualTo(0);
    }

    @Test
    void match_leafMatches_wholeChainIncluded() {
        var root = buildTree();
        var counts = newMatchCounts();

        var result = TreePageHostViewModel.match(root, matcher("profile"), counts);

        assertThat(result.getOriginal().getText()).isEqualTo("root");

        assertThat(result.getChildren())
            .extracting(f -> f.getOriginal().getText())
            .containsExactly("settings");

        var settings = result.getChildren().get(0);

        assertThat(settings.getChildren())
            .extracting(f -> f.getOriginal().getText())
            .containsExactly("profile");

        assertThat(settings.getChildren().get(0).getChildren()).isEmpty();

        assertThat(counts.getTotalItems()).isEqualTo(10);
        assertThat(counts.getTotalMatches()).isEqualTo(1);
    }

    @Test
    void match_leafMatches_siblingsExcluded() {
        var root = buildTree();
        var counts = newMatchCounts();

        var result = TreePageHostViewModel.match(root, matcher("profile"), counts);

        var settings = childByText(result, "settings");

        assertThat(settings.getChildren())
            .extracting(f -> f.getOriginal().getText())
            .containsExactly("profile");

        assertThat(counts.getTotalItems()).isEqualTo(10);
        assertThat(counts.getTotalMatches()).isEqualTo(1);
    }

    @Test
    void match_leafMatches_otherBranchesExcluded() {
        var root = buildTree();
        var counts = newMatchCounts();

        var result = TreePageHostViewModel.match(root, matcher("profile"), counts);

        assertThat(result.getChildren())
            .extracting(f -> f.getOriginal().getText())
            .doesNotContain("dashboard", "users");

        assertThat(counts.getTotalItems()).isEqualTo(10);
        assertThat(counts.getTotalMatches()).isEqualTo(1);
    }

    @Test
    void match_intermediateNodeMatches_childrenExcluded() {
        var root = buildTree();
        var counts = newMatchCounts();

        var result = TreePageHostViewModel.match(root, matcher("users"), counts);

        var users = childByText(result, "users");

        assertThat(users.getChildren()).isEmpty();

        assertThat(counts.getTotalItems()).isEqualTo(10);
        assertThat(counts.getTotalMatches()).isEqualTo(1);
    }

    @Test
    void match_rootMatches_childrenExcluded() {
        var root = buildTree();
        var counts = newMatchCounts();

        var result = TreePageHostViewModel.match(root, matcher("root"), counts);

        assertThat(result.getOriginal().getText()).isEqualTo("root");
        assertThat(result.getChildren()).isEmpty();

        assertThat(counts.getTotalItems()).isEqualTo(10);
        assertThat(counts.getTotalMatches()).isEqualTo(1);
    }

    @Test
    void match_manyMatches_findResultCorrect() {
        var root = buildTree();
        var counts = newMatchCounts();

        var result = TreePageHostViewModel.match(root, matcher("s"), counts);

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

        assertThat(counts.getTotalItems()).isEqualTo(10);
        assertThat(counts.getTotalMatches()).isEqualTo(8);
    }
}
