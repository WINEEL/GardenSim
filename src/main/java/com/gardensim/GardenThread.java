package com.gardensim;

/**
 * A simple wrapper thread class used for running garden-related tasks
 * with a given name.
 */
class GardenThread extends Thread {

    public GardenThread(Runnable task, String name) {
        super(task, name);
    }

    // No need to override run() since no custom behavior is added.
}
