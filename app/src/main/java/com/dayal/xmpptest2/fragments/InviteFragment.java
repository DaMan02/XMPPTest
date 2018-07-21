package com.dayal.xmpptest2.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dayal.xmpptest2.R;
import com.dayal.xmpptest2.XmppConnectService;
import com.dayal.xmpptest2.utils.Util;


/**
 * A simple {@link Fragment} subclass.
 */
public class InviteFragment extends Fragment {

    private Button inviteBtn;
    private TextView invitationText;
    private EditText inviteEditText;

    public InviteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_invite, container, false);

        inviteBtn = view.findViewById(R.id.invite_btn);
        invitationText = view.findViewById(R.id.invitaion_info_text);
        inviteEditText = view.findViewById(R.id.invite_friend_id);

        inviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(XmppConnectService.INVITE);
                intent.putExtra(Util.EXTRA_FRIEND_TO_INVITE,inviteEditText.getText().toString());
                getActivity().sendBroadcast(intent);
            }
        });



        return view;
    }

}
