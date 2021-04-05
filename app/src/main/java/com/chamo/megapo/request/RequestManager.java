package com.chamo.megapo.request;

import java.util.concurrent.LinkedBlockingQueue;

public class RequestManager {

    private static volatile RequestManager instance;
    //阻塞式队列
    private final LinkedBlockingQueue<BitmapRequest> requestQueue;
    //转发器管理
    private BitmapDispatcher[] dispatchers;

    private RequestManager() {
        requestQueue = new LinkedBlockingQueue<>();
        start();
    }

    public static RequestManager getInstance() {
        RequestManager manager = instance;
        if (manager == null) {
            synchronized (RequestManager.class) {
                if (manager == null) {
                    manager = new RequestManager();
                }
                instance = manager;
            }
        }
        return manager;
    }

    public void addBitmapRequest(BitmapRequest request) {
        if (!requestQueue.contains(request)) {
            requestQueue.add(request);
        }
    }

    public void start() {
        stop();
        int threadCount = Runtime.getRuntime().availableProcessors();
        dispatchers = new BitmapDispatcher[threadCount];
        for (int i = 0; i < dispatchers.length; i++) {
            BitmapDispatcher bitmapDispatcher = new BitmapDispatcher(requestQueue);
            bitmapDispatcher.start();
            dispatchers[i] = bitmapDispatcher;
        }
    }

    public void stop() {
        if (dispatchers != null && dispatchers.length > 0) {
            for (BitmapDispatcher bitmapDispatcher : dispatchers) {
                if (!bitmapDispatcher.isInterrupted()){
                    bitmapDispatcher.interrupt();
                }
            }
        }
    }
}
