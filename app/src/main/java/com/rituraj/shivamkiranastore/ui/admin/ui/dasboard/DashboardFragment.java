package com.rituraj.shivamkiranastore.ui.admin.ui.dasboard;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.shivamkiranastore.Models.HeroImage;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.adapter.ImageSliderRecycler;
import com.rituraj.shivamkiranastore.helper.UploadHelper;
import com.rituraj.shivamkiranastore.ui.admin.ui.OnlineUserActivity;
import com.rituraj.shivamkiranastore.ui.admin.ui.UploadProductActivity;
import com.rituraj.shivamkiranastore.ui.product.ProductActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DashboardFragment extends Fragment {
    private View view;
    private DashboardViewModel mViewModel;
    private TextView tvTotalProducts, tvTotalCustomers, tvTotalOrders;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private Button uploadHeroImgBtn;
    private ImageView heroImagePreview;
    private FloatingActionButton heroImageCameraBtn;
    private int REQUEST_IMAGE = 101;
    private String heroImageUrl;
    private ProgressDialog progressDialog;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public static DashboardFragment newInstance() {
        return new DashboardFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        fun();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        // TODO: Use the ViewModel
    }

    private void fun() {
        LinearLayout tvCustomers = view.findViewById(R.id.tvCustomers);
        LinearLayout orderPageBigBtn = view.findViewById(R.id.orderPageBigBtn);
        LinearLayout tvUploadProduct = view.findViewById(R.id.tvUploadProduct);
        LinearLayout productPage = view.findViewById(R.id.productPage);
        productPage.setOnClickListener((v) -> {
            requireActivity().finish();
        });
        tvUploadProduct.setOnClickListener((v) -> {
            requireContext().startActivity(new Intent(requireContext(), UploadProductActivity.class));
        });
        tvCustomers.setOnClickListener((v) -> {
            requireContext().startActivity(new Intent(requireContext(), OnlineUserActivity.class));
        });
        orderPageBigBtn.setOnClickListener((v) -> {
            Toast.makeText(requireContext(), "Swiped right", Toast.LENGTH_SHORT).show();
        });
        init();
        initViews();
        getTotalCustomers();
        getTotalOrders();
        getTotalProducts();
        heroImgSetup();
    }

    private void init() {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
    }

    private void initViews() {
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Uploading ...");
        progressDialog.setCancelable(false);
        tvTotalCustomers = view.findViewById(R.id.tvTotalCustomers);
        tvTotalOrders = view.findViewById(R.id.tvTotalOrders);
        tvTotalProducts = view.findViewById(R.id.tvTotalProducts);
        uploadHeroImgBtn = view.findViewById(R.id.uploadHeroImgBtn);
        heroImagePreview = view.findViewById(R.id.heroImagePreview);
        heroImageCameraBtn = view.findViewById(R.id.heroImageCameraBtn);
        tvTotalCustomers.setText("0");
        tvTotalProducts.setText("0");
        tvTotalOrders.setText("0");
    }

    private void getTotalProducts() {
        reference = database.getReference("Products");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    tvTotalProducts.setText(String.valueOf(snapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTotalCustomers() {
        reference = database.getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    tvTotalCustomers.setText(String.valueOf(snapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTotalOrders() {
        reference = database.getReference("Orders");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    tvTotalOrders.setText(String.valueOf(snapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void heroImgSetup() {
        heroImageCameraBtn.setOnClickListener((v) -> {
            ImagePicker.with(this)
                    .crop()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start(REQUEST_IMAGE);
        });
        uploadHeroImgBtn.setOnClickListener((v) -> {
            if (heroImageUrl == null || heroImageUrl.isEmpty()) {
                Toast.makeText(requireContext(), "Please select image first", Toast.LENGTH_SHORT).show();
                return;
            }
            progressDialog.show();
            uploadImageFirebase();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();

            if (uri != null) {
                heroImagePreview.setImageURI(uri);
                Bitmap bitmap = getBitmapFromUri(uri);
                heroImageUrl = encodeImageToBase64(bitmap);
            }
        }
    }

    public String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private Bitmap getBitmapFromUri(@NonNull Uri uri) {
        try {
            Context ctx = requireContext();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source src = ImageDecoder.createSource(ctx.getContentResolver(), uri);
                return ImageDecoder.decodeBitmap(src);
            } else {
                return MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), uri);
            }
        } catch (IOException e) {
            return null;
        }
    }

    private void uploadImageFirebase() {
        HeroImage heroImage = new HeroImage();
        heroImage.setImageUrl(heroImageUrl);
        reference = database.getReference().child("HeroImages").push();
        heroImage.setImageId(reference.getKey());
        reference.setValue(heroImage)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        heroImageUrl = null;
                        progressDialog.dismiss();
                        heroImagePreview.setImageResource(0);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}