package com.mogujie.tt.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.mogujie.tt.R;
import com.mogujie.tt.entity.ContactSortEntity;
import com.mogujie.tt.imlib.IMSession;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.DepartmentEntity;
import com.mogujie.tt.imlib.proto.GroupEntity;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.utils.ContactUtils;
import com.mogujie.widget.imageview.MGWebImageView;

public class ContactAllAdapter extends BaseAdapter implements SectionIndexer,
		ContactBaseAdapter {

	public static final int VIEW_TYPE_CONTACT = 0;
	public static final int VIEW_TYPE_GROUP = 1;
	private Context context;

	private Logger logger = Logger.getLogger(ContactAllAdapter.class);

	// todo eric should i assign value here?
	private List<ContactEntity> contactList = new ArrayList<ContactEntity>();
	private List<GroupEntity> groupList = new ArrayList<GroupEntity>();

	public ContactAllAdapter(Context context) {
		this.context = context;
	}

	public int getCount() {
		return contactList.size() + groupList.size();
	}

	public long getItemId(int position) {
		return position;
	}

	public final ContactEntity getContact(int position) {
		// todo eric, check bounds
		return contactList.get(position);
	}

	public void setGroupData(Map<String, GroupEntity> groups) {
		logger.d("group#setGroupData");
		// todo efficiency
		groupList = new ArrayList<GroupEntity>(groups.values());
		Collections.sort(groupList, new Comparator<GroupEntity>() {

			@Override
			public int compare(GroupEntity entity1, GroupEntity entity2) {

				return entity1.pinyinElement.pinyin.compareToIgnoreCase(entity2.pinyinElement.pinyin);
			}
		});

		logger.d("group#groupList size:%d", groupList.size());
		notifyDataSetChanged();
	}

	public void setContactsData(Map<String, DepartmentEntity> departments,
			Map<String, ContactEntity> contacts) {
		// TODO Auto-generated method stub

		logger.d("contact#setContactsData");
		// todo efficiency
		contactList = new ArrayList<ContactEntity>(contacts.values());
		Collections.sort(contactList, new Comparator<ContactEntity>() {

			@Override
			public int compare(ContactEntity entity1, ContactEntity entity2) {
				// TODO Auto-generated method stub
				if (entity2.pinyinElement.pinyin.startsWith("#")) {
					return -1;
				} else if (entity1.pinyinElement.pinyin.startsWith("#")) {
					return 1;
				} else {

					return entity1.pinyinElement.pinyin.compareToIgnoreCase(entity2.pinyinElement.pinyin);
				}
			}
		});

		notifyDataSetChanged();
	}

	private String getSectionName(int position) {
		final ContactEntity contact = contactList.get(position);
		if (contact == null) {
			return "";
		}

		String sectionName = ContactUtils.getSectionName(contact);
		if (position == 0) {
			return sectionName;
		}

		ContactEntity upperContact = contactList.get(position - 1);
		if (sectionName.equals(ContactUtils.getSectionName(upperContact))) {
			return "";
		} else {
			return sectionName;
		}
	}

	private String getGroupSectionName(int position) {
		if (groupList.isEmpty()) {
			return "";
		}

		if (position == 0) {
			// todo
			return "群";
		}

		return "";
	}

	// todo eric
	private View getViewImpl(View view, String sectionName, String avatarUrl,
			String name, int sessionType) {
		logger.d("contactUI#sectionName:%s,  avatarUrl:%s, name:%s",
				sectionName, avatarUrl, name);

		ViewHolder viewHolder = null;
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(context).inflate(
					R.layout.tt_item_contact, null);
			viewHolder.nameView = (TextView) view
					.findViewById(R.id.contact_item_title);
			viewHolder.sectionView = (TextView) view
					.findViewById(R.id.contact_category_title);
			viewHolder.avatar = (MGWebImageView) view
					.findViewById(R.id.contact_portrait);
			viewHolder.viewType = VIEW_TYPE_GROUP;
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		if (sectionName.isEmpty()) {
			viewHolder.sectionView.setVisibility(View.GONE);
		} else {
			viewHolder.sectionView.setVisibility(View.VISIBLE);
			viewHolder.sectionView.setText(sectionName);
		}

		viewHolder.nameView.setText(name);
		
		IMUIHelper.setWebImageViewAvatar(viewHolder.avatar, avatarUrl, sessionType);

		return view;

	}

	private View getGroupView(final int position, View view, ViewGroup arg2) {
		logger.d("contactUI#getGroupView position:%d", position);

		GroupEntity group = groupList.get(position);
		if (group == null) {
			return null;
		}

		return getViewImpl(view, getGroupSectionName(position),
				group.avatarUrl, group.name, group.type);
	}

	public static class PositionInfo {
		private boolean overflow;
		private int position;

		public PositionInfo(boolean overflow, int position) {
			this.overflow = overflow;
			this.position = position;
		}

		public boolean isOverflow() {
			return overflow;
		}

		public void setOverflow(boolean overflow) {
			this.overflow = overflow;
		}

		public int getPosition() {
			return position;
		}

		public void setPosition(int position) {
			this.position = position;
		}

		@Override
		public String toString() {
			return "PositionInfo [overflow=" + overflow + ", position="
					+ position + "]";
		}

	}

	private PositionInfo getListPosition(int position, List<?> entityList) {
		logger.d("contactUI#getListPosition:%d", position);
		PositionInfo positionInfo = new PositionInfo(true, 0);
		if (entityList == null) {
			logger.d("contactUI#entityList is null");
			return positionInfo;
		}

		int entitySize = entityList.size();
		logger.d("contactUI#entitySize:%d", entitySize);

		if (position >= entitySize) {
			logger.d("contactUI#overflow");
			positionInfo.setPosition(position - entitySize);

			return positionInfo;
		}

		// right case
		logger.d("contactUI#not overFlow");

		positionInfo.setOverflow(false);
		positionInfo.setPosition(position);

		return positionInfo;
	}

	private View getContactView(final int position, View view, ViewGroup arg2) {
		logger.d("contactUI#getContactView position:%d", position);

		ContactEntity contact = contactList.get(position);
		if (contact == null) {
			return null;
		}

		return getViewImpl(view, getSectionName(position), contact.avatarUrl,
				contact.name, IMSession.SESSION_P2P);
	}

	public View getView(final int position, View view, ViewGroup arg2) {
		logger.d("contactUI#getView position:%d", position);

		PositionInfo pi = getListPosition(position, groupList);
		logger.d("contactUI#groupPosition:%s", pi);

		if (!pi.isOverflow()) {
			return getGroupView(pi.getPosition(), view, arg2);
		}

		pi = getListPosition(pi.getPosition(), contactList);
		logger.d("contactUI#contactPosition:%s", pi);

		if (!pi.isOverflow()) {
			return getContactView(pi.getPosition(), view, arg2);
		}

		return null;
	}

	final static class ViewHolder {
		TextView sectionView;
		TextView nameView;
		MGWebImageView avatar;
		int viewType;

	}

	@Override
	public Object[] getSections() {
		return null;
	}

	// todo eric make section as a string
	@Override
	public int getPositionForSection(int section) {
		for (int i = 0; i < contactList.size(); ++i) {
			ContactEntity contact = contactList.get(i);
			if (contact.pinyinElement.pinyin.charAt(0) == section) {
				return i;
			}
		}

		logger.e("can't find such section:%d", section);
		return -1;
	}
	
	public void handleItemClick(Context ctx, int position) {
		logger.d("contactUI#handleItemClick position:%d", position);
		
		PositionInfo pi = getListPosition(position, groupList);
		logger.d("contactUI#groupPosition:%s", pi);

		if (!pi.isOverflow()) {
			handleGroupItemClick(ctx, pi.getPosition());
			return;
		}

		pi = getListPosition(pi.getPosition(), contactList);
		logger.d("contactUI#handleItemClick contactPosition:%s", pi);

		if (!pi.isOverflow()) {
			handleContactItemClick(ctx, pi.getPosition());
		}

	 	logger.e("contact#handleItemClick can't find entity -> position:%d", position);
	}

	private void handleContactItemClick(Context ctx, int position) { 
		logger.d("contactUI#handleContactItemClick position:%d", position);
		
		ContactEntity contact = contactList.get(position);
		logger.d("chat#clicked contact:%s", contact);

		IMUIHelper.openContactChatActivity(ctx, contact);
	}
	
private void handleGroupItemClick(Context ctx, int position) { 
	logger.d("contactUI#handleGroupItemClick position:%d", position);

		GroupEntity group = groupList.get(position);
		logger.d("chat#clicked group:%s", group);

		IMUIHelper.openGroupChatActivity(ctx, group);
	}

@Override
public void updateListView(List<ContactSortEntity> list) {
	// TODO Auto-generated method stub
	
}

@Override
public Object getItem(int position) {
	// TODO Auto-generated method stub
	return null;
}

@Override
public int getSectionForPosition(int arg0) {
	// TODO Auto-generated method stub
	return 0;
}

}
