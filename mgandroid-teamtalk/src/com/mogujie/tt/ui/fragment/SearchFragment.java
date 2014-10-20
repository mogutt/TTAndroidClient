package com.mogujie.tt.ui.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.mogujie.tt.R;
import com.mogujie.tt.adapter.EntityListViewAdapter;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.DepartmentEntity;
import com.mogujie.tt.imlib.proto.GroupEntity;
import com.mogujie.tt.imlib.service.IMService;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.imlib.utils.SearchElement;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.ui.activity.MainActivity;
import com.mogujie.tt.ui.base.TTBaseFragment;
import com.mogujie.tt.ui.utils.EntityList;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.ui.utils.IMServiceHelper.OnIMServiceListner;
import com.mogujie.tt.utils.pinyin.PinYin.PinYinArea;

public class SearchFragment extends TTBaseFragment
		implements
			OnIMServiceListner {

	private Logger logger = Logger.getLogger(SearchFragment.class);
	private IMServiceHelper imServiceHelper = new IMServiceHelper();
	private View curView = null;
	private ListView listView;
	private EntityListViewAdapter adapter;
	IMService imService;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		imServiceHelper.connect(this.getActivity(), null, IMServiceHelper.INTENT_MAX_PRIORITY, this);

		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		curView = inflater.inflate(R.layout.tt_fragment_search, topContentView);

		initTopBar();

		initListView();

		return curView;
	}

	@Override
	public void onResume() {

		super.onResume();
	}

	private void initTopBar() {
		setTopBar(R.color.half_transparent_light);
		showTopSearchBar();
		setTopLeftButton(R.drawable.tt_top_back);
		hideTopRightButton();

		topLeftBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().finish();
			}
		});

		topSearchEdt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				String key = s.toString();
				adapter.setEnabled(!key.isEmpty());

				adapter.onSearch(key);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	private void initListView() {
		listView = (ListView) curView.findViewById(R.id.search);
		adapter = new EntityListViewAdapter(getActivity());
		adapter.setEnabled(false);

		listView.setAdapter(adapter);
		adapter.initClickEvents(listView);
	}

	private void initSearchEntityLists() {
		initDepartmentEntityList();
		initGroupEntityList();
		initContactEntityList();
		
	}

	private void initContactEntityList() {
		if (imService == null) {
			return;
		}

		Map<String, ContactEntity> contacts = imService.getContactManager().getContacts();
		List<Object> contactList = IMUIHelper.getContactSortedList(contacts);

		EntityList entityList = new EntityList(contactList) {
			@Override
			public String getSectionName(int position) {
				if (!list.isEmpty() && position == 0) {
					return getString(R.string.contact);
				}

				return "";
			}

			@Override
			public void onSearchImpl(String key) {
				ArrayList<Object> searchList = new ArrayList<Object>();
				for (Object obj : backupList) {
					ContactEntity contact = (ContactEntity) obj;

					if (IMUIHelper.handleNameSearch(contact.name, key, contact.searchElement)
							|| IMUIHelper.handleContactPinyinSearch(logger, contact.pinyinElement, key, contact.searchElement)) {
						searchList.add(obj);
					}
				}

				list = searchList;
			}
		};

		adapter.add(0, entityList);
	}
	
	private void initGroupEntityList() {
		if (imService == null) {
			return;
		}

		Map<String, GroupEntity> groups = imService.getGroupManager().getGroups();
		List<Object> groupList = IMUIHelper.getGroupSortedList(groups);

		EntityList entityList = new EntityList(groupList) {
			@Override
			public String getSectionName(int position) {
				if (!list.isEmpty() && position == 0) {
					return getString(R.string.fixed_group_or_temp_group);
				}

				return "";
			}

			@Override
			public void onSearchImpl(String key) {
				ArrayList<Object> searchList = new ArrayList<Object>();
				for (Object obj : backupList) {
					GroupEntity group = (GroupEntity) obj;

					if (IMUIHelper.handleNameSearch(group.name, key, group.searchElement)
							|| IMUIHelper.handleContactPinyinSearch(logger, group.pinyinElement, key, group.searchElement)) {
						searchList.add(obj);
					}
				}

				list = searchList;
			}
		};

		adapter.add(0, entityList);
	}
	
	private void initDepartmentEntityList() {
		if (imService == null) {
			return;
		}

		Map<String, DepartmentEntity> departments = imService.getContactManager().getDepartments();
		List<Object> departmentList = IMUIHelper.getDepartmentSortedList(departments);

		EntityList entityList = new EntityList(departmentList) {
			@Override
			public String getSectionName(int position) {
				if (!list.isEmpty() && position == 0) {
					return getString(R.string.department);
				}

				return "";
			}

			@Override
			public void onSearchImpl(String key) {
				ArrayList<Object> searchList = new ArrayList<Object>();
				for (Object obj : backupList) {
					DepartmentEntity department = (DepartmentEntity) obj;

					if (IMUIHelper.handleNameSearch(department.title, key, department.searchElement)
							|| IMUIHelper.handleContactPinyinSearch(logger, department.pinyinElement, key, department.searchElement)) {
						searchList.add(obj);
					}
				}

				list = searchList;
			}

			@Override
			public void onItemClick(Context ctx, View view, int position) {
				DepartmentEntity department = (DepartmentEntity) list.get(position);
				locateDepartment(ctx, department);
			}
			
			private void locateDepartment(Context ctx, DepartmentEntity department) {
				if (ctx == null || department == null) {
					return;
				}
				
				Intent intent = new Intent(ctx, MainActivity.class);
				intent.putExtra(SysConstant.KEY_LOCATE_DEPARTMENT, department.id);
				
				ctx.startActivity(intent);
			}
		};

		adapter.add(0, entityList);
	}



	
	@Override
	protected void initHandler() {
	}

	@Override
	public void onAction(String action, Intent intent,
			BroadcastReceiver broadcastReceiver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onIMServiceConnected() {
		logger.d("config#onIMServiceConnected");

		imService = imServiceHelper.getIMService();

		initSearchEntityLists();
	}
}
