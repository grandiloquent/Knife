package euphoria.psycho.common.pool;

public interface FutureListener<T> {
    public void onFutureDone(Future<T> future);
}
