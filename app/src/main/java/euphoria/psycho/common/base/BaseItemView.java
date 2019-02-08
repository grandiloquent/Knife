package euphoria.psycho.common.base;

public interface BaseItemView<T> {

    void bindView(T t);

    void initializeActionDelegate(BaseActionDelegate<T> delegate);
}
