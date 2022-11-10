package com.schwarz.crystalapi;

import org.jetbrains.annotations.Nullable;

public interface TypeConversion {

    @Nullable
    Object write(@Nullable Object value);

    @Nullable
    Object read(@Nullable Object value);

}
