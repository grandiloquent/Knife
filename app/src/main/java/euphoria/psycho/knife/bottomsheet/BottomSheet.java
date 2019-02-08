package euphoria.psycho.knife.bottomsheet;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.knife.R;

public class BottomSheet implements View.OnClickListener {

    private static BottomSheet INSTANCE;
    private final Context mContext;
    private BottomSheetAdapter mBottomSheetAdapter;
    private View mContainer;
    private RecyclerView mRecyclerView;
    private BottomSheetDialog mDialog;

    private BottomSheet(Context context) {
        mContext = context;
    }

    public void showDialog() {
        if (mContainer == null) {
            mContainer = LayoutInflater.from(mContext).inflate(R.layout.bottomsheet_menu, null);
            mRecyclerView = mContainer.findViewById(R.id.recycler_view);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));
            mBottomSheetAdapter = new BottomSheetAdapter(this, new Pair[]{
                    Pair.create(R.drawable.ic_root_internal, "Internal Storage"),
                    Pair.create(R.drawable.ic_root_sdcard, "SD Card"),
                    Pair.create(R.drawable.ic_action_file_download, "Download")
            });
            mRecyclerView.setAdapter(mBottomSheetAdapter);

            BottomSheetDialog dialog = new BottomSheetDialog(mContext);
            dialog.setContentView(mContainer);

            mDialog = dialog;

        }
        mDialog.show();


    }

    public static BottomSheet instance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new BottomSheet(context);
        }
        return INSTANCE;
    }

    @Override
    public void onClick(View v) {

    }

    interface OnClickListener {
        void onClicked(Pair<Integer, String> item);
    }
}
