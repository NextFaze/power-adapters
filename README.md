<img align="right" src="../gh-pages/showcase.gif">

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**

- [Power Adapters](#power-adapters)
- [Feature Summary](#feature-summary)
- [Usage](#usage)
  - [Basic](#basic)
  - [RxJava](#rxjava)
  - [Kotlin](#kotlin)
  - [Adapter Composition](#adapter-composition)
  - [Headers and Footers](#headers-and-footers)
  - [Data Type Binding](#data-type-binding)
    - [Binder](#binder)
    - [Mapper](#mapper)
  - [Conversion](#conversion)
  - [Nested Adapters](#nested-adapters)
  - [Asynchronous Data Loading](#asynchronous-data-loading)
    - [Basic Data Usage](#basic-data-usage)
    - [Invalidating and Reloading](#invalidating-and-reloading)
    - [DataLayout](#datalayout)
    - [RxJava Module](#rxjava-module)
    - [Data Views](#data-views)
  - [Samples](#samples)
- [Build](#build)
- [License](#license)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# Power Adapters

Presenting large data sets efficiently can be a challenging part of Android development. It gets more complicated as we
begin to handle edge cases and add additional decorations like headers. We also often find ourselves repeating
undesirable boilerplate as we write adapters for each data source. In addition, Android doesn't provide a clean
object-oriented, reusable way of presenting collections of multiple types.

# Feature Summary

This library provides the following features:

* Present **multiple data types** within an adapter in a **type-safe** manner
* **Concatenate** multiple adapters together
* Show **headers** and **footers**
* Show a **loading indicator** to indicate a loading state
* Show an **empty item** to indicate an empty underlying data set
* Add **dividers** in between items of an existing adapter
* Show an adapter or item range only when a **condition** evaluates to `true`
* Present **nested** adapters, a powerful substitute for `ExpandableListView` without any limitation of nesting level
* Load from remote or slow data sources **asynchronously**
* Backed up by **unit tests**, verifying the correct notifications are issued and state maintained
* Minimal **dependencies**; doesn't include any unnecessary transitive dependencies
* All adapters issue the correct insertion/removal/change notifications needed for full `RecyclerView` animation support
* Kotlin extension modules, which add idiomatic Kotlin APIs
* RxJava extension modules, adding easy integration with `Observable`s, etc

Power adapters are compatible with the following collection view classes:
* `android.support.v7.widget.RecyclerView`
* `android.widget.ListView`
* `android.widget.GridView`
* `android.support.v4.view.ViewPager`
* Any other view that accepts a `android.widget.Adapter`

# Usage

Get it from Maven Central, using Gradle:

```groovy
implementation 'com.nextfaze.poweradapters:power-adapters:0.21.0'
implementation 'com.nextfaze.poweradapters:power-adapters-recyclerview-v7:0.21.0'
```

## Basic

```java
// Declare a binder for your item type
class TweetHolder extends ViewHolder {

    TextView textView;

    TweetHolder(View view) {
        super(view);
        textView = (TextView) view.findViewById(R.id.text);
    }
}

Binder<Tweet, View> tweetBinder = 
        ViewHolderBinder.create(R.layout.tweet, TweetHolder::new, (container, tweet, tweetHolder, holder) -> {
    tweetHolder.textView.setText(tweet.getText());
});

// Construct your "core" adapter
ListBindingAdapter<Tweet> tweetsAdapter = new ListBindingAdapter<>(tweetBinder);

// Assign to your RecyclerView
recyclerView.setAdapter(RecyclerPowerAdapters.toRecyclerAdapter(tweetsAdapter));
```

## RxJava

RxJava modules are available. Simply append `-rxjava2` to get the RxJava module:

```groovy
implementation 'com.nextfaze.poweradapters:power-adapters-rxjava2:0.21.0'
implementation 'com.nextfaze.poweradapters:power-adapters-data-rxjava2:0.21.0'
```

## Kotlin

Kotlin modules are also provided for most modules. Append `-kotlin` to get the Kotlin module:

```groovy
implementation 'com.nextfaze.poweradapters:power-adapters-kotlin:0.21.0'
implementation 'com.nextfaze.poweradapters:power-adapters-data-kotlin:0.21.0'
implementation 'com.nextfaze.poweradapters:power-adapters-rxjava2-kotlin:0.21.0'
implementation 'com.nextfaze.poweradapters:power-adapters-data-rxjava2-kotlin:0.21.0'
implementation 'com.nextfaze.poweradapters:power-adapters-recyclerview-v7-kotlin:0.21.0'
```

Some of the Kotlin APIs include:

- Top-level factory functions:
    ```kotlin
    val data = data { api.getPosts() }
    ```
    ```kotlin
    val data = cursorData({ db.getUsers() }, ::User)
    ```
    ```kotlin
    val header = viewFactory<TextView>(R.layout.header) {
        text = "News"
    }
    ```
- Extension functions:
    ```kotlin
    recyclerView.adapter = myPowerAdapter.toRecyclerAdapter()
    ```
- `PowerAdapter` and `Data` Factory methods: `adapterOf()`, `dataOf()`
- `Binder` factory methods:
    ```kotlin
    val binder = binder<Item, ItemView>(R.layout.item) { container, item, holder ->
        title = item.name
        imageUri = item.imageUri
    }
    ```
- Operator overloads:
    ```kotlin
    adapter.showOnlyWhile(empty and !anotherThing)
    ```
    ```
    val adapter = itemsAdapter + anotherAdapter
    ```
    ```kotlin
    data += dataObserver { updateViews() }
    ```
- Property delegates:
    ```kotlin
    val condition = ValueCondition()
    var enabled by condition
    val adapter = myAdapter.showOnlyWhile(condition)
    // Reassign property to control visibility of adapter
    enabled = false
    ```
- Type-safe builder: 
    ```kotlin
    adapter {
        layoutResource(R.layout.header)
        +myItemsAdapter
        layoutResource(R.layout.footer)
    }
    ```

## Adapter Composition

Power Adapters can be composed by using the fluent chaining methods.
For example, say you want to present a list of tweets, with a loading indicator, but show an empty message when there
are no tweets, you can write the following:

```java
PowerAdapter adapter = tweetsAdapter
    .limit(10) // Only show up to 10 tweets
    .append(
        // Show empty item while no tweets have loaded
        asAdapter(R.layout.tweets_empty_item).showOnlyWhile(noTweets()),
        // Show loading indicator while loading
        asAdapter(R.layout.loading_indicator).showOnlyWhile(tweetsAreLoading())
    )
recyclerView.setAdapter(RecyclerPowerAdapters.toRecyclerAdapter(adapter));
```

This lets you write a simple `TweetAdapter` class, the only responsibility of which is to present tweets. By using
`PowerAdapter.append` as such, the `TweetAdapter` need not be modified, and can be potentially reused elsewhere more
easily. The use of `showOnlyWhile` applies a condition to the empty footer item, so it remains hidden unless the
underlying list of tweets is empty.

## Headers and Footers

Headers and footers can be added using `prepend` and `append`:

```java
// Prepend a header view.
PowerAdapter adapter = tweetAdapter.prepend(R.layout.header);
```

```java
// Append a footer view.
PowerAdapter adapter = tweetAdapter.append(R.layout.footer);
```

## Data Type Binding

Included in Power Adapters is the ability to bind elements in your data set to views in a reusable, readable, and
type-safe manner.

### Binder

The primary class needed to achieve this is a `Binder`. The responsibilities of a `Binder` include:

* Construct a `View` to be bound, and re-used by the adapter/recycler view
* Bind an object and/or data set index to the `View`

Multiple types of commonly required binders are supplied. If you prefer the widely used view holder pattern, use
a `ViewHolderBinder`:

```java
Binder<BlogPost, View> blogPostBinder = 
        ViewHolderBinder.create(R.layout.post, BlogPostHolder::new, (container, blogPost, blogPostHolder, holder) -> {
    blogPostHolder.labelView.setText("Blog: " + blogPost.getTitle());
});

class BlogPostHolder extends ViewHolder {

    TextView labelView;

    BlogPostHolder(View view) {
        super(view);
        labelView = (TextView) view.findViewById(android.R.id.text1);
    }
}
```

If you use custom views for each of your data models, use `Binder.create`. It takes a layout resource or a `ViewFactory`.
The view returned by the `ViewFactory` is passed to subsequent `bindView` calls, saving you from writing a separate `ViewHolder`.

For example:

```java
Binder<Tweet, TweetView> tweetBinder = Binder.create(R.layout.tweet_item, ((container, sample, v, holder) -> {
    v.setTweet(tweet);
    v.setOnClickListener(v -> onTweetClick(tweet));
}))
```


### Mapper

The examples above have all dealt with a single item type, and so there has only been a single `Binder`. When you want your list to contain multiple items, a `Mapper` is consulted to determine which `Binder` to use for presenting each particular item. Typically you'll use `MapperBuilder` to declaratively assign your model classes to
binders:

```java
Mapper mapper = new MapperBuilder()
    .bind(Tweet.class, new TweetBinder())
    .bind(Ad.class, new AdBinder())
    .bind(Video.class, new VideoBinder())
    .build();
ListBindingAdapter<Object> adapter = new ListBindingAdapter<>(mapper);
adapter.add(new Tweet());
adapter.add(new Ad());
adapter.add(new Video());
```

## Conversion

PowerAdapter is designed to be used with different collection view implementations, so a final step is converting it to implement the expected adapter interface. This would usually be done as soon as the collection view is created, say in `onViewCreated`:

```java
recyclerView.setAdapter(toRecyclerAdapter(powerAdapter));
```

The following conversion methods are provided:

|Collection View    |Converter                                  |Extension Module                                           |
|:------------------|------------------------------------------:|:---------------------------------------------------------:|
|`ListView`         |            `PowerAdapters.toListAdapter()`|None                                                       |
|`RecyclerView`     |`RecyclerPowerAdapters.toRecyclerAdapter()`|`power-adapters-recyclerview-v7`                           |
|`ViewPager`        |`SupportPowerAdapters.toPagerAdapter()`    |`power-adapters-support-v4`                                |

## Nested Adapters

The `TreeAdapter` class allows you to present hierarchical data structures with no intrinsic depth limit. Each layer is
comprised of just another adapter - your children can themselves can be `TreeAdapter`s!

```java
PowerAdapter rootAdapter = new FileAdapter(new File("/"));
TreeAdapter treeAdapter = new TreeAdapter(rootAdapter, position -> {
    // Create a child adapter for this position in the root data set.
    // Can be another TreeAdapter!
   return createChildAdapter(position);
});
treeAdapter.setExpanded(15, true);
```

## Asynchronous Data Loading

Implementing a UI for presenting the contents of a remote collection, like a list of comments or products, requires
several different mechanics. Among them are:
* Perform requests asynchronously to avoid blocking the UI thread
* Presenting a loading indicator to give the user feedback on progress
* Allow the user to page through results
* Handle and present errors as they occur
* Dispatch change notifications to your adapter so your `RecyclerView` or `ListView` can react to content changes

The `power-adapters-data` extension module aims to simplify this by encapsulating the above concerns into a single
object: `Data<T>`. In doing so, it allows you to retain one object when a config change occurs, like an orientation
change. This way you don't need to reload or parcel/unparcel all of your list results when that occurs. The `Data<T>`
object comprises much of the repetitive asynchronous UI "glue" code you'd otherwise have to write (and debug) yourself.

```groovy
implementation 'com.nextfaze.poweradapters:power-adapters-data:0.21.0'
```

### Basic Data Usage

The recommended usage pattern is to instantiate a `Data<T>` object in your retained `Fragment`. The `Data.fromList` 
factory method supports the simplest use case, fetching a list of items asynchronously:

```java
public final class ProductListFragment extends Fragment {

    private final Data<Product> products = Data.fromList(() -> api.getProducts());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment so we don't need to reload the products after a config change
        setRetainInstance(true);
    }
}
```

Now hook up your `Data<Product>` instance and a `Binder` with your `RecyclerView`:

```java
@Override
public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    PowerAdapter adapter = new DataBindingAdapter(products, productBinder);
    recyclerView.setAdapter(RecyclerPowerAdapters.toRecyclerAdapter(adapter));
}
```

### Invalidating and Reloading

At some stage you'll want to request a reload of the elements from the remote source. You can do this using `reload()`,
`refresh()`, or `invalidate()`. The behaviour of these methods differ slightly, but ultimately they all result in your
items being reloaded from the source. See the `Data` javadoc for how they differ.

### DataLayout

`DataLayout` aids in presenting the various states of a `Data` instance, by hiding and showing contents, empty, error,
and loading child views.
It's a `RelativeLayout` subclass, and it works by accepting a `Data` instance, then registering to receive change
notifications. If the contents are empty, your marked empty view will be shown instead of the list view. If an error 
occurs, the error view will be shown until a reload is triggered. `DataLayout` has several extension points to customize
 this behaviour to suite the needs of your application.

Here's an example of how to declare a `DataLayout` in XML. Notice the `layout_component` attributes:

```xml
<com.nextfaze.poweradapters.data.widget.DataLayout
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

</com.nextfaze.poweradapters.data.widget.DataLayout>
```

The `DataLayout` observes state changes of the `Data` to know when to update the view visibility. Connecting to your 
`DataLayout` and `RecyclerView` in Java code:

```java
@Override
public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    PowerAdapter adapter = new DataBindingAdapter(products, productBinder);
    listView.setAdapter(RecyclerPowerAdapters.toRecyclerAdapter(adapter));
    dataLayout.setData(products);
}
```

### RxJava Module

An RxJava module is provided: `power-adapters-data-rxjava2`. This is a simple adapter library that provides `Observable`s
for properties of `Data`:

```java
RxData.inserts(products).subscribe(event -> handleProductInsert(event));
```

### Data Views

`Data` has fluent chaining methods for providing filtered, transformed, or sorted views of its contents:

```java
Data<String> names = ...
Data<Integer> lengths = names.transform(name -> name.length);
```

```java
Data<Post> allPosts = ...
Data<Post> todaysPosts = names.filter(post -> isToday(post.getDate()));
```

## Samples

Check the included sample project for a range of usage pattern examples.

# Build

Building instructions:

```bash
$ git clone git@github.com:NextFaze/power-adapters.git
$ cd power-adapters
$ ./gradlew clean build

```

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
