package euphoria.psycho.share.concurrent;

public interface FutureListener<T> {
    public void onFutureDone(Future<T> future);
}
