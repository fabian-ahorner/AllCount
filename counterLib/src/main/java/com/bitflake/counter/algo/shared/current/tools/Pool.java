package com.bitflake.counter.algo.shared.current.tools;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by fahor on 05/09/2016.
 */
public class Pool<T> {
    private final Factory<T> factory;
    private Queue<T> particles = new LinkedList();
    private RecycleListener<T> recycleListener;

    public interface Factory<T> {
        T createNew();
    }

    public interface RecycleListener<T> {
        void onRecycle(T p);
    }

    public Pool(Factory f) {
        this.factory = f;
    }

    public Pool(Factory<T> f, int size) {
        this(f);
        for (int i = 0; i < size; i++) {
            particles.add(f.createNew());
        }
    }

    public T take() {
        if (particles.isEmpty())
            return factory.createNew();
        return particles.poll();
    }

    public void recycle(T p) {
        if (recycleListener != null)
            recycleListener.onRecycle(p);
        particles.add(p);
    }

    public void recycleAll(List<T> particles) {
        for (T p : particles) {
            recycle(p);
        }
    }

    public void setOnRecycleListener(RecycleListener<T> l) {
        this.recycleListener = l;
    }
}
