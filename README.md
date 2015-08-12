Async Data
==========

This project uses semantic versioning
http://semver.org/

Databind Migration Notes
------------------------

### Clearing, reloading, refreshing:

|Databind            |Async Data    |Description|
|--------------------|--------------|-----------|
|clear()             |reload()      |Clears the existing elements, then reloads them asynchronously.|
|invalidate()        |refresh()     |Reloads elements asynchronously, without clearing them first.|
|invalidateDeferred()|invalidate()  |Flags the existing elements as invalidated, causing the to be reloaded asynchronously next time the `Data` is shown.|
|                    |clear()       |Removes all elements, in accordance with `List` contract.|