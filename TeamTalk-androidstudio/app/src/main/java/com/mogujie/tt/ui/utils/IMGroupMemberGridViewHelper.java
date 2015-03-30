package com.mogujie.tt.ui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;

import com.mogujie.tt.adapter.GroupManagerAdapter;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.imlib.IMSession;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.service.IMService;
import com.mogujie.tt.log.Logger;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

public class IMGroupMemberGridViewHelper {
	private Logger logger = Logger.getLogger(IMGroupMemberGridViewHelper.class);
	private GridView gridView = null;
	private GroupManagerAdapter adapter = null;
	private String sessionId;
	private int sessionType;

	public GroupManagerAdapter getAdapter() {
		return adapter;
	}

	public String getSessionId() {
		return sessionId;
	}

	public int getSessionType() {
		return sessionType;
	}

	public void onInit(View parentView, int gridViewResId, Context ctx,
			boolean hasAddButton, OnItemClickListener itemClickListner, OnItemLongClickListener itemLongClickListener, GroupManagerAdapter.OnDeleteItemListener deleteImteListner) {
		if (ctx == null) {
			return;
		}

		logger.d("groupmgr#onInit");

		gridView = (GridView) parentView.findViewById(gridViewResId);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));// 去掉点击时的黄色背影
		gridView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));

		if (itemClickListner != null) {
			gridView.setOnItemClickListener(itemClickListner);
		}
		
		if (itemLongClickListener != null) {
			gridView.setOnItemLongClickListener(itemLongClickListener);
		}
		
		adapter = new GroupManagerAdapter(ctx, hasAddButton, deleteImteListner);
		gridView.setAdapter(adapter);
	}

	public void onSetGridData(IMService imService, Intent intent) {
		if (adapter == null) {
			logger.e("groupmgr#adapter is null");
			return;
		}

		logger.d("groupmgr#setGridViewData");

		if (imService == null) {
			logger.e("groupmgr#imservice is null");
			return;
		}

		sessionId = intent.getStringExtra(SysConstant.KEY_SESSION_ID);
		sessionType = intent.getIntExtra(SysConstant.KEY_SESSION_TYPE, 0);
		logger.d("groupmgr#sessionType:%d, sessionId:%s", sessionType,
				sessionId);

		List<ContactEntity> contactList = new ArrayList<ContactEntity>();
		if (sessionType == IMSession.SESSION_P2P) {
			ContactEntity contact = imService.getContactManager().findContact(
					sessionId);
			if (contact == null) {
				logger.e("groupmgr#no such contact by id:%s", sessionId);
				return;
			}

			contactList.add(contact);
		} else {
			contactList = imService.getGroupManager()
					.getGroupMembers(sessionId);
			if (contactList == null) {
				logger.e("groupmgr#get members from group id:%s failed",
						sessionId);
				return;
			}
		}

		adapter.setData(contactList);
	}

}
