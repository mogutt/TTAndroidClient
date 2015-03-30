package com.mogujie.tt.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.mogujie.tt.R;
import com.mogujie.tt.entity.ContactSortEntity;
import com.mogujie.tt.imlib.IMSession;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.DepartmentEntity;
import com.mogujie.tt.imlib.proto.GroupEntity;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.imlib.utils.SearchElement;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.ui.utils.EntityList;

public class EntityListViewAdapter extends BaseAdapter
		implements
			SectionIndexer,
			ContactBaseAdapter,
			OnItemClickListener,
			OnItemLongClickListener {

	// private static int VIEW_TYPE_CONTACT = 0;
	// private static int VIEW_TYPE_GROUP = 1;
	// EntityList could be a group of ContactEntity, or GroupEntity

	private Context ctx;

	private Logger logger = Logger.getLogger(EntityListViewAdapter.class);

	private List<EntityList> entityListMgr = new ArrayList<EntityList>();

	private boolean showCheckBox = false;

	private boolean enabled = true;

	public EntityListViewAdapter(Context context) {
		this.ctx = context;
	}

	public void initClickEvents(ListView listView) {
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
	}

	public void add(int position, EntityList entityList) {
		logger.d("entityListViewAdapter#add entityList, current size:%d", entityListMgr.size());

		entityListMgr.add(position, entityList);

		notifyDataSetChanged();
	}
	
	public void addTail(EntityList entityList) {
		logger.d("entityListViewAdapter#addTail entityList, current size:%d", entityListMgr.size());

		entityListMgr.add(entityList);

		notifyDataSetChanged();
	}

	public int getCount() {
		if (!isEnabled()) {
			return 0;
		}

		int cnt = 0;
		for (EntityList entityList : entityListMgr) {
			cnt += entityList.list.size();
		}

		return cnt;
	}

	public long getItemId(int position) {
		return position;
	}
	
	public void clear() {
		entityListMgr.clear();
	}

	public void showCheckbox() {
		logger.d("entityListViewAdapter#showCheckBox");
		showCheckBox = true;
	}

	// todo eric too many args, need refactor
	private View getViewImpl(boolean noAvatar, boolean isSearchMode,
			SearchElement searchElement, EntityList entityList, int position,
			View view, String sectionName, String avatarUrl, String name,
			int sessionType) {

		ViewHolder viewHolder = null;
		if (view == null) {
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(ctx).inflate(R.layout.tt_item_contact, null);
			viewHolder.nameView = (TextView) view.findViewById(R.id.contact_item_title);
			viewHolder.sectionView = (TextView) view.findViewById(R.id.contact_category_title);
			viewHolder.avatar = (ImageView) view.findViewById(R.id.contact_portrait);
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

		if (isSearchMode) {
			logger.d("pinyin#isSearchMode, searchElement:%s", searchElement);
			IMUIHelper.setTextViewCharHilighted(viewHolder.nameView, name, searchElement.startIndex, searchElement.endIndex, Color.rgb(69, 192, 26));
		} else {
			viewHolder.nameView.setText(name);
		}

		//handle avatar, todo eric, move it a sub function?
		viewHolder.avatar.setVisibility(View.VISIBLE);
		if (!noAvatar) {
			IMUIHelper.setEntityImageViewAvatar(viewHolder.avatar, avatarUrl, sessionType);
		} else {
			viewHolder.avatar.setVisibility(View.INVISIBLE);
		}
		
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
		logger.d("entityListViewAdapter#getListPosition:%d", position);
		PositionInfo positionInfo = new PositionInfo(true, 0);
		if (entityList == null) {
			logger.d("entityListViewAdapter#entityList is null");
			return positionInfo;
		}

		int entitySize = entityList.size();
		logger.d("entityListViewAdapter#entitySize:%d", entitySize);

		if (position >= entitySize) {
			logger.d("entityListViewAdapter#overflow");
			positionInfo.setPosition(position - entitySize);

			return positionInfo;
		}

		// right case
		logger.d("entityListViewAdapter#not overFlow");

		positionInfo.setOverflow(false);
		positionInfo.setPosition(position);

		return positionInfo;
	}

	private View getContactEntityView(View convertView, EntityList entityList,
			final int position, ContactEntity contact, boolean isSearchMode) {
		if (contact == null) {
			return null;
		}
		String displayName=null;
		if (!TextUtils.isEmpty(contact.nickName)) {
            displayName = contact.nickName;
        }else if(!TextUtils.isEmpty(contact.name)){
            displayName = contact.name;
        }else{
            displayName = contact.id;
        }
		return getViewImpl(false, isSearchMode, contact.searchElement, entityList, position, convertView, entityList.getSectionName(position), contact.avatarUrl, displayName, IMSession.SESSION_P2P);
	}

	// todo eric use generic
	private View getGroupEntityView(View convertView, EntityList entityList,
			final int position, GroupEntity group, boolean isSearchMode) {
		if (group == null) {
			return null;
		}

		return getViewImpl(false, isSearchMode, group.searchElement, entityList, position, convertView, entityList.getSectionName(position), group.avatarUrl, group.name, group.type);
	}

	// todo eric use generic
	private View getDepartmentEntityView(View convertView,
			EntityList entityList, final int position, DepartmentEntity department,
			boolean isSearchMode) {
		if (department == null) {
			return null;
		}

		return getViewImpl(true, isSearchMode, department.searchElement, entityList, position, convertView, entityList.getSectionName(position), "", department.title, IMSession.SESSION_ERROR);
	}

	private View getEntityView(EntityList entityList, final int position,
			View convertView) {
		logger.d("entityListViewAdapter#getEntityView position:%d", position);

		Object object = entityList.list.get(position);
		if (object instanceof ContactEntity) {
			return getContactEntityView(convertView, entityList, position, (ContactEntity) object, entityList.isSearchMode());
		} else if (object instanceof GroupEntity) {
			return getGroupEntityView(convertView, entityList, position, (GroupEntity) object, entityList.isSearchMode());
		} else if (object instanceof DepartmentEntity) {
			return getDepartmentEntityView(convertView, entityList, position, (DepartmentEntity) object, entityList.isSearchMode());
		}

		return null;
	}

	public View getView(final int positionArg, View view, ViewGroup arg2) {
		int position = positionArg;

		for (EntityList entityList : entityListMgr) {
			PositionInfo pi = getListPosition(position, entityList.list);
			logger.d("entityListViewAdapter#groupPosition:%s", pi);

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
		ImageView avatar;
		public CheckBox checkBox;
		int viewType;

	}

	@Override
	public Object[] getSections() {
		return null;
	}

	public int locateDepartment(String departmentTitle) {
		logger.d("department#locateDepartment departmentTitle:%s", departmentTitle);

		int index = 0;
		for (EntityList entityList : entityListMgr) {
			List<Object> list = entityList.list;
			for (int i = 0; i < list.size(); ++i) {
				String sectionName = entityList.getSectionName(i);
				if (sectionName != null && !sectionName.isEmpty() && (0 == sectionName.compareToIgnoreCase(departmentTitle)) ) {
					return index;
				}
				
				index++;
			}
		}
		
		return -1;
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
		logger.d("entityListViewAdapter#handleItemClick position:%d", position);

		for (EntityList entityList : entityListMgr) {
			PositionInfo pi = getListPosition(position, entityList.list);
			logger.d("entityListViewAdapter#groupPosition:%s", pi);

			if (!pi.isOverflow()) {
				entityList.onItemClick(ctx, view, pi.position);
				return;
			}

			position = pi.getPosition();

		}
	}

	public void handleItemLongClick(View view, Context ctx, int position) {
		logger.d("entityListViewAdapter#handleItemLongClick position:%d", position);

		for (EntityList entityList : entityListMgr) {
			PositionInfo pi = getListPosition(position, entityList.list);
			logger.d("entityListViewAdapter#groupPosition:%s", pi);

			if (!pi.isOverflow()) {
				entityList.onItemLongClick(view, ctx, pi.position);
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
		logger.d("entityListViewAdapter#onSearch key:%s", key);

		key = key.toUpperCase();

		for (EntityList entityList : entityListMgr) {
			entityList.onSearch(key);
		}

		notifyDataSetChanged();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {

		handleItemLongClick(view, ctx, position);

		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		handleItemClick(view, ctx, position);
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

}
