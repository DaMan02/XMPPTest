package com.dayal.xmpptest2;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dayal.xmpptest2.database.GroupDatabase;
import com.dayal.xmpptest2.fragments.ContactFragment;
import com.dayal.xmpptest2.fragments.GroupFragment;
import com.dayal.xmpptest2.fragments.InviteFragment;
import com.dayal.xmpptest2.models.Group;
import com.dayal.xmpptest2.utils.Util;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TabbedActivity extends AppCompatActivity {

    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.viewpager)
    ViewPager vPager;

    AlertDialog.Builder builder;
    AlertDialog dialog;
    private GroupDatabase db;

    public FragmentRefreshListener getFragmentRefreshListener() {
        return fragmentRefreshListener;
    }

    public void setFragmentRefreshListener(FragmentRefreshListener fragmentRefreshListener) {
        this.fragmentRefreshListener = fragmentRefreshListener;
    }

    private FragmentRefreshListener fragmentRefreshListener;

    private static final String TAG = "TabbedActivity";
       @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        ButterKnife.bind(this);

           db = new GroupDatabase(this);
           builder = new AlertDialog.Builder(this);

        MyPagerAdapter viewPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());

        getSupportActionBar().setTitle("MY APP");

        vPager.setAdapter(viewPagerAdapter);

        tabLayout.setupWithViewPager(vPager);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contact_list, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

//           Button createGpBtn;
//           final EditText editGpName;
        if(item.getItemId() == R.id.xmpp_logout)
        {
            //Disconnect from server

            Intent loginIntent = new Intent(this,LoginActivity.class);
            startActivity(loginIntent);
//            Log.d(TAG,"Initiating the log out process");
            Intent i1 = new Intent(this,XmppConnectService.class);
            stopService(i1);

            //Finish this activity


            //Start login activity for user to login

            finish();

        }else  if(item.getItemId() == R.id.create_room){

            createGroup();

        }

        return super.onOptionsItemSelected(item);
    }


    public interface FragmentRefreshListener{
        void onRefresh();
    }
    public void createGroup() {
         Button createGpBtn;
        final EditText editGpName;

        View v = getLayoutInflater().inflate(R.layout.create_group_popup, null);
        createGpBtn = v.findViewById(R.id.dialog_create_gp_btn);
        editGpName = v.findViewById(R.id.edit_group_dialog);

        builder.setView(v);
        dialog = builder.create();
        dialog.show();


        createGpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editGpName.getText())) {
                    saveGroupToDatabase(editGpName.getText().toString());
                    String groupName = editGpName.getText().toString();

                    Intent i = new Intent();
                    i.setAction(XmppConnectService.CREATE_ROOM);
                    i.putExtra("EXTRA_GROUP_NAME",groupName);
                    Log.w(TAG,"gp name:" + groupName);
                    sendBroadcast(i);
                }
            }
        });



    }

    private void saveGroupToDatabase(String gpName) {

        Group g = new Group();
        g.setGpName(gpName);
        g.setAddress(gpName + "@"  + Util.GP_SERVICE_NAME);

        db.addGroup(g);
        if(getFragmentRefreshListener()!=null){
            getFragmentRefreshListener().onRefresh();
        }

        dialog.dismiss();
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {


        public MyPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {

                case 0:
                    GroupFragment groupFragment = new GroupFragment();
                    return groupFragment;

                case 1:
                    ContactFragment contactsFragment = new ContactFragment();
                    return contactsFragment;
                case 2:
                   return new InviteFragment();

            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return "GROUPS";

                case 1:
                    return "PRIVATE CHAT";

                case 2:
                    return "INVITATIONS";

                default:
                    return null;
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
