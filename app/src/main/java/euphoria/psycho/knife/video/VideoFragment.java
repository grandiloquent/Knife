package euphoria.psycho.knife.video;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import euphoria.psycho.common.C;
import euphoria.psycho.knife.R;

public class VideoFragment extends Fragment {
    Toolbar mToolbar;
    VideoView mVideoView;

    void loadVideo() {
        Bundle bundle = getArguments();
        if (bundle == null) return;

        String key;
        String filePath = bundle.getString(C.EXTRA_FILE_PATH);

        mVideoView.setVideoPath(filePath);

    }

    public void onPrepared(MediaPlayer mp) {
        mVideoView.start();

    }

    public static void show(FragmentManager manager, String filePath) {

        FragmentTransaction transaction = manager.beginTransaction();
        VideoFragment fragment = new VideoFragment();
        Bundle bundle = new Bundle();
        bundle.putString(C.EXTRA_FILE_PATH, filePath);
        fragment.setArguments(bundle);
        transaction.replace(R.id.container, fragment);
        transaction.commitNowAllowingStateLoss();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mVideoView = view.findViewById(R.id.video_view);
        mToolbar = view.findViewById(R.id.toolbar);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        mVideoView.setOnPreparedListener(this::onPrepared);
        loadVideo();
    }

}
