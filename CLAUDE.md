# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Techsenger ShellFX is a Java/JavaFX platform for building applications structured as a tree of MVP
(Model-View-Presenter) components (window, tab, area, page, dialog, popup), each with its own lifecycle and
history. It is built on top of [PatternFX](https://github.com/techsenger/patternfx) (`patternfx-mvp`,
`patternfx-core`), which provides the underlying MVP component/tree machinery. ShellFX deliberately stays on
MVP — do not propose MVVM, StateFX, or property-binding patterns for presenters.

Requires Java 25 and JavaFX 26. Multi-module Maven build, parent POM inherits from `com.techsenger.maven.root`.

## Commands

```
mvn clean install          # build all modules (runs Checkstyle automatically via parent POM)
cd shellfx-demo && mvn javafx:run   # run the demo app (debugger settings are in shellfx-demo/pom.xml)
mvn test -pl shellfx-material       # run tests for a single module
mvn test -pl shellfx-material -Dtest=ColumnListViewTest   # run a single test class
```

Checkstyle runs as part of `mvn install`/`mvn verify` (results land in `target/checkstyle-result.xml` per
module) — a build failure may be a style violation, not just a compile error.

Only some modules currently have tests: `shellfx-material`, `shellfx-layout`, `shellfx-storage`. UI tests that
need a real `Stage` (e.g. `shellfx-material/.../column/*Test.java`) run headless via
`glass.platform=Headless` + `prism.order=sw`, started once through a shared `FxTestSupport`/`FxPlatform.start()`
helper and executed on the FX Application thread with `FxPlatform.runLaterAndWait`. When such a test asserts on
measured geometry, a real AtlantaFX user-agent stylesheet must be applied first, otherwise CSS `-color-*`
lookups silently fall back to defaults and mask regressions.

## Module structure

Modules and their dependency direction (all depend on `shellfx-material`, which depends on nothing else
in-repo):

```
material  (base UI elements, no shellfx deps)
  ├── core      (Shell, Window, Tab, Page, Dialog, Popup, registries, settings)
  ├── shared    (depends on core; FindBase/FindPanel used by other modules)
  ├── layout    (depends on core, shared; TabHost, DockHost, PageHost, TreePageHost)
  ├── storage   (depends on core; file-system abstractions)
  ├── dialogs   (depends on core, shared, storage; AlertDialog, FileChooserDialog, NameValueDialog)
  ├── devtools  (depends on core, shared, dialogs, layout; component-tree/scene-graph inspectors)
  ├── icons     (depends on material; Material Design Icons font + stylesheets)
  └── demo      (depends on everything; showcase app, not published — see publishing.plugin.exclusions)
```

Every module is a JPMS module (`module-info.java` under `src/main/java`); when adding a new public package,
remember to add both the `exports` (and `opens` for CSS/FXML-loaded packages) in `module-info.java`.

## Architecture

- **Component tree + scene graph are two parallel hierarchies.** Every component addition/removal must be
  reflected in both. Removing a node from the JavaFX scene graph without removing it from the component tree
  leaks memory. DevTools (`shellfx-devtools`) can inspect both trees live.
- **Lifecycle is explicit and developer-controlled.** Component init/deinit happens in `Composer` methods, not
  automatically — see Naming Convention below.
- **Core interfaces vs. base implementations.** Each component is an interface + a default `Abstract*`
  implementation (e.g. `ShellFxView` interface backing `Shell`). Code should reference the interface, not the
  concrete class, matching the platform's own convention (`ShellFxView`, not `DefaultShellFxView`).
- **Menu system.** The main menu is assembled dynamically at runtime by `ControlRegistry` from
  registered/unregistered menu, group, and item factories (supports plugin-style dynamic contribution).
  `MenuManager` tracks the focused component via `Scene#focusOwnerProperty()`, walks up the component tree to
  find the nearest ancestor implementing `MenuAwarePort`, and dispatches state/actions to it. A component that
  should focus on click of an empty area must call `requestFocus()` explicitly.
- **Windows.** `Window` comes in `NESTED` (managed by `WindowManager`, hosted inside a `HostWindow` or
  `HostTab`) and `TOP_LEVEL` (own OS `Stage`) variants, both accessed through the same API — dialogs/wizards
  built on `Window` work unmodified in either mode.
- **DockHost** (in `shellfx-layout`) has a whole-tree API (`ModelNode`/`GroupNode`/`AreaNode` built via
  `ModelNodeBuilder`, applied/captured via `Composer#applyModel`/`captureModel`) for full layout
  construction/restoration, and a partial-tree API (anchors resolved live via `Composer#getModelNode(AreaFxView)`)
  for incremental runtime changes (add-next-to, replace, remove, user-driven docking).

