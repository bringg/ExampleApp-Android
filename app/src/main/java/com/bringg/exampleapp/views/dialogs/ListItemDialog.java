package com.bringg.exampleapp.views.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;

import com.bringg.exampleapp.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListItemDialog {

    private final String mTitle;
    private final List<String> mListItem;
    private final SelectedItemListener mOnSelectedItemListener;
    private final Context mContext;

    private ListItemDialog(Builder builder) {
        mContext = builder.context;
        mTitle = builder.title;
        mListItem = new ArrayList<>(builder.items);
        mOnSelectedItemListener = builder.onSelectItemListener;
    }

    public void show() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(mContext);
        builderSingle.setTitle(mTitle);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mContext, R.layout.select_dialog_singlechoice, mListItem);
        builderSingle.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mOnSelectedItemListener != null)
                    mOnSelectedItemListener.onSelectedItem(which, mListItem.get(which));
            }
        });
        builderSingle.show();
    }

    public static class Builder {
        private final Context context;
        private String title;
        private Collection<String> items;
        private SelectedItemListener onSelectItemListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setTitle(int id) {
            this.title = context.getString(id);
            return this;
        }

        public Builder setItems(Collection<String> items) {
            this.items = items;
            return this;
        }

        public Builder setOnSelectedItemListener(SelectedItemListener listener) {
            this.onSelectItemListener = listener;
            return this;
        }

        public ListItemDialog build() {
            return new ListItemDialog(this);
        }

    }

    public interface SelectedItemListener {
        void onSelectedItem(int index, String value);
    }
}
