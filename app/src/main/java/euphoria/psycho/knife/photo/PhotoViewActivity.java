package euphoria.psycho.knife.photo;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity to view the contents of an album.
 */
public class PhotoViewActivity extends AppCompatActivity
        implements PhotoViewController.ActivityInterface {

    private PhotoViewController mController;
    private ActionBarWrapper mActionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController = createController();

        mController.onCreate(savedInstanceState);
    }

    protected PhotoViewController createController() {
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
            mActionBar = new ActionBarWrapper(getSupportActionBar());
        }
        return mActionBar;
    }

}
