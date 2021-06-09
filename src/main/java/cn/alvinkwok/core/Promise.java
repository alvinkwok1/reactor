package cn.alvinkwok.core;

public interface Promise<V> extends Future<V> {

    Promise<V> setSuccess(V result);

    Promise<V> setFailure(Throwable cause);
}
