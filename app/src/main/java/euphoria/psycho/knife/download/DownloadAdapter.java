package euphoria.psycho.knife.download;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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

    public void updateItem(DownloadInfo downloadInfo) {

            for (DownloadItemView view : mViews) {
                if (view.getItem()._id == downloadInfo._id) {

                    switch(downloadInfo.status){
                        case DownloadStatus.STARTED:{
                            break;
                        }
                        case DownloadStatus.IN_PROGRESS:{
                            break;
                        }
                        case DownloadStatus.PAUSED:{
                            break;
                        }
                        case DownloadStatus.COMPLETED:{
                            break;
                        }
                        case DownloadStatus.FAILED:{
                            break;
                        }
                        case DownloadStatus.RETIRED:{
                            break;
                        }
                        case DownloadStatus.PENDING:{
                            break;
                        }
                    }


                    if (downloadInfo.status == DownloadStatus.IN_PROGRESS) {
                        view.updateProgress(downloadInfo);
                    } else {

                        view.displayItem(downloadInfo);
                    }
                    break;

                }
            }
    }

    public void switchDatas(List<DownloadInfo> infos) {
        mDownloadInfos.clear();
        mDownloadInfos.addAll(infos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DownloadItemView downloadItemView = (DownloadItemView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download_list, parent, false);
        downloadItemView.setSelectionDelegate(mSelectionDelegate);
        mViews.add(downloadItemView);
        return new BaseViewHolder(downloadItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {

        ((DownloadItemView) holder.itemView).displayItem(mDownloadInfos.get(position));
    }

    @Override
    public int getItemCount() {
        return mDownloadInfos.size();
    }
}
