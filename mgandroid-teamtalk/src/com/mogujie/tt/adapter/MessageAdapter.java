package com.mogujie.tt.adapter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mogujie.tt.R;
import com.mogujie.tt.audio.biz.AudioPlayerHandler;
import com.mogujie.tt.biz.MessageHelper;
import com.mogujie.tt.cache.biz.CacheHub;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.entity.TimeTileMessage;
import com.mogujie.tt.imlib.IMMessageManager;
import com.mogujie.tt.imlib.IMSession;
import com.mogujie.tt.imlib.service.IMService;
import com.mogujie.tt.imlib.utils.IMUIHelper;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.ui.activity.DetailPortraitActivity;
import com.mogujie.tt.ui.activity.DisplayImageActivity;
import com.mogujie.tt.ui.activity.MessageActivity;
import com.mogujie.tt.ui.activity.PreviewTextActivity;
import com.mogujie.tt.ui.tools.BubbleImageHelper;
import com.mogujie.tt.ui.tools.Emoparser;
import com.mogujie.tt.ui.tools.MessageBitmapCache;
import com.mogujie.tt.ui.utils.IMServiceHelper;
import com.mogujie.tt.utils.CommonUtil;
import com.mogujie.tt.utils.DateUtil;
import com.mogujie.tt.utils.FileUtil;
import com.mogujie.tt.widget.MGProgressbar;
import com.mogujie.tt.widget.MessageOperatePopup;
import com.mogujie.tt.widget.SpeekerToast;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * @Description 消息适配器
 * @author Nana
 * @date 2014-3-15
 */
public class MessageAdapter extends BaseAdapter {
	public static final int MESSAGE_TYPE_INVALID = -1;
	public static final int MESSAGE_TYPE_MINE_TETX = 0x00;
	public static final int MESSAGE_TYPE_MINE_IMAGE = 0x01;
	public static final int MESSAGE_TYPE_MINE_AUDIO = 0x02;
	public static final int MESSAGE_TYPE_OTHER_TEXT = 0x03;
	public static final int MESSAGE_TYPE_OTHER_IMAGE = 0x04;
	public static final int MESSAGE_TYPE_OTHER_AUDIO = 0x05;
	public static final int MESSAGE_TYPE_TIME_TITLE = 0x07;
	public static final int MESSAGE_TYPE_HISTORY_DIVIDER = 0x08;
	private static final int VIEW_TYPE_COUNT = 9;
	public static final String HISTORY_DIVIDER_TAG = "history_divider_tag";

	public static Handler messageHanler = null;
	private Activity context = null;
	private LayoutInflater inflater = null;
	private ArrayList<Object> messageList = new ArrayList<Object>();
	@SuppressWarnings("unused")
	private static int selectedPosition = -1;

	// key = msgid, value = the position in messageList
	// private MsgIdToPositionMap msgIndexMap = new MsgIdToPositionMap();

	private volatile static HashMap<String, AnimationDrawable> audioPathAnimMap = new HashMap<String, AnimationDrawable>();
	private static String playingPath = null;
	MessageOperatePopup currentPopupView;

	private boolean mHistoryFirstAdd = true;;
	private Logger logger = Logger.getLogger(MessageAdapter.class);
	private IMSession session;
	private IMServiceHelper imServiceHelper;
	private IMService imService;
	private IMMessageManager msgMgr;

	public void setIMServiceHelper(IMServiceHelper imServiceHelper) {
		this.imServiceHelper = imServiceHelper;
	}

	public MessageAdapter(Activity cxt) {
		super();

		// todo eric any recycle reference issue here
		context = cxt;
		if (null != context) {
			inflater = ((Activity) context).getLayoutInflater();
		}
	}

	public void clearMsgIndexMap() {
		// if (null != msgIndexMap) {
		// msgIndexMap.clear();
		// }
		mHistoryFirstAdd = true;
	}

	public void stopVoicePlayAnim(String path) {

		if (null == path) {
			return;
		}

		if (audioPathAnimMap.containsKey(path)) {
			AnimationDrawable anim = audioPathAnimMap.get(path);
			if (null != anim && anim.isRunning()) {
				anim.stop();
				anim.selectDrawable(0);
				audioPathAnimMap.remove(path);
			}
		}
		if (null != playingPath && playingPath.equals(path)) {
			playingPath = "";
		}
	}

	@Override
	public void notifyDataSetChanged() {
		// DumpUtils.dumpStacktrace(logger, "debug#notifyDataSetChanged",
		// false);
		// logger.i("debug#notifyDataSetChanged");
		super.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (null == messageList) {
			return 0;
		} else {
			return messageList.size();
		}
	}

