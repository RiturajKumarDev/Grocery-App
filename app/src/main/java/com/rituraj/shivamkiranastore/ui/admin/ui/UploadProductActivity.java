package com.rituraj.shivamkiranastore.ui.admin.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rituraj.shivamkiranastore.Models.LanguageModel;
import com.rituraj.shivamkiranastore.Models.Product;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.translationLanguage.LanguageManager;

import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UploadProductActivity extends AppCompatActivity {

    // Views
    private Toolbar toolbar;
    private LinearProgressIndicator progressBar;
    private FirebaseDatabase database;
    private DatabaseReference myReference;

    // Image Upload Views
    private CardView cardMainImage, cardImage1, cardAddMore;
    private ImageView ivMainImage, ivImage1, ivRemove1;
    private TextView tvMainImageLabel;

    // Input Fields
    private TextInputEditText etProductNameEn, etProductNameHi, etDescription;
    private TextInputEditText etPrice, etMrp, etDiscount;
    private TextInputEditText etWeight, etBrand;
    private AutoCompleteTextView actCategory, actUnit, actShelfLife, actOfferType;
    private CheckBox cbOffer;
    private LinearLayout layoutOfferFields;
    private MaterialButton btnUploadProduct;

    // Data Lists
    private String[] categories = {"Masala", "Rice & Grains", "Dal & Pulses", "Oil & Ghee", "Snacks", "Beverages", "Personal Care", "Household"};
    private String[] units = {"kg", "g", "Liter", "ml", "Piece", "Pack", "Dozen"};
    private String[] shelfLife = {"3 Months", "6 Months", "9 Months", "12 Months", "18 Months", "24 Months"};
    private String[] offerTypes = {"Percentage Off", "Flat Discount", "Buy 1 Get 1", "Buy 2 Get 1", "Combo Offer"};

    // Selected Images URIs
    private Uri mainImageUri = null;
    private Uri additionalImageUri1 = null;
    private List<Uri> imageUris = new ArrayList<>();

    // Activity Result Launchers
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        if (uri != null) {
            handleImageSelection(uri);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable vector drawables
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        setContentView(R.layout.activity_upload_product);

        initViews();
        setupToolbar();
        setupDropdowns();
        setupImageClickListeners();
        setupOfferCheckbox();
        setupPriceCalculation();
        setupButtons();
    }

    private void initViews() {
        database = FirebaseDatabase.getInstance();
        myReference = database.getReference();

        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);

        // Image Views
        cardMainImage = findViewById(R.id.cardMainImage);
        cardImage1 = findViewById(R.id.cardImage1);
        cardAddMore = findViewById(R.id.cardAddMore);
        ivMainImage = findViewById(R.id.ivMainImage);
        ivImage1 = findViewById(R.id.ivImage1);
        ivRemove1 = findViewById(R.id.ivRemove1);
        tvMainImageLabel = findViewById(R.id.tvMainImageLabel);

        // Text Inputs
        etProductNameEn = findViewById(R.id.etProductNameEn);
        etProductNameHi = findViewById(R.id.etProductNameHi);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etMrp = findViewById(R.id.etMrp);
        etDiscount = findViewById(R.id.etDiscount);
        etWeight = findViewById(R.id.etWeight);
        etBrand = findViewById(R.id.etBrand);

        // AutoCompleteTextViews
        actCategory = findViewById(R.id.actCategory);
        actUnit = findViewById(R.id.actUnit);
        actShelfLife = findViewById(R.id.actShelfLife);
        actOfferType = findViewById(R.id.actOfferType);

        // Checkbox and Layout
        cbOffer = findViewById(R.id.cbOffer);
        layoutOfferFields = findViewById(R.id.layoutOfferFields);

        // Buttons
        btnUploadProduct = findViewById(R.id.btnUploadProduct);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Upload Product");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupDropdowns() {
        // Category Dropdown
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        actCategory.setAdapter(categoryAdapter);

        // Unit Dropdown
        actUnit.setText("kg", false);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, units);
        actUnit.setAdapter(unitAdapter);

        // Shelf Life Dropdown
        actShelfLife.setText("12 Months", false);
        ArrayAdapter<String> shelfLifeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, shelfLife);
        actShelfLife.setAdapter(shelfLifeAdapter);

        // Offer Type Dropdown
        ArrayAdapter<String> offerTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, offerTypes);
        actOfferType.setAdapter(offerTypeAdapter);
    }

    private void setupImageClickListeners() {
        // Main Image Click
        cardMainImage.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
            currentImageTarget = "main";
        });

        // Additional Image 1 Click
        cardImage1.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
            currentImageTarget = "additional1";
        });

        // Add More Images Click
        cardAddMore.setOnClickListener(v -> {
            if (imageUris.size() < 5) {
                imagePickerLauncher.launch("image/*");
                currentImageTarget = "additional_new";
            } else {
                Toast.makeText(this, "Maximum 5 images allowed", Toast.LENGTH_SHORT).show();
            }
        });

        // Remove Image 1
        ivRemove1.setOnClickListener(v -> {
            cardImage1.setVisibility(View.GONE);
            additionalImageUri1 = null;
            imageUris.remove(additionalImageUri1);
            ivRemove1.setVisibility(View.GONE);
        });
    }

    private String currentImageTarget = "";

    private void handleImageSelection(Uri uri) {
        switch (currentImageTarget) {
            case "main":
                mainImageUri = uri;
                ivMainImage.setImageURI(uri);
                ivMainImage.setPadding(0, 0, 0, 0);
                ivMainImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                tvMainImageLabel.setVisibility(View.VISIBLE);
                break;

            case "additional1":
                additionalImageUri1 = uri;
                cardImage1.setVisibility(View.VISIBLE);
                ivImage1.setImageURI(uri);
                ivImage1.setPadding(0, 0, 0, 0);
                ivImage1.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ivRemove1.setVisibility(View.VISIBLE);
                imageUris.add(uri);
                break;

            case "additional_new":
                // Add new image card logic here
                Toast.makeText(this, "Additional image added", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void setupOfferCheckbox() {
        cbOffer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutOfferFields.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
    }

    private void setupPriceCalculation() {
        // Calculate discount when MRP or Price changes
        TextWatcher priceWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                calculateDiscount();
            }
        };

        etMrp.addTextChangedListener(priceWatcher);
        etPrice.addTextChangedListener(priceWatcher);
    }

    private void calculateDiscount() {
        String mrpStr = etMrp.getText() != null ? etMrp.getText().toString() : "";
        String priceStr = etPrice.getText() != null ? etPrice.getText().toString() : "";

        if (!mrpStr.isEmpty() && !priceStr.isEmpty()) {
            try {
                double mrp = Double.parseDouble(mrpStr);
                double price = Double.parseDouble(priceStr);

                if (mrp > 0 && price > 0 && mrp > price) {
                    double discount = ((mrp - price) / mrp) * 100;
                    etDiscount.setText(String.format("%.0f", discount));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupButtons() {
        btnUploadProduct.setOnClickListener(v -> {
            if (validateInputs()) {
                uploadProduct();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Check main image
        if (mainImageUri == null) {
            Toast.makeText(this, "Please select main product image", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Check product name
        if (TextUtils.isEmpty(etProductNameEn.getText())) {
            etProductNameEn.setError("Product name is required");
            isValid = false;
        }

        // Check category
        if (TextUtils.isEmpty(actCategory.getText())) {
            actCategory.setError("Category is required");
            isValid = false;
        }

        // Check price
        if (TextUtils.isEmpty(etPrice.getText())) {
            etPrice.setError("Price is required");
            isValid = false;
        }

        // Check MRP
        if (TextUtils.isEmpty(etMrp.getText())) {
            etMrp.setError("MRP is required");
            isValid = false;
        }

        // Check weight
        if (TextUtils.isEmpty(etWeight.getText())) {
            etWeight.setError("Weight/Quantity is required");
            isValid = false;
        }

        return isValid;
    }

    private void uploadProduct() {
        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        btnUploadProduct.setEnabled(false);

        // Simulate API call
        new Handler().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            btnUploadProduct.setEnabled(true);
            Product product;
            if (!cbOffer.isChecked()) {
                product = new Product(etProductNameEn.getText().toString(), etProductNameHi.getText().toString(), etDescription.getText().toString(), actCategory.getText().toString(), Double.parseDouble(etPrice.getText().toString()), Double.parseDouble(etMrp.getText().toString()), Double.parseDouble(etDiscount.getText().toString()), encodeImageToBase64(getBitmapFromUri(mainImageUri)), etWeight.getText().toString(), actUnit.getText().toString(), etBrand.getText().toString(), actShelfLife.getText().toString());
                myReference = database.getReference("Products");
            } else {
                product = new Product(etProductNameEn.getText().toString(), etProductNameHi.getText().toString(), etDescription.getText().toString(), actCategory.getText().toString(), Double.parseDouble(etPrice.getText().toString()), Double.parseDouble(etMrp.getText().toString()), Double.parseDouble(etDiscount.getText().toString()), encodeImageToBase64(getBitmapFromUri(mainImageUri)), etWeight.getText().toString(), actUnit.getText().toString(), etBrand.getText().toString(), actShelfLife.getText().toString(), actOfferType.getText().toString());
                myReference = database.getReference("SpecialOffer");
            }
            myReference.push().setValue(product);
            finish();
            Toast.makeText(this, "Product uploaded successfully!", Toast.LENGTH_LONG).show();
        }, 3000);
    }

    public String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private Bitmap getBitmapFromUri(@NonNull Uri uri) {
        try {
            Context ctx = UploadProductActivity.this;
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}