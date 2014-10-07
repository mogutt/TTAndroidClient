package com.mogujie.tt.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.mogujie.tt.R;
import com.mogujie.tt.entity.ContactSortEntity;
import com.mogujie.tt.imlib.IMSession;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.GroupEntity;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.ui.utils.EntityList;
import com.mogujie.widget.imageview.MGWebImageView;

public class EntityListViewAdapter extends BaseAdapter implements
		SectionIndexer, ContactBaseAdapter {

	// private static int VIEW_TYPE_CONTACT = 0;
	// private static int VIEW_TYPE_GROUP = 1;
	// EntityList could be a group of ContactEntity, or GroupEntity

	private Context context;

	private Logger logger = Logger.getLogger(EntityListViewAdapter.class);

	private List<EntityList> entityListMgr = new ArrayList<EntityList>();

	private boolean showCheckBox = false;

	public EntityListViewAdapter(Context context) {
		this.context = context;
	}

	public void add(int position, EntityList entityList) {
		logger.d("contactUI#add entityList, current size:%d",
				entityListMgr.size());

		entityListMgr.add(position, entityList);

		notifyDataSetChanged();
	}

	public int getCount() {
		int cnt = 0;
		for (EntityList entityList : entityListMgr) {
			cnt += entityList.list.size();
		}

		return cnt;
	}

	public long getItemId(int position) {
		return position;
	}

	public void showCheckbox() {
		logger.d("contactUI#showCheckBox");
		showCheckBox = true;
	}

	// todo eric
	private View getViewImpl(EntityList entityList, int position, View view,
			String sectionName, String avatarUrl, String name, int sessionType) {

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
			viewHolder.checkBox = (CheckBox) view.findViewById(R.id.checkBox);
			if (showCheckBox) {
				viewHolder.checkBox.setVisibility(View.VISIBLE);
			}

			// viewHolder.viewType = VIEW_TYPE_GROUP;
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		boolean checked = entityList.shouldCheckBoxChecked(position);
		viewHolder.checkBox.setChecked(checked);

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

	private PositionInfo getListPosition(int position, List<Object> entityList) {
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

	private View getContactEntityView(View convertView, EntityList entityList,
			final int position, ContactEntity contact) {
		if (contact == null) {
			return null;
		}

		return getViewImpl(entityList, position, convertView,
				entityList.getSectionName(position), contact.avatarUrl,
				contact.name, IMSession.SESSION_P2P);
	}

	// todo eric use generic
	private View getGroupEntityView(View convertView, EntityList entityList,
			final int position, GroupEntity group) {
		if (group == null) {
			return null;
		}

		return getViewImpl(entityList, position, convertView,
				entityList.getSectionName(position), group.avatarUrl,
				group.name, group.type);
	}

	private View getEntityView(EntityList entityList, final int position,
			View convertView) {
		logger.d("contactUI#getEntityView position:%d", position);

		Object object = entityList.list.get(position);
		if (object instanceof ContactEntity) {
			return getContactEntityView(convertView, entityList, position,
					(ContactEntity) object);
		} else if (object instanceof GroupEntity) {
			return getGroupEntityView(convertView, entityList, position,
					(GroupEntity) object);
		}

		return null;
	}

	public View getView(final int positionArg, View view, ViewGroup arg2) {
		int position = positionArg;

		for (EntityList entityList : entityListMgr) {
			PositionInfo pi = getListPosition(position, entityList.list);
			logger.d("contactUI#groupPosition:%s", pi);

			if (!pi.isOverflow()) {
				return getEntityView(entityList, pi.getPosition(), view);
			}

			position = pi.getPosition();
		}

		return null;
	}

	public final static class ViewHolder {
		TextView sectionView;
		TextView nameView;
		MGWebImageView avatar;
		public CheckBox checkBox;
		int viewType;

	}

	@Override
	public Object[] getSections() {
		return null;
	}

	// todo eric make section as a string
	@Override
	public int getPositionForSection(int section) {
		logger.d("pinyin#getPositionForSection secton:%d", section);

		int index = 0;
		for (EntityList entityList : entityListMgr) {
			if (!entityList.isPinYinIndexable()) {
				logger.d("pinyin#not indexable");
				index += entityList.list.size();
				continue;
			}

			logger.d("pinyin#indexable");

			List<Object> list = entityList.list;
			for (int i = 0; i < list.size(); ++i) {
				int firstCharacter = entityList.getPinYinFirstCharacter(i);
				// logger.d("firstCharacter:%d", firstCharacter);
				if (firstCharacter == section) {
					logger.d("pinyin#find sectionName");
					return index;
				}

				index++;
			}
		}

		logger.e("pinyin#can't find such section:%d", section);
		return -1;
	}

	public void handleItemClick(View view, Context ctx, int position) {
		logger.d("contactUI#handleItemClick position:%d", position);

		for (EntityList entityList : entityListMgr) {
			PositionInfo pi = getListPosition(position, entityList.list);
			logger.d("contactUI#groupPosition:%s", pi);

			if (!pi.isOverflow()) {
				entityList.onItemClick(view, pi.position);
				return;
			}

			position = pi.getPosition();

		}
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

	public void onSearch(String key) {
		logger.d("contactUI#onSearch key:%s", key);

		key = key.toUpperCase();

		for (EntityList entityList : entityListMgr) {
			entityList.onSearch(key);
		}

		notifyDataSetChanged();
	}

}
