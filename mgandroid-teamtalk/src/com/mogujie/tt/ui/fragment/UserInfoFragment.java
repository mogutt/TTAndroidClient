
package com.mogujie.tt.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mogujie.tt.R;
import com.mogujie.tt.cache.biz.CacheHub;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.User;
import com.mogujie.tt.ui.activity.MessageActivity;
import com.mogujie.tt.ui.base.TTBaseFragment;

public class UserInfoFragment extends TTBaseFragment {

    private View curView = null;
    private User user = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_user_detail, topContentView);

        initRes();

        return curView;
    }

    @Override
    public void onResume() {
        Intent intent = getActivity().getIntent();
        if (null != intent) {
            String fromPage = intent.getStringExtra(SysConstant.USER_DETAIL_PARAM);
            setTopLeftText(fromPage);
        }
        super.onResume();
    }

    /**
     * @Description 初始化资源
     */
    private void initRes() {
        // 设置标题栏
        setTopTitle(getActivity().getString(R.string.page_user_detail));
        setTopLeftButton(R.drawable.tt_top_back);
        topLeftBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                getActivity().finish();
            }
        });
        // 设置界面信息
        Button chatBtn = (Button) curView.findViewById(R.id.chat_btn);
        chatBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                int readCount = CacheHub.getInstance().clearUnreadCount(
                        user.getUserId());
                // 设置当前选中用户本地DB中消息为已读
                CacheHub.getInstance().updateMsgReadStatus(
                        CacheHub.getInstance().getLoginUserId(),
                        user.getUserId(),
                        SysConstant.MESSAGE_ALREADY_READ);
                // 设置新的聊天对象
                CacheHub.getInstance().setChatUser(user);
                // 切换到消息界面
                Intent i = new Intent(getActivity(), MessageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(SysConstant.READCOUNT, readCount);
                i.putExtras(bundle);
                getActivity().setResult(Activity.RESULT_OK, i);
                getActivity().finish();
            }
        });
    }

    @Override
    protected void initHandler() {
    }

}
