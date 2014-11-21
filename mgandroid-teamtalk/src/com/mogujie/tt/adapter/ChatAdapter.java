package com.mogujie.tt.adapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mogujie.tt.R;
import com.mogujie.tt.entity.RecentInfo;
import com.mogujie.tt.imlib.IMSession;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.log.Logger;

/**
 * 
 * @Description 联系人列表适配器
 */
@SuppressLint("ResourceAsColor")
public class ChatAdapter extends BaseAdapter {
	private LayoutInflater mInflater = null;
	private List<RecentInfo> recentSessionList = new ArrayList<RecentInfo>();
	private Logger logger = Logger.getLogger(ChatAdapter.class);

	public ChatAdapter(Context context) {
		this.mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return recentSessionList.size();
	}

		@Override
		public RecentInfo getItem(int position) {
			//logger.d("recent#getItem position:%d", position);
			if (position >= recentSessionList.size() || position < 0) {
				return null;
			}
	
			return recentSessionList.get(position);
		}

	@Override
	public long getItemId(int position) {
		// todo eric won't use it, right?
		////		logger.d("recent#getItemId:%d", position);
		//		if (position >= recentSessionList.size() || position < 0) {
		//			return -1;
		//		}
		//
		//		return position;
		return 0;
	}

	public final class ContactViewHolder {
		public ImageView avatar;
		public TextView uname;
		public TextView lastContent;
		public TextView lastTime;
		public TextView msgCount;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//		logger.d("recent#getview position:%d", position);

		try {
			ContactViewHolder holder = null;
			if (null == convertView && null != mInflater) {
				convertView = mInflater.inflate(R.layout.tt_item_chat, null);
				if (null != convertView) {
					holder = new ContactViewHolder();
					holder.avatar = (ImageView) convertView.findViewById(R.id.contact_portrait);
					holder.uname = (TextView) convertView.findViewById(R.id.shop_name);
					holder.lastContent = (TextView) convertView.findViewById(R.id.message_body);
					holder.lastTime = (TextView) convertView.findViewById(R.id.message_time);
					holder.msgCount = (TextView) convertView.findViewById(R.id.message_count_notify);
					convertView.setTag(holder);
				}

			} else {
				holder = (ContactViewHolder) convertView.getTag();
			}

			String avatarUrl = null;
			String userName = "";
			String lastContent = "";
			String lastTime = "";
			int unReadCount = 0;
			int sessionType = IMSession.SESSION_P2P;

			if (position < recentSessionList.size()) {
				RecentInfo recentInfo = recentSessionList.get(position);
				if(!TextUtils.isEmpty(recentInfo.getNickname())){
				    userName = recentInfo.getNickname();
				}else if(!TextUtils.isEmpty(recentInfo.getUserName())){
				    userName = recentInfo.getUserName();
				}else{
				    userName = recentInfo.getUserId();
				}
				lastContent = recentInfo.getLastContent();
				lastTime = recentInfo.getLastTimeString();
				unReadCount = recentInfo.getUnreadCount();
				avatarUrl = recentInfo.getUserAvatar();
				sessionType = recentInfo.getSessionType();
				
//				logger.d("recent#userName:%s,  unReadCount:%d", userName, unReadCount);
			}
			// 设置未读消息计数
			if (unReadCount > 0) {
			    String strCountString=String.valueOf(unReadCount);
			    if (unReadCount>99) {
                    strCountString = "99+";
                }
				holder.msgCount.setVisibility(View.VISIBLE);
				holder.msgCount.setText(strCountString);
			} else {
				holder.msgCount.setVisibility(View.GONE);
			}

			IMUIHelper.setEntityImageViewAvatar(holder.avatar, avatarUrl, sessionType);

			// 设置其它信息
			holder.uname.setText(userName);
			holder.lastContent.setText(lastContent);
			holder.lastTime.setText(lastTime);

			// todo eric this should be a bug?
			// this.notifyDataSetChanged();
			return convertView;
		} catch (Exception e) {
			logger.e(e.getStackTrace().toString());
			return null;
		}
	}

	public void setData(List<RecentInfo> recentSessionList) {
		logger.d("recent#set New recent session list");

		this.recentSessionList = recentSessionList;

		logger.d("recent#notifyDataSetChanged");

		notifyDataSetChanged();
	}
}
