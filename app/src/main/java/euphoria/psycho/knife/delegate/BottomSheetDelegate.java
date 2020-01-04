package euphoria.psycho.knife.delegate;

import android.content.Intent;
import android.os.Environment;
import android.util.Pair;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import euphoria.psycho.common.FileUtils;
import euphoria.psycho.knife.DirectoryFragment;
import euphoria.psycho.knife.DocumentUtils;
import euphoria.psycho.knife.R;
import euphoria.psycho.knife.download.DownloadActivity;
import euphoria.psycho.knife.helpers.FileHelper;
import euphoria.psycho.knife.server.FileServerActivity;
import euphoria.psycho.knife.util.DialogUtils.DialogListener;

public class BottomSheetDelegate {
    private DirectoryFragment mFragment;

    public BottomSheetDelegate(DirectoryFragment fragment) {
        mFragment = fragment;
        initialize();
    }

    private void initialize() {

        mFragment.getBottomSheet().setOnClickListener(this::onClicked);
    }

    public void onClicked(Pair<Integer, String> item) {
        switch (item.first) {
            case R.drawable.ic_action_storage:
                mFragment.updateRecyclerView(Environment.getExternalStorageDirectory());
                break;
            case R.drawable.ic_action_sd_card:
                mFragment.updateRecyclerView(new File(FileUtils.getSDCardPath()));
                break;
            case R.drawable.ic_action_file_download:
                mFragment.updateRecyclerView(new File(Environment.getExternalStorageDirectory(), "Download"));
                break;
//            case R.drawable.ic_action_photo:
//                Intent pictureIntent = new Intent(mFragment.getContext(), PhotoViewActivity.class);
//                mFragment.getContext().startActivity(pictureIntent);
//                break;
            case R.drawable.ic_phonelink_blue_24px:
                Intent serverIntent = new Intent(mFragment.getContext(), FileServerActivity.class);
                mFragment.getContext().startActivity(serverIntent);
                break;
            case R.drawable.ic_file_download_blue_24px:
                Intent downloadIntent = new Intent(mFragment.getContext(), DownloadActivity.class);
                mFragment.getContext().startActivity(downloadIntent);
                break;
            case R.drawable.ic_create_new_folder_blue_24px:
                DocumentUtils.buildNewDirectoryDialog(mFragment.getContext(), new DialogListener<CharSequence>() {
                    @Override
                    public void cancel() {

                    }

                    @Override
                    public void ok(CharSequence charSequence) {
                        if (charSequence == null) return;
                        String name = FileHelper.getValidFilName(charSequence.toString(), ' ');
                        euphoria.common.Files.createDirectoryIfNotExists(new File(mFragment.getDirectory(), name.trim()));

                        mFragment.updateRecyclerView(false);

                    }
                });
                break;
        }
    }
}
