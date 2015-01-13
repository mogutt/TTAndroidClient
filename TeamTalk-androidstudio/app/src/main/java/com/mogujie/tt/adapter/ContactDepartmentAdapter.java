
package com.mogujie.tt.adapter;

import java.util.List;
import java.util.Map;

import com.mogujie.tt.R;
import com.mogujie.tt.entity.ContactSortEntity;
import com.mogujie.tt.imlib.proto.ContactEntity;
import com.mogujie.tt.imlib.proto.DepartmentEntity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class ContactDepartmentAdapter extends BaseAdapter implements SectionIndexer,
        ContactBaseAdapter {
    private List<ContactSortEntity> list = null;

    public void setContactsData(Map<String, DepartmentEntity> departments,
			Map<String, ContactEntity> contacts) {
		// TODO Auto-generated method stub
		
	}

	private Context context;

    public ContactDepartmentAdapter(Context mContext, List<ContactSortEntity> list) {
        this.context = mContext;
        this.list = list;
    }

    public int getCount() {
        return this.list.size();
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup arg2) {
        ViewHolder viewHolder = null;
        final ContactSortEntity mContent = list.get(position);
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.tt_item_contact, null);
            viewHolder.tvTitle = (TextView) view.findViewById(R.id.contact_item_title);
            viewHolder.tvLetter = (TextView) view.findViewById(R.id.contact_category_title);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        // 根据position获取分类的首字母的char ascii值
        int section = getSectionForPosition(position);

        // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (position == getPositionForSection(section)) {
            viewHolder.tvLetter.setVisibility(View.VISIBLE);
            viewHolder.tvLetter.setText(mContent.getSortLetters());
        } else {
            viewHolder.tvLetter.setVisibility(View.GONE);
        }

        viewHolder.tvTitle.setText(this.list.get(position).getName());

        return view;

    }

    final static class ViewHolder {
        TextView tvLetter;
        TextView tvTitle;
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    @Override
    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = list.get(i).getSortLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public int getSectionForPosition(int position) {
        return list.get(position).getSortLetters().charAt(0);
    }

    @Override
    public void updateListView(List<ContactSortEntity> obj) {
        this.list = obj;
        notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        if (position < 0 || position >= getCount()) {
            return null;
        }
        return list.get(position);
    }
}
