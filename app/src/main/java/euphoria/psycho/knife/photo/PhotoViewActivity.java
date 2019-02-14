package euphoria.psycho.knife.photo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class PhotoViewActivity extends FragmentActivity
        implements PhotoViewController.ActivityInterface {

    private PhotoViewController mController;
    private ActionBarWrapper mActionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController = createController();
        mController.onCreate(savedInstanceState);
    }

    public PhotoViewController createController() {
        return new PhotoViewController(this);
    }

    @Override
    public PhotoViewController getController() {
        return mController;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mController.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mController.onResume();
    }

    @Override
    protected void onPause() {
        mController.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mController.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mController.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!mController.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mController.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return mController.onCreateOptionsMenu(menu) || super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return mController.onPrepareOptionsMenu(menu) || super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mController.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mController.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public ActionBarInterface getActionBarInterface() {
        if (mActionBar == null) {
            mActionBar = new ActionBarWrapper(getActionBar());
        }
        return mActionBar;
    }

}
