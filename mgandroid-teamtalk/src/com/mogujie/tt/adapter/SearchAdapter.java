
package com.mogujie.tt.adapter;

import java.text.ParseException;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mogujie.tt.R;
import com.mogujie.tt.biz.SearchHelper;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.SearchResultItem;
import com.mogujie.tt.log.Logger;
import com.mogujie.widget.imageview.MGWebImageView;

public class SearchAdapter extends BaseAdapter {
    private LayoutInflater inflater = null;
    private static Logger logger = Logger.getLogger(SearchAdapter.class);

    public SearchAdapter(Context context) throws ParseException {
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        int count = SearchHelper.getResultList().size();
        return count;
    }

    @Override
    public Object getItem(int position) {
        if (position >= getCount() || position < 0) {
            return null;
        }
        return SearchHelper.getResultList().get(position);
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
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            SearchViewHolder holder = null;
            if (null == convertView && null != inflater) {
                convertView = inflater.inflate(R.layout.tt_item_searchresult, null);
                if (null != convertView) {
                    holder = initHolder(convertView);
                    convertView.setTag(holder);
                }

            } else {
                holder = (SearchViewHolder) convertView.getTag();
            }

            setHolder(holder, SearchHelper.getResultList().get(position));

            return convertView;
        } catch (Exception e) {
            logger.e(e.getMessage());
            return null;
        }
    }

    private void setHolder(SearchViewHolder holder, SearchResultItem item) {
        if (null == item || null == holder)
            return;
        if (item.getType() == SysConstant.CHAT_SEARCH_RESULT_TYPE_CATEGORY) {
            holder.categoryTitle.setText(item.getTitle());
            holder.categoryTitle.setVisibility(View.VISIBLE);
            holder.itemContainer.setVisibility(View.GONE);
        } else {
            holder.categoryTitle.setVisibility(View.GONE);
            if (null == item.getAvatar() || !item.getAvatar().contains("http")) {
                holder.avatar.setImageResource(R.drawable.tt_default_user_portrait_corner);
            } else {
                holder.avatar.setImageUrlNeedFit(item.getAvatar());
            }
            holder.itemContainer.setVisibility(View.VISIBLE);

            holder.title.setText(item.getTitle());
            holder.content.setText(item.getContent());
        }

    }

    private SearchViewHolder initHolder(View convertView) {
        SearchViewHolder holder = new SearchViewHolder();
        holder.avatar = (MGWebImageView) convertView.findViewById(R.id.contact_portrait);
        holder.title = (TextView) convertView.findViewById(R.id.shop_name);
        holder.content = (TextView) convertView.findViewById(R.id.message_body);
        holder.categoryTitle = (TextView) convertView.findViewById(R.id.contact_category_title);
        holder.itemContainer = (RelativeLayout) convertView.findViewById(R.id.item_container);
        return holder;
    }

    public static final class SearchViewHolder {
        public MGWebImageView avatar;
        public TextView title;
        public TextView content;
        public TextView categoryTitle;
        public RelativeLayout itemContainer;
    }
}
