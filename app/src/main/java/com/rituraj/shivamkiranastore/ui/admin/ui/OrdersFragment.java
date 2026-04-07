package com.rituraj.shivamkiranastore.ui.admin.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.shivamkiranastore.Models.User;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.ui.order.OrderListActivity;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrdersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrdersFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private View view;
    private ArrayList<String> userIds = new ArrayList<>(), userOrders = new ArrayList<>();
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private ListView userOrdersList;

    public OrdersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OrdersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrdersFragment newInstance(String param1, String param2) {
        OrdersFragment fragment = new OrdersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_orders, container, false);

        fun();
        return view;
    }

    private void fun() {
        init();
        getUserOrders();
    }

    private void init() {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        userOrdersList = view.findViewById(R.id.userOrdersList);
    }

    private void getUserOrders() {
        reference = database.getReference().child("Orders");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    String userId = snapshot.getKey();
                    userIds.add(userId);
                    reference = database.getReference().child("Users").child(userId);
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                User user = dataSnapshot.getValue(User.class);
                                if (user != null)
                                    userOrders.add(user.getUserName() + ", " + user.getEmail() + "\n" + "Ordered Items : " + snapshot.getChildrenCount());
                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireActivity(),
                                        android.R.layout.simple_list_item_1, userOrders);
                                userOrdersList.setAdapter(arrayAdapter);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    showRooms();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showRooms() {
        userOrdersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(requireContext(), OrderListActivity.class);
                intent.putExtra("userId", userIds.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
