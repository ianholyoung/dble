package com.actiontech.dble.buffer;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentMap;

/**
 * BufferPool
 *
 * @author Hash Zhang
 * @version 1.0
 * @time 12:19 2016/5/23
 */
public interface BufferPool {
    ByteBuffer allocate();

    ByteBuffer allocate(int size);

    void recycle(ByteBuffer theBuf);

    long capacity();

    long size();

    int getSharedOptsCount();

    int getChunkSize();

    ConcurrentMap<Long, Long> getNetDirectMemoryUsage();
}
