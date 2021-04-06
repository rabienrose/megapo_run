package com.chamo.megapo.utils;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import com.chamo.megapo.R;
import com.chamo.megapo.adapter.RecyclerItemClickListener;
import com.chamo.megapo.ui.MainActivity;

public class PopupWindowRight extends PopupWindow {
    private static final String TAG = "PopupWindowRight";
    private RecyclerView recyclerView;
    private final View view;
    private Context context;
    public static MainActivity mMainActivity = null;

    public PopupWindowRight(Context context) {

        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.popup_dialog_right, null);
        setContentView(view);
        initView();
        setWidth(RecyclerView.LayoutParams.MATCH_PARENT);
        setHeight(RecyclerView.LayoutParams.MATCH_PARENT);
        setFocusable(false);
        setOutsideTouchable(true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainActivity.Event(0);
            }
        });
    }

    private void initData() {
//        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
//        recyclerView.setLayoutManager(layoutManager);
//        layoutManager.setOrientation(OrientationHelper.HORIZONTAL);
//        recyclerView.setAdapter(recycleAdapter);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
//        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//            }
//            @Override
//            public void onLongClick(View view, int position) {
//            }
//            @Override
//            public void onScroll(View view, int position) {
//            }
//        }));
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        ObjectAnimator.ofFloat(getContentView(), "translationX", getWidth(), 0).setDuration(600).start();
    }

    private static int[] getScreenSize(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return new int[]{displayMetrics.widthPixels, displayMetrics.heightPixels};
    }

    public static int dp2px(final float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void initView() {
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
    }

//    public void setData(List<Media> mediaList, MainActivity mainActivity, int scroll_pos) {
//        recycleAdapter = new NormalAdapter(mediaList,context,mainActivity);
//        recycleAdapter.setOnItemClickListener(new NormalAdapter.OnItemClickListener() {
//            @Override
//            public void onClick(int position) {
//                Media media = mediaList.get(position);
//                mMainActivity.play(media,1);
//            }
//        });
//        initData();
//        recyclerView.getLayoutManager().scrollToPosition(scroll_pos);
//    }
}
