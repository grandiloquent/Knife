package euphoria.psycho.knife.download;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.common.Log;
import euphoria.psycho.common.base.BaseViewHolder;
import euphoria.psycho.common.widget.selection.SelectionDelegate;
import euphoria.psycho.knife.R;

public class DownloadAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private final List<DownloadInfo> mDownloadInfos;
    private final List<DownloadItemView> mViews;
    private SelectionDelegate<DownloadInfo> mSelectionDelegate;

    public DownloadAdapter(SelectionDelegate<DownloadInfo> delegate, DownloadFragment fragment) {
        mDownloadInfos = new ArrayList<>();
        mViews = new ArrayList<>();
        mSelectionDelegate = delegate;
    }


    public void removeItem(DownloadInfo downloadInfo) {
        int length = mDownloadInfos.size();
        for (int i = 0; i < length; i++) {
            if (downloadInfo._id == mDownloadInfos.get(i)._id) {
                mDownloadInfos.remove(i);

                notifyItemRemoved(i);
                break;
            }
        }
    }

    public void switchDatas(List<DownloadInfo> infos) {
        mDownloadInfos.clear();
        mDownloadInfos.addAll(infos);
        notifyDataSetChanged();
    }

    public void updateItem(DownloadInfo downloadInfo) {

        for (DownloadItemView view : mViews) {
            if (view.getItem()._id == downloadInfo._id) {
                view.updateProgress(downloadInfo);
                break;

            }
        }
    }
    public void fullUpdate(DownloadInfo downloadInfo) {

        Log.e("TAG/", "fullUpdate: " + "downloadInfo = " + downloadInfo
                );

        Log.e("TAG/DownloadAdapter", "fullUpdate: "+mViews.size());

        for (DownloadItemView view : mViews) {

            Log.e("TAG/DownloadAdapter", "fullUpdate: "+view.getItem()._id);

            if (view.getItem()._id == downloadInfo._id) {
                view.displayItem(downloadInfo);
                
                Log.e("TAG/DownloadAdapter", "fullUpdate: ");

                break;

            }
        }
    }

    @Override
    public int getItemCount() {
        return mDownloadInfos.size();
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {

        ((DownloadItemView) holder.itemView).displayItem(mDownloadInfos.get(position));
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DownloadItemView downloadItemView = (DownloadItemView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download_list, parent, false);
        downloadItemView.setSelectionDelegate(mSelectionDelegate);
        mViews.add(downloadItemView);
        return new BaseViewHolder(downloadItemView);
    }
}
