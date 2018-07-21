package com.dayal.xmpptest2.fragments;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dayal.xmpptest2.R;
import com.dayal.xmpptest2.TabbedActivity;
import com.dayal.xmpptest2.database.GroupDatabase;
import com.dayal.xmpptest2.models.Group;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment {

    private RecyclerView groupsRecyclerView;
    private GroupAdapter mAdapter;
    
    private List<Group> groups;
    private List<Group> listItems;
    private GroupDatabase db;
    
    private static String TAG = "GroupFragment";

    public GroupFragment() {
        // Required empty public constructor
        Log.w(TAG,"GroupFragment()");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.w(TAG,"oncreateView()");

        View view = inflater.inflate(R.layout.fragment_group, container, false);
        db = new GroupDatabase(getContext());
        
        groupsRecyclerView = view.findViewById(R.id.group_list_recycler_view);

        groups = new ArrayList<>();
        listItems = new ArrayList<>();

        groupsRecyclerView.setHasFixedSize(true);
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new GroupAdapter(listItems);
        groupsRecyclerView.setAdapter(mAdapter);

        ((TabbedActivity)getActivity()).setFragmentRefreshListener(new TabbedActivity.FragmentRefreshListener() {
            @Override
            public void onRefresh() {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(GroupFragment.this).attach(GroupFragment.this).commit();
            }
        });

        setUpGroupRecyclerView();
        // Inflate the layout for this fragment
        return view;
    }
    
    public void setUpGroupRecyclerView() {

           listItems.clear();

        Log.w(TAG,"setting up recyclerview");
        //get from DB
        groups = db.getAllGroups();
        for(Group g : groups){
            Group group = new Group();
            group.setGpName(g.getGpName());
            group.setAddress(g.getAddress());
            group.setId(g.getId());

            listItems.add(group);
        }

        mAdapter.notifyDataSetChanged();
    }

    private class GroupHolder extends RecyclerView.ViewHolder
    {
        private TextView groupTextView;
        private Group mGroup;
        public GroupHolder ( View itemView)
        {
            super(itemView);

            groupTextView = itemView.findViewById(R.id.gp_name);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setMessage("Remove Group ?")
                            .setNegativeButton("No",null)
                            .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    GroupDatabase db = new GroupDatabase(getContext());
                                    db.deleteGroup(mGroup.getId());

                                    setUpGroupRecyclerView();
//                                    startActivity(getActivity().getIntent());
                                }
                            })
                            .show();

                    return true;
                }
            });

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    //Inside here we start the chat activity
//                    Intent intent = new Intent(getContext()
//                            ,MessageListActivity.class);
//                    intent.putExtra("EXTRA_CONTACT_NAME",mGroup.getGpName());
//                    intent.putExtra("EXTRA_CONTACT_JID",mGroup.getAddress());
//                    startActivity(intent);


//                }
//            });
        }


        public void bindGroup( Group group)
        {
            mGroup = group;
            if (mGroup == null)
            {
//                Log.d(TAG,"Trying to work on a null Group object ,returning.");
                return;
            }
            String gpName = mGroup.getGpName();

            groupTextView.setText(gpName);

        }
    }


    private class GroupAdapter extends RecyclerView.Adapter<GroupHolder>
    {
        private List<Group> mGroups;

        public GroupAdapter( List<Group> groupList)
        {
            mGroups = groupList;
        }

        @Override
        public GroupHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater
                    .inflate(R.layout.list_item_group, parent,
                            false);
            return new GroupHolder(view);
        }

        @Override
        public void onBindViewHolder(GroupHolder holder, int position) {
            Group group = mGroups.get(position);
            holder.bindGroup(group);

        }

        @Override
        public int getItemCount() {
            return mGroups.size();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

}
