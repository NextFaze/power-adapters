package com.nextfaze.poweradapters;

import lombok.NonNull;

enum GetCall {
    ITEM_ID {
        @Override
        void get(@NonNull PowerAdapter adapter, int position) {
            adapter.getItemId(position);
        }
    },
    ITEM_VIEW_TYPE {
        @Override
        void get(@NonNull PowerAdapter adapter, int position) {
            adapter.getItemViewType(position);
        }
    },
    ENABLED {
        @Override
        void get(@NonNull PowerAdapter adapter, int position) {
            adapter.isEnabled(position);
        }
    };

    abstract void get(@NonNull PowerAdapter adapter, int position);
}
