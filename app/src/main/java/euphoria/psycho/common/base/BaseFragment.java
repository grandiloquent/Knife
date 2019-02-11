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
import euphoria.psycho.common.Log;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (provideMenuId() > 0) {
            setHasOptionsMenu(true);
     


        }
    }

    protected abstract void initViews();

    protected abstract void bindViews(View view);

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




        if (provideMenuId() != 0) {
            inflater.inflate(provideMenuId(), menu);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindViews(view);
        initViews();
    }


}
