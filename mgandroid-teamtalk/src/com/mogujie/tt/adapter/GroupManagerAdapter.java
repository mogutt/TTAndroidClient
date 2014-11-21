package com.mogujie.tt.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mogujie.tt.R;
import com.mogujie.tt.imlib.IMSession;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.utils.DumpUtils;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.log.Logger;

public class GroupManagerAdapter extends BaseAdapter {
	private Logger logger = Logger.getLogger(GroupManagerAdapter.class);
	private Context context = null;
	private boolean removeState = false;// 用于控制是否是删除状态
	// key == contactId
	private List<ContactEntity> memberList = new ArrayList<ContactEntity>();
	private Map<String, ContactEntity> originalMemberMap = new HashMap<String, ContactEntity>();
	private boolean hasAddButton;
	private OnDeleteItemListener deleteItemListener;
	private Set<String> fixedIdSet;
	private int itemLayout = R.layout.tt_group_manage_grid_item;

	public void setItemLayout(int itemLayout) {
		this.itemLayout = itemLayout;
	}

	public interface OnDeleteItemListener {
		void onDeleteItem(String contactId);
	}

	public GroupManagerAdapter(Context c, boolean hasAddButton,
			OnDeleteItemListener deleteItemListner) {
		this.context = c;
		this.hasAddButton = hasAddButton;
		this.deleteItemListener = deleteItemListner;
	}

	public void setFixIdSet(Set<String> fixedIdSet) {
		this.fixedIdSet = fixedIdSet;
	}

	private boolean isFixId(String contactId) {
		if (fixedIdSet == null) {
			return false;
		}
		
		return fixedIdSet.contains(contactId);
	}

	public List<String> getMemberList() {
		List<String> memberList = new ArrayList<String>();
		for (ContactEntity entity : this.memberList) {
			memberList.add(entity.id);
		}

		return memberList;
	}

	public void setData(List<ContactEntity> members) {
		logger.d("groupmgr#adapter setData size:%d", members.size());

		for (ContactEntity contact : members) {
			if (contact == null) {
				return;
			}

			memberList.add(contact);
			originalMemberMap.put(contact.id, contact);
		}

		notifyDataSetChanged();
	}

	
	public List<String> getAddingMemberList() {
		
		DumpUtils.dumpStringList(logger, "tempgroup#current member list", getMemberList());
		DumpUtils.dumpStringList(logger, "tempgroup#original member list", new ArrayList<String>(originalMemberMap.keySet()));

		
		// todo eric any union, diff functions for set?
		List<String> addingList = new ArrayList<String>();
		for (ContactEntity contact : memberList) {
			if (!originalMemberMap.containsKey(contact.id)) {
				logger.d("dialog#adding id:%s", contact.id);
				addingList.add(contact.id);
			}
		}

		return addingList;
	}

	public List<String> getRemovingMemberList() {
		// todo eric any union, diff functions for set?
		List<String> removingList = new ArrayList<String>();
		for (String id : originalMemberMap.keySet()) {
			ContactEntity contact = originalMemberMap.get(id);
			if (!memberList.contains(contact)) {
				logger.d("dialog#removing id:%s", contact.id);
				removingList.add(contact.id);
			}
		}

		return removingList;
	}

	public boolean isInGroup(String contactId) {
		for (ContactEntity contact : memberList) {
			if (contact.id.equals(contactId)) {
				return true;
			}
		}

		return false;
	}

	public int getCount() {
		if (memberList != null) {
			int memberListSize = memberList.size();

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

		ContactEntity contact = memberList.get(position);
		if (contact == null) {
			return;
		}

		removeByIdImpl(contact.id);

		this.notifyDataSetChanged();
	}

	public void removeById(String contactId) {
		removeByIdImpl(contactId);

		this.notifyDataSetChanged();
	}

	public void removeByIdImpl(String contactId) {
		for (ContactEntity contact : memberList) {
			if (contact.id.equals(contactId)) {
				memberList.remove(contact);
				return;
			}
		}
	}

	public void add(ContactEntity contact) {
		setRemoveState(false);
		memberList.add(contact);
		notifyDataSetChanged();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		logger.d("debug#getView position:%d, member size:%d", position, memberList.size());

		GroupHolder holder = null;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(itemLayout, null);

			holder = new GroupHolder();
			holder.imageView =  (ImageView) convertView.findViewById(R.id.grid_item_image);
			holder.userTitle = (TextView) convertView.findViewById(R.id.group_manager_user_title);
			holder.deleteImg = convertView.findViewById(R.id.deleteLayout);

			convertView.setTag(holder);
		} else {
			holder = (GroupHolder) convertView.getTag();
		}

		if (position >= 0 && memberList.size() > position) {
			logger.d("groupmgr#in mebers area");
			final ContactEntity contactEntity = memberList.get(position);
			logger.d("debug#add contact name:%s", contactEntity.name);
			setHolder(holder, position, contactEntity.avatarUrl, 0, contactEntity.name, contactEntity);
			
			if (holder.imageView != null) {
				holder.imageView.setOnClickListener( new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						IMUIHelper.openUserProfileActivity(context, contactEntity.id);
					}
				});
			}
			
		} else if (position >= memberList.size() && hasAddButton) {
			logger.d("groupmgr#add + button");
			setHolder(holder, position, null, R.drawable.tt_group_manager_add_user, "", null);
		}

		return convertView;
	}

	private void setHolder(final GroupHolder holder, int position,
			String avatarUrl, int avatarResourceId, String name,
			ContactEntity contactEntity) {
		logger.d("debug#setHolder position:%d", position);

		if (null != holder) {

			// holder.imageView.setAdjustViewBounds(false);
			// holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			if (avatarUrl != null) {
				IMUIHelper.setEntityImageViewAvatar(holder.imageView, avatarUrl, IMSession.SESSION_P2P);
			} else {
				logger.d("groupmgr#setimageresid %d", avatarResourceId);

				holder.imageView.setImageResource(avatarResourceId);

			}

			holder.contactEntity = contactEntity;
			if (contactEntity != null) {
				logger.d("debug#setHolderContact name:%s", contactEntity.name);

				holder.deleteImg.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						logger.d("debug#name:%s", holder.contactEntity.name);
						
						GroupManagerAdapter.this.removeById(holder.contactEntity.id);
						if (deleteItemListener != null) {
							deleteItemListener.onDeleteItem(holder.contactEntity.id);
						}
					}
				});
			}

			holder.userTitle.setText(name);
			holder.imageView.setVisibility(View.VISIBLE);
			holder.userTitle.setVisibility(View.VISIBLE);
			if (shouldShowDeleteStatus(contactEntity)) {
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

	private boolean shouldShowDeleteStatus(ContactEntity contact){
		if (!removeState) {
			return false;
		}
		
		if (contact == null) {
			return false;
		}
		
		if (isFixId(contact.id)) {
			return false;
		}
		
		return true;
	}
	
	final class GroupHolder {
		ImageView imageView;
		TextView userTitle;
		View deleteImg;
		ContactEntity contactEntity;
	}

	public void setRemoveState(boolean remove) {
		removeState = remove;
	}

	public boolean getRemoveState() {
		return removeState;
	}

	public boolean isAddMemberButton(int position) {
		return (position == memberList.size());
	}
}
