# Techsenger TabShell

| Support the Project! |
|:-------------|
| This project is open-source and free to use, both commercially and non-commercially, which is why we need your help in its development. If you like it, please give it a star ⭐ on GitHub — it helps others discover the project and increases its visibility. You can also contribute, for example, by fixing bugs 🐛 or suggesting improvements 💡, see [Contributing](#contributing). If you can, financial support 💰 is always appreciated, see [Support Us](#support-us). Thank you! |

## Table of Contents
* [Overview](#overview)
* [Demo](#demo)
    * [Text Editor](#demo-text-editor)
    * [Terminal](#demo-terminal)
    * [Dialogs](#demo-dialogs)
* [Features](#features)
* [Requirements](#requirements)
* [Modules](#modules)
* [Dependencies](#dependencies)
* [Usage](#usage)
    * [Quick Start](#usage-quick-start)
    * [Component](#usage-component)
    * [Shell](#usage-shell)
    * [Tab](#usage-tab)
    * [Dialog](#usage-dialog)
* [Code building](#code-building)
* [Running Demos](#running-demos)
    * [Core Demo](#running-core-demo)
    * [Full Demo](#running-full-demo)
* [License](#license)
* [Contributing](#contributing)
* [👉 Support Us](#support-us)

## Overview <a name="overview"></a>

Techsenger TabShell is a lightweight platform for building tab-based applications in JavaFX using the MVVM pattern.

The platform consists of two parts: the core and ready-made components. The core includes the core shell and classes
for creating components. Ready-made components are used as needed and significantly reduce the development time of the
final application.

## Demo <a name="demo"></a>

### Text Editor <a name="demo-text-editor"></a>

### Terminal <a name="demo-terminal"></a>

### Dialogs <a name="demo-dialogs"></a>

## Features <a name="features"></a>

Key features of TabShell include:

* Abstract classes to simplify component development.
* Dynamically configurable menu.
* Ability to preserve component history.
* Support for dialogs with two scopes — shell and tab.
* Window styling that matches the theme.
* Support for 7 themes (4 dark and 3 light).
* Styling with CSS.

Currently, TabShell contains the following ready-made components:

* Terminal.
* Text Viewer/Editor.
* Dialogs.

## Requirements <a name="requirements"></a>

The library requires Java 17 or later. Due to some bugs, use JavaFX versions 19–20, or a version of JavaFX after
24-ea+19 (see JDK-8344372).

## Modules<a name="modules"></a>

The platform consists of the following modules:

* Material — provides UI elements (menus, text areas, etc.) and supporting classes.
* Core — includes the shell itself, base classes for component development, settings, and core utility classes.
If you don't plan to use ready-made components, just two modules (material and core) are sufficient to run TabShell
and develop custom components. See [Core Demo](#running-core-demo) for details.
* Tabs — offers abstract components for creating tabs with various layouts.
* Storage — contains classes to interact with storage systems that are not natively recognized by the operating system
(such as Google Drive, Dropbox, FTP, and similar). At the same time, the module only includes implementations for
working with the OS's default storage systems.
* Dialogs — provides ready-to-use dialogs: alert, file chooser, confirmation etc.
* Text — contains text viewer and editor components.
* Terminal — includes a terminal emulator component.
* Registrars — provides default registrars (for menu items, etc.).
* Icons — contains the Material Design Icons font and module-specific stylesheets that utilize these icons. To use
custom icons instead, simply create your own stylesheets and add them to Shell.
* Core Demo — showcases TabShell's core functionality and provides examples for building custom components. This demo
only requires the material and core modules.
* Full Demo — showcases the complete platform with all components. This comprehensive demo uses all modules.

## Dependencies <a name="dependencies"></a>

This project is available on Maven Central.

## Usage <a name="usage"></a>

### Quick Start <a name="usage-quick-start"></a>
To get started with TabShell, it is recommended to follow these steps:

1. Familiarize yourself with the [mvvm4fx](https://github.com/techsenger/mvvm4fx) framework and its sampler.
2. Explore and run core demo. See [Core Demo](#running-core-demo) for details.
3. Explore and run full demo. See [Full Demo](#running-full-demo) for details.

### Component <a name="usage-component"></a>

The component is the main building block for creating an application using this platform. There are the following
types of components:

* [Shell](#usage-shell) component.
* [Tab](#usage-tab) component.
* [Dialog](#usage-dialog) component.
* Page component, which represents a titled component that can be selected.
* Pane component, which represents a rectangular area.
* Node component, which is used for the simplest and smallest elements.

When working with components, there are a few key points to remember:

1. A component is initialized manually (by calling the `initialize()` method) and deinitialized automatically (by
calling the `deinitialize()` method). This is because the developer may need to perform certain actions with the
component after initialization but before passing it to the parent component.

2. The following components can be closed: `Shell`, `ShellTab`, `Tab`, `Dialog`. Each of these components has
a `requestClose()` method in its `ViewModel` and a `close()` method in its `View`. In all components, when the
`ViewModel#requestClose()` method is called, it triggers the `View#close()` method via a listener.

### Shell <a name="usage-shell"></a>

`Shell` is the main component and it is responsible for the following tasks:

* Window management.
* Dynamic menu management.
* Shell tab management.
* Shell-scoped dialog management.
* Theme management.

`Shell` core doesn't have any business logic. It is only a shell for tabs that contain logic.

Working with the main menu of the `Shell` is carried out in two directions:

1. Configuring menu elements
2. Managing the state of elements and responding to user actions

The configuration of menu elements is performed dynamically and in any order, with the final result being unknown in
advance. This feature is crucial in cases where plugins/extensions are used, as they can be added/removed dynamically by
the user. Each plugin may introduce its own menu items and interact with existing menus. Therefore, it is impossible
to predict the final structure of the menu that the user will work with.

The implementation of this feature is structured as follows. There are three key elements: the menu, the group, and the
item. Each element has its own key, which is used for identification. A menu consists of groups separated by a
separator. Items are added to groups, and empty groups are ignored. All three elements are registered/unregistered in
the `ControlRegistry`. When the menu needs to be updated, this `ControlRegistry` is used by `Shell` to construct
the final menu.

The `MenuManager` is responsible for managing the state of menu elements and responding to their actions. It interacts
with a component that implements the `MenuAware` interface. This interface is always implemented by both `Shell` and
`ShellTab`. If all tabs are closed, `MenuManager` interacts with `Shell`. When tabs are present, `MenuManager`
interacts with the currently selected tab.

It is also important to remember that the `MenuManager` also interacts with MenuAware when the user uses accelerators.

To gain a complete understanding of working with the menu, it is recommended to familiarize yourself with the
`MenuAware` interface, experiment with the menu in the demo, and pay attention to log messages at the debug level.

Regarding `Shell` closure, it should be noted that as the top-level component (i.e., having no parent component),
`Shell` is unique in self-managing its own closure process (whereas all other components are closed by their
parent components).

### Tab <a name="usage-tab"></a>

There are two types of tabs: `ShellTab` and `Tab`. A `ShellTab` component can be opened through the `Shell`, so
`ShellTab` components are second-level components under the `Shell`. `ShellTab` manages tab-scoped dialogs.

A `Tab` component cannot be opened directly through the `Shell`, so it always resides inside a `ShellTab`.

The `ShellTab` a `Tab` components are closed in the following way. When the `View#close()` method is called, control is
transferred to their parent component (e.g., `Shell`, `TabManager`), which is responsible for their actual closure.
Thus, these tabs can also be closed directly through their parent if a reference to the tab is available.

The tab closing procedure is largely determined by the asynchronous nature of dialogs in the TabShell project and
consists of the following steps:

1. The parent component calls the tab's `boolean View#doOnCloseAttempt(CloseScope, Runnable)` method.
2. By default, this method calls two ViewModel methods: `ViewModel#isReadyToClose()` and
`ViewModel#prepareForClose(CloseScope, Runnable)`:

```
boolean onCloseAttempt(CloseScope scope, Runnable retryCallback) {
    if (getViewModel().isReadyToClose()) {
        return true;
    } else {
        getViewModel().prepareForClose(scope, retryCallback);
        return false;
    }
}
```
3. If `ViewModel#prepareForClose(CloseScope, Runnable)` successfully prepares the component for closure, it invokes
the provided callback to restart the closing process. If preparation fails, the closing process is silently aborted.

### Dialog <a name="usage-dialog"></a>

All dialogs in TabShell are `inline`, `asynchronous` and have a `scope` that affects what will be blocked when the
dialog is open.

Inline dialogs are UI elements that appear embedded within the current application window, typically overlaid on top
of the existing content with a semi-transparent backdrop to focus attention. They are contextually tied to a specific
section (e.g., a tab or component) and do not create a separate OS-level window. In contrast, [modal] window dialogs
(or native dialogs) open as standalone OS-managed windows with their own frames and system controls, completely
independent of the parent UI.

Asynchronous dialogs allow the program to continue running while the dialog is open, relying on callbacks, promises,
or event listeners. These avoid UI freezes and enable background tasks but require handling user responses indirectly,
often via lambda functions or observable states. Synchronous dialogs, conversely, block the application's execution
flow until the user responds, pausing all other interactions (e.g., showAndWait() in JavaFX). They simplify code logic
by enforcing a linear sequence but risk freezing the UI during operation. The key distinction lies in control flow:
asynchronous dialogs prioritize responsiveness, deferring action until the user completes the interaction, while
synchronous ones enforce immediate resolution. Modern UI design increasingly favors async approaches for scalability
and user experience.

There are two types of scope: `Shell` and `Tab`. If a dialog has a `Shell` scope, the user will not be able to do
anything in `Shell` while this dialog is displayed until it is closed. If a dialog has a `Tab` scope, only the
tab that triggered the dialog will be blocked when it is displayed. All other tabs, the main menu, etc., will be
available to the user.

Dialogs are invoked from the `ViewModel` using `ComponentHelper`.

The `Dialog` component is closed in the following way. When the `View#close()` method is called on a `Dialog`, control
is delegated to the `DialogManager`, which handles its actual closure. Therefore, a `Dialog` can also be closed
directly through the `DialogManager` when holding a reference to the dialog instance.

## Code Building <a name="code-building"></a>

To build the library use standard Git and Maven commands:

    git clone https://github.com/techsenger/tabshell
    cd tabshell
    mvn clean install

## Running Demos <a name="running-demos"></a>

The project provides two demo applications:

* Core Demo — showcases TabShell basics (material and core modules) including shell operation and custom component
development.
* Full Demo — demonstrates the complete platform with all components, featuring pre-built tabs, dialogs, text editor,
terminal emulator, and other ready-to-use components.

### Core Demo <a name="running-core-demo"></a>

To run the demo, execute the following commands in the project root:

    cd tabshell-demos/tabshell-demos-core
    mvn javafx:run

Please note, that debugger settings are in `pom.xml` file.

### Full Demo <a name="running-full-demo"></a>

To run the demo, execute the following commands in the project root:

    cd tabshell-demos/tabshell-demos-full
    mvn javafx:run

Please note, that debugger settings are in `pom.xml` file.

## License <a name="license"></a>

Techsenger TabShell is licensed under the Apache License, Version 2.0.

## Contributing <a name="contributing"></a>

We welcome all contributions. You can help by reporting bugs, suggesting improvements, or submitting pull requests
with fixes and new features. If you have any questions, feel free to reach out — we’ll be happy to assist you.

## 👉 Support Us <a name="support-us"></a>

You can support us financially through [GitHub Sponsors](https://github.com/sponsors/techsenger). Your
contribution directly helps us keep our open-source projects active, improve their features, and offer ongoing support.
Besides, we offer multiple sponsorship tiers, with different rewards.


