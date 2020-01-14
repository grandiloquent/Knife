package euphoria.psycho.knife;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

public class Settings {

    private static Settings sSettings;

    private SharedPreferences mPreferences;


    public Settings(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getRecentDirectory() {
        return mPreferences.getString("directory", Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    public void setRecentDirectory(String path) {
        mPreferences.edit().putString("directory", path).apply();
    }

    public int getScrollY() {
        return mPreferences.getInt("scroll_y", 0);
    }

    public void setScrollY(int value) {
        mPreferences.edit().putInt("scroll_y", value);
    }

    public int getSortBy() {
        return mPreferences.getInt("sort_by", 0);
    }

    public void setSortBy(int value) {
        mPreferences.edit().putInt("sort_by", value).apply();
    }

    public static Settings initialize(Context context) {
        if (sSettings == null) {
            sSettings = new Settings(context);
        }
        return sSettings;
    }

    public static Settings instance() {
        if (sSettings == null) {
            throw new NullPointerException();
        }
        return sSettings;
    }

    public boolean isActionCalculateDirectory() {
        return mPreferences.getBoolean("action_calculate_directory", false);
    }

    public boolean isActionCombineFiles() {
        return mPreferences.getBoolean("action_combine_files", false);
    }

    public boolean isActionCopyDirectoryStructure() {
        return mPreferences.getBoolean("action_copy_directory_structure", false);
    }

    public boolean isActionMoveFiles() {
        return mPreferences.getBoolean("action_move_files", false);
    }

    public boolean isActionRemoveEmptyFolders() {
        return mPreferences.getBoolean("action_remove_empty_folders", false);
    }

    public boolean isActionRenameByRegex() {
        return mPreferences.getBoolean("action_rename_by_regex", false);
    }

    public boolean isFileOverride() {
        return mPreferences.getBoolean("file_override", false);
    }

    public boolean isSortByAscending() {
        return mPreferences.getBoolean("sort_by_ascending", false);
    }

    public void setIsSortByAscending(boolean value) {
        mPreferences.edit().putBoolean("sort_by_ascending", value);
    }


}
