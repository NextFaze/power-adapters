Change Log
==========

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
  other `RecyclerVew` libraries.
* `RecyclerConverterAdapter` now avoids some kinds of memory leaks by unregistering from the wrapped `PowerAdapter`
  while the `RecyclerView` is detached from the window. This means you no longer need to nullify the `RecyclerView`
  adapter manually to prevent leaks.
* Add static factory methods to `Binder` and `ViewHolderBinder`. These reduce `Binder` declaration boilerplate for simple use cases.
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