	@Override
	public Object getItem(int position) {
		if (position >= getCount() || position < 0) {
			return null;
		}
		return messageList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void clearItem() {
		// logger.d("chat#clearItem callstack:%s", Log.getStackTraceString(new
		// Throwable()));
		messageList.clear();
	}

	public void setSelectedPosition(int position) {
		selectedPosition = position;
	}

	/**
	 * 历史消息的添加请千万走这个函数
	 * 
	 * @param fromStart
	 * @param list
	 */
	public void addItem(boolean fromStart, List<MessageInfo> list) {
		// logger.d("debug#dump addItem fromStart msgList");
		int index = 0;
		for (MessageInfo msgInfo : list) {
			// logger.d("debug#index:%d, msg:%s", index, msgInfo);
			++index;
		}

		try {
			if (null == list || list.size() == 0) {
				return;
			}
			// 如果是历史消息，从头开始加
			messageList.addAll(fromStart ? 0 : messageList.size(), list);

			// 先取出需要加进去的数据数量
			final int count = list.size();
			// logger.d("debug#list size:%d", count);

			if (fromStart) {
				// 因为第一次插入历史数据的时候，需要插入一条divider数据，所以后移的偏移量是count + 1
				// updatePositonSeqMap(0, mHistoryFirstAdd ? count + 1 : count);
				// msgIndexMap.fix(0, count);
			}
			// 从加进去的那个info开始，赋值各条消息的状态
			for (int i = 0; i < count; i++) {
				MessageInfo info = (MessageInfo) list.get(i);
				if (null == info) {
					continue;
				}
				// msgIndexMap.put(info.msgId, fromStart ? i : count + i);
			}

			int position = count - 1;
			while (position >= 0) {
				Object obj = messageList.get(position);
				if (!(obj instanceof MessageInfo)) {
					position--;
					continue;
				}

				MessageInfo preInfo = null;
				for (int i = position - 1; i >= 0; i--) {
					if (messageList.get(i) instanceof MessageInfo) {
						preInfo = (MessageInfo) messageList.get(i);
						break;
					}
				}

				MessageInfo info = (MessageInfo) obj;

				if (DateUtil.needDisplayTime(null == preInfo
						? null
						: preInfo.getMsgCreateTime(), info.getMsgCreateTime())) {
					// 如果当前位置已经有了time title，就不需要再加了
					if (!(messageList.get(position) instanceof TimeTileMessage)) {
						TimeTileMessage timeTile = new TimeTileMessage();
						timeTile.setTime(info.getMsgCreateTime());

						// 更新一下状态位置映射
						// msgIndexMap.fix(position, 1);
						messageList.add(position, timeTile);
					}
				}

				position--;
			}
			// if (mHistoryFirstAdd) {
			// mHistoryFirstAdd = false;
			// }
		} catch (Exception e) {
			logger.e(e.getMessage());
		}
	}

	public void setSession(IMSession session) {
		logger.d("chat#seSession");
		this.session = session;
	}

	/**
	 * 这个函数只能从末尾添加
	 * 
	 * @param fromStart
	 * @param item
	 */
	public void addItem(MessageInfo info) {
//		logger.d("chat#addItem, msg:%s, callstack:%s", info, Log.getStackTraceString(new Throwable()));
		logger.d("chat#addItem, msg:%s", info);
		
		try {
			if (null == info || messageList.contains(info)) {
				logger.d("chat#already has this item");
				return;
			}
			messageList.add(info);
			// msgIndexMap.put(info.msgId, messageList.size() - 1);

			final int count = messageList.size();
			MessageInfo preInfo = null;
			for (int i = count - 2; i >= 0; i--) {
				if (messageList.get(i) instanceof MessageInfo) {
					preInfo = (MessageInfo) messageList.get(i);
					break;
				}
			}

			if (DateUtil.needDisplayTime(null == preInfo
					? null
					: preInfo.getMsgCreateTime(), info.getMsgCreateTime())) {
				TimeTileMessage timeTitle = new TimeTileMessage();
				timeTitle.setTime(info.getMsgCreateTime());
				// msgIndexMap.fix(count - 1, 1);
				messageList.add(count - 1, timeTitle);
			}

			logger.d("chat#finish add item");
		} catch (Exception e) {
			logger.e("chat#find exception:%s", e.getMessage());
			logger.e(e.getMessage());
		}
	}

	private MessageInfo getMsgInfo(String msgId) {
		for (Object obj : messageList) {
			if (!(obj instanceof MessageInfo)) {
				continue;
			}
			MessageInfo msgInfo = (MessageInfo) obj;

			if (msgInfo.msgId.equals(msgId)) {
				return msgInfo;
			}
		}

		return null;
	}

	/**
	 * @Description 由MessageInfo对象修改消息状态(可修改图片文本消息)
	 * @param messageInfo
	 * @param state
	 */
	public void updateMessageState(MessageInfo messageInfo, int state) {
		// logger.d("debug#updateMessageState");
		if (null == messageInfo)
			return;
		try {
			if (state < SysConstant.MESSAGE_STATE_UNLOAD
					|| state > SysConstant.MESSAGE_STATE_FINISH_FAILED
					|| null == context)
				return;

			String msgId = messageInfo.msgId;
			MessageInfo msgInfo = getMsgInfo(msgId);
			if (msgInfo == null) {
				logger.e("chat#error can't find msgInfo:%s", msgId);
				return;
			}

			msgInfo.setMsgLoadState(state);
			// CacheHub.getInstance().updateMsgStatus(msgId, state);

			if (messageInfo.getDisplayType() == SysConstant.DISPLAY_TYPE_IMAGE) {
				logger.d("pic#this is image");
				String savePath = "";
				if (null != messageInfo.getSavePath()) {
					savePath = messageInfo.getSavePath();
				}
				logger.d("pic#setsavepath:%s", savePath);
				msgInfo.setSavePath(savePath);
				msgInfo.setMsgReadStatus(SysConstant.MESSAGE_ALREADY_READ);
				// CacheHub.getInstance().updateMsgImageSavePath(messageInfo.msgId,
				// savePath);

				if (imServiceHelper != null) {
					IMService imService = imServiceHelper.getIMService();
					if (imService != null) {
						imService.getDbManager().updatePictureMessagePath(messageInfo);
					}
				}
			}
			notifyDataSetChanged();

		} catch (Exception e) {
			logger.e(e.getMessage());
		}
	}

	/**
	 * @Description 由消息id去修改消息状态（用于只有id的情况）
	 * @param msgId
	 * @param state
	 */
	public void updateItemState(String msgId, int state) {
		try {

//			String stackTraceString = Log.getStackTraceString(new Throwable());
//			stackTraceString.replaceAll("\n", "###");
			// logger.d("debug#updateItemState stack:%s", stackTraceString);
			MessageInfo msgInfo = getMsgInfo(msgId);
			if (msgInfo == null) {
				logger.e("chat#error can't find msgInfo:%s", msgId);
				return;
			}

			if (null != CacheHub.getInstance()) {
				// CacheHub.getInstance().updateMsgStatus(msgId, state);
			}

			msgInfo.setMsgLoadState(state);
			// logger.d("debug#updateItemState,  msg:%s", msgInfo);
			notifyDataSetChanged();
		} catch (Exception e) {
			logger.e(e.getMessage());
		}
	}

	public void updateItemSavePath(String msgId, String path) {
		try {
			MessageInfo msgInfo = getMsgInfo(msgId);
			if (msgInfo == null) {
				logger.e("chat#error can't find msgInfo:%s", msgId);
				return;
			}

			msgInfo.setSavePath(path);
			notifyDataSetChanged();
		} catch (Exception e) {
			logger.e(e.getMessage());
		}
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {
		try {
			final int type = getItemViewType(position);
			// 所有需要被赋值的holder都是基于MessageHolderBase的
			MessageHolderBase holder = null;

			if (null == convertView && null != inflater) {
				if (type == MESSAGE_TYPE_TIME_TITLE) {
					convertView = inflater.inflate(R.layout.tt_message_title_time, parent, false);
					// 这货是个特殊情况
					TimeTitleMessageHodler ttHodler = new TimeTitleMessageHodler();
					convertView.setTag(ttHodler);
					ttHodler.time_title = (TextView) convertView.findViewById(R.id.time_title);
				} else if (type == MESSAGE_TYPE_HISTORY_DIVIDER) {
					// 这货只可能有一条，所以不用缓存了
					convertView = inflater.inflate(R.layout.tt_history_divider_item, parent, false);
				} else if (type == MESSAGE_TYPE_MINE_TETX) {
					convertView = inflater.inflate(R.layout.tt_mine_text_message_item, parent, false);
					holder = new TextMessageHolder();
					convertView.setTag(holder);
					fillTextMessageHolder((TextMessageHolder) holder, convertView);
				} else if (type == MESSAGE_TYPE_MINE_IMAGE) {
					convertView = inflater.inflate(R.layout.tt_mine_image_message_item, parent, false);
					holder = new ImageMessageHolder();
					convertView.setTag(holder);
					fillImageMessageHolder((ImageMessageHolder) holder, convertView);
				} else if (type == MESSAGE_TYPE_MINE_AUDIO) {
					logger.d("chat#start inflating audio item");
					convertView = inflater.inflate(R.layout.tt_mine_audio_message_item, parent, false);
					logger.d("chat#finish inflating audio item");
					holder = new AudioMessageHolder();
					convertView.setTag(holder);
					fillAudioMessageHolder((AudioMessageHolder) holder, convertView);
					logger.d("chat#after fillAudioMessageHolder");
				} else if (type == MESSAGE_TYPE_OTHER_TEXT) {
					convertView = inflater.inflate(R.layout.tt_other_text_message_item, parent, false);
					holder = new TextMessageHolder();
					convertView.setTag(holder);
					fillTextMessageHolder((TextMessageHolder) holder, convertView);
				} else if (type == MESSAGE_TYPE_OTHER_IMAGE) {
					convertView = inflater.inflate(R.layout.tt_other_image_message_item, parent, false);
					holder = new ImageMessageHolder();
					convertView.setTag(holder);
					fillImageMessageHolder((ImageMessageHolder) holder, convertView);
				} else if (type == MESSAGE_TYPE_OTHER_AUDIO) {
					convertView = inflater.inflate(R.layout.tt_other_audio_message_item, parent, false);
					holder = new AudioMessageHolder();
					convertView.setTag(holder);
					fillAudioMessageHolder((AudioMessageHolder) holder, convertView);
				}
			} else {
				if (type != MESSAGE_TYPE_TIME_TITLE) {
					holder = (MessageHolderBase) convertView.getTag();
				}
			}

			// 这些都是不需要被赋值的
			if (type == MESSAGE_TYPE_HISTORY_DIVIDER
					|| type == MESSAGE_TYPE_INVALID) {
				return convertView;
			}

			if (type == MESSAGE_TYPE_TIME_TITLE) {
				TimeTileMessage msg = (TimeTileMessage) messageList.get(position);
				((TimeTitleMessageHodler) convertView.getTag()).time_title.setText(DateUtil.getTimeDiffDesc(msg.getTime()));

				return convertView;
			}

			final MessageInfo info = (MessageInfo) messageList.get(position);
			// logger.d("debug#getview MessageInfo position:%d, info:%s",
			// position, info);
			// DumpUtils.dumpStacktrace(logger, "debug#getview", false);

			IMUIHelper.setMessageOwnerAvatar(logger, session, info, holder.portrait);

			setName(holder, info);

			final View baseView = getBaseViewForMenu(holder, info);

			if (info.getMsgLoadState() == SysConstant.MESSAGE_STATE_FINISH_FAILED) {

				holder.messageFailed.setVisibility(View.VISIBLE);

				holder.messageFailed.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						logger.d("debug#onClick, msg:%s", info);
						if (!info.isMyMsg() && info.isImage()) {
							logger.d("debug#pic#found failed receiving image message");
							updateItemState(info.msgId, SysConstant.MESSAGE_STATE_UNLOAD);
						}

						int menuType = getMenuType(info);
						if (menuType > 0) {
							// logger.d("debug#showMenu  MessageInfo:%s", info);
							showMenu(context, menuType, parent, info, baseView);
						}
					}
				});
			} else {
				holder.messageFailed.setVisibility(View.GONE);
			}

			if (info.getMsgLoadState() == SysConstant.MESSAGE_STATE_LOADDING
					&& !info.isImage()) {
				holder.loadingProgressBar.setVisibility(View.VISIBLE);
			} else {
				holder.loadingProgressBar.setVisibility(View.GONE);
			}

			if (type == MESSAGE_TYPE_MINE_TETX
					|| type == MESSAGE_TYPE_OTHER_TEXT) {
				handleTextMessage((TextMessageHolder) holder, info, parent);
			} else if (type == MESSAGE_TYPE_MINE_IMAGE) {
				handleImageMessage((ImageMessageHolder) holder, info, position, true, parent);
			} else if (type == MESSAGE_TYPE_OTHER_IMAGE) {
				handleImageMessage((ImageMessageHolder) holder, info, position, false, parent);
			} else if (type == MESSAGE_TYPE_MINE_AUDIO) {
				handleAudioMessage((AudioMessageHolder) holder, info, true, parent, position);
			} else if (type == MESSAGE_TYPE_OTHER_AUDIO) {
				handleAudioMessage((AudioMessageHolder) holder, info, false, parent, position);
			}
			return convertView;
		} catch (Exception e) {
			if (null != e && null != logger) {
				logger.e("chat#%s", e);
			}
			return null;
		}
	}

	private void setName(MessageHolderBase holder, MessageInfo info) {
		if (holder.name == null) {
			return;
		}

		if (session.getSessionType() != IMSession.SESSION_P2P) {
			holder.name.setVisibility(View.VISIBLE);

			IMUIHelper.setMessageOwnerName(logger, session, info, holder.name);
		} else {
			holder.name.setVisibility(View.GONE);
		}
	}

	@Override
	public int getItemViewType(int position) {
		// logger.d("chat#getItemViewType -> position:%d", position);
		try {
			if (position >= messageList.size()) {
				return MESSAGE_TYPE_INVALID;
			}

			Object obj = messageList.get(position);
			if (obj instanceof TimeTileMessage) {
				return MESSAGE_TYPE_TIME_TITLE;
			} else if (obj instanceof String && obj.equals(HISTORY_DIVIDER_TAG)) {
				return MESSAGE_TYPE_HISTORY_DIVIDER;
			} else {
				MessageInfo info = (MessageInfo) obj;
				if (info.getMsgFromUserId().equals(CacheHub.getInstance().getLoginUserId())) {
					if (info.getDisplayType() == SysConstant.DISPLAY_TYPE_TEXT) {
						return MESSAGE_TYPE_MINE_TETX;
					} else if (info.getDisplayType() == SysConstant.DISPLAY_TYPE_IMAGE) {
						return MESSAGE_TYPE_MINE_IMAGE;
					} else if (info.getDisplayType() == SysConstant.DISPLAY_TYPE_AUDIO) {
						return MESSAGE_TYPE_MINE_AUDIO;
					}
				} else {
					if (info.getDisplayType() == SysConstant.DISPLAY_TYPE_TEXT) {
						return MESSAGE_TYPE_OTHER_TEXT;
					} else if (info.getDisplayType() == SysConstant.DISPLAY_TYPE_IMAGE) {
						return MESSAGE_TYPE_OTHER_IMAGE;
					} else if (info.getDisplayType() == SysConstant.DISPLAY_TYPE_AUDIO) {
						return MESSAGE_TYPE_OTHER_AUDIO;
					}
				}
			}
			return MESSAGE_TYPE_INVALID;
		} catch (Exception e) {
			logger.e(e.getMessage());
			return MESSAGE_TYPE_INVALID;
		}
	}

	private int getMenuType(MessageInfo msg) {
		if (msg.getDisplayType() == SysConstant.DISPLAY_TYPE_TEXT) {
			return SysConstant.POPUP_MENU_TYPE_TEXT;
		} else if (msg.getDisplayType() == SysConstant.DISPLAY_TYPE_IMAGE) {
			return SysConstant.POPUP_MENU_TYPE_IMAGE;
		} else if (msg.getDisplayType() == SysConstant.DISPLAY_TYPE_AUDIO) {
			return SysConstant.POPUP_MENU_TYPE_AUDIO;
		} else {
			return -1;
		}
	}

	private View getBaseViewForMenu(MessageHolderBase holder, MessageInfo msg) {
		if (msg.getDisplayType() == SysConstant.DISPLAY_TYPE_TEXT) {
			return ((TextMessageHolder) holder).message_content;
		} else if (msg.getDisplayType() == SysConstant.DISPLAY_TYPE_IMAGE) {
			return ((ImageMessageHolder) holder).message_layout;
		} else if (msg.getDisplayType() == SysConstant.DISPLAY_TYPE_AUDIO) {
			return ((AudioMessageHolder) holder).message_layout;
		} else {
			return null;
		}
	}

	@Override
	public int getViewTypeCount() {
		return VIEW_TYPE_COUNT;
	}

	public void hidePopup() {
		if (currentPopupView != null) {
			currentPopupView.dismiss();
		}
	}

	private void fillBaseMessageholder(MessageHolderBase holder,
			View convertView) {
		holder.portrait = (ImageView) convertView.findViewById(R.id.user_portrait);
		holder.messageFailed = (ImageView) convertView.findViewById(R.id.message_state_failed);
		holder.loadingProgressBar = (ProgressBar) convertView.findViewById(R.id.progressBar1);

		holder.name = (TextView) convertView.findViewById(R.id.name);
		logger.d("name#holder.name:%s", holder.name);
	}

	private void fillTextMessageHolder(TextMessageHolder holder,
			View convertView) {
		fillBaseMessageholder(holder, convertView);

		holder.message_content = (TextView) convertView.findViewById(R.id.message_content);
	}

	private void fillImageMessageHolder(ImageMessageHolder holder,
			View convertView) {
		fillBaseMessageholder(holder, convertView);
		holder.message_layout = convertView.findViewById(R.id.message_layout);
		holder.message_image = (ImageView) convertView.findViewById(R.id.message_image);
		holder.image_progress = (MGProgressbar) convertView.findViewById(R.id.tt_image_progress);
		holder.image_progress.setShowText(false);
	}

	private void fillAudioMessageHolder(AudioMessageHolder holder,
			View convertView) {
		fillBaseMessageholder(holder, convertView);

		holder.message_layout = convertView.findViewById(R.id.message_layout);
		holder.audio_antt_view = convertView.findViewById(R.id.audio_antt_view);
		holder.audio_duration = (TextView) convertView.findViewById(R.id.audio_duration);
		holder.audio_unread_notify = convertView.findViewById(R.id.audio_unread_notify);
	}

	private void handleTextMessage(final TextMessageHolder holder,
			final MessageInfo info, final View parent) {
		if (null == holder || null == info) {
			return;
		}
		holder.message_content.setText(Emoparser.getInstance(context).emoCharsequence(info.getMsgContent()));

		holder.message_content.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				showMenu(context, SysConstant.POPUP_MENU_TYPE_TEXT, parent, info, holder.message_content);
				return true;
			}
		});

		holder.message_content.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				CommonUtil.skipLink(context, info.getMsgContent());
			}
		});
		holder.message_content.setOnTouchListener(new onDoubleClick(info.getMsgContent()));

		handlePortraitClick(holder, info);
	}

	private void handlePortraitClick(MessageHolderBase holder,
			final MessageInfo msg) {
		if (holder == null || msg == null) {
			return;
		}

		if (holder.portrait != null) {
			holder.portrait.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					IMUIHelper.openUserProfileActivity(context, msg.talkerId);
				}
			});
		}
	}

	/**
	 * @Description 处理图片消息
	 * @param holder
	 * @param info
	 * @param position
	 * @param isMine
	 * @param parent
	 */
	private void handleImageMessage(final ImageMessageHolder holder,
			final MessageInfo info, int position, boolean isMine,
			final View parent) {
		logger.d("chat#handleImageMessage");

		handlePortraitClick(holder, info);

		try {
			boolean setBackground = false;// 图片加载失败时是否显示背景
			if (null == holder || null == info)
				return;

			if (isMine) {
				logger.d("chat#is mine");
				holder.message_image.setOnClickListener(new BtnImageListener(info, position, isMine));
			}

			String path = info.getSavePath();
			logger.d("chat#save path is:%s", path);

			@SuppressWarnings("unused")
			String url = info.getUrl();
			logger.d("chat#url:%s", url);

			Bitmap bmp = null;
			if (!TextUtils.isEmpty(path)) {
				bmp = MessageBitmapCache.getInstance().get(path);
				if (isMine) {
					bmp = BubbleImageHelper.getInstance(context).getBubbleImageBitmap(bmp, R.drawable.tt_mine_image_default_bk);
				} else {
					bmp = BubbleImageHelper.getInstance(context).getBubbleImageBitmap(bmp, R.drawable.tt_other_image_default_bk);
				}
			}

			int msgLoadState = info.getMsgLoadState();
			logger.d("chat#msgloadState:%d, msgId:%s, msg:%s", msgLoadState, info.msgId, info);

			switch (msgLoadState) {
				case SysConstant.MESSAGE_STATE_UNLOAD : {
					logger.d("chat#pic#MESSAGE_STATE_UNLOAD");
					holder.message_image.setImageResource(R.drawable.tt_default_message_image);
					setBackground = true;
					holder.image_progress.showProgress();
					// no matter the message belongs to myself(in muti-login
					// situation) or the peer,
					// we use the state to load the message
					// if (!isMine) {
					logger.d("chat#pic#not mine,  need donwload");
					downLoadImage(info);
					// } else {
					// holder.message_image.setLayoutParams(new
					// FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					// LayoutParams.MATCH_PARENT));
					// holder.message_layout.setBackgroundResource(0);
					// holder.message_image.setImageBitmap(bmp);
					// holder.image_progress.hideProgress();
					// setBackground = false;
					// }
				}
					break;

				case SysConstant.MESSAGE_STATE_LOADDING : {
					logger.d("chat#pic#MESSAGE_STATE_LOADDING");
					if (null != bmp) {
						holder.message_image.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
						holder.message_layout.setBackgroundResource(0);
						holder.message_image.setImageBitmap(bmp);
						setBackground = false;
					} else {
						holder.message_image.setImageResource(R.drawable.tt_default_message_image);
						setBackground = true;
					}
					holder.image_progress.showProgress();
				}
					break;

				case SysConstant.MESSAGE_STATE_FINISH_SUCCESSED : {
					logger.d("chat#pic#MESSAGE_STATE_FINISH_SUCCESSED");
					if (null != bmp) {
						holder.message_image.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
						holder.message_layout.setBackgroundResource(0);
						holder.message_image.setImageBitmap(bmp);
						if (!isMine) {
							holder.message_image.setOnClickListener(new BtnImageListener(info, position, isMine));
						}
						setBackground = false;
					} else {
						holder.message_image.setImageResource(R.drawable.tt_default_message_error_image);
						setBackground = true;
					}
					holder.image_progress.hideProgress();

				}
					break;

				case SysConstant.MESSAGE_STATE_FINISH_FAILED : {
					holder.message_image.setOnClickListener(new BtnImageListener(info, position, isMine));
					if (null != bmp) {
						holder.message_image.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
						holder.message_layout.setBackgroundResource(0);
						holder.message_image.setImageBitmap(bmp);
						setBackground = false;
					} else {
						holder.message_image.setImageResource(R.drawable.tt_default_message_error_image);
						setBackground = true;
					}
					holder.image_progress.hideProgress();
				}
					break;

				default : {
					holder.message_image.setImageResource(R.drawable.tt_default_message_error_image);
					setBackground = true;
					holder.image_progress.hideProgress();
				}
					break;
			}

			if (setBackground) {
				FrameLayout.LayoutParams imageLayout = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
				if (isMine) {
					holder.message_layout.setBackgroundResource(R.drawable.tt_mine_item_bg);
					imageLayout.rightMargin = 9;
				} else {
					holder.message_layout.setBackgroundResource(R.drawable.tt_other_default_image_bk);
					imageLayout.leftMargin = 9;
				}
				imageLayout.gravity = Gravity.CENTER;
				holder.message_image.setLayoutParams(imageLayout);
			}

			holder.message_image.setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					showMenu(context, SysConstant.POPUP_MENU_TYPE_IMAGE, parent, info, holder.message_layout);
					return true;
				}
			});
		} catch (Exception e) {
			logger.e(e.getMessage());
		}
	}

	/**
	 * @Description 处理语音消息
	 * @param holder
	 * @param info
	 * @param isMine
	 * @param parent
	 * @param position
	 */
	private void handleAudioMessage(final AudioMessageHolder holder,
			final MessageInfo info, boolean isMine, final View parent,
			int position) {
		logger.d("chat#handleAudioMessage");
		try {
			handlePortraitClick(holder, info);

			holder.message_layout.setOnClickListener(new BtnImageListener(info, position, isMine));
			if (null != info.getSavePath()) {
				holder.audio_antt_view.setBackgroundResource(isMine
						? R.anim.tt_voice_play_mine
						: R.anim.tt_voice_play_other);
				AnimationDrawable animationDrawable = (AnimationDrawable) holder.audio_antt_view.getBackground();
				if (null != playingPath
						&& playingPath.equals(info.getSavePath())) {
					animationDrawable.start();
				} else {
					if (animationDrawable.isRunning()) {
						animationDrawable.stop();
						animationDrawable.selectDrawable(0);
					}
				}

				if (info.getMsgReadStatus() < SysConstant.MESSAGE_DISPLAYED
						&& !isMine) {
					holder.audio_unread_notify.setVisibility(View.VISIBLE);
				} else {
					holder.audio_unread_notify.setVisibility(View.GONE);
				}

				holder.audio_duration.setText(String.valueOf(info.getPlayTime()) + '"');

				holder.message_layout.setOnLongClickListener(new View.OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
						showMenu(context, SysConstant.POPUP_MENU_TYPE_AUDIO, parent, info, holder.message_layout);
						return true;
					}
				});
				RelativeLayout.LayoutParams layoutParam = new RelativeLayout.LayoutParams(CommonUtil.getAudioBkSize(info.getPlayTime(), context), LayoutParams.WRAP_CONTENT);
				holder.message_layout.setLayoutParams(layoutParam);
				if (isMine) {
					layoutParam.addRule(RelativeLayout.RIGHT_OF, R.id.audio_duration);
					RelativeLayout.LayoutParams param = (android.widget.RelativeLayout.LayoutParams) holder.audio_duration.getLayoutParams();
					param.addRule(RelativeLayout.RIGHT_OF, R.id.message_state_failed);
					param.addRule(RelativeLayout.RIGHT_OF, R.id.progressBar1);
				}
			}

			logger.d("chat#finish handleAudioMessage");
		} catch (Exception e) {
			logger.d("chat#find exception:%s", e.getMessage());
			logger.e(e.getMessage());
		}
	}

	class BtnImageListener implements OnClickListener {
		private MessageInfo msgInfo = null;
		private int position = 0;
		private boolean isMine = false;

		public BtnImageListener(MessageInfo msg, int p, boolean me) {
			this.msgInfo = msg;
			this.position = p;
			this.isMine = me;
		}

		@Override
		public void onClick(View v) {
			try {
				if (msgInfo.getDisplayType() == SysConstant.DISPLAY_TYPE_AUDIO) {
					if (!new File(msgInfo.getSavePath()).exists()) {
						Toast.makeText(context, context.getResources().getString(R.string.notfound_audio_file), Toast.LENGTH_LONG).show();
						return;
					}
					if (msgInfo.getMsgReadStatus() < SysConstant.MESSAGE_DISPLAYED) {
						logger.d("chat#audio#set audio meessage read status");

						updateItemReadState(msgInfo.msgId, SysConstant.MESSAGE_DISPLAYED);

						IMService imService = imServiceHelper.getIMService();
						if (imService != null) {
							msgInfo.setMsgReadStatus(SysConstant.MESSAGE_DISPLAYED);
							imService.getDbManager().updateMessageContent(msgInfo);
						}
					}

					if (AudioPlayerHandler.getInstance().isPlaying()) {
						AudioPlayerHandler.getInstance().stopPlayer();
						if (playingPath.equals(msgInfo.getSavePath())) {
							return;
						}
					}

					final AnimationDrawable animationDrawable = (AnimationDrawable) v.findViewById(R.id.audio_antt_view).getBackground();
					audioPathAnimMap.put(msgInfo.getSavePath(), animationDrawable);
					playingPath = msgInfo.getSavePath();

					// 延迟播放
					Thread myThread = new Thread() {
						public void run() {
							try {
								Thread.sleep(200);
								AudioPlayerHandler.getInstance().startPlay(msgInfo.getSavePath());
								animationDrawable.start();
							} catch (Exception e) {
								e.printStackTrace();
								logger.e(e.toString());
							}
						}
					};
					myThread.start();

				} else if (msgInfo.getDisplayType() == SysConstant.DISPLAY_TYPE_IMAGE) {

					if (msgInfo.getMsgLoadState() == SysConstant.MESSAGE_STATE_FINISH_FAILED
							&& !isMine) {

						if (FileUtil.isSdCardAvailuable()) {
							updateItemState(msgInfo.msgId, SysConstant.MESSAGE_STATE_UNLOAD);
						} else {
							Toast.makeText(context, context.getString(R.string.sdcard_unavaluable), Toast.LENGTH_LONG).show();
						}

					} else {
						// msgIndexMap.put(msgInfo.msgId, position);
						//                        Intent i = new Intent(context, DisplayImageActivity.class);
						//                        Bundle bundle = new Bundle();
						//                        bundle.putSerializable(SysConstant.CUR_MESSAGE, msgInfo);
						//                        bundle.putBoolean("ISMINE", isMine);
						//                        i.putExtras(bundle);
						//                        context.startActivity(i);
						//                        context.overridePendingTransition(R.anim.tt_image_enter, R.anim.tt_stay);

						if (context != null) {
							Intent intent = new Intent(context, DetailPortraitActivity.class);

							String imageResPath = msgInfo.getSavePath();
							logger.d("displayimage#imageResPath:%s", imageResPath);

							if (TextUtils.isEmpty(imageResPath)) {
								logger.e("displayimage#no local path found");
								return;
							}
							//to match universal image loader format
							imageResPath = "file:///" + imageResPath;

							logger.d("displayimage#imageResourcePath:%s", imageResPath);

							intent.putExtra(SysConstant.KEY_AVATAR_URL, imageResPath);
							intent.putExtra(SysConstant.KEY_DONT_MODIFY_IMAGE_URI, true);
							context.startActivity(intent);
						}
					}
				}
			} catch (Exception e) {
				logger.e(e.getMessage());
			}
		}
	}

	private static class MessageHolderBase {
		/**
		 * 头像
		 */
		ImageView portrait;

		/**
		 * 消息状态
		 */
		ImageView messageFailed;

		ProgressBar loadingProgressBar;

		TextView name;
	}

	private static class TextMessageHolder extends MessageHolderBase {
		/**
		 * 文字消息体
		 */
		TextView message_content;
	}

	private static class ImageMessageHolder extends MessageHolderBase {
		/**
		 * 可点击的view
		 */
		View message_layout;

		/**
		 * 图片消息体
		 */
		ImageView message_image;

		/**
		 * 图片状态指示
		 */
		MGProgressbar image_progress;
	}

	private static class AudioMessageHolder extends MessageHolderBase {
		/**
		 * 可点击的消息体
		 */
		View message_layout;

		/**
		 * 播放动画的view
		 */
		View audio_antt_view;

		/**
		 * 指示语音是否被播放
		 */
		View audio_unread_notify;

		/**
		 * 语音时长
		 */
		TextView audio_duration;
	}

	private static class TimeTitleMessageHodler {
		TextView time_title;
	}

	/**
	 * @Description 显示菜单
	 * @param cxt
	 * @param listener
	 * @param Parent
	 * @param msg
	 */
	private void showMenu(Context cxt, int menuType, View parent,
			MessageInfo msg, View layout) {
		boolean bIsSelf = msg.getMsgFromUserId().equals(CacheHub.getInstance().getLoginUserId());
		OperateItemClickListener listener = new OperateItemClickListener(menuType);
		listener.setMessageInfo(msg);

		MessageOperatePopup popupView = new MessageOperatePopup(context, parent);
		popupView.setOnItemClickListener(listener);
		currentPopupView = popupView;

		boolean bResend = msg.getMsgLoadState() == SysConstant.MESSAGE_STATE_FINISH_FAILED;
		popupView.show(layout, menuType, bResend, bIsSelf);

	}

	private class OperateItemClickListener
			implements
				MessageOperatePopup.OnItemClickListener {

		private MessageInfo mMsgInfo;
		/*
		 * type = 1 为文本 type = 2 为图片 type = 3 为语音
		 */
		private int mType;

		public OperateItemClickListener(int nType) {
			this.mType = nType;
		}

		public void setMessageInfo(MessageInfo msgInfo) {
			// logger.d("debug#setMessageInfo msgInfo:%s, this:%s", msgInfo,
			// this);

			mMsgInfo = msgInfo;
		}

		@SuppressWarnings("deprecation")
		@SuppressLint("NewApi")
		@Override
		public void onCopyClick() {
			try {
				ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

				logger.d("menu#onCopyClick content:%s", mMsgInfo.getMsgContent());
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
					ClipData data = ClipData.newPlainText("data", mMsgInfo.getMsgContent());
					manager.setPrimaryClip(data);
				} else {
					manager.setText(mMsgInfo.getMsgContent());
				}
			} catch (Exception e) {
				logger.e(e.getMessage());
			}
		}

		@Override
		public void onResendClick() {
			try {
				if (mType == SysConstant.POPUP_MENU_TYPE_TEXT
						|| mType == SysConstant.POPUP_MENU_TYPE_AUDIO) {

					if (mMsgInfo.getDisplayType() == SysConstant.DISPLAY_TYPE_AUDIO) {
						if (null == mMsgInfo.getAudioContent()) {
							MessageHelper.setMsgAudioContent(mMsgInfo);
						}
					}
				} else if (mType == SysConstant.POPUP_MENU_TYPE_IMAGE) {
					logger.d("pic#resend");

					mMsgInfo.setMsgLoadState(SysConstant.MESSAGE_STATE_LOADDING);
					updateItemState(mMsgInfo.msgId, SysConstant.MESSAGE_STATE_LOADDING);
				}

				resendMsg(mMsgInfo.msgId);

			} catch (Exception e) {
				logger.e("chat#exception:" + e.toString());
			}

		}

		private void resendMsg(String msgId) {
			MessageInfo msgInfo = getMsgInfo(msgId);
			if (msgInfo == null) {
				logger.d("chat#resend#getMsgInfo failed.id:%s", msgId);
				return;
			}

			imService = imServiceHelper.getIMService();
			if (imService != null) {
				imService.getMessageManager().resendMessage(msgInfo);
			}
		}

		@Override
		public void onSpeakerClick() {
			if (MessageActivity.getAudioMode() == SysConstant.AUDIO_PLAY_MODE_NORMAL) {
				MessageActivity.setAudioMode(SysConstant.AUDIO_PLAY_MODE_IN_CALL);
				SpeekerToast.show(context, context.getText(R.string.audio_in_call), Toast.LENGTH_SHORT);
			} else {
				MessageActivity.setAudioMode(SysConstant.AUDIO_PLAY_MODE_NORMAL);
				SpeekerToast.show(context, context.getText(R.string.audio_in_speeker), Toast.LENGTH_SHORT);
			}
		}
	}

	/**
	 * @Description 下载图片信息
	 * @param messageInfo
	 */
	private void downLoadImage(MessageInfo messageInfo) {
		try {
			if (null == messageInfo
					|| messageInfo.getMsgLoadState() == SysConstant.MESSAGE_STATE_FINISH_SUCCESSED
					|| messageInfo.getMsgLoadState() == SysConstant.MESSAGE_STATE_LOADDING)
				return;

			if (null == context)
				return;

			messageInfo.setMsgLoadState(SysConstant.MESSAGE_STATE_LOADDING);
			updateItemState(messageInfo.msgId, SysConstant.MESSAGE_STATE_LOADDING);

			// todo eric
			// final String smallImageUrl =
			// SysConstant.DOWNLOAD_IMAGE_URL_REPFIX
			// + messageInfo.getUrl();//
			// StringUtil.getSmallerImageLink(messageInfo.getUrl());
			final String smallImageUrl = messageInfo.getUrl();

			logger.d("chat#pic#download image ulr:%s", smallImageUrl);

			String smallImagePath = CommonUtil.getMd5Path(smallImageUrl, SysConstant.FILE_SAVE_TYPE_IMAGE);

			logger.d("chat#pic#image save path:%s", smallImagePath);

			messageInfo.setSavePath(smallImagePath);

			File myFile = new File(smallImagePath);
			if (myFile.exists()) {
				logger.d("chat#pic#image file already exists");
				updateMessageState(messageInfo, SysConstant.MESSAGE_STATE_FINISH_SUCCESSED);
				return;
			}

			final MessageInfo messageInfoFinal = messageInfo;

			ImageLoader.getInstance().loadImage(smallImageUrl, null, null, new SimpleImageLoadingListener() {
				// message_image

				@Override
				public void onLoadingComplete(String imageUri, View view,
						Bitmap bitmap) {
					logger.d("chat#pic#icon onLoadingComplete");
					// holder.image.setImageBitmap(loadedImage);

					logger.d("chat#pic#onBitmapLoaded");
					String smallImagePath = CommonUtil.getMd5Path(smallImageUrl, SysConstant.FILE_SAVE_TYPE_IMAGE);
					File myFile = new File(smallImagePath);
					if (myFile.exists()) {
						logger.d("chat#pic#image already exists, no need to save");
						return;
					}
					BufferedOutputStream bos = null;
					try {
						if (null != bitmap) {
							FileOutputStream fout = new FileOutputStream(myFile);
							bos = new BufferedOutputStream(fout);
							bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
							bos.flush();
							bos.close();
							bos = null;
							logger.d("chat#pic#save image ok");
							updateMessageState(messageInfoFinal, SysConstant.MESSAGE_STATE_FINISH_SUCCESSED);
						}
					} catch (Exception e) {
						logger.e("chat#pic#downloading image got exception:%s", e.getMessage());
					} finally {
						try {
							if (null != bos) {
								bos.flush();
								bos.close();
							}
						} catch (IOException e) {
							e.printStackTrace();

						}
					}
				}

				@Override
				public void onLoadingFailed(String imageUri, View view,
						FailReason failReason) {
					logger.d("chat#pic#icon onLoadingFailed");

					logger.d("chat#pic#onBitmapFailed");
					updateMessageState(messageInfoFinal, SysConstant.MESSAGE_STATE_FINISH_FAILED);
					logger.d("download failed");
				}

			});

			// MGWebImageView.fetchBitmap(context, smallImageUrl, new
			// MGWebImageView.TargetCallback() {
			// @Override
			// public void onPrepareLoad(Drawable placeHolderDrawable) {
			// }
			//
			// @Override
			// public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
			// logger.d("chat#pic#onBitmapLoaded");
			// String smallImagePath = CommonUtil.getMd5Path(smallImageUrl,
			// SysConstant.FILE_SAVE_TYPE_IMAGE);
			// File myFile = new File(smallImagePath);
			// if (myFile.exists()) {
			// logger.d("chat#pic#image already exists, no need to save");
			// return;
			// }
			// BufferedOutputStream bos = null;
			// try {
			// if (null != bitmap) {
			// FileOutputStream fout = new FileOutputStream(myFile);
			// bos = new BufferedOutputStream(fout);
			// bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			// bos.flush();
			// bos.close();
			// bos = null;
			// logger.d("chat#pic#save image ok");
			// updateMessageState(messageInfoFinal,
			// SysConstant.MESSAGE_STATE_FINISH_SUCCESSED);
			// }
			// } catch (Exception e) {
			// logger.e("chat#pic#downloading image got exception:%s",
			// e.getMessage());
			// } finally {
			// try {
			// if (null != bos) {
			// bos.flush();
			// bos.close();
			// }
			// } catch (IOException e) {
			// e.printStackTrace();
			//
			// }
			// }
			// }
			//
			// @Override
			// public void onBitmapFailed(Drawable errorDrawable) {
			// logger.d("chat#pic#onBitmapFailed");
			// updateMessageState(messageInfoFinal,
			// SysConstant.MESSAGE_STATE_FINISH_FAILED);
			// logger.d("download failed");
			// }
			// });

		} catch (Exception e) {
			logger.e(e.getMessage());
		}
	}

	/**
	 * @Description 更新消息的已读状态
	 * @param msgId
	 * @param readStatus
	 */
	private void updateItemReadState(String msgId, int readStatus) {
		try {

			MessageInfo msgInfo = getMsgInfo(msgId);
			if (msgInfo == null) {
				logger.e("chat#error can't find msgInfo:%s", msgId);
				return;
			}

			msgInfo.setMsgReadStatus(readStatus);

			// CacheHub.getInstance().updateMsgReadStatus(msgId, readStatus);

			notifyDataSetChanged();
		} catch (Exception e) {
			logger.e(e.getMessage());
		}
	}

	/**
	 * @Description 添加历史分隔线
	 */
	public void addHistoryDivideTag() {
		try {
			if (!mHistoryFirstAdd) {
				return;
			}

			mHistoryFirstAdd = false;

			// msgIndexMap.fix(0, 1);

			messageList.add(0, HISTORY_DIVIDER_TAG);
		} catch (Exception e) {
			logger.e(e.getMessage());
		}
	}

	/**
	 * @Description 获取正在播放语音的路径
	 * @return
	 */
	public static String GetPlayingPath() {
		return playingPath;
	}

	/**
	 * @Description 判断是否正在播放语音
	 * @return
	 */
	public boolean isAudioPlaying() {
		if (TextUtils.isEmpty(playingPath)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @Description 双击事件
	 * @author Nana
	 * @date 2014-7-30
	 */
	class onDoubleClick implements View.OnTouchListener {
		int count = 0;
		int firClick = 0;
		int secClick = 0;
		String content = null;

		private onDoubleClick(String txt) {
			content = txt;
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (MotionEvent.ACTION_DOWN == event.getAction()) {
				count++;
				if (count == 1) {
					firClick = (int) System.currentTimeMillis();

				} else if (count == 2) {
					secClick = (int) System.currentTimeMillis();
					if (secClick - firClick < 1000) {
						if (v.getId() == R.id.message_content) {
							Intent intent = new Intent(context, PreviewTextActivity.class);
							intent.putExtra(SysConstant.PREVIEW_TEXT_CONTENT, content);
							context.startActivity(intent);
						}
					}
					count = 0;
					firClick = 0;
					secClick = 0;
				}
			}
			return false;
		}
	}

}
