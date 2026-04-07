package com.rituraj.shivamkiranastore.ui.profile;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rituraj.shivamkiranastore.Models.LanguageModel;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.translationLanguage.LanguageManager;
import com.rituraj.shivamkiranastore.ui.address.AddAddressActivity;
import com.rituraj.shivamkiranastore.ui.address.ManageAddressActivity;
import com.rituraj.shivamkiranastore.ui.auth.LoginActivity;
import com.rituraj.shivamkiranastore.ui.carts.CartActivity;
import com.rituraj.shivamkiranastore.ui.order.OrderListActivity;

public class ProfileFragment extends Fragment {
    private View view;
    private ProfileViewModel mViewModel;
    private FirebaseAuth auth;
    private String userId;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        fun(view);
        translationViews(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        // TODO: Use the ViewModel
    }

    private LanguageModel getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        String name = prefs.getString("language_name", "English");
        String code = prefs.getString("language_code", "en");

        return new LanguageModel(name, code);
    }

    private void translationViews(View rootView) {
        LanguageManager.init(getSavedLanguage(requireContext()).code, () -> LanguageManager.translateView(rootView));
    }

    public void fun(View view) {
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();

        Button signOutBtn = view.findViewById(R.id.signOutBtn);
        Button profileOrderBtn = view.findViewById(R.id.profileOrderBtn);
        Button profileCartBtn = view.findViewById(R.id.profileCartBtn);
        Button profileManageAddress = view.findViewById(R.id.profileManageAddress);
        Button profileSettingBtn = view.findViewById(R.id.profileSettingBtn);
        ImageView profileImg = view.findViewById(R.id.profileImg);
        TextView profileUserName = view.findViewById(R.id.profileUserName);
        TextView profileEmail = view.findViewById(R.id.profileEmail);

        profileCartBtn.setOnClickListener((v) -> {
            requireContext().startActivity(new Intent(requireContext(), CartActivity.class));
        });

        profileOrderBtn.setOnClickListener((v) -> {
            Intent intent = new Intent(requireContext(), OrderListActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        profileSettingBtn.setOnClickListener((v) -> {
            requireContext().startActivity(new Intent(requireContext(), OrderListActivity.class));
        });

        profileManageAddress.setOnClickListener((v) -> {
            requireContext().startActivity(new Intent(requireContext(), ManageAddressActivity.class));
        });

        signOutBtn.setOnClickListener((v) -> {
            Toast.makeText(requireContext(), "Sign Out", Toast.LENGTH_SHORT).show();
            auth.signOut();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        FirebaseUser user = auth.getCurrentUser();
        profileUserName.setText(user.getDisplayName());
        profileEmail.setText(user.getEmail());
        Glide.with(requireContext())
                .load(user.getPhotoUrl())
                .placeholder(R.drawable.baseline_account_circle_24)
                .error(R.drawable.baseline_account_circle_24)
                .into(profileImg);
    }

}