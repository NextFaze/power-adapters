Change Log
==========

## Version 0.20.0

_2018-7-03_

* Allow comparing `PowerAdapter.getItemViewType()` to a `ViewFactory`

## Version 0.19.0

_2018-7-02_

* Add Kotlin `observableData` diff strategy `None`

## Version 0.18.0

_2018-6-26_

* Fix bug where Kotlin `observableData` default loading behavior emitted too early, making it appear that the data set
was empty briefly, causing a visual glitch in many cases

## Version 0.17.0

_2018-6-22_

* Add Kotlin reactive `Data` top-level function `observableData`

## Version 0.16.0

_2018-6-22_

* Fix bug where `ObservableDataBuilder` created `Data` with loading remaining `true` after content error
* Fix bug where `ObservableDataBuilder` created multiple subscriptions to the content observable

## Version 0.15.0

_2017-10-18_

* Migrate to RxJava 2.x
* Add `Binder.withStableIds()` Kotlin extension function
* Add `Binder.withViewType()` Kotlin extension functions
* Add `Data.toAdapter()` Kotlin extension functions
* Add `List.toAdapter()` Kotlin extension functions
* Add Kotlin top-level function `viewFactory()`
* Bump minSdk to `14`
* Fix bug in DiffList where clear() failed to dispatch a remove notification

## Version 0.14.0

_2017-06-27_

* Add `Data.fromList` and Kotlin method `data(() -> List<T>, ExecutorService)`, which create a `Data<T>` that presents 
  an asynchronously loaded list
* Add `Data.fromCursor` and Kotlin method `cursorData(() -> Cursor, (Cursor) -> T, ExecutorService)`, which create a 
  `Data<T>` that presents the contents of a asynchronously loaded database `Cursor`
* Remove `ArrayData.onClear()`
* Make `ArrayData.asList()` `final`
* Add `ArrayData` constructor that accepts an `ExecutorService` for performing the asynchronous load
* Add `Binder.inflate(ViewGroup, @LayoutRes int)` convenience method
* Add `DataExecutors` for accessing the default `ExecutorService`(s) used by the library

## Version 0.13.0

_2017-04-07_

* Add Kotlin modules, which add Kotlin-idiomatic extensions
* Expand on RxJava bridging modules:
    - Move many APIs from `power-adapters-data-rx` into new module `power-adapters-rx`
    - Add `ObservableAdapterBuilder` and `ObservableDataBuilder`, for creating adapters and `Data` objects based on 
        RxJava `Observable`s. Both use `DiffUtil` internally to dispatch fine-grained notifications, and thus have full
        item animation support.
    - Add `Observable`-based `Condition`s, available in the `RxCondition` class.
* Add `Mapper` type param `T`, which lets callers specify an upper bound of types the `Mapper` handles.
* Fix some `Binder` API generics variance problems, making them much more flexible.
* `MapperBuilder` now evaluates rules from top to bottom. This can result in breaking changes if your usages relied on
  the previous behavior of evaluating rules based on class.
* Add `PowerAdapter.wrapItems`, for wrapping item views in another `ViewGroup` to apply extra layout effects.
* Remove aggressive notification consistency verification, allowing for batches of notifications to be dispatched as
  long as they are consistent by the time `RecyclerView` responds to them. This mainly affects custom `Data` or 
  `PowerAdapter` implementations.
* Deprecate `DataBindingAdapter(Data, Binder)`, `DataBindingAdapter(Data, Mapper)`, and add 
  `DataBindingAdapter(Binder, Data)`, `DataBindingAdapter(Mapper, Data)` to be consistent with other APIs.
* Add more helper conditions to `DataConditions`.
* Removed `RecyclerView` and `ListView` converter adapter caching, as it caused a memory leak in certain use cases.
  This means `RecyclerPowerAdapters.toRecyclerAdapter(adapter) != RecyclerPowerAdapters.toRecyclerAdapter(adapter)`, ie,
  you can longer rely on the same instance being returned from `toRecyclerAdapter` for a given input adapter.
* Remove many unnecessary methods by eliminating synthetic accessors to private members.
* Remove Lombok annotations, replacing them with Android support annotations. Runtime `null` checks are still present.
* Fix a few other misc memory leaks.

## Version 0.12.1

_2016-11-30_

* Fix bug where `ArrayData` and `IncrementalArrayData` emitted notifications that were not consistent with their 
  reported `size`

## Version 0.12.0

_2016-11-16_

* Fix incorrect return type of `BindViewFunction`

## Version 0.11.0

_2016-10-31_

* Add `Container` API:
    - `Container` grants access to the context in which adapter items are bound, so a `Binder` can control scrolling or
      access the enclosing `ViewGroup`.
    - Add `Container` argument to `PowerAdapter.bindView`.
    - Add `Container` argument to `Binder.bindView`.
    - Add `Container` argument to `ViewHolderBinder.bindView`.
* Add `PowerAdapters.toSpinnerAdapter` method, for converting a `PowerAdapter` to `android.widget.SpinnerAdapter`
  (see javadoc for restrictions, however.)
* Make `RecyclerConverterAdapter` public, and available for extension, since this is sometimes needed to interact with
  other `RecyclerView` libraries.
* `RecyclerConverterAdapter` now avoids some kinds of memory leaks by unregistering from the wrapped `PowerAdapter`
  while the `RecyclerView` is detached from the window. This means you no longer need to nullify the `RecyclerView`
  adapter manually to prevent leaks.
* Add static factory methods to `Binder` and `ViewHolderBinder`. These reduce `Binder` declaration boilerplate for 
  simple use cases.
* Fix bug where `RxData.available` and `RxData.loading` never emitted values.
* Fix bug where `FilterData` (accessed via `Data.filter`) updated its index incorrectly in response to batch removals.
* Deprecate `AbstractBinder`. Replace uses of this with `Binder`, which has been made an abstract class.
* Fix bug where extra unwelcome calls to `DataLayout.onFinishInflate` caused some child components to become invisible.
* Remove deprecated classes:
    - `TypedBinder`
    - `EmptyAdapterBuilder`
    - `FooterAdapterBuilder`
    - `HeaderAdapterBuilder`
    - `LoadingAdapterBuilder`
    - `DataEmptyDelegate`
    - `DataLoadingDelegate`
* Remove deprecated methods:
    - `ViewFactories.viewFactoryForResource`
