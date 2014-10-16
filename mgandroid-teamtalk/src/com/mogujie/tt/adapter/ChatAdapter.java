package com.mogujie.tt.adapter;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
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
	private List<RecentInfo> recentSessionList = new LinkedList<RecentInfo>();
	private static Logger logger = Logger.getLogger(ChatAdapter.class);

	public ChatAdapter(Context context) throws ParseException {
		this.mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return recentSessionList.size();
	}

	@Override
	public RecentInfo getItem(int position) {
		logger.d("recent#getItem position:%d", position);
		if (position >= recentSessionList.size() || position < 0) {
			return null;
		}

		return recentSessionList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// todo eric won't use it, right?
		logger.d("recent#getItemId:%d", position);
		if (position >= recentSessionList.size() || position < 0) {
			return -1;
		}

		return position;
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
			// 设置holder信息
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
			if (null == holder) {
				return null;
			}

			String avatarUrl = null;
			String userName = "";
			String lastContent = "";
			String lastTime = "";
			int backgroundResource = 0;
			int unReadCount = 0;
			int sessionType = IMSession.SESSION_P2P;

			if (null != recentSessionList
					&& position < recentSessionList.size()) {
				userName = recentSessionList.get(position).getUserName();
				lastContent = recentSessionList.get(position).getLastContent();
				lastTime = recentSessionList.get(position).getLastTimeString();
				if (10 > recentSessionList.get(position).getUnreadCount()) {
					backgroundResource = R.drawable.tt_message_notify_single;
				} else {
					backgroundResource = R.drawable.tt_message_notify_double;
				}
				unReadCount = recentSessionList.get(position).getUnreadCount();
				logger.d("recent#userName:%s,  unReadCount:%d", userName, unReadCount);
				avatarUrl = recentSessionList.get(position).getUserAvatar();
				sessionType = recentSessionList.get(position).getSessionType();

			}

			// 设置未读消息计数
			if (unReadCount > 0) {
				holder.msgCount.setBackgroundResource(backgroundResource);
				holder.msgCount.setVisibility(View.VISIBLE);
				holder.msgCount.setText(String.valueOf(unReadCount));
			} else {
				holder.msgCount.setVisibility(View.GONE);
			}

			if (avatarUrl == null) {
				avatarUrl = "";
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
			logger.e(e.getMessage());
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
