package euphoria.psycho.knife;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.common.base.BaseViewHolder;
import euphoria.psycho.common.widget.selection.SelectionDelegate;

public class DocumentsAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private static final String TAG = "TAG/" + DocumentsAdapter.class.getSimpleName();
    private DocumentActionDelegate mDelegate;
    private List<DocumentInfo> mInfos = new ArrayList<>();
    private SelectionDelegate mSelectionDelegate;
    //private List<DocumentView> mViews = new ArrayList<>();

    public DocumentsAdapter(DocumentActionDelegate delegate, SelectionDelegate selectionDelegate) {
        mDelegate = delegate;
        mSelectionDelegate = selectionDelegate;

    }

//    private String dump() {
//        StringBuilder sb = new StringBuilder();
//        for (DocumentView view : mViews) {
//            sb.append(view.getItem().getFileName()).append('\n');
//        }
//        return sb.toString();
//    }

    public void onSelectionStateChange(boolean selectionEnabled) {

    }

    public void removeItem(DocumentInfo documentInfo) {
        int length = mInfos.size();
        for (int i = 0; i < length; i++) {

            if (mInfos.get(i).getPath().equals(documentInfo.getPath())) {
                mInfos.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

//    public void updateItem(DocumentInfo documentInfo) {
//        for (DocumentView view : mViews) {
//            if (view.getItem().getFileName().equals(documentInfo.getFileName())) {
//                view.setItem(documentInfo);
//                return;
//            }
//        }
//
//
//    }

    public void switchDataSet(List<DocumentInfo> infos) {
        mInfos.clear();

        if (infos != null) {
            mInfos.addAll(infos);
        }
        //mViews.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {


        return mInfos.size();
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        DocumentView itemView = (DocumentView) holder.itemView;
        itemView.initializeActionDelegate(mDelegate, mSelectionDelegate);
        itemView.setItem(mInfos.get(position));

    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DocumentView view = (DocumentView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_document_list, parent, false);
        // mViews.add(view);

        return new BaseViewHolder(view);
    }
}
