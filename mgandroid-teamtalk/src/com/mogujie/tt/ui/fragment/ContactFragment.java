package com.mogujie.tt.ui.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.mogujie.tt.R;
import com.mogujie.tt.adapter.ContactBaseAdapter;
import com.mogujie.tt.adapter.EntityListViewAdapter;
import com.mogujie.tt.config.HandlerConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.ContactSortEntity;
import com.mogujie.tt.imlib.IMActions;
import com.mogujie.tt.imlib.IMContactManager;
import com.mogujie.tt.imlib.common.ErrorCode;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.DepartmentEntity;
import com.mogujie.tt.imlib.proto.GroupEntity;
import com.mogujie.tt.imlib.service.IMService;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.ui.utils.EntityList;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.ui.utils.IMServiceHelper.OnIMServiceListner;
import com.mogujie.tt.utils.CharacterParser;
import com.mogujie.tt.utils.ContactUtils;
import com.mogujie.tt.utils.SortComparator;
import com.mogujie.tt.widget.SearchEditText;
import com.mogujie.tt.widget.SortSideBar;
import com.mogujie.tt.widget.SortSideBar.OnTouchingLetterChangedListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

public class ContactFragment extends MainFragment
		implements
			OnTouchingLetterChangedListener,
			OnIMServiceListner {
	private View curView = null;
	private static Handler uiHandler = null;
	private ListView allContactListView;
	private ListView departmentContactListView;
	private SortSideBar sortSideBar;
	private TextView dialog;
	private EntityListViewAdapter contactAdapter;
	private EntityListViewAdapter departmentAdapter;
	private SearchEditText searchEditText;

	private CharacterParser characterParser;
	private List<ContactSortEntity> SourceDateList;

	private SortComparator sortComparator;
	private int curTabIndex = 0;

	private IMServiceHelper imServiceHelper = new IMServiceHelper();
	private IMService imService;

	// todo eric when to release?
	private IMContactManager contactMgr;
	
	private boolean isContactDataAlreadyReady = false;
	private boolean isGroupDataAlreadyReady = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initHandler();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		logger.d("contact#register actions");
		ArrayList<String> actions = new ArrayList<String>();
		actions.add(IMActions.ACTION_CONTACT_READY);
		actions.add(IMActions.ACTION_GROUP_READY);
		actions.add(IMActions.ACTION_SEARCH_DATA_READY);
		actions.add(IMActions.ACTION_LOGIN_RESULT);

		imServiceHelper.connect(getActivity(), actions, IMServiceHelper.INTENT_NO_PRIORITY, this);

		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		curView = inflater.inflate(R.layout.tt_fragment_contact, topContentView);

		initRes();

		return curView;
	}

	private List<Object> getDepartmentTabSortedList(
			Map<String, ContactEntity> contacts) {
		// todo eric efficiency
		List<Object> contactList = new ArrayList<Object>(contacts.values());
		Collections.sort(contactList, new Comparator<Object>() {
			@Override
			public int compare(Object objEntity1, Object objEntity2) {
				ContactEntity entity1 = (ContactEntity) objEntity1;
				ContactEntity entity2 = (ContactEntity) objEntity2;

				DepartmentEntity group1 = contactMgr.findDepartment(entity1.departmentId);
				DepartmentEntity group2 = contactMgr.findDepartment(entity2.departmentId);

				if (group1.title.equals(group2.title)) {
					// todo eric efficiency
					return new IMUIHelper.ContactPinyinComparator().compare(objEntity1, objEntity2);
				} else {
					return group1.title.compareToIgnoreCase(group2.title);
				}
			}
		});

		return contactList;

	}

	public void locateDepartment(String departmentId) {
		logger.d("department#locateDepartment id:%s", departmentId);

		if (topContactTitle == null) {
			logger.e("department#TopTabButton is null");
			return;
		}

		Button tabDepartmentBtn = topContactTitle.getTabDepartmentBtn();
		if (tabDepartmentBtn == null) {
			return;
		}

		tabDepartmentBtn.performClick();
		locateDepartmentImpl(departmentId);
	}

	private void locateDepartmentImpl(String departmentId) {
		if (imService == null) {
			return;
		}

		DepartmentEntity department = imService.getContactManager().findDepartment(departmentId);
		if (department == null) {
			logger.e("department#no such id:%s", departmentId);
			return;
		}

		logger.d("department#go to locate department:%s", department);

		final int position = departmentAdapter.locateDepartment(department.title);
		logger.d("department#located position:%d", position);

		if (position < 0) {
			logger.e("department#locateDepartment id:%s failed", departmentId);
			return;
		}

		//the first time locate works
		//from the second time, the locating operations fail ever since
		departmentContactListView.post(new Runnable() {

			@Override
			public void run() {
				departmentContactListView.setSelection(position);
			}
		});
	}

	private void onContactsReady() {
		hideProgressBar();
		
		if (isContactDataAlreadyReady) {
			logger.w("contactFragment#contact data is already ready");
			return;
		}

		isContactDataAlreadyReady = true;
		
		contactTabOnContactsReady();
		departmentTabOnContactsReady();
	}

	private void departmentTabOnContactsReady() {
		logger.d("contact#departmentTabOnContactsReady");

		Map<String, ContactEntity> contacts = contactMgr.getContacts();
		List<Object> contactList = getDepartmentTabSortedList(contacts);

		EntityList entityList = new EntityList(contactList) {
			private String getContactSectioName(ContactEntity contact) {
				if (contact == null) {
					return "";
				}
				DepartmentEntity group = contactMgr.findDepartment(contact.departmentId);
				if (group == null) {
					return "";
				}

				return group.title;
			}

			@Override
			public String getSectionName(int position) {
				if (searchMode) {
					return "";
				}

				// TODO Auto-generated method stub
				final ContactEntity contact = (ContactEntity) list.get(position);
				if (contact == null) {
					return "";
				}

				String sectionName = getContactSectioName(contact);
				if (position == 0) {
					return sectionName;
				}

				ContactEntity upperContact = (ContactEntity) list.get(position - 1);
				if (sectionName.equals(getContactSectioName(upperContact))) {
					return "";
				} else {
					return sectionName;
				}

			}

			public boolean isPinYinIndexable() {
				return true;
			}

			public int getPinYinFirstCharacter(int position) {
				// TODO Auto-generated method stub
				final ContactEntity contact = (ContactEntity) list.get(position);
				if (contact == null) {
					return 0;
				}

				DepartmentEntity department = contactMgr.findDepartment(contact.departmentId);
				if (department == null) {
					return 0;
				}

				return department.pinyinElement.pinyin.charAt(0);
			}

			@Override
			public void onSearchImpl(String key) {
				ArrayList<Object> searchList = new ArrayList<Object>();
				for (Object obj : list) {
					ContactEntity contact = (ContactEntity) obj;
					if (contact.pinyinElement.pinyin.contains(key)
							|| contact.name.contains(key)) {
						searchList.add(obj);
					}
				}

				list = searchList;
			}
		};

		departmentAdapter.add(0, entityList);
	}

	private void contactTabOnContactsReady() {
		if (imService == null) {
			return;
		}
		
		
		logger.d("contact#onContactReady");

		Map<String, ContactEntity> contacts = contactMgr.getContacts();
		List<Object> contactList = IMUIHelper.getContactSortedList(contacts);

		EntityList entityList = new EntityList(contactList) {
			@Override
			public String getSectionName(int position) {
				if (searchMode) {
					return "";
				}

				// TODO Auto-generated method stub
				final ContactEntity contact = (ContactEntity) list.get(position);
				if (contact == null) {
					return "";
				}

				String sectionName = ContactUtils.getSectionName(contact);
				if (position == 0) {
					return sectionName;
				}

				ContactEntity upperContact = (ContactEntity) list.get(position - 1);
				if (sectionName.equals(ContactUtils.getSectionName(upperContact))) {
					return "";
				} else {
					return sectionName;
				}

			}

			public boolean isPinYinIndexable() {
				return true;
			}

			public int getPinYinFirstCharacter(int position) {
				// TODO Auto-generated method stub
				final ContactEntity contact = (ContactEntity) list.get(position);
				if (contact == null) {
					return 0;
				}

				if(contact.pinyinElement.pinyin == null 
					|| 0 == contact.pinyinElement.pinyin.length()) {
					return 0;
				}
				
				return contact.pinyinElement.pinyin.charAt(0);
			}

			@Override
			public void onSearchImpl(String key) {
				ArrayList<Object> searchList = new ArrayList<Object>();
				for (Object obj : list) {
					ContactEntity contact = (ContactEntity) obj;
					if (contact.pinyinElement.pinyin.contains(key)
							|| contact.name.contains(key)) {
						searchList.add(obj);
					}
				}

				list = searchList;
			}
		};

		contactAdapter.addTail(entityList);
	}

	private void onGroupReady() {
		logger.d("group#onGroupReady");
		if (imService == null) {
			return;
		}

		hideProgressBar();
		
		if (isGroupDataAlreadyReady) {
			logger.w("contactFragment#group data is already ready");
			return;
		}

		isGroupDataAlreadyReady = true;

		// todo efficiency
		List<Object> groupList = new ArrayList<Object>(imService.getGroupManager().getNormalGroupList());
		Collections.sort(groupList, new IMUIHelper.GroupPinyinComparator());

		logger.d("group#groupList size:%d", groupList.size());

		EntityList entityList = new EntityList(groupList) {
			@Override
			public String getSectionName(int position) {
				if (searchMode) {
					return "";
				}

				if (list.isEmpty()) {
					return "";
				}

				if (position == 0) {
					return getString(R.string.fixed_group_name);
				}

				return "";
			}

			@Override
			public void onSearchImpl(String key) {
				logger.d("search#key:%s", key);

				if (key.isEmpty()) {
					logger.d("search#key is empty");
					recoverFromBackup();
					return;
				}

				ArrayList<Object> searchList = new ArrayList<Object>();
				for (Object obj : list) {
					GroupEntity group = (GroupEntity) obj;
					logger.d("search#pinyin:%s", group.pinyinElement.pinyin);
					if (group.pinyinElement.pinyin.contains(key)
							|| group.name.contains(key)) {
						logger.d("search#group contains the key");
						searchList.add(obj);
					}
				}

				list = searchList;
			}

		};

		contactAdapter.add(0, entityList);
	}

	/**
	 * @Description 初始化界面资源
	 */
	private void initRes() {
		// 设置顶部标题栏
		showContactTopBar();
		// if (chooseMode) {
		// setTopLeftButton(R.drawable.tt_top_back);
		// setTopLeftText(getActivity().getString(R.string.top_left_back));
		// topLeftBtn.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View view) {
		// getActivity().finish();
		// }
		// });
		// }
		//

		super.init(curView);
		showProgressBar();

		sortSideBar = (SortSideBar) curView.findViewById(R.id.sidrbar);
		dialog = (TextView) curView.findViewById(R.id.dialog);
		sortSideBar.setTextView(dialog);
		sortSideBar.setOnTouchingLetterChangedListener(this);

		allContactListView = (ListView) curView.findViewById(R.id.all_contact_list);
		departmentContactListView = (ListView) curView.findViewById(R.id.department_contact_list);

		characterParser = CharacterParser.getInstance();
		sortComparator = new SortComparator();

		SourceDateList = filledData(getResources().getStringArray(R.array.data));
		Collections.sort(SourceDateList, sortComparator);

		// logger.d("--------------dump contacts------------------");
		// for (ContactSortEntity user : SourceDateList) {
		// logger.d("userName:%s, pinyin:%s", user.getName(),
		// user.getSortLetters());
		// }

		contactAdapter = new EntityListViewAdapter(getActivity());
		departmentAdapter = new EntityListViewAdapter(getActivity());

		allContactListView.setAdapter(contactAdapter);
		departmentContactListView.setAdapter(departmentAdapter);

		contactAdapter.initClickEvents(allContactListView);
		departmentAdapter.initClickEvents(departmentContactListView);

		//this is critical, disable loading when finger sliding, otherwise you'll find sliding is not very smooth
		allContactListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
		departmentContactListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));

		searchEditText = (SearchEditText) curView.findViewById(R.id.filter_edit);

		searchEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				((EntityListViewAdapter) getCurAdapter()).onSearch(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		// todo eric
		// showLoadingProgressBar(true);
	}

	/**
	 * 为ListView填充数据
	 * 
	 * @param data
	 * @return
	 */
	private List<ContactSortEntity> filledData(String[] data) {
		List<ContactSortEntity> mSortList = new ArrayList<ContactSortEntity>();

		for (int i = 0; i < data.length; i++) {
			ContactSortEntity sortModel = new ContactSortEntity();
			sortModel.setName(data[i]);
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(data[i]);
			String sortString = pinyin.substring(0, 1).toUpperCase();

			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				sortModel.setSortLetters(sortString.toUpperCase());
			} else {
				sortModel.setSortLetters("#");
			}

			mSortList.add(sortModel);
		}
		return mSortList;

	}

	private ContactBaseAdapter getCurAdapter() {
		if (0 == curTabIndex) {
			return contactAdapter;
		} else {
			return departmentAdapter;
		}
	}

	private ListView getCurListView() {
		if (0 == curTabIndex) {
			return allContactListView;
		} else {
			return departmentContactListView;
		}
	}

	public static Handler getHandler() {
		return uiHandler;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		imServiceHelper.disconnect(getActivity());
	}

	@Override
	protected void initHandler() {
		uiHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
					case HandlerConstant.HANDLER_CHANGE_CONTACT_TAB :
						if (null != msg.obj) {
							curTabIndex = (Integer) msg.obj;
							if (0 == curTabIndex) {
								allContactListView.setVisibility(View.VISIBLE);
								departmentContactListView.setVisibility(View.GONE);
							} else {
								departmentContactListView.setVisibility(View.VISIBLE);
								allContactListView.setVisibility(View.GONE);
							}
						}
						break;
				}
			}
		};
	}

	@Override
	public void onTouchingLetterChanged(String s) {
		int position = getCurAdapter().getPositionForSection(s.charAt(0));
		if (position != -1) {
			getCurListView().setSelection(position);
		}
	}

	@Override
	public void onAction(String action, Intent intent,
			BroadcastReceiver broadcastReceiver) {
		// TODO Auto-generated method stub
		logger.d("contact#receive action:%s", action);

		if (action.equals(IMActions.ACTION_CONTACT_READY)) {
			logger.d("contact#action is contact_ready");

			onContactsReady();
		} else if (action.equals(IMActions.ACTION_GROUP_READY)) {
			logger.d("group#action is group_ready");

			onGroupReady();
		} else if (action.equals(IMActions.ACTION_LOGIN_RESULT)) {
			handleLoginResultAction(intent);
		}

		tryHandleSearchAction(action);
	}

	private void handleLoginResultAction(Intent intent) {
		logger.d("contact#handleLoginResultAction");
		int errorCode = intent
				.getIntExtra(SysConstant.lOGIN_ERROR_CODE_KEY, -1);

		if (errorCode == ErrorCode.S_OK) {
			onLoginSuccess();
		}
	}

	private void onLoginSuccess() {
		logger.d("contact#onLogin Successful");
		contactAdapter.clear();
		departmentAdapter.clear();
		
		isContactDataAlreadyReady = false;
		isGroupDataAlreadyReady = false;
	}

	
	@Override
	public void onIMServiceConnected() {
		logger.d("contactUI#onIMServiceConnected");

		imService = imServiceHelper.getIMService();

		if (imService != null) {
			contactMgr = imService.getContactManager();
		}

		if (contactMgr.ContactsDataReady()) {
			onContactsReady();
		}

		if (imService.getGroupManager().groupReadyConditionOk()) {
			onGroupReady();
		}

		tryInitSearch(imService);
	}
}
