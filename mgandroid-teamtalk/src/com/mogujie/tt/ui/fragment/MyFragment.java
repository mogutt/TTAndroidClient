
package com.mogujie.tt.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mogujie.tt.R;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.ui.activity.UserInfoActivity;
import com.mogujie.tt.ui.base.TTBaseFragment;

public class MyFragment extends TTBaseFragment {
    private View curView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_my, topContentView);

        initRes();

        return curView;
    }

    /**
     * @Description 初始化资源
     */
    private void initRes() {
        // 设置顶部标题栏
        setTopTitle(getActivity().getString(R.string.page_me));
        // 设置页面其它控件
        RelativeLayout userContainer = (RelativeLayout) curView.findViewById(R.id.user_container);
        userContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                intent.putExtra(SysConstant.USER_DETAIL_PARAM, getActivity().getString(R.string.page_me));
                getActivity().startActivity(intent);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void initHandler() {

    }
}
