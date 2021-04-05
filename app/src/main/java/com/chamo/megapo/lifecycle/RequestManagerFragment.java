package com.chamo.megapo.lifecycle;

import android.content.Context;
import android.support.v4.app.Fragment;

public class RequestManagerFragment extends Fragment {

    private int activityCode;
    LifecycleObservable lifecycleObservable = LifecycleObservable.getInstance();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityCode = context.hashCode();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        lifecycleObservable.onDestroy(activityCode);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
