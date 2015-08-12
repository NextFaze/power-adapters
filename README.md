Power Adapters
==============

This project uses semantic versioning
http://semver.org/


Databind Migration Notes
------------------------

### `DisposeableListAdapter`

The `DisposeableListAdapter` interface has been eliminated. You no longer need to `dispose` of adapters. Instead,
adapters keep track of external observers, and use a reference counting system to automatically unregister their own
internal observers once no external clients are observing.