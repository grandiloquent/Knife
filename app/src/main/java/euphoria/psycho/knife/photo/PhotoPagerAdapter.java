package euphoria.psycho.knife.photo;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import euphoria.psycho.common.Log;

public class PhotoPagerAdapter extends BaseFragmentPagerAdapter {
    private final Context mContext;
    private final List<ImageInfo> mImageInfos = new ArrayList<>();
    private final int mMaxScale;

    public PhotoPagerAdapter(Context context, FragmentManager fm, int maxScale) {
        super(fm);
        mContext = context;
        mMaxScale = maxScale;
    }


    @Override
    public int getCount() {
        return mImageInfos.size();
    }

    public void switchDatas(List<ImageInfo> imageInfos) {
        mImageInfos.clear();
        if (imageInfos != null)
            mImageInfos.addAll(imageInfos);
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {



        Intent intent = new Intent();

        boolean onlyShowSpinner = false;

        Intent bundle = new Intent();
        bundle.putExtra(PhotoManager.EXTRA_PHOTO_URI, mImageInfos.get(position).getPath());
        intent.putExtra(PhotoViewFragment.ARG_INTENT, intent);
        intent.putExtra(PhotoViewFragment.ARG_POSITION, position);
        intent.putExtra(PhotoViewFragment.ARG_SHOW_SPINNER, onlyShowSpinner);

        return PhotoViewFragment.newInstance(intent, position, onlyShowSpinner);
    }
}
