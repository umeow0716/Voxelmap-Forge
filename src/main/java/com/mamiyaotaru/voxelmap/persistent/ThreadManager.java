// 
// Decompiled by Procyon v0.6.0
// 

package com.mamiyaotaru.voxelmap.persistent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadManager
{
    static final int concurrentThreads;
    static final LinkedBlockingQueue<Runnable> queue;
    public static ThreadPoolExecutor executorService;
    
    public static void emptyQueue() {
        for (final Runnable runnable : ThreadManager.queue) {
            if (runnable instanceof FutureTask) {
                ((FutureTask<?>) runnable).cancel(false);
            }
        }
        ThreadManager.executorService.purge();
    }
    
    static {
        concurrentThreads = Math.min(Math.max(Runtime.getRuntime().availableProcessors() - 1, 1), 4);
        queue = new LinkedBlockingQueue<Runnable>();
        (ThreadManager.executorService = new ThreadPoolExecutor(ThreadManager.concurrentThreads, ThreadManager.concurrentThreads, 0L, TimeUnit.MILLISECONDS, ThreadManager.queue)).setThreadFactory(new NamedThreadFactory("Voxelmap WorldMap Calculation Thread"));
    }
    
    private static class NamedThreadFactory implements ThreadFactory
    {
        private final String name;
        private final AtomicInteger threadCount;
        
        public NamedThreadFactory(final String name) {
            this.threadCount = new AtomicInteger(1);
            this.name = name;
        }
        
        @Override
        public Thread newThread(final Runnable runnable) {
            return new Thread(runnable, this.name + " " + this.threadCount.getAndIncrement());
        }
    }
}
