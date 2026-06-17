package com.rituraj.shivamkiranastore.ui.home;

import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.shivamkiranastore.Models.HeroImage;
import com.rituraj.shivamkiranastore.Models.LanguageModel;
import com.rituraj.shivamkiranastore.Models.Product;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.adapter.ImageSliderRecycler;
import com.rituraj.shivamkiranastore.adapter.ShowProductList;
import com.rituraj.shivamkiranastore.databinding.FragmentHomeBinding;
import com.rituraj.shivamkiranastore.translationLanguage.LanguageManager;
import com.rituraj.shivamkiranastore.ui.admin.AdminDashboardActivity;
import com.rituraj.shivamkiranastore.ui.search.SearchProductActivity;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private HomeViewModel mViewModel;
    private View view;
    private FragmentHomeBinding binding;
    private ViewPager2 viewPager;
    private ImageSliderRecycler adapter;
    private ArrayList<HeroImage> heroImages = new ArrayList<>();
    private Handler sliderHandler = new Handler();
    private FirebaseDatabase database;
    private DatabaseReference myReference;
    private ArrayList<Product> productListSpecialOffer = new ArrayList<>(), productListTopProduct = new ArrayList<>(), productList = new ArrayList<>();
    private ShowProductList showSpecialOfferListAdapter, showTopProductListAdapter, showAllProductListAdapter;
    private RecyclerView recyclerViewSpecialProduct, recyclerViewTopProduct, recyclerViewAllProduct;
    private String userId;

    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (viewPager != null && adapter != null && heroImages.size() > 1) {
                int nextPos = (viewPager.getCurrentItem() + 1) % heroImages.size();
                viewPager.setCurrentItem(nextPos, true);
                sliderHandler.postDelayed(this, 3000);
            }
        }
    };

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        view = binding.getRoot();

        fun();
        translationViews(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
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

    private void fun() {
        database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        binding.searchBoxAsBtn.setOnClickListener((v) -> {
            startActivity(new Intent(requireContext(), SearchProductActivity.class));
        });
        setImageSlider();
        getSpecialOfferProducts();
        getTopProducts();
        getAllProducts();
    }

    private void setImageSlider() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String email = auth.getCurrentUser().getEmail();
        heroImages.add(new HeroImage(R.drawable.shivam_kirana_store_bg));
        heroImages.add(new HeroImage(R.drawable.shivam_kirana_store_bg));
        heroImages.add(new HeroImage(R.drawable.shivam_kirana_store_bg));
        imageAdepterSetting(email);
        myReference = database.getReference().child("HeroImages");
        myReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    heroImages.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        HeroImage heroImage = dataSnapshot.getValue(HeroImage.class);
                        if (heroImage != null) {
                            heroImage.setImageId(dataSnapshot.getKey());
                            heroImages.add(heroImage);
                        }
                    }
                    imageAdepterSetting(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void imageAdepterSetting(String email) {
        viewPager = view.findViewById(R.id.homeShaliderViewPager);
        adapter = new ImageSliderRecycler(requireContext(), heroImages, email);
        viewPager.setAdapter(adapter);
        viewPager.setClipToPadding(false);
        viewPager.setClipChildren(false);
        if (viewPager.getChildAt(0) != null) {
            viewPager.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        viewPager.setOffscreenPageLimit(1);

        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        transformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.90f + r * 0.10f);
            page.setAlpha(0.8f + r * 0.2f);
        });
        viewPager.setPageTransformer(transformer);
        sliderHandler.removeCallbacks(sliderRunnable);
        if (heroImages.size() > 1) {
            sliderHandler.postDelayed(sliderRunnable, 3000);
        }
    }

    private void getSpecialOfferProducts() {
        showSpecialOfferProducts();
        myReference = database.getReference().child("SpecialOffer");
        productListSpecialOffer.clear();
        myReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                if (binding.homeProgressBarSpecialOfferProducts.getVisibility() == View.VISIBLE)
//                    binding.homeProgressBarSpecialOfferProducts.setVisibility(View.GONE);
                if (snapshot.exists() && getContext() != null) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        product.setProductId(snapshot.getKey());
                        productListSpecialOffer.add(product);
                        showSpecialOfferListAdapter.notifyItemInserted(productListSpecialOffer.size() - 1);
                    }
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
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSpecialOfferProducts() {
        recyclerViewSpecialProduct = binding.specialOfferRecyclerView;
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewSpecialProduct.setLayoutManager(layoutManager);
        showSpecialOfferListAdapter = new ShowProductList(requireContext(), productListSpecialOffer, userId);
        recyclerViewSpecialProduct.setAdapter(showSpecialOfferListAdapter);
    }

    private void getTopProducts() {
        showTopProducts();
        productListTopProduct.clear();
        myReference = database.getReference().child("Products");
        myReference.limitToFirst(20);
        myReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                if (binding.homeProgressBarTopProducts.getVisibility() == View.VISIBLE)
//                    binding.homeProgressBarTopProducts.setVisibility(View.GONE);
                Product product = snapshot.getValue(Product.class);
                if (product != null) {
                    product.setProductId(snapshot.getKey());
                    productListTopProduct.add(product);
                    showTopProductListAdapter.notifyItemInserted(productListTopProduct.size() - 1);
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
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTopProducts() {
        recyclerViewTopProduct = binding.topProductsRecyclerView;
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewTopProduct.setLayoutManager(layoutManager);
        showTopProductListAdapter = new ShowProductList(requireContext(), productListTopProduct, userId);
        recyclerViewTopProduct.setAdapter(showTopProductListAdapter);
    }

    private void getAllProducts() {
        showAllProducts();
        productList.clear();
        myReference = database.getReference().child("Products");
        myReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Product product = snapshot.getValue(Product.class);
                if (product != null) {
                    product.setProductId(snapshot.getKey());
                    productList.add(product);
                    showAllProductListAdapter.notifyItemInserted(productList.size() - 1);
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
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAllProducts() {
        recyclerViewAllProduct = binding.allProductsRecyclerView;
        recyclerViewAllProduct.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        showAllProductListAdapter = new ShowProductList(requireContext(), productList, userId);
        recyclerViewAllProduct.setAdapter(showAllProductListAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (heroImages.size() > 1) {
            sliderHandler.postDelayed(sliderRunnable, 3000);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sliderHandler.removeCallbacks(sliderRunnable);
        binding = null;
    }
}
