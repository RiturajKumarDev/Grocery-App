package com.rituraj.shivamkiranastore.ui.carts;

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

import com.rituraj.shivamkiranastore.Models.LanguageModel;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.translationLanguage.LanguageManager;

public class CatsFragment extends Fragment {
    private View view;
    private CatsViewModel mViewModel;

    public static CatsFragment newInstance() {
        return new CatsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_cats, container, false);
        startActivity(new Intent(requireContext(), CartActivity.class));

        // Fragment destroy / remove
        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(CatsViewModel.class);
        // TODO: Use the ViewModel
    }
}