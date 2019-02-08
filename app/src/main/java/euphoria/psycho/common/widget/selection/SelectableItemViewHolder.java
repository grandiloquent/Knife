package euphoria.psycho.common.widget.selection;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * An ViewHolder for a {@link SelectableItemView}.
 *
 * @param <E> The type of the item associated with the {@link SelectableItemView}.
 */
public class SelectableItemViewHolder<E> extends RecyclerView.ViewHolder {
    private SelectableItemView<E> mItemView;

    /**
     * @param itemView The {@link SelectableItemView} to be held by this ViewHolder.
     * @param delegate The {@link SelectionDelegate} for the itemView.
     */
    @SuppressWarnings("unchecked")
    public SelectableItemViewHolder(View itemView, SelectionDelegate<E> delegate) {
        super(itemView);
        mItemView = (SelectableItemView<E>) itemView;
        mItemView.setSelectionDelegate(delegate);
    }

    /**
     * @param item The item to display in the {@link SelectableItemView} held by this
     *             {@link ViewHolder}.
     */
    public void displayItem(E item) {
        mItemView.setItem(item);
    }

    /**
     * @return The {@link SelectableItemView} held by this ViewHolder.
     */
    public SelectableItemView<E> getItemView() {
        return mItemView;
    }
}
