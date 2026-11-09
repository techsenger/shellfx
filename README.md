# Techsenger ShellFX

Techsenger ShellFX is a platform for building JavaFX applications, where an application is structured
as a tree of MVVM components, each of which has its own lifecycle, config, etc. The platform provides abstract
classes for creating the main types of components: window, tab, area, page, dialog, and popup.

It also includes ready-to-use implementations of containers (including a docking layout) and dialogs (including a
universal file chooser). In addition, the platform provides powerful devtools that allow you to inspect both the MVVM
component tree and the underlying JavaFX scene graph. These tools make it easy to understand how the platform works
and are invaluable during development.

ShellFX is built around two core subsystems: the dynamic main menu and the workspace. The main menu is assembled
at runtime and automatically adapts to the currently focused component. The workspace is everything below the main
menu in the `Shell` — it provides the structural foundation of the application and defines how components are
arranged and interact visually. The platform supports different types of workspace models.

`ShellFX` is built according to the KISS principle. We aimed to keep it as simple as possible — with no magic and
no overly complex solutions. For example, the platform is based on a slightly extended classic MVVM pattern and provides
components for the core parts of an application, such as windows, tabs, dialogs, and others. The main idea was to
allow developers to start working with the platform within a single day, and we believe this goal has been achieved.

ShellFX is built on top of the [PatternFX](https://github.com/techsenger/patternfx) framework.

## Table of Contents
* [Demo](#demo)
    * [Workspaces](#demo-workspaces)
    * [Windows](#demo-windows)
    * [Pages](#demo-pages)
    * [Dialogs](#demo-dialogs)
    * [DevTools](#demo-devtools)
* [Features](#features)
* [When to Use?](#when-to-use)
* [Modules](#modules)
* [Component Overview](#component-overview)
* [Core Components](#core)
    * [Shell](#core-shell)
    * [Window](#core-window)
    * [Tab](#core-tab)
    * [Page](#core-page)
    * [Dialog](#core-dialog)
    * [Popup](#core-popup)
    * [Area](#core-area)
* [Layout Components](#layout)
    * [TabHost](#layout-tab-host)
    * [DockHost](#layout-dock-host)
    * [PageHost](#layout-page-host)
    * [TreePageHost](#layout-tree-page-host)
* [Shared Components](#shared)
    * [Find](#shared-find)
    * [NavigableFind](#shared-navigable-find)
    * [FindPanel](#shared-find-panel)
* [Dialog Components](#dialog)
    * [AlertDialog](#dialog-alert)
    * [FileChooserDialog](#dialog-file-chooser)
    * [NameValueDialog](#dialog-name-value)
    * [ProgressDialog](#dialog-progress)
    * [TextDialog](#dialog-text)
    * [TextsDialog](#dialog-texts)
    * [LongTextDialog](#dialog-long-text)
    * [TextChoiceDialog](#dialog-text-choice)
* [DevTools Components](#devtools)
    * [DevToolsTabDock](#devtools-tab-dock)
    * [ComponentTab](#devtools-component-tab)
    * [NodeTab](#devtools-node-tab)
    * [EventTab](#devtools-event-tab)
    * [StylesheetTab](#devtools-stylesheet-tab)
    * [EnvironmentTab](#devtools-environment-tab)
* [Component Config](#config)
    * [Config Object](#config-object)
    * [ConfigManager](#config-manager)
* [Component Port](#port)
* [Registries](#registries)
    * [Registry Types](#registries-types)
    * [Slot Registry](#registries-slot)
    * [Control Registry](#registries-control)
    * [Component Resolution](#registries-resolution)
    * [Control Builder](#registries-control-builder)
    * [Control State](#registries-control-state)
    * [Menu Visibility](#registries-menu-visibility)
    * [Stylesheet Registry](#registries-stylesheet)
    * [Action Registry](#registries-action)
    * [Bundle Registry](#registries-bundle)
* [FileStorage](#storage)
    * [Packages](#storage-packages)
    * [Core Concepts](#storage-concepts)
    * [Two-Phase Loading](#storage-two-phase)
    * [Appearance](#storage-appearance)
    * [Custom Storage](#storage-custom)
* [Naming Convention](#naming-convention)
* [Quick Start](#quick-start)
* [Requirements](#requirements)
* [Dependencies](#dependencies)
* [Code Building](#code-building)
* [Running Demo](#running-demo)
* [License](#license)
* [Contributing](#contributing)
* [Support Us](#support-us)

## Demo <a name="demo"></a>

### Workspaces <a name="demo-workspaces"></a>
<p><img width="1200" height="785" alt="ShellFX Browser Workspace" src="https://github.com/user-attachments/assets/01cb16d6-dd51-4139-9a93-acb46b167ff9" /></p>
<p><img width="1200" height="785" alt="ShellFX IDE Workspace" src="https://github.com/user-attachments/assets/436ada94-c33d-46c2-89bb-0ff539a88c02" /></p>

### Windows <a name="demo-windows"></a>
<p><img width="1200" height="800" alt="ShellFX Windows" src="https://github.com/user-attachments/assets/f14da3cf-895f-4c58-a7dd-cab87371b893" /></p>
<p><img width="1200" height="800" alt="ShellFX Windows Tile Grid" src="https://github.com/user-attachments/assets/d85615cb-66ec-4027-ab71-9717584982ed" /></p>

### Pages <a name="demo-pages"></a>

<img width="1200" height="784" alt="ShellFX Pages" src="https://github.com/user-attachments/assets/8811707e-1e9b-415a-b0e7-124b889e240f" />

### Dialogs <a name="demo-dialogs"></a>

<img width="1200" height="785" alt="ShellFX Dialogs" src="https://github.com/user-attachments/assets/fb619502-44ad-4f23-a767-3d129368da9c" />

### DevTools <a name="demo-devtools"></a>

<p><img width="1200" height="785" alt="ShellFX DevTools Components" src="https://github.com/user-attachments/assets/d4c3c1be-02ff-4080-8d95-bbbb13abb38a" /></p>

<p><img width="1200" height="785" alt="ShellFX DevTools Nodes" src="https://github.com/user-attachments/assets/4c4e105c-9138-4318-afba-8003835e7b6c" /></p>

<p><img width="1200" height="785" alt="ShellFX DevTools Events" src="https://github.com/user-attachments/assets/613aed3a-96e3-4c2a-afa5-63e5db1aeee8" /></p>

## Features <a name="features"></a>

Key features of ShellFX include:

* Dynamically configurable menu.
* Support for different types of workspace.
* Abstract classes to simplify component development.
* A set of ready-made components that can be used out of the box.
* Support for different layouts, including a docking layout.
* Set of devtools for inspecting the application at both the component layer and the JavaFX scene graph layer.
* Ability to preserve component config.
* Support for inline popups and dialogs with two scopes — window and tab.
* Window styling that matches the theme.
* Support for 7 themes (4 dark and 3 light).
* API for working with all colors in the palettes of all themes
* Styling with CSS.

## When to Use <a name="when-to-use"></a>

ShellFX is well suited for medium to large JavaFX applications that require a structured UI architecture and
flexible workspace management.

It is particularly effective for projects that:

- Rely on a component-based MVVM architecture.
- Contain multiple tabs or require complex workspace layouts (including docking-based layouts).
- Need dynamic menus, theming support, and centralized shell-level infrastructure.
- Benefit from built-in DevTools for inspecting both the component tree and the JavaFX scene graph.

ShellFX provides a scalable foundation for applications where UI complexity grows over time and clear structural
boundaries are essential.

Typical application types include:

- Enterprise systems managing different data entities.
- Code editors and lightweight IDEs.
- Database and query tools.
- File managers and content browsers.
- Monitoring and analytics dashboards.
- Tools that require parallel workflows within multiple tabs or panels.

The tab-based approach allows users to maintain workflow context while switching between different tasks, making
complex applications more intuitive and productive.

## Modules<a name="modules"></a>

The platform consists of the following modules:

* Base — the most basic classes of ShellFX, its very foundation, without UI.
* Material — provides UI elements (menus, text areas, etc.) and supporting classes.
* Core — includes the shell itself, base classes for component development, settings, and core utility classes.
* Layout — offers abstract components for creating tabs with various layouts.
* Shared — includes components that are used by other components from different modules.
* Icons — contains module-specific stylesheets that utilize the Material Design Icons font. The fonts come from the
[FontJars](https://github.com/techsenger/fontjars) modules, and the license of a font is stored in its module. To use
custom icons instead, simply create your own stylesheets and add them to Shell.
* Storage — provides abstractions for working with file systems. The module includes a default implementation for the
local file system. Additional storage providers (for Google Drive, Dropbox, FTP, and similar) can be implemented
separately.
* Dialogs — provides ready-to-use dialogs: alert, file chooser, confirmation, text input and choice etc.
* DevTools — contains tools for exploring component tree and JavaFX scene graph.
* Test — provides shared test support, such as starting the JavaFX toolkit in headless mode and running code on the
JavaFX Application Thread. It is used by the tests of other modules and is not published.
* Demo — showcases ShellFX's core functionality, provides examples for building custom components, and
presents ready-made components.

## Component Overview <a name="component-overview"></a>

The following diagram shows the basic components, containers, and their implementations in the `core` and `layout`
modules:

```mermaid
classDiagram

class Parent {
    <<interface>>
}

class Child {
    <<interface>>
}

class PageContainer {
    <<interface>>
}

class TreePageContainer {
    <<interface>>
}

class PopupContainer {
    <<interface>>
}

class WindowContainer {
    <<interface>>
}

class TabContainer {
    <<interface>>
}

class AbstractParent
class AbstractChild

class AbstractArea
class AbstractPage
class AbstractPopup

class AbstractPageHost
class PageHost
class TreePageHost

class TabHost
class TabDock

class AbstractWindow
class AbstractDialog
class AbstractHostWindow
class DefaultShell

class AbstractTab
class AbstractHostTab

Parent <|-- Child

Child <|-- PageContainer
Child <|-- TreePageContainer
Child <|-- PopupContainer
Child <|-- TabContainer

PopupContainer <|-- WindowContainer

Parent <|.. AbstractParent

AbstractParent <|-- AbstractChild

AbstractChild <|-- AbstractArea
AbstractChild <|-- AbstractWindow
AbstractChild <|-- AbstractTab

AbstractArea <|-- AbstractPage
AbstractArea <|-- AbstractPopup
AbstractArea <|-- AbstractPageHost
AbstractArea <|-- TabHost

AbstractPageHost <|-- PageHost
AbstractPageHost <|-- TreePageHost

TabHost <|-- TabDock

AbstractWindow <|-- AbstractDialog
AbstractWindow <|-- AbstractHostWindow

AbstractHostWindow <|-- DefaultShell

AbstractTab <|-- AbstractHostTab

PageContainer <|.. PageHost
TreePageContainer <|.. TreePageHost

TabContainer <|.. TabHost

WindowContainer <|.. AbstractHostWindow
WindowContainer <|.. AbstractHostTab

%% composition: 0..N TabDock inside DockHost
DockHost "1" o-- "0..*" TabDock
AbstractArea <|-- DockHost
```

## Core Components <a name="core"></a>

These components form the architectural foundation of the platform, and all higher-level platform components are built
upon them.

ShellFX is built on top of the PatternFX platform, which supports working both with and without a component tree.
In ShellFX, all components form a tree structure, and multiple trees may exist depending on the number of `Window`s.
For this reason, all ShellFX core components inherit from the `Parent` and `Child` components provided by PatternFX.

Each component is defined by an interface accompanied by a base implementation. This approach ensures loose coupling
while still providing default implementations out of the box. It also allows developers to replace or extend the default
behavior with custom implementations when required. For instance, the platform consistently references `Shell`
through the `ShellView` interface rather than a concrete class.

When working with components, there are several important points to keep in mind:

1. The developer must control the component lifecycle. Component initialization is performed either manually or in
the `open*` or `show*` methods of `Composer`, which may delegate this logic to `create*` methods (using `create*`
methods makes it easy to replace the component being created). Component deinitialization is performed either manually
or in the `close*` or `hide*` methods of `Composer`. See [Naming Convention](#naming-convention) for details.

2. Working with components involves maintaining two hierarchies — the component tree and the JavaFX scene graph.
Therefore, any addition or removal of a component must be reflected in both structures. For example, removing a component
from the node tree without removing it from the component tree will result in a memory leak. DevTools provide the ability
to inspect and monitor both hierarchies.

3. The order of the changes of the two hierarchies matters. When a component is added, it is added to the component tree
first and its node to the node tree afterwards, so everything the node change triggers (for example, selecting a newly
added tab) already finds the component in the tree. When a component is removed, the order is the opposite: the node is
removed first and the component afterwards, so everything the node change triggers (for example, deselecting the tab)
still finds the component in the tree.

### Shell <a name="core-shell"></a>

`Shell` is the main and top-level component. It extends the `HostWindow` component and inherits its responsibilities for
managing the JavaFX `Stage` and window-level infrastructure. In addition, `Shell` defines the primary application
structure and user experience layer.

It is responsible for the following tasks:

* Dynamic menu management.
* Workspace management.
* Context management.

The Shell core does not contain any business logic. It is only a shell for other components that contain logic.

Working with the main menu of the `Shell` is carried out in two directions:

1. Configuring menu elements
2. Keeping the state of elements actual and responding to user actions, which the controls do themselves

The configuration of menu elements is performed dynamically and in any order, with the final result being unknown in
advance. This feature is crucial in cases where plugins/extensions are used, as they can be added/removed dynamically by
the user. Each plugin may introduce its own menu items and interact with existing menus. Therefore, it is impossible
to predict the final structure of the menu that the user will work with.

The implementation is built on two registries (see [Registries](#registries)). The
`SlotRegistry` holds the structure of the menu: the menu bar, its menus and the groups of menu items are slots, put
into each other at positions. The `ControlRegistry` holds the factories that create the controls: the menu bar, the
menus, the groups and the menu items. Both registries can be changed and unregistered from at any time, so a plugin
can add its own menu or put its items into an existing one. When the menu needs to be updated, `Shell` has
`ControlBuilder` read both registries and build a new menu bar from them. How the built controls behave at runtime is
described in [Control State](#registries-control-state) and [Menu Visibility](#registries-menu-visibility).

A menu consists of groups separated by separators. Items are added to groups, and empty groups are ignored. Each menu
and group is identified by its slot. The controls of the menu keep their own state and react to their own actions (see
[Control State](#registries-control-state)). Each of them decides for itself what to depend on; a control that wants to
follow a component the user is working with finds that component itself (see below) and observes it. A control belongs
to the view side, so it can work with the view of the component as well as with its port.

The `Shell` tracks the focused component. It follows `Scene#focusOwnerProperty()`, identifies the component that owns
the focused node, and stores the result in `ShellView.Composer#focusedProperty()`.

`Shell` does not decide which component the menu is about, because the application knows it better. A control that
depends on a component finds the component itself: it can be the focused component, found with `focusedProperty()`, the
selected tab of the workspace or anything else. Focus is not always the right answer: when the user clicks the main
menu, the focus moves away from the component the user was working with. The demo uses the focus. The control observes
the component it found (its view or its port), so its state stays actual whether the menu is open or an accelerator is
pressed.

To gain a complete understanding of working with the menu, it is recommended to experiment with the menu in the demo
and pay attention to log messages at the debug level.

The second key part of ShellFX is the workspace, which represents one of the available layouts. ShellFX supports
different types of workspace:

1. Browser-like. This workspace is a `TabHost` with slanted tabs (see `TabHost`). Additionally, the tabs added to
this `TabHost` can contain a docking layout created with the `DockHost` component.
2. IDE-like. This workspace is a straightforward docking layout created with the `DockHost` component.

### Window <a name="core-window"></a>

`Window` is one of the core components of the platform and is available in two variants: `WindowType#NESTED` and
`WindowType#TOP_LEVEL`.

`NESTED` windows are internal windows managed by `WindowManager`. `WindowManager` allows an unlimited number of
windows to be opened simultaneously, tracks the active window, manages window state, and provides various window
arrangement operations such as cascade, tile, and others.

The platform provides two implementations of window hosts: `HostWindow` and `HostTab`. This allows nested windows
to be displayed either inside another window or inside a tab. The latter approach is particularly useful for
tab-based applications, where each tab can maintain its own set of dialogs and auxiliary windows, similar to
how modern web browsers isolate dialogs and popups per tab.

`TOP_LEVEL` windows are created in a separate `Stage` and are integrated with the operating system's windowing environment.

Despite their different implementations, both `NESTED` and `TOP_LEVEL` windows are accessed through the same API.
As a result, components built on top of `Window` (such as dialogs, wizards, or utility windows) can be displayed
either inside the application or in separate system windows without any changes to application code. This allows
window-based components to be implemented once and reused with any window type.

A window is created invisible. The client fills the window in and makes it visible with `setVisible(true)` of the
window `Port`, so the window appears with its final content, not an empty one that is filled in afterwards. A window
that becomes visible gets the focus; `requestFocus()` of the `Port` brings the focus back to the window later. Making a
window invisible is not closing it: an invisible window stays in the component tree and has to be closed to be removed.
What happens when a window becomes visible depends on its type.

A `NESTED` window passes two independent phases. First the `Composer` adds it to the window manager, which puts its
node into the container; the window is a part of the component tree but is invisible. Then it is made visible or
invisible with `setVisible`. A window that is not visible takes no part in z-order, focus and modality: a modal window
blocks its container only while it is visible. Removing a visible window ends both phases at once, so it does not have
to be made invisible first. When the window becomes visible, the window manager gives it the focus.

A `TOP_LEVEL` window gets its `Stage` from the `Composer`, and the stage is not visible yet. `setVisible` is `show()`
and `hide()` of the stage. The stage is sized when it becomes visible, so the content has to be there before. A
minimized window is still visible. The platform gives the focus to the window when it becomes visible.

Every `TOP_LEVEL` window has one scene, and the scene has one map of the accelerators, a key combination for each
action, shared by all the content of the window, including its `NESTED` windows. A menu item with an accelerator puts
itself into this map. This works while there is one item per key. When a component is opened in several instances, for
example in several tabs, or the same action has items in several menus, their items have the same accelerators and
overwrite each other: the item that is fired may belong to an instance the user is not working with.
`AcceleratorManager` solves it. It installs one entry per key, taken from the items that matter now, and puts into the
map and removes from it only its own entries. By default a window has no `AcceleratorManager`, so it has to be added
manually when it is needed. Whoever adds it creates it for the scene of the window, with a supplier of an iterator over
the components to ask, in the order of their priority, and calls `update` whenever those components or the accelerators
of the items may have changed. A component with items that have accelerators implements `AcceleratorAware`, and the
manager asks the components of the iterator for their items in one pass. The manager recognizes its entries by the
`Runnable` of its own kind and puts them back if JavaFX replaces or removes them, for example when an item gets into the
scene or leaves it, so the items can be installed by JavaFX as usual.

### Tab <a name="core-tab"></a>

`Tab` is an abstract component used for creating custom tab implementations. In ShellFX, `Tab` is one of the central
platform components, since the primary application functionality is delivered through tabs.

`Tab` can be added to any component that implements the `TabContainer` interface. The platform
provides two components that implement this interface: `TabHost` and `TabDock`, where `TabDock` extends `TabHost`.

### Page <a name="core-page"></a>

`Page` is a component that represents a titled, selectable element. A key feature of this component is its lazy
initialization. For example, if a container displays one of N `Page`s, only the `Page` that the user actually chooses to
view will be initialized.

`Page` can be added to any component that implements the `PageContainer` interface. The default implementation of
this interface is `PageHost`.

### Dialog <a name="core-dialog"></a>

`Dialog` inherits `Window` and is a specialized component designed for user interaction and result acquisition.
Since `Dialog` inherits `Window`, it can be displayed in any environment that supports windows and uses the same
API as regular windows.

All dialogs in ShellFX are asynchronous. Opening a dialog does not block the application's execution flow or freeze
the user interface. Instead, user responses are delivered through callbacks, events, observable properties, or
other asynchronous mechanisms. This approach keeps the UI responsive and simplifies background processing.

Like any window, a dialog is created invisible: the client sets its content and calls `setVisible(true)`. A nested
dialog is centered once, when it is shown for the first time; if it is hidden and shown again, it stays where it is.

Dialogs can be displayed either as `NESTED` or `TOP_LEVEL` windows. Nested dialogs are rendered within a
`WindowContainer` and appear as an integral part of the application's user interface. Top-level dialogs are displayed
in a separate `Stage` and are integrated with the operating system's windowing environment.

Nested dialogs can be displayed in any implementation of `WindowContainer`. The platform provides two implementations:
`HostWindow` and `HostTab`. This allows dialogs to be associated either with a window or with a specific tab.
For example, in a browser-like application, each tab can maintain its own set of dialogs and auxiliary windows,
isolated from all other tabs.

Dialogs fall into two kinds, depending on who owns the behavior behind them.

A *dumb* dialog only shows data and collects input. It does not know what the result is used for: the code that
opens it (the client) validates the input, performs the action and decides when the dialog closes. `ProgressDialog` is
a dumb dialog: it displays the progress of an operation, while the operation itself lives elsewhere and outlives
the window. `AlertDialog`, `NameValueDialog` and the text dialogs (`TextDialog`, `TextsDialog`, `LongTextDialog`,
`TextChoiceDialog`) are dumb too. A dumb dialog intentionally does not know how its result is used, and that is what
makes it reusable.

A *smart* dialog owns a complete task. It has its own domain logic and returns a ready result, so the client does not
repeat that logic. `FileChooserDialog` is a smart dialog: given a `FileStorage`, it browses the directories itself
and returns the chosen file.

Dumb dialogs are more universal. They need almost nothing from their surroundings, so they can be opened from any
place and reused by any process; this is why one error or progress dialog can serve many different operations. A smart
dialog is bound to the context its logic needs (a storage, a file), but any client gets the behavior for free.

The price of a dumb dialog is that every client implements the logic around it, and it is easy to end up with
several copies of the same validation. This is solved by moving the shared logic into a helper that clients call, not
necessarily by making the dialog smart. A smart dialog fits a single, quick action in one window. It does not fit a
long process that goes through several dialogs: the process must outlive each window, and a dialog that owns it would
have to stay open or open its own successors. Such a process is better owned by a separate object that opens dumb
dialogs as its steps.

### Popup <a name="core-popup"></a>

All `Popup`s in ShellFX are inline and have a scope that affects what will be blocked when the `Popup` is open.

Inline `Popup`s are components that appear embedded within the current application window, typically overlaid on top
of the existing content. They are contextually tied to a specific section (e.g., a `Shell` or `Tab`) and do not
create a separate OS-level window. In contrast, modal window `Popup`s (or native `Popup`s) open as standalone
OS-managed windows with their own frames and system controls, completely independent of the parent UI.

There are two types of scope: `Window` and `Tab`. `Popup`s in the `Tab` scope are bound to a specific tab and are visible
only while that tab is open. `Popup`s in the `Window` scope are global to the `Window` and remain visible even when all
tabs are closed.

`Popup` can be added to any component that implements the `PopupContainer` interface. The platform provides two
components that implement this interface: `Tab` and `Window`.

Like a window, a popup is created invisible: the client fills it in and calls `setVisible(true)` of its `Port`. An
invisible popup is a part of the component tree, but takes no part in z-order and modality: a modal popup blocks its
container only while it is visible. Making a popup invisible is not closing it: it stays in the component tree and has
to be closed to be removed.

However, a popup differs from a window in one respect: it never takes the focus automatically, because notifications
must not interrupt the user. The focus moves to a popup only when the client calls `requestFocus()` or the user clicks
it.

### Area <a name="core-area"></a>

`Area` is an abstract base component that represents a rectangular region. Naturally, `AreaView#getNode()` returns a
`Region`.

## Layout Components <a name="layout"></a>

Layout components are responsible for arranging `Tab`, `Page`, and, in some cases, `Area` components and their derivatives.

### TabHost <a name="layout-tab-host"></a>

`TabHost` is the primary component that can contain `Tab` components; therefore, it implements the `TabContainer` interface.
This component provides all the necessary APIs for working with tabs — adding, selecting, removing, transferring
tab ports, and more.

The tab pane of the host is created by a factory passed to the second constructor, so the look of the tabs is chosen
by the caller. `new TabHostView<>(viewModel, SlantedTabPanePro::new)` gives the host slanted tabs with a more
saturated tab header; this is the workspace of browser-like applications, where the application shell consists of a
main menu and this single component. `SlantedTabPanePro` is a `TabPanePro` of the `material` module; its shape is set
only by CSS (`slanted-tab-pane-pro.css`): the `-sfx-slant-left`, `-sfx-slant-right`, `-sfx-top-offset`, `-sfx-padding`
and `-sfx-tab-gap` properties. The size of a tab is the same as of a regular one: it is chosen with the style classes
`size-l`, `size-m` (the default) and `size-s` of the tab pane, and all the values are in `em`, so they follow the
font size.

### DockHost <a name="layout-dock-host"></a>

`DockHost` is the main component of the docking layout and one of the most complex components in the platform.
Before describing how it works, let’s examine its child components.

`TabDock` extends `TabHost`, meaning it can contain tabs. In addition, it introduces docking-specific functionality
such as dragging an entire `TabDock` from one layout position to another, collapsing it into a `SideBar`, and
similar behaviors.

`SideBar` is a component that displays collapsed `TabDock` instances. It is important to note that a `SideBar` can
be shown even when it contains no collapsed `TabDock` components, using `SideBarPolicy`. This is useful when the
`SideBar` is intended to host additional UI elements besides collapsed `TabDock`s.

When a `TabDock` is minimized to a `SideBar`, any of its minimized tabs can be previewed in a `TabPopup` component,
which allows its width to be resized.

In addition to defining the layout structure, a `DockHost` can have a main component. The main component is intended
to host the primary application content, while `TabDock`s typically contain auxiliary tools and supporting
components. The main component can be any `Area`-based component and is specified in the model using
`ModelNodeBuilder#mainArea(...)`. The defining characteristic of the main component is that it remains part of the
docking layout for the entire lifetime of the `DockHost` and cannot be minimized to a `SideBar`. When a main
component is present, it is used as the reference for determining the target `SideBar` when minimizing a `TabDock`.

If no main component is defined, `DockHost` falls back to determining the target side based on the position of the
`TabDock` within the docking layout.

Now that the components are introduced, let’s outline how everything works together. `DockHost` provides two
complementary APIs for working with docking layouts.

The first is the whole-tree API, which is intended for complete layout construction, restoration, and serialization.
A docking layout is described as a `ModelNode` tree. Each node represents either a group or a leaf component. Group
nodes (`GroupNode`) define the layout orientation (`HORIZONTAL` or `VERTICAL`) and their children, while leaf nodes
(`AreaNode`) contain `Area`-based components displayed in the workspace. An immutable model tree is created using
`ModelNodeBuilder` and applied to `DockHost` using `Composer#applyModel(GroupNode)`. The current layout can be
captured as such an immutable tree using `Composer#captureModel()`. This approach is recommended when initializing a
workspace, restoring a previously saved layout, or persisting the current layout state.

The second is the partial-tree API, which is intended for incremental runtime modifications. Instead of rebuilding
the entire layout, it performs targeted operations relative to an existing anchor component. This API is used for
operations such as adding a new area next to an existing area, replacing a component, removing a component, or
performing docking operations initiated by the user. Anchors are addressed the same way as in the whole-tree API —
via `ModelNode` — but obtained live from the current layout using `Composer#getModelNode(AreaView)` rather than
built by hand. A node obtained this way is not a snapshot: navigating it via `ModelNode#getParent()` or
`GroupNode#getChildren()` always reflects the layout's actual current state, which lets an anchor be resolved to any
ancestor group regardless of nesting depth.

### PageHost <a name="layout-page-host"></a>

`PageHost` is a simple component that displays `Page` components and performs their lazy initialization.
It can be used to display navigable pages with a flat menu-like structure in diffent components - tabs, dialogs etc.

### TreePageHost <a name="layout-tree-page-host"></a>

`TreePageHost` is almost identical to `PageHost`, except for the menu structure: `PageHost` uses a flat menu
(`ListView`), whereas `TreePageHost` uses a hierarchical menu (`TreeView`).

## Shared Components <a name="shared"></a>

Shared components are auxiliary components built on top of Core components and used by components from other modules.

### Find <a name="shared-find"></a>
`Find` is an abstract base search component that contains the entire search view implementation, including both
submit search and instant search functionality. Since child components may be of different types (toolbar, panel, etc.),
this component includes only minimal CSS styling. It owns the full logic of a generic search UI — which trigger
mode is in effect (submit vs. instant), debounce timing for both search and adding the find text to the earlier
find texts, and match-count display formatting (including an optional, domain-agnostic
`FindResult` contract a component can implement over its own result type to get that formatting for free). The
only thing it does not know is what a match actually is or how to find one; that stays entirely up to the concrete
component: `onFind()` returns a `CompletableFuture` with the outcome, so a component can either run its own search
synchronously or delegate to something external and report back asynchronously — either way, `Find` clears the
displayed result before a new search starts and discards a stale outcome on its own if a newer search has since
superseded it, so no implementation has to worry about that itself.

### NavigableFind <a name="shared-navigable-find"></a>
`NavigableFind` extends `Find` with the ability to move between individual matches one at a time. It adds
previous/next buttons, disabling them when there is nothing to navigate to, and can display the current match
position alongside the total count (e.g. `1 / 10`) instead of a total-only count. It works with
`NavigableFindResult`, a `FindResult` that also knows how to move to its next/previous match.

### FindPanel <a name="shared-find-panel"></a>
`FindPanel` is an abstract class for find panels that are placed at the bottom of other components, built on top
of `NavigableFind`. Besides the generic search orchestration described above, it also adds whole word, regular
expression, and highlight-all toggles, plus a close button — none of which `Find`/`NavigableFind` know about.

## Dialog Components <a name="dialog"></a>

In this section, the dialogs from the `dialogs` module are described. This module contains implementations of the most
commonly used dialogs.

### AlertDialog <a name="dialog-alert"></a>

`AlertDialog` is a dialog for common user notification scenarios such as informational messages, warnings, errors,
and confirmation requests.

### FileChooserDialog <a name="dialog-file-chooser"></a>

`FileChooserDialog` is a dialog for selecting a file when opening or saving. The dialog type is defined using the
`FileChooserType` enumeration. It is a smart dialog (see [Dialog](#core-dialog)).

It is important to note that this dialog works with files provided by classes from the `storage` module. This makes
it possible to use the dialog with virtually any file storage implementation, provided that an appropriate `FileStorage`
implementation is supplied.

### NameValueDialog <a name="dialog-name-value"></a>

`NameValueDialog` is a simple dialog for displaying name–value pairs. The parameter name is shown in a `TextField`,
while the value is displayed in a `TextArea`.

### ProgressDialog <a name="dialog-progress"></a>

`ProgressDialog` is a dialog for reporting the progress of a long-running operation, with an optional message and
an optional step counter (e.g. `3 / 10`) shown alongside the progress bar. It is a dumb dialog (see
[Dialog](#core-dialog)).

### TextDialog <a name="dialog-text"></a>

`TextDialog` is a dialog for entering or viewing a single line of text in a `TextField`, with a label next to it
(for example, a name of a new file). It is a dumb dialog (see [Dialog](#core-dialog)): the client validates the text
and decides when the dialog closes.

### TextsDialog <a name="dialog-texts"></a>

`TextsDialog` is a dialog for entering a single line of text that the user can either type or pick from a list of
suggested texts (for example, earlier entries), shown in an editable `ComboBox`. The texts belong to the client and
can be changed while the dialog is open. It is a dumb dialog (see [Dialog](#core-dialog)).

The `editable` flag of `TextsDialog` only makes the editor read-only, so the dialog still looks like a text field with
an arrow. A combobox without an input field looks different, which is why choosing without typing is a separate
dialog, `TextChoiceDialog`.

### LongTextDialog <a name="dialog-long-text"></a>

`LongTextDialog` is a dialog for entering or viewing a multiline text in a `TextArea`, with a label next to it. It is
a dumb dialog (see [Dialog](#core-dialog)). Together with `TextDialog` it forms a pair that differs only in the
length of the text.

### TextChoiceDialog <a name="dialog-text-choice"></a>

`TextChoiceDialog` is a dialog for choosing one of the given texts in a non-editable `ComboBox`; nothing can be typed.
The texts belong to the client and can be changed while the dialog is open. It is a dumb dialog (see
[Dialog](#core-dialog)).

## DevTools Components <a name="devtools"></a>

DevTools components are tools for inspecting and analyzing the application at two levels: the component tree and
the JavaFX scene graph. They are primarily intended for developers building components on top of the platform.

### DevToolsTabDock <a name="devtools-tab-dock"></a>

This component is a container for `Tab` components and provides shared tab management mechanisms. It can be added to
any layout, whether a simple layout or a docking layout.

### ComponentTab <a name="devtools-component-tab"></a>

This component allows exploring the tree of active components and inspecting their properties. In addition, it
provides information about the class hierarchy of the selected component.

### NodeTab <a name="devtools-node-tab"></a>

NodeTab is an interactive inspector for the JavaFX scene graph. It allows developers to explore the node hierarchy,
inspect JavaFX properties, and modify supported property values at runtime without restarting the application.
The component also enables opening reference documentation (Javadoc) for both classes and their properties.

When working with JavaFX properties, the right panel provides the ability to inspect property values and edit them
using dedicated dialogs, which can be opened by right-clicking a `Property` in the panel.

`ViewDialog` displays the full value of a property. This is particularly useful when the value is too long to fit
within the property table.

`TextEditorDialog`, `EnumEditorDialog`, and `InsetsEditorDialog` allow editing different types of properties.
`TextEditorDialog` supports properties whose values can be represented as text (such as `Number`, `Boolean`, `String`,
and similar types), `EnumEditorDialog` is intended for properties backed by enumerations, and `InsetsEditorDialog`
is used for properties of type `Insets`.

Being able to modify property values without restarting the application greatly simplifies debugging and makes it
much easier to experiment with and determine the appropriate values for JavaFX node properties.

### EventTab <a name="devtools-event-tab"></a>

This component allows recording node events. It can operate with or without filters. Events can be filtered by
selected component, message, event type, and other criteria.

### StylesheetTab <a name="devtools-stylesheet-tab"></a>

This component allows inspecting which stylesheets are applied to nodes within the scene.

### EnvironmentTab <a name="devtools-environment-tab"></a>

This component provides access to platform settings, system properties, and environment variables.

## Component Config <a name="config"></a>

A component often has state that is worth keeping: the size of a dialog, the layout of the columns of a table,
the recent queries of a search field. ShellFX keeps such state in a config. A config is stored outside the component,
so it serves two purposes:

* Saving between sessions. The config is written to a file when the application stops and read again at the next
start, so the component comes back the way the user left it.
* Synchronization between components. Components of the same kind may share one config, and a change made by one of
them is seen by the others. For example, two search fields that share a config show the same list of recent
queries: a query submitted in one field appears in the drop-down list of the other one immediately.

A config is optional. A component that has nothing to keep, or whose state must not be shared, simply works without it.

### Config Object <a name="config-object"></a>

A config is a plain serializable object that extends `AbstractConfig` and contains no logic, only fields with
getters and setters. Configs follow the same naming as the rest of the component (`FileChooserDialogConfig`,
`DockHostConfig`). A config may contain other configs: for example, `DockHostConfig` holds the configs of its side
bars, and the parent component passes the corresponding part to each child.

The config is passed to the component as the first argument of its `Params`. It is either required (the `Params`
checks it in `validate()`) or optional (the caller passes `null` and the component just does not keep its state).
The default values are set in the constructor of the config, so a new config already describes a sensible
component. Configs are read from a file with Java serialization, which does not call constructors, therefore keep
in mind that a field added after a file was written has the Java zero value when that file is read, and that every
class of a config must declare its own `serialVersionUID`.

The base `ViewModel` classes of the three main components — `Window`, `Tab` and `Area` — hold the config and give
a `ViewModel` two hooks that are called from `postInitialize()`, only if the config is set:

* `loadStateFromConfig()` copies the values of the config into the state of the `ViewModel`.
* `observeStateForConfig()` adds listeners to the state of the `ViewModel` that write every change into the config
and call `notifyListeners()`.

The second hook is what makes a config different from history. History is a snapshot: it is filled with the state of
a component when the component is closed, and it belongs to that one component. A config is updated all the time while
the component works, because it is changed together with the properties of the `ViewModel`. This is why a config can
be shared: other components can listen to it with a `ConfigListener` and update themselves when it changes. A listener
is told that the config has changed and may get a hint saying what exactly happened, so it can react only to the
changes it is interested in. A component that registers a listener must remove it when it is deinitialized, because a
config lives as long as the application.

### ConfigManager <a name="config-manager"></a>

`ConfigManager` owns the configs of the application. A config is stored either by its class, and then it is shared
by all components of that kind, or by the UUID of a component instance, and then it belongs to that instance only.
For each of the two ways the manager can get a config, create it with a factory if it does not exist yet, put a new
one and remove it.

A manager keeps the configs in memory. `DefaultConfigManager` is the only implementation: it is given a `ConfigData`
and works with it as its live state, so whoever creates the manager chooses where the data comes from and where it is
saved to. Before saving, that one calls `warnAboutListeners()`, because by that time the components are gone and a
listener that is still registered on a config is a leak.

The manager is available to components through `ShellContextAccess`, and the code that creates a component takes
the config from it and passes it to the `Params`.

## Component Port <a name="port"></a>

Every component has a port. A port is a safe interface for working with a component: other components and application
code interact with the component only through it and never get its `ViewModel` directly (see
[PatternFX](https://github.com/techsenger/patternfx#templates-component-port)).

The default port of a component (`AlertDialogPort`, `TabPort`, `WindowPort`, etc.) gives full access to the
component, both for reading and for writing. Properties that are decided by the `View` or by the platform, such as the
size of a window or the selected state of a tab, are exposed as read-only properties with a request setter where
changing them makes sense. A few operations that only a container should perform, such as selecting a tab within its
container, are kept in a separate container-only port (`ContainerTabPort`) that is not part of the client API.

If a user needs to restrict access, they can create their own port that exposes only what is needed and make the
component implement it. This is intentional: it is impossible to foresee how restricted a port a user will need. One
user needs only a title, another needs everything except closing.

The platform does not provide a read-only port for each component, because:

* Read-only ports provided by the platform for each component would not solve the problem anyway: they can only hide
all setters at once, and it is impossible to hide a particular getter or read-only property from them.
* The need for a port that gives full read-only access is very rare in practice.
* Read-only ports double the number of types and bloat the code, since every property would have to be declared twice —
as a read-only property in one port and as a writable one in another.

## Registries <a name="registries"></a>

A registry provides a runtime mechanism for components, plugins, and modules to contribute functionality
without creating direct compile-time dependencies between them. Extensions can be registered and removed at any time,
and registrations do not depend on the order in which components or plugins are loaded.

Shell provides five registries: `SlotRegistry` and `ControlRegistry` build the menus and tool bars,
`StylesheetRegistry` holds the stylesheets that are applied to the whole application, `ActionRegistry` holds the
actions a user can do, with the hotkeys the user can reassign, and `BundleRegistry` holds the bundles of texts.

Registries are not tied to a particular layer and can be used by both views and view models. In the shell, the slot,
control, stylesheet and action registries are used only on the view side, so they are exposed only through
`ShellContext`, which is available to views. `BundleRegistry` is needed on both sides, so it is exposed through
`ShellContextAccess`, the part of the context that view models see. `ShellContext` extends `ShellContextAccess`, like
a composer extends its `ComposerAccess`, and `DefaultShellContext` implements it, so an application creates a single
context object while a view model is given only the part intended for it (`getContextAccess()` of the port).

### Registry Types <a name="registries-types"></a>

Every registry implements `Registry`. The interface promises nothing about threads: whether a registry is synchronized
is stated by the registry itself (`ActionRegistry` can be changed from any thread), and listeners are called in the
thread that changed it. Registries differ in what a listener is told about a change, and there are two types:

- `StateRegistry` tells only that something has changed. `getStateUuid()` returns an identifier of the current state
  that is replaced on every change, and a `StateRegistryListener` gets the new identifier in `stateChanged(UUID)`. The
  listener reads the registry again and compares the identifier with the one it saw before: if the registry has a
  different one by the time the listener reads it, the state has changed again and must be re-read. `SlotRegistry`
  and `ControlRegistry` are state registries, because they hold the structure of a tree that is rebuilt as a whole
  and have no items that could be listed.
- `ItemRegistry<T>` tells what has changed. An `ItemRegistryListener<T>` gets the added and the removed items in
  `changed(List<T> added, List<T> removed)`; either list can be empty but never `null`, and both are unmodifiable.
  `T` is the domain item, such as `Stylesheet` for `StylesheetRegistry` or `Action` for `ActionRegistry`.

The type is a matter of how a change is reported, not of how often the registry changes: all registries can change at
any time. A listener of an item registry that applies the changes one by one depends on the order of the
notifications, which is not guaranteed between threads; a listener that only re-reads the registry does not.

### Slot Registry <a name="registries-slot"></a>

A slot is an extension point in a component tree. It represents a place where components and plugins can contribute
content, such as a menu bar, menu, menu group, or tool bar group.

The type of a slot defines what it can contain: `MenuBarSlot`, `MenuSlot`, `ContextMenuSlot`, `ToolBarSlot`, or
`GroupSlot<V, C>`. A slot is declared once, typically as a constant in a public catalog owned by the module that
defines the extension point. The slot itself does not know which contributions will be added to it.

`SlotRegistry` stores the structure of the extension tree: which slot belongs to which parent slot and at what
position. A plugin can declare its own slots and insert them into existing slots. Another plugin can then contribute
to those slots without knowing anything about the plugin that declared them.

The slot registry contains only the structure of the tree. It does not create or store controls. Menu items, buttons,
and other controls are contributed through the [Control Registry](#registries-control).

**Types.** Generic parameters make invalid slot relationships compile-time errors:

- `V` is the view type of the component the slot belongs to, for example `ShellView<?>`. Two slots used in the same
  registration must have compatible `V` types. `V` also determines the type of component instance passed to a control
  provider. When a control is created, its provider receives the component instance into which the control may be
  placed.
- `C` is the control type accepted by a `GroupSlot<V, C>`. A provider registered for the group in the
  `ControlRegistry` must create a compatible control.

Only compatible slots can be connected. A menu bar contains menus. A menu or context menu contains groups of menu
items (`GroupSlot<V, ? extends MenuItem>`). A tool bar contains groups of controls (`GroupSlot<V, ? extends Control>`).
A group of menu items can also contain nested menus.

### Control Registry <a name="registries-control"></a>

`ControlRegistry` stores factories of `ControlProvider`s for registered slots. A provider creates and owns the
control of one slot for one component view: the control is passed to the constructor or created in `initialize(view)`
(`setControl`), `initialize` hooks it onto the view, `getControl()` returns it, `getSlot()` tells which slot it was
registered for and `deinitialize(view)` unhooks it. Both can be called only once and throw an
`IllegalStateException` otherwise, so an override calls `super` first (the check is not enforced: an override that
forgets `super` is simply not checked). A provider keeps its control, so a new provider is created for every build,
which is why a `ControlProviderFactory` is registered (`BackButtonProvider::new` or
`() -> new SimpleControlProvider<>(control) {...}`) and not the provider itself. `ControlProvider` is an interface;
`SimpleControlProvider` is its base class that keeps the control and the slot and guards `initialize` and
`deinitialize`.

A provider can be registered in three ways:

- **For a control slot.** A menu bar, menu, context menu, or tool bar is itself a control and can have one provider.
  A control is passed to the constructor of the provider; one that needs nothing to be hooked onto the view needs no
  more:

  ```java
  register(ShellSlots.MAIN_MENU, () -> new SimpleControlProvider<>(new MenuBar()));
  ```

  Otherwise `initialize` hooks it onto the view with `getControl()`. A control whose creation depends on the view can
  be created in `initialize` and given to `setControl` instead.

- **For a group.** A group is a `ControlGroup<C>` and has one provider too (`SimpleGroupProvider` gives a plain
  group), so a custom provider can, for example, create a group that tracks the number of its items. A group without
  a provider is left out by the builder.

  ```java
  register(ShellSlots.FileMenu.DEMO_GROUP, SimpleGroupProvider::new);
  ```

- **For a group item.** A provider can be registered at a specific position within a `GroupSlot<V, C>`. It creates a
  control belonging to that group, such as a menu item. The generic type of the group ensures that the provider
  creates a compatible control.

  ```java
  register(ShellSlots.FileMenu.DEMO_GROUP, 100, () -> new SimpleControlProvider<>(new MenuItem("Main Tab")) {
      @Override
      public void initialize(ShellView<?> view) {
          super.initialize(view);
          getControl().setOnAction(new MainTabItemHandler(shell));
      }
  });
  ```

  A control that hooks itself onto long-living state unhooks in `deinitialize`, which the owner of the controls
  calls when it is done with them:

  ```java
  register(ShellSlots.FileMenu.DEMO_GROUP, 200, () -> new SimpleControlProvider<>(new MenuItem()) {

      @Override
      public void initialize(ShellView<?> view) {
          super.initialize(view);
          getControl().textProperty().bind(view.getViewModel().titleProperty());
          getControl().setOnAction(new TitleItemHandler(shell));
      }

      @Override
      public void deinitialize(ShellView<?> view) {
          super.deinitialize(view);
          getControl().textProperty().unbind();
      }
  });
  ```

  The type of the control comes from the slot; to get a narrower one, for example a `Button`, name the type
  arguments: `new SimpleControlProvider<ToolBarView, Button>() {...}`.

The builder fills the `ControlGroup` created by the group's provider with the controls of the group and decides how
the group is laid out.

Registering a factory does not invoke it. Factories are called only when a builder creates the corresponding
controls.

### Component Resolution <a name="registries-resolution"></a>

Registrations in `SlotRegistry` and `ControlRegistry` are associated with the component class of the slot. When
resolving registrations for an actual component, the registry considers its class, superclasses, and interfaces.
Therefore, a registration made for a base view type such as `TabHostView` also applies to its subtypes.

The resolved result is cached by `Class#getName()` rather than by the `Class` object itself. This avoids retaining the
`ClassLoader` of an unloaded plugin through the cache. The cache is invalidated whenever the registrations change.
`StylesheetRegistry` is not tied to a component class and does not resolve anything.

### Control Builder <a name="registries-control-builder"></a>

Neither registry creates the final control hierarchy. `ControlBuilder` combines the two registries: it resolves the
slots and providers for a component, walks the tree from a root slot, creates and initializes the providers, takes
their controls, and orders them by their registered positions.

A build has two passes. The first plans what is going to be built, so a menu or a group that would be left out is
never created; the second creates the providers in the order of the tree, initializes them and takes their controls.
Every provider in the result therefore has its control in the built controls. The builder takes the view first and the
slot second, and returns `Controls<V, R>`: the root control and the providers of all the controls created for it,
from the root down, already initialized like any other created component. The owner of the root calls
`Controls#deinitializeAll(view)` when it no longer needs the controls (for example, before it builds the menu bar
again); the providers are deinitialized from the last to the first. A provider is a one-shot object: it is
initialized once by the builder and deinitialized once by the owner of the controls, and neither can be repeated
(the second call of `deinitialize` of a provider throws an `IllegalStateException`). A provider and its control are
never reused; a new provider is created for every build.

Each build reports itself in two trees. The built tree is logged at debug level. The defects of the registrations - a
group or a menu without a provider, controls of a group put nowhere, controls at the same position - are logged at
warning level, with only the places that lead to them, followed by the paths to the warnings of the built tree.

`ControlBuilder` builds both menus and tool bars. A menu bar slot produces a `MenuBar` containing its menus; a menu
produces its groups and nested menus; groups are separated by separators; and empty menus and groups are omitted.
See [Menu Visibility](#registries-menu-visibility). A tool bar slot produces the `ToolBar` created by its provider,
filled with the controls of its groups in the order of their positions; groups are separated by separators and
empty groups are omitted.

### Control State <a name="registries-control-state"></a>

The controls are regular JavaFX controls such as `Menu`, `MenuItem`, `CheckMenuItem`, and `RadioMenuItem`. A control is
created and owned by its provider, so it can keep its state actual itself instead of being updated from the outside. A
control observes whatever it depends on - the view model of the shell, the view or the port of a component - and keeps
its `visible` and `disable` properties actual at all times. For this a provider binds the properties in `initialize` and
always unbinds them in `deinitialize`:

```java
register(ShellSlots.ExtraMenu.FOO_GROUP, 100, () -> new SimpleControlProvider<>(new MenuItem("_Foo")) {

    @Override
    public void initialize(ShellView<?> view) {
        super.initialize(view);
        getControl().disableProperty().bind(observeFooPort(view).flatMap(FooPort::fooDisabledProperty));
        getControl().setOnAction(e -> System.out.println("Foo"));
    }

    @Override
    public void deinitialize(ShellView<?> view) {
        super.deinitialize(view);
        getControl().disableProperty().unbind();
    }
});
```

Here `observeFooPort` is a method of the application that returns an observable value of the `FooPort` of the component
the control depends on, for example the one that has focus; it is `null` when there is none. The control uses `flatMap`
because that component changes while the user works: `flatMap` follows the property of the *current* port and picks up
the property of a new one by itself, so the control neither listens to the port nor rebinds. A plain `map` is not
enough, because the function returns another observable, not a value.

Since the state is always actual, nothing has to be refreshed when a menu is shown or an accelerator is pressed: a
disabled item reacts neither to a mouse click nor to its accelerator.

### Menu Visibility <a name="registries-menu-visibility"></a>

A menu decides its own visibility: that is the logic of whoever provides it, and the platform does not interfere.
A menu assembled from contributions of independent plugins, however, cannot know whether any of its items will be
visible, so the `material` module has ready-made classes for the common case:

* `DynamicMenu` is visible only while at least one of its items is visible, and it hides the separators around the
  groups that have nothing to show. Its `visible` property is bound and is kept actual all the time, not only when the
  menu is shown, so a top-level menu appears in or disappears from the menu bar as soon as its items change. Because
  the property is bound it cannot be set; use a plain `Menu` for a menu that decides its own visibility.
* `DynamicContextMenu` is not shown when none of its items is visible, and it hides the separators around the groups
  that have nothing to show. A context menu has no `visible` property and is not on screen until it is opened, so
  this is checked when it is about to be shown.
* When the visibility of a dynamic menu depends on something else as well, a condition is added with
  `addVisibleCondition(ObservableValue<Boolean>)` and removed with `removeVisibleCondition(...)`; the menu is visible
  while it has a visible item and all its conditions are true, and a `null` value of a condition counts as `false`.
  A condition can be anything that observes a boolean: a property, a `Bindings` expression or the result of `map`,
  `flatMap`, `orElse` and `when`. The provider of the menu creates the condition in `initialize` and removes it in
  `deinitialize`, because the provider owns it; the menu listens to it weakly. Every call of `map` and `flatMap`
  creates a new object, so the provider keeps the condition in a field to remove the very same one. The lazy values
  created by `map` and `flatMap` stop observing their sources when the menu removes its listener; a binding created
  by `Bindings` should be disposed after that:

  ```java
  register(ShellSlots.ExtraMenu.MENU, () -> new SimpleControlProvider<ShellView<?>, DynamicMenu>(
          new DynamicMenu("_Extra")) {

      private ObservableValue<Boolean> fooPort;

      @Override
      public void initialize(ShellView<?> view) {
          super.initialize(view);
          fooPort = observeFooPort(view).map(port -> true).orElse(false);
          getControl().addVisibleCondition(fooPort);
      }

      @Override
      public void deinitialize(ShellView<?> view) {
          super.deinitialize(view);
          getControl().removeVisibleCondition(fooPort);
      }
  });
  ```

  The type arguments are named because `getControl()` must return a `DynamicMenu`: with `<>` the compiler takes the
  control type from the slot, which is a plain `Menu`.

  `DynamicContextMenu` has the same two methods: the menu is shown only while it has a visible item and all its
  conditions are true.
* A plain `Menu` is the choice when the menu has its own logic; the provider binds `visible` itself, as shown in
  [Control State](#registries-control-state), and unbinds it in `deinitialize`:

  ```java
  menu.visibleProperty().bind(observeFooPort(view).map(port -> true));
  ```

* `GroupCollapser` is installed by the dynamic menus themselves. Just before a menu is shown, it hides the separators
  at the start and the end of the menu and next to another separator, because the section between them has no visible
  items. It never changes the visibility of the menu or of its items and does not depend on `ControlRegistry` or
  `ControlBuilder`. The builder puts a separator between the groups of any menu, so a plain `Menu` that has groups
  with nothing to show should install it too: `GroupCollapser.install(menu)`.

### Stylesheet Registry <a name="registries-stylesheet"></a>

`StylesheetRegistry` holds the application-wide stylesheets that every top-level window applies: the default ones, the
icon stylesheets of the platform and of plugins. It starts empty, and the application decides what to register. It is
available through `ShellContext#getStylesheetRegistry()`, and there is only one registry in the application.

A stylesheet is registered alone or in a list as one contribution that is undone as a whole. Windows read the registry
when they are created and follow it while they are open, so stylesheets can be registered and unregistered at any time
and from any thread: the windows apply the changes in the JavaFX Application Thread. Dialogs take the registry in their
constructors.

A `Stylesheet` is a URL of a CSS file, optionally with a set of themes for which it has an extension. For such a
theme the window applies one more file next to the base one, and the name of that file is generated from the name of
the base file and the theme: for `base.css` and the `CUPERTINO_DARK` theme it is `base-cupertino-dark.css`. It is meant
for the rules that cannot be written in JavaFX CSS for all themes at once, and should be avoided where possible.

A stylesheet registered more than once is applied once and stays until all its registrations are undone. Stylesheets
are compared by their URL only, so the set of themes does not make two stylesheets different.

It is an item registry, so a listener gets the stylesheets that have appeared in and disappeared from
`getStylesheets()`; a stylesheet that another registration still holds is neither added nor removed.

### Action Registry <a name="registries-action"></a>

`ActionRegistry` holds the actions a user can do, whose hotkeys the user can reassign. It is available through
`ShellContext#getActionRegistry()`, and there is only one registry in the application. It is only a map of the
registered actions by their names: it does not know where the choices of the user are kept and never saves them.
Keeping them is the job of the module that declares the actions, so how and where it is done is up to the application
and its plugins; ShellFX only offers the classes below.

* `ActionName` identifies an action independently of its hotkey. By convention a name is a constant of an enum,
because the choice of the user is saved under the name and found by it after a restart. A name is `Multilingual`: its
`getTextKey()` gives the `TextKey` a text for the user, such as a title, is to be found by in the bundles.
* `ActionGroupName` identifies a group of actions that are listed together, for example in a window for reassigning
hotkeys. By convention it is a constant of an enum too, and it is `Multilingual` as well.
* `Action` is a name, a group name and a default hotkey, which can be `null`. Its current hotkey is the property
`hotkeyProperty()`, which starts with the default one; `isCustomHotkey()` tells whether the user has changed it.
Controls bind to this property, so a change of the hotkey is seen at once. An action carries no behavior.
* `CustomHotkeyData` is the choices of the user for the actions of a module, the root object that is kept in a file. It
keeps one record, a name and a hotkey in the form of `KeyCombination#getName()`, for each action that the user has
changed; the records are its own detail. `copyTo(actions)` gives the actions the saved hotkeys, and `copyFrom(actions)`
takes the changed ones from them; an action that the user has not changed leaves no record, so a new default hotkey
reaches everyone who has not changed it. It is a `StoreData` and is empty when nothing is changed, so a
[FileStore](#config-manager) deletes its file.

The whole chain of a module that declares actions looks like this:

```java
// activation: the actions are created from scratch, with their defaults
var actions = List.of(
        new Action(EditorAction.SAVE, EditorGroup.FILE, KeyCombination.valueOf("Shortcut+S")),
        new Action(EditorAction.FIND, EditorGroup.SEARCH, KeyCombination.valueOf("Shortcut+F")));

// the choices of the user are read from the file
var store = new FileStore<>(dataDirectory.resolve("hotkeys.bin"), getClass().getClassLoader(),
        CustomHotkeyData::new);
store.restore();                       // IOException if the file is there but cannot be read

// the choices are given to the actions before anyone can see them
store.getData().copyTo(actions);

// the actions become available to the controls
var registration = context.getActionRegistry().register(actions);

// ... the user changes hotkeys with action.setHotkey(...), controls follow the property ...

// deactivation, in the opposite order
registration.unregister();             // nobody sees the actions any more
store.getData().copyFrom(actions);     // the changed ones become the records
store.save();                          // the file is written, or deleted if nothing is changed
// the list of actions is dropped, a new activation starts again from the first step
```

Everything else is a choice of the application. It decides where the file lives (one per module, as above, or one for
the whole application), when `restore()` and `save()` are called, and what to do if the file is damaged: `restore()`
throws instead of replacing it with empty data, so that the next `save()` does not overwrite what is still there.

A control finds an action by its name with `getActionsByName()`, which returns an unmodifiable snapshot in the order of
registration, and asks for it again when the registry tells that actions have been added or removed. A window for
reassigning hotkeys checks a conflict the same way, by looking through the values of this map for the actions that
already have the hotkey. A name can be registered only once: registering a taken name throws `IllegalStateException`
and registers none of the actions of that contribution. Actions are registered alone or in a list as one contribution
that is undone as a whole. It is an item registry, and the registry can be changed from any thread.

The registry holds every action of a module, including the ones that have no hotkey at the moment: an action has no
default hotkey, or the user has removed it, and `getHotkey()` is `null`. This is deliberate. The registry does not hold
hotkeys, it holds the very `Action` objects, and a control binds to the `hotkeyProperty()` of such an object. Were the
actions without a hotkey left out, a control would have nothing to bind to, and a hotkey assigned later by the user
would reach it only if it looked the action up again. With the action in the registry the control binds once and is
updated by itself, whether the hotkey appears, changes or disappears. The module sees the same changes, because it holds
the same objects, and takes the changed ones with `copyFrom(actions)` on deactivation.

```java
// a menu item follows the hotkey of the action; while it is null the item has no accelerator
var action = context.getActionRegistry().getActionsByName().get(EditorAction.FIND);
if (action != null) {
    menuItem.acceleratorProperty().bind(action.hotkeyProperty());
}
```

### Bundle Registry <a name="registries-bundle"></a>

`BundleRegistry` holds the bundles of texts, so that a text for the user can be found in any module without knowing
whose bundle it is. It is available through `ShellContextAccess#getBundleRegistry()`, and there is only one registry
in the application. Each module and plugin has its own bundle that only it can reach, so the module loads the bundle
itself and gives the loaded `ResourceBundle` to the registry; the registry only holds it. The language is therefore the
one the bundle has been loaded for. How the application chooses it is up to the application, for example with the
standard system properties `-Duser.language` and `-Duser.country` in the start script; a text does not change while the
application runs.

* `BundleName` identifies a bundle, for example the bundle of a module. By convention a name is a constant of an enum.
* `TextKey` is the address of a text: the name of the bundle and the key in that bundle. A key must be unique only
within its bundle, so bundles of different modules do not clash.
* `Multilingual` is what has a text for the user: `getTextKey()` tells where the text is, and `getText(bundles)`
returns the text. `ActionName` and `ActionGroupName` are `Multilingual`.
* `TextUtils.getText(bundles, textKey)` returns the text of a key, and is what `Multilingual#getText` calls; use it for
a `TextKey` that does not belong to an object. If there is no bundle or no such text, it returns the key in the form
`!key!` and writes a warning to the log, so that a gap is seen in the UI and never fails a control. Take the bundle from
the registry when you need more than the text, such as `MessageFormat`.

`Multilingual` and `TextUtils` take not the registry but a function from a bundle name to the bundle, which is `null`
if there is none. The registry hands out its own method as that function, `registry::getBundle`. This way the same call
works everywhere, in a view model and in a control of the `material` module, which does not know the registry, and a
test passes `name -> bundle`.

* `BundleRegistry` registers a bundle under a name and returns it with `getBundle(name)`, which is `null` if there is
no such bundle, for example because its module has been unloaded. Registering a taken name throws
`IllegalStateException`. It is an item registry, which tells its listeners the names of the bundles that have been
added and removed, and it can be changed from any thread.

A module registers its bundle before the things that use its texts and withdraws it after them:

```java
public enum EditorBundle implements BundleName {
    EDITOR
}

public enum EditorAction implements ActionName {
    SAVE("action.save"),
    FIND("action.find");

    private final TextKey textKey;

    EditorAction(String key) {
        this.textKey = new TextKey(EditorBundle.EDITOR, key);
    }

    @Override
    public TextKey getTextKey() {
        return textKey;
    }

    // getText() of Name is omitted
}
```

```java
// activation: the module loads its own bundle for the language of the application, then registers it
var bundle = ResourceBundle.getBundle("com.example.editor.messages");
var bundleRegistration = context.getBundleRegistry().register(EditorBundle.EDITOR, bundle);
var actionRegistration = context.getActionRegistry().register(actions);

// anywhere that has the context, for example in a view model
var title = EditorAction.SAVE.getText(context.getBundleRegistry()::getBundle);

// deactivation, in the opposite order
actionRegistration.unregister();
bundleRegistration.unregister();
```

The files of the bundle are named by the language, for example `messages.properties` with `action.save=Save` and
`messages_de.properties` with the same keys and their texts in German.

## FileStorage <a name="storage"></a>

`FileStorage` is an abstraction layer over any disk. It gives one uniform API for listing, reading, writing, creating
and renaming entries, no matter where they live: the local file system, an FTP server, a cloud drive, an archive or a
virtual tree. Entries are addressed by `URI`, every storage has its own root URI, and everything above the layer works
with `StorageFile` and never with `java.nio.file.Path` or any other technology-specific type. The `storage` module
contains the layer itself and the default storages for the local file system (`UnixFileStorage`,
`WindowsFileStorage`). Storages for other disks are meant to be implemented separately and plugged in.

Currently the layer is used in ShellFX only by the [FileChooserDialog](#dialog-file-chooser), which is why a
dialog can show Google Drive or an FTP server as easily as the local disk. No other component depends on it.

### Packages <a name="storage-packages"></a>

* `storage` — the model (`StorageFile`, `EntryLink`), the storages (`FileStorage` and its base and local
implementations), `StorageProvider` that tells which storages there are, operation types, listeners and utilities.
* `storage.async` — loading of the files in the background in two phases: `FileProvider` and `FileRefiner`, with their
default implementations, the cancellable task and the sort policy.
* `storage.style` — the icon and the style the files and storages are shown with: resolvers of files,
`StorageDecorator` that gives a storage its `IconStyleDecoration` (icon and style), `FileDecorator` that gives files
theirs, the icon set and the types of decoration.
* `storage.settings` — the settings of the styles (colors, bold, italic, opacity) grouped by kinds of files into a
scheme, with the default schemes of the themes.
* `storage.view` — the helpers of the views that show files: the columns, `FileCellUpdater` and `DecorationCellUtils`
that keep a cell up to date, and `StorageErrors`, which tells the user why an operation of a storage failed.

### Core Concepts <a name="storage-concepts"></a>

* **FileStorage** — a source of entries. `FileStorageType` (`BASE`, `HDD`, `SSD`, `REMOVABLE`, `FLOPPY`, `OPTICAL`,
`NETWORK`, `VIRTUAL`) tells the client what to expect from it, for example that a network storage is slow. A storage
follows the EAFP principle: it performs the operation at once and investigates only if it failed, so a successful
operation costs one request. A storage is either a volume or the whole device. A device with a partition table (a hard
disk, an SSD, a USB flash drive, a memory card) is split into volumes, and every volume is a storage of its own. A
device with a removable medium and no partitions (a floppy disk, an optical drive, a tape) has exactly one volume, so
the device itself is the storage: it stays when the medium is taken out or replaced, and only what it contains changes.
The reader of the API should never assume that one disk means one storage, nor that one storage means one disk. A
storage carries an `id` that the operating system keeps for the volume across reboots; the system storages take it from
the drive found by DriveKit. It is a part of `equals`, so a provider can tell the volume found again from another one.
* **StorageFile** — an entry of a storage. Its structural kind is `FileEntryType`: `DIRECTORY`, `FILE`, `LINK` (a
symbolic link, or a junction on Windows) or `OTHER` (a socket, a pipe, a device). A link is a kind of its own and is
never treated as a directory, so code that walks a tree does not leave it by accident. Where a link leads is described
by `EntryLink`: the URI it states, the URI at the end of the whole chain and, once it is known, the target file. The
target may lie in another storage.
* **OperationType** — every operation says whether it is `PRIMARY`, initiated by the user (open, save, rename), or
`SECONDARY`, a step of internal machinery (copying a tree, resolving a link). It lets a listener, for example a
"recently used" tracker, react only to what the user did.
* **StorageListener** — gets the successful operations of a storage: list, create, rename, read, write.
* **StorageProvider** — tells which storages there are at the moment. Storages come and go (a removable drive is
plugged in or taken out), so the provider, the refiner and the file chooser do not keep a list of storages: they ask the
`StorageProvider` each time they need one. A client that works with only some of the storages gives a provider that
returns just those; the interface has one method, so a lambda is enough.
* **Appearance of the storage itself** — a storage is loaded dynamically, so only it knows its own icon and style, which
it provides through its `StorageDecorator`, which gives both in one step from one state of the storage.

### Two-Phase Loading <a name="storage-two-phase"></a>

A directory is not always cheap to show. A local disk lists a directory in milliseconds, but a network storage may take
seconds for a listing and more for every detail: the target of a link that lies in another storage, the type of a file
found from its header, the size of a remote file. If a client waited for all of it, the user would stare at an empty
list, and one slow file would hold the whole directory back.

That is why loading is split into two phases:

1. **The first phase gets what is quick.** `FileProvider` lists the directory on a background thread and completes only
what costs almost nothing, for example the targets of the links of a storage that is not a network one. The client gets
the files at once, sorts them and shows them. A file that still needs the second phase is marked as *pending* and is
shown provisionally, with a pending icon.
2. **The second phase gets the rest.** `FileRefiner` refines the pending files in the background, a few at a time for
every storage, and gives each file to the client **as soon as it is refined**, not when all of them are. A refinement
is a `FileRefinement`: the refiner only prepares the change and never touches a file itself. The client applies it on
the UI thread (`FileRefinement.apply()`), after which the file is final, its icon and style are resolved again, and
the cells that show it follow its observable properties. So a slow file never delays the others, and the list becomes
complete little by little while the user already works with it.

Details that make it safe:

* **Who decides.** `FileRefiner.isRefinable(file)` says which files need the second phase. The client marks them, and
only they are given to `refineFilesAsync`. The files that need nothing are final after the first phase.
* **Threads.** Every callback of the provider and the refiner runs on a background thread, and the end callback runs
once. All changes of a shown file are made on the UI thread, which is what `apply()` is for.
* **Cancellation.** Both return an `AsyncTask`. When the user navigates elsewhere, the client cancels the tasks it no
longer needs, and nothing is given after the cancellation. `AsyncResult` tells how the operation ended: its
`AsyncStatus` is `SUCCESS`, `FAILED` or `CANCELLED`.
* **Failures.** A file that can't be refined is logged and given a refinement that changes nothing, so one broken file
does not break the list. The provider does the same for a file it can't complete. If the whole operation fails, for
example the directory can't be listed, the `AsyncResult` of a failed operation carries the exception, so the client can
tell the user the reason with `StorageErrors.describe`, which chooses the words by the type of the exception.
* **Sorting.** Some sort keys are known only after the second phase, so `TwoPhaseSortPolicy` tells when to sort:
`ON_REQUEST` (never automatically, the default), `AFTER_FIRST_PHASE` (once, refined values are not known yet and such
files go last), `AFTER_SECOND_PHASE` (once, the list does not move twice) or `AFTER_EACH_PHASE` (after both, the order
may change when the values become known). A directory whose files are all final after the first phase is sorted at once
whatever the policy is.
* **No state.** The provider and the refiner keep no state of the operations, so one instance serves any number of lists
at once.

The default implementations, `DefaultFileProvider` and `DefaultFileRefiner`, work for any storage: links of a network
storage are left to the second phase, and the links of the others are resolved in the first one. A subclass completes
the listed files by overriding `prepare`, and loads in its own way with `provideAsync`, which gives it the same
background thread, cancellation and end as the provider has. The demo shows the whole process with a deliberately slow
storage in the `SAVE_FILE_SLOW` dialog.

The diagram shows how a directory gets onto the screen, from the listing to the final icon of a refined file. The
view model owns the process, and the cells only show what the files carry.

```mermaid
sequenceDiagram
    participant VM as ViewModel
    participant P as FileProvider
    participant R as FileRefiner
    participant V as View
    participant C as Cell
    participant S as FileCellUpdater
    participant D as FileDecorator
    participant X as Icon and style resolvers

    Note over VM,X: First phase
    VM->>P: provideFilesAsync
    Note right of P: background thread
    P-->>VM: onProvided with the files
    VM->>R: isRefinable for every file
    Note right of VM: refinable files become pending
    Note over VM,C: UI thread
    VM->>V: setFiles
    V->>C: the files are shown
    C->>D: decorate file
    D->>X: resolve icon and style, TEMP for a pending file
    D-->>C: the file has a decoration
    C->>S: observe the properties of the file
    S-->>C: the update sets the icon and the style

    Note over VM,X: Second phase
    VM->>R: refineFilesAsync for the pending files
    Note right of R: background thread, a few files at a time
    R-->>VM: onRefined with a FileRefinement
    Note over VM,C: UI thread
    VM->>VM: refinement.apply and the file leaves pending
    VM-->>S: the values of the file change, such as a size or a type
    S-->>C: the update sets the text
    VM->>V: decorateSource with the refined files
    V->>D: reset the refined files
    Note right of D: the decoration of the files becomes null
    D-->>S: the decoration of the file changes
    Note right of S: the update ignores a decoration that is not complete
    V->>D: decorate the files of the cells that exist in the virtual flow
    D->>X: resolve icon and style, FINAL
    D-->>S: the decoration of the file becomes complete
    S-->>C: the update sets the icon and the style
```

### Appearance <a name="storage-appearance"></a>

A file carries its decoration, `IconStyleDecoration`, as an observable property: the icon and the style (the inline styles
of the icon and of the texts) that are given to the file in one step, so a cell that shows it is told once. They are
resolved by two independent resolvers, `FileIconResolver` and `FileStyleResolver`, which are passed to the view and
replaced when an application knows more kinds of files than the defaults. A resolver gets the `FileDecorationType` of
the file, `TEMP` while the file is still to be refined and `FINAL` otherwise, which is how the pending icon becomes the
real one.

Decoration is lazy. A cell decorates a file only if its decoration is not complete (`FileDecorator`): it is `null`, or
it has no style, in which case the icon is kept and only the style is resolved again. So only the files that are shown
are decorated, however many the directory has. When the decoration of a file becomes outdated (the second phase refined
it, the theme changed, the file was marked), the view resets it and decorates again the files of the cells that exist in
the virtual flow, while the other files wait until their cells show them. A theme or a mark changes only the style, so
the view resets the style and keeps the icon. The default schemes of the themes are created on first use too, because
the palette of a theme is created when it is first needed.

A cell decorates the file it is about to show with the `FileDecorator` of its view, and owns a `FileCellUpdater`. The
updater knows nothing about decoration or about what a cell shows: the cell gives it its update and, when it shows a
file, the file and the properties it is bound to as method references (`StorageFile::decorationProperty`, and
`StorageFile::sizeProperty` or the like if the cell shows a value). The updater takes the properties out of the file,
runs the update for the file when it starts observing and whenever one of the properties changes, and stops with
`unobserve` when the cell becomes empty. The file is given to the updater and not read from the cell, because some
cells set their item only at the end of `updateItem`. For the icon and the styles the update is
`DecorationCellUtils.updateDecoration`, which ignores an outdated decoration that a reset leaves; a cell that shows a
value adds its own `setText` to the update. The updater never calls the cell otherwise, so several cells of one row,
one for each column, are each updated by the properties they observe, and the cell is not asked to show its file again.

Whoever resets the decoration decorates the files again: the view walks the cells that exist in the virtual flow with
`ColumnViewUtils.forEachItem` and `TableViewUtils.forEachItem` of the `material` module, which pass the item of each
cell without touching the cell or creating one, and decorates every item. A cell that has left the viewport is empty
and gets its item through `updateItem` when it returns, so it decorates the file itself. A control whose cells cannot
be walked, such as the combo box of locations in `FileChooserDialog`, is decorated by the view directly.

```mermaid
classDiagram
    class StorageFile
    class FileProvider
    class FileRefiner
    class FileRefinement
    class FileIconResolver
    class FileStyleResolver
    class IconStyleDecoration
    class FileDecorator
    class FileCellUpdater
    class DecorationCellUtils
    class ColumnViewUtils
    class TableViewUtils
    class ViewModel
    class View
    class Cell

    ViewModel --> FileProvider : lists
    ViewModel --> FileRefiner : refines
    FileProvider ..> StorageFile : provides
    FileRefiner ..> FileRefinement : creates
    FileRefinement ..> StorageFile : changes on apply
    View --> FileDecorator : creates and resets
    View ..> ColumnViewUtils : walks the cells with
    View ..> TableViewUtils : walks the cells with
    FileDecorator o-- FileIconResolver
    FileDecorator o-- FileStyleResolver
    FileDecorator ..> IconStyleDecoration : creates
    FileDecorator ..> StorageFile : writes the decoration
    StorageFile o-- IconStyleDecoration
    View *-- Cell
    Cell --> FileDecorator : decorates with
    Cell *-- FileCellUpdater
    Cell ..> DecorationCellUtils : applies the decoration with
    FileCellUpdater ..> StorageFile : observes the properties of
```

### Custom Storage <a name="storage-custom"></a>

To add a storage for another disk, extend `AbstractFileStorage` (the local storages extend `AbstractSystemFileStorage`),
implement the listing and the operations, report them to the listeners, throw the specific exceptions
(`NoSuchFileException`, `AccessDeniedException`), which `StorageErrors` turns into the words the user reads, and give
the icon and style resolvers of the storage. Make slow things
slow only where the client can wait for them: listing returns quickly, and what is expensive goes to the refiner.
Then give the storage to the file chooser dialog together with the others, through a `StorageProvider`.

## Naming Convention <a name="naming-convention"></a>

ShellFX is built on top of PatternFX and fully conforms to its patterns. Because of this, the naming of classes and
interfaces for components follows a consistent scheme:

1. A unique name (may be omitted for brevity) — `Alert`, `File`, `Info`, etc.
2. The component role — `Tab`, `Window`, `Popup`, `Area`, `Panel`, `ToolBar`, etc.
3. The component element — `View`, `ViewModel`, `Params`, `Port`, `Config` etc.

Examples: `AlertDialogView`, `EditorTabViewModel`, `InfoPopupParams`, `ToolBarPort`

This approach is justified by the following reasons. When a complex component is split into multiple components
(due to complexity, reuse of components, or use of a docking layout), there may be several components with the same
unique name but different roles — such as `FooTab` and `FooArea`. Another reason is that the role of a component
immediately makes it clear how to work with it and where to place it.

When working with `Composer` methods, there are two categories of methods:

1. Methods that create/destroy a component and compose/decompose it. It is important to note that these methods
manage the component lifecycle, meaning they are responsible for component initialization and deinitialization.
Such methods include: `open*`, `close*`, `show*`, and `hide*`.

2. Methods that only compose/decompose a component. These methods are responsible solely for structural component
composition and do not manage the component lifecycle. Such methods include: `add*`, `remove*`, and `replace*`.

Examples of `Composer` methods using `open*` and `close*`:

| Component | Create + Add         | Remove + Destroy      | Add Only            | Remove Only            |
|-----------|----------------------|-----------------------|---------------------|------------------------|
| Window    | `openWindow(params)` | `closeWindow(window)` | `addWindow(window)` | `removeWindow(window)` |
| Tab       | `openTab(params)`    | `closeTab(tab)`       | `addTab(tab)`       | `removeTab(tab)`       |
| Dialog    | `openDialog(params)` | `closeDialog(dialog)` | `addDialog(dialog)` | `removeDialog(dialog)` |
| Popup     | `openPopup(params)`  | `closePopup(popup)`   | `addPopup(popup)`   | `removePopup(popup)`   |
| Page      | `openPage(params)`   | `closePage(page)`     | `addPage(page)`     | `removePage(page)`     |
| Area      | `openArea(params)`   | `closeArea(area)`     | `addArea(area)`     | `removeArea(area)`     |

`View` methods also follow a naming convention that distinguishes two kinds of methods:

## Quick Start <a name="quick-start"></a>

To get started with ShellFX, it is recommended to follow these steps:

1. Familiarize yourself with the [PatternFX](https://github.com/techsenger/patternfx) framework,
the [MVVM](https://github.com/techsenger/patternfx#templates-mvvm) template, and its demo.
2. Explore and run the demo. See [Running Demo](#running-demo) for details.

## Requirements <a name="requirements"></a>

The platform requires Java 25 and JavaFX 27.

## Dependencies <a name="dependencies"></a>

This project is available on Maven Central. Minimal set of required dependencies:

```
<dependency>
    <groupId>com.techsenger.shellfx</groupId>
    <artifactId>shellfx-base</artifactId>
    <version>${shellfx.version}</version>
</dependency>
<dependency>
    <groupId>com.techsenger.shellfx</groupId>
    <artifactId>shellfx-material</artifactId>
    <version>${shellfx.version}</version>
</dependency>
<dependency>
    <groupId>com.techsenger.shellfx</groupId>
    <artifactId>shellfx-core</artifactId>
    <version>${shellfx.version}</version>
</dependency>
<dependency>
    <groupId>com.techsenger.shellfx</groupId>
    <artifactId>shellfx-layout</artifactId>
    <version>${shellfx.version}</version>
</dependency>
<dependency>
    <groupId>com.techsenger.shellfx</groupId>
    <artifactId>shellfx-icons</artifactId>
    <version>${shellfx.version}</version>
</dependency>
```

## Code Building <a name="code-building"></a>

To build the library use standard Git and Maven commands:

    git clone https://github.com/techsenger/shellfx
    cd shellfx
    mvn clean install

## Running Demo <a name="running-demo"></a>

To run the demo, execute the following commands in the project root:

    cd shellfx-demo
    mvn javafx:run

Please note, that debugger settings are in `pom.xml` file.

## License <a name="license"></a>

Techsenger ShellFX is licensed under the Apache License, Version 2.0.

## Contributing <a name="contributing"></a>

We welcome all contributions. You can help by reporting bugs, suggesting improvements, or submitting pull requests
with fixes and new features. If you have any questions, feel free to reach out — we’ll be happy to assist you.

## Support Us <a name="support-us"></a>

You can support our open-source work through [GitHub Sponsors](https://github.com/sponsors/techsenger).
Your contribution helps us maintain projects, develop new features, and provide ongoing improvements.
Multiple sponsorship tiers are available, each offering different levels of recognition and benefits.


