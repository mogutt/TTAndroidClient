package com.mogujie.tt.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.mogujie.tt.R;
import com.mogujie.tt.entity.GroupManagerEntity;
import com.mogujie.tt.imlib.IMSession;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.log.Logger;
import com.mogujie.widget.imageview.MGWebImageView;

public class GroupManagerAdapter extends BaseAdapter {
	private Logger logger = Logger.getLogger(GroupManagerAdapter.class);
	private Context context = null;
	private boolean removeState = false;// 用于控制是否是删除状态
	private GroupHolder holder = null;
	// key == contactId
	private Map<String, ContactEntity> memberMap;
	private Map<String, ContactEntity> originalMemberMap;
	private boolean hasAddButton;

	public GroupManagerAdapter(Context c, boolean hasAddButton) {
		this.context = c;
		this.hasAddButton = hasAddButton;
	}

	public List<String> getMemberList() {
		List<String> memberList = new ArrayList<String>();
		Collection<ContactEntity> contacts = memberMap.values();
		for (ContactEntity entity : contacts) {
			memberList.add(entity.id);
		}
		
		return memberList;
	}
	
	public void setData(List<ContactEntity> members) {
		logger.d("groupmgr#adapter setData size:%d", members.size());

		memberMap = new HashMap<String, ContactEntity>();
		originalMemberMap = memberMap;
		for (ContactEntity contact : members) {
			memberMap.put(contact.id, contact);
		}

		notifyDataSetChanged();
	}
	
	public List<String> getAddingMemberList() {
		//todo eric any union, diff functions for set?
		List<String> addingList = new ArrayList<String>();
		for (String id : memberMap.keySet()) {
			if (!originalMemberMap.containsKey(id)) {
				logger.d("dialog#adding id:%s", id);
				addingList.add(id);
			}
		}
		
		return addingList;
	}
	
	public List<String> getRemovingMemberList() {
		//todo eric any union, diff functions for set?
		List<String> removingList = new ArrayList<String>();
		for (String id : originalMemberMap.keySet()) {
			if (!memberMap.containsKey(id)) {
				logger.d("dialog#removing id:%s", id);
				removingList.add(id);
			}
		}
		
		return removingList;
	}


	public boolean isInGroup(String contactId) {
		return memberMap.containsKey(contactId);
	}

	public int getCount() {
		if (memberMap != null) {
			int memberListSize = memberMap.size();

			if (hasAddButton) {
				// 1 means the "+" button
				return memberListSize + 1;
			} else {
				return memberListSize;
			}
		}

		return 0;
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	public void remove(int position) {
		List<ContactEntity> memberList = new ArrayList<ContactEntity>(
				memberMap.values());

		ContactEntity contact = memberList.get(position);
		if (contact == null) {
			return;
		}

		removeById(contact.id);

		this.notifyDataSetChanged();
	}

	public void removeById(String contactId) {
		memberMap.remove(contactId);
	}

	public void add(ContactEntity contact) {
		memberMap.put(contact.id, contact);
		notifyDataSetChanged();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		logger.d("groupmgr#getView position:%d, member size:%d", position,
				memberMap.size());

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.tt_group_manage_grid_item,
					null);
		}
		initHolder(convertView);

		List<ContactEntity> memberList = new ArrayList<ContactEntity>(
				memberMap.values());
		if (position >= 0 && memberMap.size() > position) {
			logger.d("groupmgr#in mebers area");
			ContactEntity contactEntity = memberList.get(position);
			setHolder(contactEntity.avatarUrl, 0, contactEntity.name);
		} else if (position >= memberMap.size() && hasAddButton) {
			logger.d("groupmgr#add + button");
			setHolder(null, R.drawable.tt_group_manager_add_user, "");
		}

		return convertView;
	}

	private void setHolder(String avatarUrl, int avatarResourceId, String name) {
		if (null != holder) {
			// holder.imageView.setAdjustViewBounds(false);
			// holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			if (avatarUrl != null) {
				IMUIHelper.setWebImageViewAvatar(holder.imageView, avatarUrl, IMSession.SESSION_P2P);
			} else {
				logger.d("groupmgr#setimageresid %d", avatarResourceId);

				holder.imageView.setImageResource(avatarResourceId);

			}

			holder.userTitle.setText(name);
			holder.imageView.setVisibility(View.VISIBLE);
			holder.userTitle.setVisibility(View.VISIBLE);
			if (removeState) {
				holder.deleteImg.setVisibility(View.VISIBLE);
				if (avatarResourceId == R.drawable.tt_group_manager_add_user) {
					holder.imageView.setVisibility(View.INVISIBLE);
					holder.userTitle.setVisibility(View.INVISIBLE);
					holder.deleteImg.setVisibility(View.INVISIBLE);
				}
			} else {
				holder.deleteImg.setVisibility(View.INVISIBLE);
			}
		}
	}

	private void initHolder(View convertView) {
		holder = new GroupHolder();
		holder.imageView = (MGWebImageView) convertView
				.findViewById(R.id.grid_item_image);
		holder.userTitle = (TextView) convertView
				.findViewById(R.id.group_manager_user_title);
		holder.deleteImg = convertView.findViewById(R.id.deleteLayout);
	}

	final class GroupHolder {
		MGWebImageView imageView;
		TextView userTitle;
		View deleteImg;
	}

	public void setRemoveState(boolean remove) {
		removeState = remove;
	}

	public boolean getRemoveState() {
		return removeState;
	}

	public boolean isAddMemberButton(int position) {
		return (position == memberMap.size());
	}
}
