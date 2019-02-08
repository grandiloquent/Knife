package euphoria.psycho.knife.bottomsheet;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.common.widget.ChromeImageView;
import euphoria.psycho.knife.R;

public class BottomSheetAdapter extends RecyclerView.Adapter<BottomSheetAdapter.ViewHolder> {
    private Pair<Integer, String>[] mInfos;
    private BottomSheet.OnClickListener mOnClickListener;

    public BottomSheetAdapter(BottomSheet.OnClickListener listener, Pair<Integer, String>[] inofs) {
        mOnClickListener = listener;
        mInfos = inofs;
    }

    @Override
    public int getItemCount() {
        return mInfos.length;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pair<Integer, String> pair = mInfos[position];
        holder.iconView.setImageResource(pair.first);
        holder.title.setText(pair.second);
        holder.itemView.setOnClickListener(v -> {
            if (mOnClickListener != null) mOnClickListener.onClicked(pair);
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.bottomsheet_menu_item, parent, false));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ChromeImageView iconView;
        TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.icon_view);
            title = itemView.findViewById(R.id.title);
        }
    }
}
