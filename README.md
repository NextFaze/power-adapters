# Power Data

# Motivation

Implementing a UI for presenting the contents of a remote collection, like a list of comments or products, requires
several different mechanics. Among them are:
* Perform requests asynchronously to avoid blocking the UI thread
* Presenting a loading indicator to give the user feedback on progress
* Allow the user to page through results
* Handle and present errors as they occur
* Dispatch change notifications to your adapter so your `RecyclerView` or `ListView` can react to content changes

Power Data aims to simplify this by encapsulating the above concerns into a single object: `Data<T>`. In doing so, it allows
you to retain one object when a config change occurs, like an orientation change. This way you don't need to reload or
parcel/unparcel all of your list results when that occurs. The `Data<T>` object comprises much of the repetitive
asynchronous UI "glue" code you'd otherwise have to write (and debug) yourself.

This library provides `Data` implementations that cover some of the most common use cases, as well as basic adapters
for connecting your `Data` instances to your `RecyclerView` or `ListView`.

# Download

Get it from Maven Central, using Gradle:

```groovy
compile 'com.nextfaze.powerdata:power-data:0.4.0'
```

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
`refresh()`, or `invalidate()`. The behaviour of these methods differ slightly, but ultimately they all result in your
items being reloaded from the source. See the `Data` javadoc for how they differ.

## DataLayout

`DataLayout` aids in presenting the various states of a `Data` instance, by hiding and showing contents, empty, error,
and loading child views.
It's a `RelativeLayout` subclass, and it works by accepting a `Data` instance, then registering to receive change
notifications. If the contents is empty, your marked empty view will be shown instead of the list view. If an error occurs,
the error view will be shown until a reload is triggered. `DataLayout` has several extension points to customize this behaviour
to suite the needs of your application.

Here's an example of how to declare a `DataLayout` in XML:

```xml
<com.nextfaze.powerdata.widget.DataLayout
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

</com.nextfaze.powerdata.widget.DataLayout>
```

Now you need to connect to your `DataLayout` and `ListView` in Java code:

```java
@Override
public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mListView.setAdapter(new DataAdapter(mProducts));
    mDataLayout.setData(mProducts);
}
```

## RxJava Module

An RxJava module is provided: `power-data-rx`. This is a simple adapter library that provides `Observable`s for properties of `Data`:

```java
RxData.changes(mProducts).subscribe(new Action1<Change>() {
    @Override
    public void call(Change change) {
        ...
    }
});
```

## Data Views

`Data` instances can be represented as a view, much like a relational database. The `Datas` utility class provides
static factory methods for wrapping an existing `Data` object, and providing a filtered or transformed view of its contents.

```java
Data<String> names = ...
Data<Integer> lengths = Datas.transform(names, new Function<String, Integer>() {
    @NonNull
    @Override
    public Integer apply(@NonNull String name) {
        return name.length;
    }
});
```

```java
Data<Post> allPosts = ...
Data<Post> todaysPosts = Datas.filter(names, new Predicate<Post>() {
    @Override
    public boolean apply(@NonNull Post post) {
        return isToday(post.getDate());
    }
});
```

# Integration with Power Adapters

This library was designed to work best with its sister library, [Power Adapters][power-adapters]. Using both, you can
present lists of varying items types, with independently-loading asynchronous data sources. Power Adapters provides
the tools for showing loading indicators, empty messages, headers and footers, and more.

# Migrating from Databind

Earlier iterations of this library assigned different meaning to `clear()`, `invalidate()`, etc. Below is a conversion table:

|Databind              |Power Data      |Description|
|----------------------|----------------|-----------|
|`clear() `            |`reload()`      |Clears the existing elements, then reloads them asynchronously.|
|`invalidate()`        |`refresh()`     |Reloads elements asynchronously, without clearing them first.|
|`invalidateDeferred()`|`invalidate()`  |Flags the existing elements as invalidated, causing the to be reloaded asynchronously next time the `Data` is shown.|
|                      |`clear()`       |Removes all elements.|


# License

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 [power-adapters]: https://github.com/NextFaze/power-adapters