## Language

Everything in the project is written in English — README, documentation, Javadoc, code comments, commit
messages, etc. Always — regardless of what language the conversation with the assistant happens in.

## Member ordering

Not caught by Checkstyle (no `DeclarationOrder` module in the config) — must be applied by hand on every
edit, not just when writing new files.

Within a class/interface, members are sorted by three nested keys, each answering a different question and
breaking ties in the one before it:

1. **Scope — static vs. instance.** Does this member belong to the class or to the object? All static
   members form one block, placed entirely before all instance members. This is absolute — e.g. a
   `private static` method is placed above a `public` constructor, because scope outranks visibility.
2. **Role — types → fields → (constructors →) methods.** This is a dependency order, not an arbitrary
   bucket: nested types define the vocabulary that fields are declared with, fields hold the state that
   methods/constructors operate on. So within the static block: nested static types → static fields →
   static methods. Within the instance block: nested instance types → instance fields → constructors →
   instance methods.
3. **Visibility — `public` → `protected` → package-private → `private`.** Within one role (e.g. "instance
   methods"), the public contract comes before implementation detail.

So the full sequence in one class is: static nested types (public→private) → static fields
(public→private) → static methods (public→private) → nested instance types (public→private) → instance
fields (public→private) → constructors (public→private) → instance methods (public→private).

## Naming convention

Component classes follow: `[UniqueName][Role][Element]`, e.g. `AlertDialogFxView`, `EditorTabPresenter`,
`InfoPopupParams`, `ToolBarPort`. Role examples: `Tab`, `Window`, `Popup`, `Area`, `Panel`, `ToolBar`. Element
examples: `View`, `Presenter`, `FxView`, `Params`, `Port`, `History`.

`Composer` methods split into two categories — keep this distinction when adding new component types:
- Lifecycle-managing: `open*`/`close*` (create+add / remove+destroy) and `show*`/`hide*`.
- Structural-only (no lifecycle): `add*`/`remove*`/`replace*`.

| Component | Create + Add | Remove + Destroy | Add Only | Remove Only |
|---|---|---|---|---|
| Window | `openWindow(params)` | `closeWindow(window)` | `addWindow(window)` | `removeWindow(window)` |
| Tab | `openTab(params)` | `closeTab(tab)` | `addTab(tab)` | `removeTab(tab)` |
| Dialog | `openDialog(params)` | `closeDialog(dialog)` | `addDialog(dialog)` | `removeDialog(dialog)` |
| Popup | `openPopup(params)` | `closePopup(popup)` | `addPopup(popup)` | `removePopup(popup)` |
| Page | `openPage(params)` | `closePage(page)` | `addPage(page)` | `removePage(page)` |
| Area | `openArea(params)` | `closeArea(area)` | `addArea(area)` | `removeArea(area)` |

Prefer `create*`-delegating implementations of `open*`/`show*` where the pattern already uses them — it keeps
the created component swappable.

A `Map`-typed field/parameter/local/getter/setter is named `<values>By<key>` (e.g. `Map<FileType, Boolean>
selectionsByType`, `getSelectionsByType()`/`setSelectionsByType(...)`), not `<key><values>` (e.g.
`typeSelections`). The `by`-form reads directly as "which value, keyed by which type of key" at the
declaration site, without having to look at the generic type arguments to tell which side is the key.

## Javadoc

Document the contract — what the member does and why a caller would use it — never how it's implemented.
If a sentence just narrates the method body in prose (the steps it performs, the fields it touches along
the way), that's implementation detail the reader can already see by opening the method; cut it. A reader
deciding whether/how to call the member should get what they need without opening its body: what it
returns or does, plus any non-obvious constraint, side effect, or precondition — not a walkthrough.

Target 120-240 characters for the main description (the summary sentence plus any `<p>` continuation)
only — this range does not apply to `@param`/`@return`/`@throws`/`@see`/`{@link}` tags at all. Under 120
characters a javadoc rarely earns its place over just reading the signature; over 240 it has usually
drifted into narrating implementation or into a multi-paragraph essay — shorten it, or split the method
instead of padding its doc further. Avoid `{@link}`/cross-references to other classes or methods where
possible, especially to types from a different module/dependency (patternfx, shellfx, toolkit) — those
references rot silently when the referenced API changes and are harder to notice/fix than one in the same
file.

Accessor methods (get, set, is, xxxProperty) — should not have Javadoc unless they provide information
that is not obvious from the method name and type.

Tags follow a different, simpler rule: keep each tag's description as short as it can be while still
saying what it needs to; the only hard limit on it is the 120-char line length itself (wrap per the
indentation rule below if one line isn't enough). There is no minimum length for a tag.

**Tag coverage.** On `public`/`protected` methods, document every element that appears in the signature —
each `@param`, every checked exception via `@throws`, and so on — as tersely as the 120-char line allows;
a missing tag reads as an oversight on API surface other code depends on. Add `@return` only when the
return value carries semantics beyond what the method name and return type already say — nullability, a
sentinel/special value, which object state it reflects, resource ownership, mutability, a specific format,
or a constraint on the value. Skip `@return` when it would just restate the method name and type (e.g.
`getName()` returning `String` needs no `@return the name`).

On package-private/private methods, add `@param`/`@throws`/etc. only when that specific tag is actually
worth calling out (a non-obvious constraint, a surprising exception) — omitting the routine ones is fine.

Each `@param`, `@return`, `@throws`, and similar tag starts on a new line. The first line holds the tag,
the parameter name (if any), and the start of the description. When the description doesn't fit the
120-char line limit, continue it on the following line(s) using a **fixed 4-space indent relative to the
leading `*`**, not aligned under the start of the description — a fixed indent stays correct regardless of
how long the tag/parameter name is, so it never needs re-indenting when a name changes length:

```java
/**
 * Resolves a file.
 *
 * @param path controls whether the operation should be performed in a
 *     lightweight mode. If {@code false}, additional validation is performed.
 * @param destination specifies the destination used to resolve relative
 *     paths and determine where the resulting files should be stored.
 * @return the resolved file.
 * @throws IOException if the file cannot be resolved.
 */
```

Do not write javadoc on an overriding (`@Override`) method — rule of thumb: the comment belongs only on
the interface method or the parent class method being overridden, not duplicated on every override.

## Code style (Checkstyle)

Checkstyle runs on every `mvn package`/`install` (see Build above) via the `com.techsenger.checkstyle.config`
artifact (Sun checks-derived, `severity=error`) — a violation fails the build, not just a lint warning. Treat
every rule below as binding when writing or editing Java. `module-info.java` files are exempt from all of it.
Run just this check with `mvn checkstyle:check`, or skip it entirely with `-Dcheckstyle.plugin.skip=true`
or `-P unit-tests`/`-P integration-tests`.

- **Layout**: 120-char line limit, no tabs, no trailing whitespace, file must end with a newline, exactly
  one blank line between the license header and the `package` declaration, no more than 5000 lines/file.
- **Methods**: max 250 non-empty lines, max 8 parameters — both signal you should split the method/introduce
  a parameter object rather than push past them.
- **Imports**: no star imports, no unused imports, no redundant imports.
- **Naming**: standard Java conventions — `PascalCase` types, `camelCase` methods/fields/params/locals,
  `UPPER_SNAKE_CASE` for non-private constants (private constants are exempt, so `private static final` in
  `camelCase`/mixed case is fine).
- **Braces & blocks**: braces required on every `if`/`for`/`while`/etc. (no single-statement bodies without
  `{}`), no empty blocks, no nested blocks, standard left/right-curly placement.
- **Whitespace**: standard spacing around operators/generics/casts/parens (`GenericWhitespace`, `ParenPad`,
  `TypecastParenPad`, `WhitespaceAround`, `WhitespaceAfter`, `NoWhitespaceBefore`/`After`, `OperatorWrap`).
- **Coding**: no empty statements, `equals`/`hashCode` always overridden together, no assignments inside
  expressions (`InnerAssignment`), one variable declaration per statement (no `int a, b;`), simplify boolean
  expressions/returns (`return x == y;` not `if (x == y) return true; else return false;`).
- **Class design**: a class with only private constructors must be `final`; utility classes (only static
  members) must have a private constructor (matches the existing `private Foo() { // empty }` pattern
  already used throughout, e.g. `NavigatorFileIconProvider`); fields should be `private` with accessors
  (`VisibilityModifier`), not exposed directly.
- **Misc**: array brackets on the type, not the variable (`String[] args`, not `String args[]`); long
  literals use uppercase `L` (`100L`, not `100l`).
- **No `TODO` comments** (`TodoComment` module) — since severity is `error`, a matching comment fails the
  build. Don't add new ones; open a tracked issue or just do the work instead.