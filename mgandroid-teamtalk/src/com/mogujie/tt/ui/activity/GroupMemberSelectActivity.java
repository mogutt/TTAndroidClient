
package com.mogujie.tt.ui.activity;

import com.mogujie.tt.R;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.GroupManagerEntity;
import com.mogujie.tt.ui.fragment.GroupManagerFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class GroupMemberSelectActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.tt_activity_group_member_select);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK != resultCode)
            return;
//        if (null != data) {
//            GroupManagerEntity user = (GroupManagerEntity) data
//                    .getSerializableExtra(SysConstant.OBJECT_PARAM);
//            GroupManagerFragment.addUser(user);
//        }
    }
}
