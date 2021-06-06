package core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class DefaultPromise<V> implements Promise<V> {

    private List<EventListener> listeners;

    private short waiters;

    private boolean notifyingListeners;

    private volatile Object result;

    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<DefaultPromise, Object> RESULT_UPDATER =
        AtomicReferenceFieldUpdater.newUpdater(DefaultPromise.class, Object.class, "result");

    private static final Object SUCCESS = new Object();
    private static final Object UNCANCELLABLE = new Object();

    private EventLoop eventLoop;

    public DefaultPromise(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    private synchronized boolean checkNotifyWaiters() {
        if (waiters > 0) {
            notifyAll();
        }
        return listeners != null;
    }

    @Override
    public boolean isSuccess() {
        return result != null && result != UNCANCELLABLE;
    }

    @Override
    public Throwable cause() {
        return cause0(result);
    }

    @Override
    public Promise<V> addListener(GenericFutureListener<? extends Future<? super V>> listener) {
        synchronized (this) {
            addListener0(listener);
        }
        if (isDone()) {
          //  ReactorLogger.info("from add");
            notifyListeners();
        }

        return this;
    }

    @Override
    public Promise<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener) {
        synchronized (this) {
            removeListener0(listener);
        }
        return this;
    }


    @Override
    public Promise<V> sync() throws InterruptedException {
        await();
        return this;
    }

    @Override
    public Promise<V> await() throws InterruptedException {
        if (isDone()) {
            return this;
        }
        if (Thread.interrupted()) {
            throw new InterruptedException(toString());
        }
        synchronized (this) {
            while (!isDone()) {
                incWaiters();
                try {
                    wait();
                } finally {
                    decWaiters();
                }
            }
        }
        return this;
    }

    @Override
    public V getNow() {
        return null;
    }

    @Override
    public Promise<V> setSuccess(V result) {
        if (setSuccess0(result)) {
            return this;
        }
        throw new IllegalStateException("complete already: " + this);
    }

    @Override
    public Promise<V> setFailure(Throwable cause) {
        if (setValue0(cause)) {
            return this;
        }
        throw new IllegalStateException("complete already: " + this, cause);
    }

    @Override
    public boolean isDone() {
        return isDone0(result);
    }

    private boolean setSuccess0(V result) {
        return setValue0(result == null ? SUCCESS : result);
    }

    private static boolean isDone0(Object result) {
        return result != null && result != UNCANCELLABLE;
    }

    private boolean setValue0(Object objResult) {
        if (RESULT_UPDATER.compareAndSet(this, null, objResult) ||
            RESULT_UPDATER.compareAndSet(this, UNCANCELLABLE, objResult)) {
            if (checkNotifyWaiters()) {
//                ReactorLogger.info("from success");
                notifyListeners();
            }
            return true;
        }
        return false;
    }


    private void notifyListeners() {
        safeExecute(getEventLoop(), () -> notifyListenersNow());
    }

    private void notifyListenersNow() {
        List<EventListener> ls = this.listeners;
        synchronized (this) {
            this.notifyingListeners = true;
            this.listeners = null;
        }
        if (ls == null) {
            return;
        }

        for (;;) {
            for (int i=0;i<ls.size() ;i++ ) {
                notifyListener0(this, (GenericFutureListener<?>) ls.get(i));
            }
            synchronized (this) {
                if (this.listeners == null) {
                    // Nothing can throw from within this method, so setting notifyingListeners back to false does not
                    // need to be in a finally block.
                    notifyingListeners = false;
                    return;
                }
                ls = this.listeners;
                this.listeners = null;
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void notifyListener0(Future future, GenericFutureListener l) {
        try {
            l.operationComplete(future);
        } catch (Throwable t) {
            ReactorLogger.warn("An exception was thrown by " + l.getClass().getName() + ".operationComplete()", t);
        }
    }

    private static void safeExecute(EventLoop executor, Runnable task) {
        try {
            executor.execute(task);
        } catch (Throwable t) {
            ReactorLogger.warn("Failed to submit a listener notification task. Event loop shut down?", t);
        }
    }

    private void addListener0(GenericFutureListener<? extends Future<? super V>> listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);
    }

    private void removeListener0(GenericFutureListener<? extends Future<? super V>> listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    private Throwable cause0(Object result) {
        if (result != null && result instanceof Throwable) {
            return (Throwable) result;
        }
        return null;
    }

    private void incWaiters() {
        if (waiters == Short.MAX_VALUE) {
            throw new IllegalStateException("too many waiters: " + this);
        }
        ++waiters;
    }

    private void decWaiters() {
        --waiters;
    }

    private void rethrowIfFailed() throws Throwable {
        Throwable cause = cause();
        if (cause == null) {
            return;
        }
        throw cause;
    }

    protected EventLoop getEventLoop() {
        return null;
    }

}
