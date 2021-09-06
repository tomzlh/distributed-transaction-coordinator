package com.ops.sc.core.spi;


public interface TransMessageSPI {

    int resolveType();

    Class load();
}
