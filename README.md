# Power Adapters

# Motivation

Presenting large data sets efficiently is a challenging part of Android development. It gets more complicated as you
begin to handle edge cases and add additional scrolling content like headers. We also find ourselves writing undesirable
boilerplate as we redefine our adapters for each data source. In addition, Android doesn't provide an easy,
object-oriented way of present collections of multiple types.

This library provides universally compatible adapter classes that provide commonly required functionality, such as:

* Present **multiple data types** within an adapter
* Show **Headers** and **footers**
* Show a **loading** to indicate a loading state
* Show an **empty** item to indicate an empty underlying data set
* **Concatenate** multiple adapters together
* Add **dividers** in between items of an existing adapter
* Present **hierarchical** data structures

Power adapters are compatible with `ListView`, `GridView` (technically anything that accepts a normal `Adapter`), and
`RecyclerView`.

Its design promotes reuse via composition.

# Usage

## Adapter decoration

All adapters are designed to wrap an existing adapter, allowing you to maintain a separation of concerns. For example,
say you want to present a list of tweets, but show an empty message when there are no tweets, you can do the following:

```java
PowerAdapter adapter = new TweetsAdapter();
adapter = new EmptyAdapterBuilder()
    .resource(R.layout.tweets_empty_item)
    .build(adapter);

listView.setAdapter(PowerAdapters.toListAdapter(adapter));
```

This lets you write a simple `TweetAdapter` class, the only responsibility of which is to present tweets. By applying
the empty adapter as such, the `TweetAdapter` need not be modified to present the empty state, and can be potentially
reused elsewhere more easily.

This also means you can apply as many or as few wrapper adapters as you need to solve your problem. Need to show a loading
indicator as well? Just wrap the `PowerAdapter` instance with `LoadingAdapterBuilder`. By using the decorator pattern,
you can also control the **order** of each effect applied.

## Observer registration and strong references

Wrapping an adapter requires registering for change notifications using the `DataObserver` callback interface. This means
a strong reference cycle is established - the outer adapter holds a strong reference to the wrapped adapter, and the wrapped
adapter holds a strong reference to the outer adapter via its registered observer.

TODO: Expand on this.

## Conversion

Once you're ready to assign a `PowerAdapter` to a collection view, simply invoke one of the following conversion methods:

|Collection view    |Converter                                |
|:------------------|----------------------------------------:|
|ListView           |            PowerAdapters.toListAdapter()|
|RecyclerView       |RecyclerPowerAdapters.toRecyclerAdapter()|

## Binding

Included in Power Adapters is the ability to bind elements in your data set to views in a reusable and readable manner.
You can get started by using a `BindingAdapter`.

### Binder

The primary type needed to achieve this is a `Binder`. These have a couple of responsibilities:

* Construct a `View` to be bound, and re-used by the adapter/recycler view
* Bind an object and/or data set index to the `View`

Multiple types of commonly required binders are supplied: `TypedBinder` and `ViewHolderBinder`.

### Mapper

The second class involved is the `Mapper`. It is consulted to determine which `Binder` to use for presenting a
particular element in your data set. The most common type of `Mapper` you're likely to use is `PolymorphicMapperBuilder`.
This class allows you to specify a binder based on the `Class` of the object. If no binder is found, it will walk upwards
through the class hierarchy attempting to find a suitable binder.

Example:

```java
Mapper mapper = new PolymorphicMapperBuilder()
    .bind(Tweet.class, new TweetBinder())
    .build();
PowerAdapter adapter = new BindingAdapter(mapper) {
    // your implementation
}
```

### Samples

Check the included sample project for a wide array of usage pattern examples.

# Build

Building instructions:

```bash
$ git clone <github_repo_url>
$ cd power-adapters
$ ./gradlew clean build

```

# Design

## Why introduce another Adapter interface?

One of the goals of this library is first-class support for both `AdapterView` and `RecyclerView`. Using `Adapter` or
`RecyclerView.Adapter` as base wouldn't satisfy that goal.
Also, the two interfaces are largely incompatible, because of method signatures used for view reuse. By providing a
common custom base interface, `PowerAdapter`, we can resolve this.

# Migration notes

Some users may be familiar with earlier iterations of this library. Below are tips for migrating to Power Adapters from
the previous generation called Databind.

## `DisposeableListAdapter`

The `DisposeableListAdapter` interface has been eliminated. You no longer need to `dispose` of adapters. Instead,
adapters keep track of external observers, and use a reference counting system to automatically unregister their own
internal observers once no external clients are observing.