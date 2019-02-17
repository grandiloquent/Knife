package euphoria.psycho.knife.photo;

import android.content.Context;
import android.content.Intent;
import android.graphics.ImageDecoder;

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

    public ImageInfo getImageInfo(int position) {
        if (position < mImageInfos.size())
            return mImageInfos.get(position);
        else
            return null;
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
        ImageInfo imageInfo = mImageInfos.get(position);

        intent.putExtra(PhotoManager.EXTRA_PHOTO_URI, imageInfo.getPath());


        return PhotoViewFragment.newInstance(intent, imageInfo.getId(), position, onlyShowSpinner);
    }
}
