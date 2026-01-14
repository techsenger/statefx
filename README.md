# Techsenger StateFX

StateFX is a library for creating JavaFX node states through composition using interfaces. It allows you to define
states with the required properties and observable collections using minimal code.

A distinctive feature of the library is the ability to use both custom interfaces and interfaces automatically
generated for all JavaFX node types, which significantly simplifies development and speeds it up.

StateFX is especially useful when applying architectural patterns that separate the view from the logic, such as MVVM.

## Table of Contents
* [Overview](#overview)
* [Features](#features)
* [How It Works](#how-it-works)
* [Usage](#usage)
* [Requirements](#requirements)
* [Dependencies](#dependencies)
* [Code building](#code-building)
* [Running Demo](#demo)
* [License](#license)
* [Contributing](#contributing)
* [Support Us](#support-us)

## Overview <a name="overview"></a>

JavaFX allows the state of scene graph nodes to be described separately from the nodes themselves and bound to them
using one-way and/or two-way bindings. This approach provides significant advantages for developing and testing
application logic, as it enables working with state independently of the View layer.

However, in practice, implementing this concept is associated with certain challenges. The main problem is that even
for nodes of the same type, different use cases require different sets of properties and observable collections to
describe their state. The number of possible combinations of such sets can be quite large.

For example, for a `ToggleButton`, different scenarios may require different combinations of properties: only `selected`;
`disable` and `text`; `visible`, `text`, and `wrapText`, and so on. It is important to note that this discussion
concerns declaring the state of nodes whose properties are strictly defined by the JavaFX API. Consequently,
in each specific case, we are effectively re-declaring the same node properties and collections, but in different
combinations.

Let us review existing approaches to solving this problem and the solution proposed by StateFX. Suppose there are
two nodes — Foo and Bar.

Solution 1 — All properties are added directly to the ViewModel.

```java
public class ViewModel {
    private BooleanProperty fooDisable;
    private BooleanProperty fooVisible;
    private StringProperty fooSelectedItem;
    private ObservableList<String> fooItems;

    private BooleanProperty barSelected;
    private BooleanProperty barVisible;
    private StringProperty barText;
    private BooleanProperty barTextWrap;

    + 22 methods (7 property accessors, 7 setters, 8 getters)
}
```

Pros
* Simple
* Suitable for small screens

Cons
* ViewModel grows quickly.
* Properties of different UI elements are not grouped.
* No reuse.
* Poor scalability.
* A large amount of boilerplate code.

Solution 2 – Separate model class for each UI element

```java
public class FooState {
    private BooleanProperty fooDisable;
    private BooleanProperty fooVisible;
    private StringProperty fooSelectedItem;
    private ObservableList<String> fooItems;

    + 10 methods
}

public class BarState {
    private BooleanProperty barSelected;
    private BooleanProperty barVisible;
    private StringProperty barText;
    private BooleanProperty barTextWrap;

    + 12 methods
}
```

Pros
* Better encapsulation.
* Grouping of properties.

Cons
* Large number of classes.
* Inheritance doesn’t work well here because this is composition.
* Large amount of boilerplate code.

Solution 3: state composition via interfaces. StateFX proposes modeling UI state as a set of minimal contracts,
each responsible for a single property or collection

```java
public interface FooState extends
        BooleanDisableState,
        BooleanVisibleState,
        StringSelectedItemState,
        ListItemsState<String> { }

public interface BarState extends
        BooleanSelectedState,
        BooleanVisibleState,
        StringTextState,
        BooleanTextWrapState { }

FooState foo = StateFactory.create(FooState.class);
BarState bar = StateFactory.create(BarState.class);
```

Pros
* Composition instead of inheritance.
* No placeholder/dummy classes.
* Clean and compact ViewModel.
* Easy reuse of UI state.
* Ideal for testing.

Cons
* For read-only properties/collections, you need either two interfaces or casting.
* Overhead from class generation if it’s not cached.

It is important to note that StateFX is not intended for domain or business logic; it is solely designed for
describing UI state.

## Features <a name="features"></a>

* Separation of read-only and writable states at the type level.
* Support for synchronized collections.
* Minimal boilerplate — state generation directly from interfaces.
* Full support for JavaFX Properties — works with all property types and observable collections.
* Reusable contracts — a library of ready-made states for standard controls.
* Ideal for MVVM / clean architecture — clean separation of View and ViewModel.
* Perfect for testing — states are easy to mock and test without a UI.
* Includes benchmark tests to evaluate library performance.
* Complete documentation — detailed examples and guides.

## How it works <a name="how-it-works"></a>

After a state interface is defined, StateFX generates a corresponding implementation class and creates instances of
that class. The implementation class is generated using the Byte Buddy library and cached, so generation occurs only
once per state interface. Subsequent requests reuse the already generated classes, which significantly reduces overhead.

Reflection is used only during interface analysis and class generation. After generation, all methods execute via
compiled bytecode without using reflection.

Property and collection instances are stored in final fields and created in the constructor. Therefore, lazy
initialization is not supported. The reason is that created states must be bound to a node, so supporting lazy
initialization would only complicate class generation. Note that when the same collection must have both read-only
and writable access, two fields are created: one holds the modifiable instance, and the other holds the unmodifiable
instance.

Default state interfaces are generated using a Maven plugin, which is part of the project.

## Usage <a name="usage"></a>

When working with StateFX, there are two simple rules:

* Each property or collection has two interfaces. One interface is used when there are no write restrictions.
The other interface, with the `RO` or `RW` prefix, is used when read-only access needs to be provided to an external user.
* StateFX does not generate Javadoc and does not treat it as part of the contract. Therefore, aspects such as
modifiable/unmodifiable collections are not enforced by the library and, if needed, must be documented by the user.

Example of two interfaces for a `Property`:

```java
public interface ROBooleanDisableState extends PropertyState {

    ReadOnlyBooleanProperty disableProperty();

    boolean isDisable();
}

public interface BooleanDisableState extends ROBooleanDisableState {

    @Override
    BooleanProperty disableProperty();

    void setDisable(boolean value);
}

```

Example of two interfaces for a `List`

```java
public interface ListItemsState<T> extends CollectionState {

    ObservableList<T> getItems();
}

public interface RWListItemsState<T> extends ListItemsState<T> {

    ObservableList<T> getModifiableItems();
}

```

Example of two interfaces for a `Map`

```java
public interface MapPropertiesState<T, S> extends MapState {

    ObservableMap<T, S> getProperties();
}

public interface RWMapPropertiesState<T, S> extends MapPropertiesState<T, S> {

    ObservableMap<T, S> getModifiableProperties();
}
```

One writable property:

```java
public class ViewModel {

    private final BooleanSelectedState foo = StateFactory.create(BooleanSelectedState.class);

    public BooleanSelectedState getFoo() {
        return this.foo;
    }
}
```

One read-only property:

```java
public class ViewModel {

    private final BooleanSelectedState foo = StateFactory.create(BooleanSelectedState.class);

    public ROBooleanSelectedState getFoo() {
        return this.foo;
    }

    protected BooleanSelectedState getInternaFoo() {
        return this.foo;
    }
}
```

Multiple writable properties:

```java
public class ViewModel {

    public interface FooState extends BooleanSelectedState, BooleanDisableState { }

    private final FooState foo = StateFactory.create(FooState.class);

    public FooState getFoo() {
        return this.foo;
    }
}
```

Multiple writable and read-only properties:

```java
public class ViewModel {

    public interface FooState extends BooleanSelectedState, ROBooleanDisableState { }

    protected interface InternalFooState extends FooState, BooleanDisableState { }

    private final InternalFooState foo = StateFactory.create(InternalFooState.class);

    public FooState getFoo() {
        return this.foo;
    }

    protected InternalFoo getInternalFoo() {
        return this.foo;
    }
}
```

Modifiable collection:

```java
public class ViewModel {

    public interface FooState extends BooleanSelectedState, ListItemsState<String> { }

    private final FooState foo = StateFactory.create(FooState.class);

    public FooState getFoo() {
        return this.foo;
    }
}
```

Unmodifiable collection:

```java
public class ViewModel {

    public interface FooState extends BooleanSelectedState, ListItemsState<String> {

        /**
         * Returns an unmodifiable list.
         */
        @Override
        ObservableList<String> getItems();
    }

    public interface InternalFooState extends FooState, RWListItemsState<String> { }

    private final InternalFooState foo = StateFactory.create(InternalFooState.class);

    public FooState getFoo() {
        return this.foo;
    }

    protected InternalFoo getInternalFoo() {
        return this.foo;
    }
}
```

Synchronized collection:

```java
public class ViewModel {

    public interface FooState extends BooleanSelectedState, ListItemsState<String> {

        @Override
        @Synchronized
        ObservableList<String> getItems();
    }

    private final FooState foo = StateFactory.create(FooState.class);

    public FooState getFoo() {
        return this.foo;
    }
}
```

## Requirements <a name="requirements"></a>

Java 23+ and JavaFX 25+.

## Dependencies <a name="dependencies"></a>

This project will be available on Maven Central in a few weeks.

```
<dependency>
    <groupId>com.techsenger.statefx</groupId>
    <artifactId>statefx-core</artifactId>
    <version>${statefx.version}</version>
</dependency>

```
For generated states

```
<dependency>
    <groupId>com.techsenger.statefx</groupId>
    <artifactId>statefx-states</artifactId>
    <version>${statefx.version}</version>
</dependency>

```

## Code Building <a name="code-building"></a>

To build the library use standard Git and Maven commands:

    git clone https://github.com/techsenger/statefx
    cd statefx
    mvn install

## License <a name="license"></a>

Techsenger StateFX is licensed under the GNU General Public License version 2, with the Classpath Exception.

The copyright and the StateFX license do not apply to the interfaces in the `states` module, as they are
generated by the Maven plugin based on the JavaFX API. For licensing information about these interfaces, please
refer to the JavaFX license.

## Contributing <a name="contributing"></a>

We welcome all contributions. You can help by reporting bugs, suggesting improvements, or submitting pull requests
with fixes and new features. If you have any questions, feel free to reach out — we’ll be happy to assist you.

## Support Us <a name="support-us"></a>

You can support our open-source work through [GitHub Sponsors](https://github.com/sponsors/techsenger).
Your contribution helps us maintain projects, develop new features, and provide ongoing improvements.
Multiple sponsorship tiers are available, each offering different levels of recognition and benefits.
