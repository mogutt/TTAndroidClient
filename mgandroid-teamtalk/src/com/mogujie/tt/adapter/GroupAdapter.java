
package com.mogujie.tt.adapter;

import java.util.List;

import com.mogujie.tt.R;
import com.mogujie.tt.log.Logger;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GroupAdapter extends BaseAdapter {

    private Context context = null;

    private List<String> list = null;

    private static Logger logger = Logger.getLogger(GroupAdapter.class);

    public GroupAdapter(Context context, List<String> list) {

        this.context = context;
        this.list = list;

    }

    @Override
    public int getCount() {
        if (null == list) {
            return 0;
        } else {
            return list.size();
        }
    }

    @Override
    public Object getItem(int position) {
        if (position >= getCount() || position < 0) {
            return null;
        }
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (position >= getCount() && getCount() > 0) {
            return getCount() - 1;
        } else if (position < 0) {
            return 0;
        }
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        try {
            ViewHolder holder = null;
            if (null == convertView) {
                if (null != context) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.tt_popup_item_view,
                            null);
                    if (null != convertView) {
                        holder = new ViewHolder();
                        holder.groupItem = (TextView) convertView.findViewById(R.id.groupItem);
                        convertView.setTag(holder);
                    }
                }

            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (null == holder) {
                return null;
            }

            holder.groupItem.setTextColor(Color.BLACK);
            holder.groupItem.setText(list.get(position));

            return convertView;
        } catch (Exception e) {
            logger.e(e.getMessage());
            return null;
        }
    }

    static class ViewHolder {
        TextView groupItem;
    }

}
