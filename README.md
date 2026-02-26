# Techsenger TabShell

Techsenger TabShell is a platform for building tab-based applications in JavaFX using the MVP pattern.

The platform consists of two parts: the core and ready-made components. The core includes the core shell and classes
for creating components. Ready-made components are used as needed and significantly reduce the development time of the
final application.

TabShell is built on top of the [PatternFX](https://github.com/techsenger/patternfx) framework.

## Table of Contents
* [Demo](#demo)
    * [Text Editor](#demo-text-editor)
    * [Hex Editor](#demo-hex-editor)
    * [Terminal](#demo-terminal)
    * [Dialogs](#demo-dialogs)
* [Features](#features)
* [When to Use?](#when-to-use)
* [Modules](#modules)
* [Core Components](#core)
    * [Shell](#core-shell)
    * [Tab](#core-tab)
    * [Area](#core-area)
    * [Page](#core-page)
    * [Popup](#core-popup)
    * [Dialog](#core-dialog)
* [Layout Components](#layout)
    * [TabHost](#layout-tab-host)
    * [DockHost](#layout-dock-host)
    * [PageHost](#layout-page-host)
* [Shared Components](#shared)
    * [FindBase](#shared-find-base)
    * [FindPanel](#shared-find-panel)
* [Dialog Components](#dialog)
    * [AlertDialog](#dialog-alert)
    * [FileChooserDialog](#dialog-file-chooser)
    * [NameValueDialog](#dialog-name-value)
* [DevTools Components](#devtools)
    * [DevToolsTabDock](#devtools-tab-dock)
    * [ComponentTab](#devtools-component-tab)
    * [NodeTab](#devtools-node-tab)
    * [EventTab](#devtools-event-tab)
    * [StylesheetTab](#devtools-stylesheet-tab)
    * [EnvironmentTab](#devtools-environment-tab)
* [Web Components](#web)
    * [WebBrowserTab](#web-browser-tab)
    * [WebToolBar](#web-tool-bar)
* [Quick Start](#quick-start)
* [Requirements](#requirements)
* [Dependencies](#dependencies)
* [Code Building](#code-building)
* [Running Demo](#running-demo)
* [License](#license)
* [Contributing](#contributing)
* [Support Us](#support-us)

## Demo <a name="demo"></a>

### Text Editor <a name="demo-text-editor"></a>

![TabShell Text Editor](https://github.com/user-attachments/assets/42b61472-3ce6-4309-91fe-8ecd188f494f)

### Hex Editor (In Progress) <a name="demo-hex-editor"></a>

![Screenshot from 2025-06-11 23-30-02](https://github.com/user-attachments/assets/13fd4345-db82-40d1-b79a-b71341c90a09)

### Terminal <a name="demo-terminal"></a>

![Screenshot from 2025-06-11 23-53-30](https://github.com/user-attachments/assets/d7be0f9f-38ba-4e0f-aa75-23ed830c7f60)

### Dialogs <a name="demo-dialogs"></a>

![TabShell Dialogs](https://github.com/user-attachments/assets/83be533a-08fb-437a-aeb7-38b48ebe2291)

## Features <a name="features"></a>

Key features of TabShell include:

* Dynamically configurable menu.
* Support for different types of workspace.
* Abstract classes to simplify component development.
* A set of ready-made components that can be used out of the box.
* Support for different layouts, including a docking layout.
* Set of devtools for inspecting the application at both the component layer and the JavaFX scene graph layer.
* Ability to preserve component history.
* Support for inline dialogs with two scopes — shell and tab.
* Window styling that matches the theme.
* Support for 7 themes (4 dark and 3 light).
* API for working with all colors in the palettes of all themes
* Styling with CSS.

Currently, the primary ready-made components include:

* Shell.
* Different layouts.
* Terminal.
* Text viewer/editor.
* Hex editor.
* Simple web browser.
* Dialogs.
* Devtools.

## When to Use <a name="when-to-use"></a>

With the widespread adoption of web browsers, tab-based interfaces have become familiar and intuitive for most users.
TabShell leverages this familiarity to create desktop applications where users can comfortably work with multiple
contexts simultaneously.

Perfect for:

* Code editors where developers work with multiple files.
* Enterprise systems managing different data entities like orders, customers, and products.
* Database administration tools running parallel queries and comparisons.
* Data analysis applications with multiple datasets and visualization tabs.
* File managers and content browsers handling various file types.
* Monitoring dashboards displaying different metrics and logs
* Customer support systems with multiple client sessions open

The tab-based approach allows users to maintain workflow context while switching between different tasks, making
complex applications more intuitive and productive.

## Modules<a name="modules"></a>

The platform consists of the following modules:

* Material — provides UI elements (menus, text areas, etc.) and supporting classes.
* Core — includes the shell itself, base classes for component development, settings, and core utility classes.
* Layout — offers abstract components for creating tabs with various layouts.
* Shared — includes components that are used by other components from different modules.
* Storage — provides abstractions for working with file systems. The module includes a default implementation for the
local file system. Additional storage providers (for Google Drive, Dropbox, FTP, and similar) can be implemented
separately.
* Dialogs — provides ready-to-use dialogs: alert, file chooser, confirmation etc.
* Text — contains text viewer and editor components.
* Hex — contains hex editor.
* Terminal — includes a terminal emulator component.
* Registrars — provides default registrars (for menu items, etc.).
* Icons — contains the Material Design Icons font and module-specific stylesheets that utilize these icons. To use
custom icons instead, simply create your own stylesheets and add them to Shell.
* DevTools — contains tools for exploring component tree and JavaFX scene graph.
* Web — includes a simple web browser built on JavaFX WebView.
* Demo — showcases TabShell's core functionality, provides examples for building custom components, and
presents all ready-made components.

If you don't plan to use ready-made components, just three modules (material, core, layout) are sufficient to run
TabShell and develop custom components. See [Running Demo](#running-demo) for details.

## Core Components <a name="core"></a>

These components form the architectural foundation of the platform, and all higher-level platform components are built
upon them.

TabShell is built on top of the PatternFX platform, which supports working both with and without a component tree.
In TabShell, all components form a tree structure, and multiple trees may exist depending on the number of `Window`s.
For this reason, all TabShell core components inherit from the `Parent` and `Child` components provided by PatternFX.

Each component is defined by an interface accompanied by a base implementation. This approach ensures loose coupling
while still providing default implementations out of the box. It also allows developers to replace or extend the default
behavior with custom implementations when required. For instance, the platform consistently references `Shell`
through the `ShellFxView` interface rather than a concrete class.

When working with components, there are several important points to keep in mind:

1. A component must be initialized manually (by calling the `initialize()` method) and is typically deinitialized by
its parent component (by calling the `deinitialize()` method). This approach allows developers to perform additional
configuration or setup after initialization but before attaching the component to the component tree.

2. Working with components involves maintaining two hierarchies — the component tree and the JavaFX node tree. Therefore,
any addition or removal of a component must be reflected in both structures. For example, removing a component from
the node tree without removing it from the component tree will result in a memory leak. DevTools provide the ability
to inspect and monitor both hierarchies.

### Shell <a name="core-shell"></a>

`Shell` is the main and top-level component and is the only fully ready-to-use component in the core module. It is
responsible for the following tasks:

* Window management.
* Dynamic menu management.
* Shell-scoped dialog management.
* Theme management.
* Workspace management.

The Shell core does not contain any business logic. It is only a shell for tabs that contain logic.

Working with the main menu of the `Shell` is carried out in two directions:

1. Configuring menu elements
2. Managing the state of elements and responding to user actions

The configuration of menu elements is performed dynamically and in any order, with the final result being unknown in
advance. This feature is crucial in cases where plugins/extensions are used, as they can be added/removed dynamically by
the user. Each plugin may introduce its own menu items and interact with existing menus. Therefore, it is impossible
to predict the final structure of the menu that the user will work with.

The implementation of this feature is structured as follows. There are three key elements: the menu, the group, and the
item. Each element has its own name, which is used for identification. A menu consists of groups separated by a
separator. Items are added to groups, and empty groups are ignored. The factories of all three elements are
registered/unregistered in the `ControlRegistry`. When the menu needs to be updated, this `ControlRegistry` is used
by `Shell` to construct the final menu.

The `MenuManager` is responsible for managing the state of menu elements and responding to their actions. It
interacts with a component that provides a port implementing the `MenuAwarePort` interface. The algorithm works as
follows: `Shell` tracks the component that currently has focus and stores it in `ShellFxView#focusedProperty()`.
At the same time, the focused component may not participate in menu formation (for example, it could be just a toolbar).
Therefore, after the focused component changes, `Shell` searches from the focused component up to the root of the
tree — the Shell — for the first component whose port implements `MenuAwarePort`. Note that `Shell` can also form
the main menu, but this is usually done only when the workspace is empty. See also `ShellFxView#menuAwareProperty()`.

It is also important to remember that the `MenuManager` also interacts with `MenuAwarePort` when the user uses accelerators.

To gain a complete understanding of working with the menu, it is recommended to familiarize yourself with the
`MenuAwarePort` interface, experiment with the menu in the demo, and pay attention to log messages at the debug level.

The second key part of TabShell is the workspace, which represents one of the available layouts. TabShell supports
two primary workspace types:

1. Browser-like. This workspace is created using the `TabHost` component with a flag indicating that it is a workspace.
Additionally, the tabs added to this `TabHost` contain a docking layout created with the `DockHost` component.
2. IDE-like. This workspace is a straightforward docking layout created with the `DockHost` component.

### Tab <a name="core-tab"></a>

`Tab` is an abstract component used for creating custom tab implementations. In TabShell, `Tab` is one of the central
platform components, since the primary application functionality is delivered through tabs.

`Tab` can be added to any component that implements the `TabContainer` interface. The platform
provides two components that implement this interface: `TabHost` and `TabDock`, where `TabDock` extends `TabHost`.

### Area <a name="core-area"></a>

`Area` is an abstract base component that represents a rectangular region. Naturally, `AreaFxView#getNode()` returns a
`Region`.

### Page <a name="core-page"></a>

`Page` is a component that represents a titled, selectable element. A key feature of this component is its lazy
initialization. For example, if a container displays one of N `Page`s, only the `Page` that the user actually chooses to
view will be initialized.

`Page` can be added to any component that implements the `PageContainer` interface. The default implementation of
this interface is `PageHost`.

### Popup <a name="core-popup"></a>

All `Popup`s in TabShell are inline and have a scope that affects what will be blocked when the `Popup` is open.

Inline `Popup`s are components that appear embedded within the current application window, typically overlaid on top
of the existing content. They are contextually tied to a specific section (e.g., a `Shell` or `Tab`) and do not
create a separate OS-level window. In contrast, modal window `Popup`s (or native `Popup`s) open as standalone
OS-managed windows with their own frames and system controls, completely independent of the parent UI.

There are two types of scope: `Shell` and `Tab`. `Popup`s in the `Tab` scope are bound to a specific tab and are visible
only while that tab is open. `Popup`s in the `Shell` scope are global to the shell and remain visible even when all
tabs are closed.

`Popup` can be added to any component that implements the `PopupContainer` interface. The platform provides two
components that implement this interface: `Tab` and `Shell`.

### Dialog <a name="core-dialog"></a>

All `Dialog`s in TabShell are inline, asynchronous, and have a scope that affects what will be blocked when the
`Dialog` is open. `Dialog`s extend `Popup`s and represent a specialized type of `Popup` with dialog-specific behavior.

Asynchronous `Dialog`s allow the program to continue running while the `Dialog` is open, relying on callbacks, promises,
or event listeners. These avoid UI freezes and enable background tasks but require handling user responses indirectly,
often via lambda functions or observable states. Synchronous `Dialog`s, conversely, block the application's execution
flow until the user responds, pausing all other interactions (e.g., `showAndWait()` in JavaFX). They simplify code logic
by enforcing a linear sequence but risk freezing the UI during operation. The key distinction lies in control flow:
asynchronous `Dialog`s prioritize responsiveness, deferring action until the user completes the interaction, while
synchronous ones enforce immediate resolution. Modern UI design increasingly favors async approaches for scalability
and user experience.

There are two types of scope: `Shell` and `Tab`. If a `Dialog` has a `Shell` scope, the user will not be able to do
anything in `Shell` while this `Dialog` is displayed until it is closed. If a `Dialog` has a `Tab` scope, only the
tab that triggered the `Dialog` will be blocked when it is displayed. All other tabs, the main menu, etc., will be
available to the user.

`Dialog` can be added to any component that implements the `DialogContainer` interface. The platform provides two
components that implement this interface: `Tab` and `Shell`.

## Layout Components <a name="layout"></a>

Layout components are responsible for arranging `Tab`, `Page`, and, in some cases, `Area` components and their derivatives.

### TabHost <a name="layout-tab-host"></a>

`TabHost` is the primary component that can contain `Tab` components; therefore, it implements the `TabContainer` interface.
This component provides all the necessary APIs for working with tabs — adding, selecting, removing, transferring
tab ports, and more.

### DockHost <a name="layout-dock-host"></a>

`DockHost` is the main component of the docking layout and one of the most complex components in the platform.
Before describing how it works, let’s examine its child components.

`SplitSpace` is a component that extends `Area`. It internally contains a `SplitPane` node and is responsible for
arranging child components either vertically or horizontally.

`TabDock` extends `TabHost`, meaning it can contain tabs. In addition, it introduces docking-specific functionality
such as dragging an entire `TabDock` from one layout position to another, collapsing it into a `SideBar`, and
similar behaviors.

`SideBar` is a component that displays collapsed `TabDock` instances. It is important to note that a `SideBar` can
be shown even when it contains no collapsed `TabDock` components, using `SideBarPolicy`. This is useful when the
`SideBar` is intended to host additional UI elements besides collapsed `TabDock`s.

Now that the components are introduced, let’s outline how everything works together. A docking layout is always
represented as a tree. Therefore, the layout must be constructed using `SplitSpace` nodes. A `SplitSpace` can contain
other `SplitSpace` instances (to change orientation), `TabDock` instances (to host `Tab`s), or any `Area`-based
component as a leaf node. After constructing the component tree, the method `Composer#setRoot(SplitSpaceFxView<?>)`
must be called.

In addition to building the component tree, `DockHost` requires specifying the main component — the component relative
to which all other components are positioned. The main component can be an `Area` or any class derived from it
(including `TabDock` and `SplitSpace`). It is set using the method `Composer#setMain(AreaFxView<?>)`.

### PageHost <a name="layout-page-host"></a>

`PageHost` is a simple component that displays `Page` components and performs their lazy initialization.

It can be used to display navigable pages with a menu-like structure (similar to a website layout). Additionally,
it is commonly used in dialogs with a navigation tree on the left and a content page on the right. A classic example
is a settings dialog.

## Shared Components <a name="shared"></a>

Shared components are auxiliary components built on top of Core components and used by components from other modules.

### FindBase <a name="shared-find-base"></a>
`FindBase` is an abstract base search component that contains the entire search view implementation, including both
submit search and instant search functionality. Since child components may be of different types (toolbar, panel, etc.),
this component includes only minimal CSS styling. It is important to note that this component does not contain any
logic for executing the search itself. At the same time, the base `*FindPort` interfaces are provided without
implementations.

### FindPanel <a name="shared-find-panel"></a>
`FindPanel` is an abstract class for find panels that are placed at the bottom of other components, such as a terminal,
text editor, hex editor, etc. It is important to note that this component does not contain any logic for executing the
search itself.

## Dialog Components <a name="dialog"></a>

In this section, the dialogs from the `dialogs` module are described. This module contains implementations of the most
commonly used dialogs.

### AlertDialog <a name="dialog-alert"></a>

`AlertDialog` is a dialog for common user notification scenarios such as informational messages, warnings, errors,
and confirmation requests.

### FileChooserDialog <a name="dialog-file-chooser"></a>

`FileChooserDialog` is a dialog for selecting a file when opening or saving. The dialog type is defined using the
`FileChooserType` enumeration.

It is important to note that this dialog works with files provided by classes from the `storage` module. This makes
it possible to use the dialog with virtually any file storage implementation, provided that an appropriate `FileStorage`
implementation is supplied.

### NameValueDialog <a name="dialog-name-value"></a>

`NameValueDialog` is a simple dialog for displaying name–value pairs. The parameter name is shown in a `TextField`,
while the value is displayed in a `TextArea`.

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

`NodeTab` is a tool for analyzing the JavaFX scene graph. It allows traversing the node tree and inspecting node
properties. The component also enables opening reference documentation (Javadoc) for both classes and their properties.

### EventTab <a name="devtools-event-tab"></a>

This component allows recording node events. It can operate with or without filters. Events can be filtered by
selected component, message, event type, and other criteria.

### StylesheetTab <a name="devtools-stylesheet-tab"></a>

This component allows inspecting which stylesheets are applied to nodes within the scene.

### EnvironmentTab <a name="devtools-environment-tab"></a>

This component provides access to platform settings, system properties, and environment variables.

## Web Components <a name="web"></a>

Web components are components that reside in the web module. Together, they form a simple web browser built on
JavaFX WebView.

### WebBrowserTab <a name="web-browser-tab"></a>

WebBrowserTab is the main component. It represents a Tab for the Shell and can contain all other web components.

### WebToolBar <a name="web-tool-bar"></a>

WebToolBar is a component that represents the browser’s ToolBar.

## Quick Start <a name="quick-start"></a>

To get started with TabShell, it is recommended to follow these steps:

1. Familiarize yourself with the [PatternFX](https://github.com/techsenger/patternfx) framework,
the [MVP](https://github.com/techsenger/patternfx#templates-mvp) template, and its demo.
2. Explore and run the demo. See [Running Demo](#running-demo) for details.

## Requirements <a name="requirements"></a>

The library requires Java 25 and JavaFX 25.

## Dependencies <a name="dependencies"></a>

This project is available on Maven Central. Minimal set of required dependencies:

```
<dependency>
    <groupId>com.techsenger.tabshell</groupId>
    <artifactId>tabshell-material</artifactId>
    <version>${tabshell.version}</version>
</dependency>
<dependency>
    <groupId>com.techsenger.tabshell</groupId>
    <artifactId>tabshell-core</artifactId>
    <version>${tabshell.version}</version>
</dependency>
<dependency>
    <groupId>com.techsenger.tabshell</groupId>
    <artifactId>tabshell-layout</artifactId>
    <version>${tabshell.version}</version>
</dependency>
```

## Code Building <a name="code-building"></a>

To build the library use standard Git and Maven commands:

    git clone https://github.com/techsenger/tabshell
    cd tabshell
    mvn clean install

## Running Demo <a name="running-demo"></a>

To run the demo, execute the following commands in the project root:

    cd tabshell-demo
    mvn javafx:run

Please note, that debugger settings are in `pom.xml` file.

## License <a name="license"></a>

Techsenger TabShell is licensed under the Apache License, Version 2.0.

## Contributing <a name="contributing"></a>

We welcome all contributions. You can help by reporting bugs, suggesting improvements, or submitting pull requests
with fixes and new features. If you have any questions, feel free to reach out — we’ll be happy to assist you.

## Support Us <a name="support-us"></a>

You can support our open-source work through [GitHub Sponsors](https://github.com/sponsors/techsenger).
Your contribution helps us maintain projects, develop new features, and provide ongoing improvements.
Multiple sponsorship tiers are available, each offering different levels of recognition and benefits.


