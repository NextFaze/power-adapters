# Async Data

# Motivation

Implementing a UI for presenting the contents of a remote collection, like a list of comments or products, requires
several important mechanics. Among them are:
* Perform requests asynchronously to avoid blocking the UI thread
* Presenting a loading indicator to give the user feedback on progress
* Allow the user to page through results
* Handle and present errors as they occur
* Dispatch change notifications to your adapter so your `RecyclerView` or `ListView` can present content changes in real-time.

Async Data aims to simplify this by encapsulating the above concerns into a single object: `Data<T>`

This library provides `Data` implementations that cover some of the most common use cases, as well as basic adapters
for connecting your `Data` instances to your `RecyclerView` or `ListView`.

# Usage

## Basic

The recommended usage pattern is to instantiate a `Data<T>` object in your retained `Fragment`:

```java
public final class ProductListFragment extends Fragment {

    private final Data<Product> mProducts = new ArrayData<>() {
        @NonNull
        @Override
        protected List<Product> load() throws Throwable {
            return mApi.getProducts();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
}
```

Now hook up your `Data<Product>` instance with your `RecyclerView`:

```java
@Override
public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mRecyclerView.setAdapter(new RecyclerDataAdapter(mProducts));
}

@Override
public void onDestroyView() {
    super.onDestroyView();
    // Must nullify adapter, otherwise after a config change, RecyclerView will
    // be retained by a strong reference chain of observers.
    mRecyclerView.setAdapter(null);
}
```

## Invalidating and reloading

At some stage you'll want to request a reload of the elements from the remote source. You can do this using `reload()`,
`refresh()`, or `invalidate()`. The behaviour of these methods differ slightly, but ultimately they all resulting your
items being reloaded from the source. See the `Data` javadoc for how they differ.

# DataLayout

`DataLayout` aids in presenting the various states of a `Data` instance, by hiding and showing contents, empty, error,
and loading child views.
It's a `RelativeLayout` subclass, and it works by accepting a `Data` instance, then registering to receive change
notifications. If the contents is empty, your marked empty view will be shown instead of the list view. If an error occurs,
the error view will be shown until a reload is triggered. `DataLayout` has several extension points to customize this behaviour
to suite the needs of your application.

Here's an example of how to declare a `DataLayout` in XML:

```xml
<com.nextfaze.asyncdata.widget.DataLayout
            xmlns:android="http://schemas.android.com/apk/res/android"
            xmlns:app="http://schemas.android.com/apk/res-auto"
            android:id="@+id/news_fragment_data_layout"
            android:layout_width="match_parent"
            android:layout_height="match_parent">

    <ListView
            android:id="@+id/list"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:layout_component="content"/>

    <ProgressBar
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            app:layout_component="loading"/>

    <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            app:layout_component="empty"
            android:text="No items!"/>

    <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            app:layout_component="error"
            android:textColor="#ffff0000"/>

</com.nextfaze.asyncdata.widget.DataLayout>
```

TODO: Explain principle of registering an observer at all times to indicate interest in Data contents

TODO: Transforming, filtering

TODO: RxJava module

TODO: Synergy with Power Adapters

TODO: Data is not the authority for your data. It's a UI-layer object. Don't use it as a database.

# Migrating from Databind

Earlier iterations of this library assigned different meaning to `clear()`, `invalidate()`, etc. Below is a conversion table:

|Databind              |Async Data      |Description|
|----------------------|----------------|-----------|
|`clear() `            |`reload()`      |Clears the existing elements, then reloads them asynchronously.|
|`invalidate()`        |`refresh()`     |Reloads elements asynchronously, without clearing them first.|
|`invalidateDeferred()`|`invalidate()`  |Flags the existing elements as invalidated, causing the to be reloaded asynchronously next time the `Data` is shown.|
|                      |`clear()`       |Removes all elements.|