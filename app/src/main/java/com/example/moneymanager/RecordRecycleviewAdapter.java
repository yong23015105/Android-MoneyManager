package com.example.moneymanager;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
// this may can understand by: 'RecordRecycleviewAdapter' is a robot that made by a type of robot 'RecyclerView.Adapter'
// and uses machine part of RecordRecycleviewAdapter.ViewHolder.
// 'RecordRecycleviewAdapter' able to do what 'RecyclerView.Adapter' type of robot can do

public class RecordRecycleviewAdapter extends RecyclerView.Adapter<RecordRecycleviewAdapter.ViewHolder> {
    private Cursor mCursor;
    private Context mContext;
    private String mUserID;

    // dis is constructor
    public RecordRecycleviewAdapter(Context context, Cursor cursor, String userId) {
        // context can be many things such as an activity
        this.mContext = context;
        this.mCursor = cursor;
        this.mUserID = userId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // get UI
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_holder, parent, false);
        // give UI to view holder
        return new ViewHolder(view);
    }

    //base on view holder blueprint build every transaction record
    @Override
    public void onBindViewHolder(ViewHolder recordHolder, int position) {
        if (mCursor.moveToPosition(position)) {
            //check the database have column ID or not, if have fetch id, else set id to -1
            int idIndex = mCursor.getColumnIndex("ID");
            // set id to -1 to tell that this value got problem
            long id = idIndex != -1 ? mCursor.getLong(idIndex) : -1;
            recordHolder.id = id;

            int nameIndex = mCursor.getColumnIndex("Name");
            int typeIndex = mCursor.getColumnIndex("Type");
            int amountIndex = mCursor.getColumnIndex("Amount");
            int dateIndex = mCursor.getColumnIndex("Date");
            int stateIndex = mCursor.getColumnIndex("State");
            int descriptionIndex = mCursor.getColumnIndex("Description");

            String name = nameIndex != -1 ? mCursor.getString(nameIndex) : "No Name";
            String type = typeIndex != -1 ? mCursor.getString(typeIndex) : "Unknown Type";
            double amount = amountIndex != -1 ? mCursor.getDouble(amountIndex) : 0.0;
            String date = dateIndex != -1 ? mCursor.getString(dateIndex) : "Unknown Date";
            String state = stateIndex != -1 ? mCursor.getString(stateIndex) : "Unknown State";
            String description = descriptionIndex != -1 ? mCursor.getString(descriptionIndex) : "No Description";

            recordHolder.mNameTextView.setText(name);
            recordHolder.mTypeTextView.setText(type);
            recordHolder.mAmountTextView.setText("RM " + amount);
            recordHolder.mDateTextView.setText(date);
            recordHolder.mStateTextView.setText(state);
            recordHolder.mDescriptionTextView.setText(description);

            recordHolder.itemView.setOnClickListener(v -> {
                //check is mContext is main page if yes turn mContext into main page type so it can use the function of main page
                //and main page is an activity somehow context also turns into activity
                if (mContext instanceof Main_Page) {
                    Main_Page mainPage = (Main_Page) mContext;
                    mainPage.showDialog(name, type, amount, date, state, description, id);
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        // usage of '?:' : if mCursor != null is true, return mCursor.getCount else return 0
        return mCursor != null ? mCursor.getCount() : 0;
    }

    public void changeCursor(Cursor newCursor) {
        // replace old cursor with new one so the recycle view can refresh
        if (newCursor != null && newCursor != mCursor) {
            if (mCursor != null) {
                mCursor.close();
            }

            mCursor = newCursor;
            notifyDataSetChanged();
        }
    }

    //somehow holds the ui blueprint of every transaction record
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mNameTextView;
        TextView mTypeTextView;
        TextView mAmountTextView;
        TextView mDateTextView;
        TextView mStateTextView;
        TextView mDescriptionTextView;
        long id;

        public ViewHolder(View itemView) {
            super(itemView);
            mNameTextView = itemView.findViewById(R.id.nameTextView);
            mTypeTextView = itemView.findViewById(R.id.typeTextView);
            mAmountTextView = itemView.findViewById(R.id.amountTextView);
            mDateTextView = itemView.findViewById(R.id.dateTextView);
            mStateTextView = itemView.findViewById(R.id.StateTextView);
            mDescriptionTextView = itemView.findViewById(R.id.descriptionTextView);
        }
    }
}

