package com.nextfaze.powerdata.rx;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@EqualsAndHashCode(doNotUseGetters = true)
@Accessors(prefix = "m")
public final class Change {

    @NonNull
    private final Kind mKind;

    private final int mPosition;

    private final int mFromPosition;

    private final int mToPosition;

    private final int mCount;

    @NonNull
    public static Change newChange(int position, int count) {
        return new Change(Kind.CHANGE, position, position, position, count);
    }

    @NonNull
    public static Change newInsert(int position, int count) {
        return new Change(Kind.INSERT, position, position, position, count);
    }

    @NonNull
    public static Change newRemove(int position, int count) {
        return new Change(Kind.REMOVE, position, position, position, count);
    }

    @NonNull
    public static Change newMove(int fromPosition, int toPosition, int count) {
        return new Change(Kind.MOVE, fromPosition, fromPosition, toPosition, count);
    }

    Change(@NonNull Kind kind, int position, int fromPosition, int toPosition, int count) {
        mKind = kind;
        mPosition = position;
        mFromPosition = fromPosition;
        mToPosition = toPosition;
        mCount = count;
    }

    public enum Kind {
        CHANGE, INSERT, REMOVE, MOVE
    }
}
