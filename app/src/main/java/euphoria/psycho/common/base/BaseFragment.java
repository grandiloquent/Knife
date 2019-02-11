package euphoria.psycho.common.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public abstract class BaseFragment extends Fragment {

    public static void show(Fragment fragment, FragmentManager manager,
                            int containerViewId,
                            Bundle arguments) {

        FragmentTransaction transaction = manager.beginTransaction();


        transaction.replace(containerViewId, fragment);

        if (arguments != null)
            fragment.setArguments(arguments);

        transaction.commitNowAllowingStateLoss();

    }

    protected abstract void initViews(View view);

    protected abstract void bindViews();

    protected abstract int provideMenuId();

    protected abstract int provideLayoutId();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(provideLayoutId(), container, false);
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (provideLayoutId() != 0) {
            inflater.inflate(provideMenuId(), menu);
            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        initViews(view);
        bindViews();
    }


}
