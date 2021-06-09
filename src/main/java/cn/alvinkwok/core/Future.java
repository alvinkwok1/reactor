package cn.alvinkwok.core;

public interface Future<V>{
    boolean isSuccess();

    boolean isDone();

    Throwable cause();

    Future<V> addListener(GenericFutureListener<? extends Future<? super V>> listener);

    Future<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener);

    Future<V> sync() throws Throwable;

    Future<V> await() throws InterruptedException;

    V getNow();
}
