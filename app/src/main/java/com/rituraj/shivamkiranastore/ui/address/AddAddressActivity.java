package com.rituraj.shivamkiranastore.ui.address;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.shivamkiranastore.Models.LanguageModel;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.Models.AddressModel;
import com.rituraj.shivamkiranastore.locations.LocationManager;
import com.rituraj.shivamkiranastore.locations.PermissionManager;
import com.rituraj.shivamkiranastore.translationLanguage.LanguageManager;

import java.util.List;
import java.util.Locale;

public class AddAddressActivity extends AppCompatActivity {
    private final String[] foregroundLocationPermissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private PermissionManager permissionManager;
    private LocationManager locationManager;
    private IntentFilter localBroadcastIntentFilter;
    private Toolbar toolbar;
    private LinearProgressIndicator progressBar;
    // Address Type Chips
    private MaterialCardView chipHome, chipWork, chipOther;
    private String selectedAddressType = "Home", addressId;
    // Input Fields
    private TextInputEditText etFullName, etPhone, etAlternatePhone, etPincode;
    private TextInputEditText etAddressLine1, etAddressLine2, etCity, etState;
    private MaterialButton btnSaveAddress;
    private View layoutCurrentLocation;
    private String userId;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        init();
        initViews();
        setupToolbar();
        setupAddressTypeChips();
        setupPincodeValidation();
        setupCurrentLocation();
        setupSaveButton();
        takeUserLocation();
        fetchUserLocation();
        fetchUserLocation();
    }

    private void init() {
        database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        addressId = getIntent().getStringExtra("addressId");
    }

    private void getAddress(String addressId) {
        DatabaseReference reference = database.getReference("Address").child(userId).child(addressId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    finish();
                    return;
                }
                AddressModel address = snapshot.getValue(AddressModel.class);
                if (address != null) {
                    address.setId(snapshot.getKey());
                    uiUpdate(address);
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });
    }

    private void uiUpdate(AddressModel address) {
        if ("Home".equalsIgnoreCase(address.getType())) {
            updateChipSelection(chipHome);
            selectedAddressType = address.getType();
        } else if ("Work".equalsIgnoreCase(address.getType())) {
            updateChipSelection(chipWork);
            selectedAddressType = address.getType();
        } else {
            updateChipSelection(chipOther);
            selectedAddressType = address.getType();
        }
        etFullName.setText(address.getFullName());
        etPhone.setText(address.getPhone());
        etAlternatePhone.setText((address.getAlternatePhone() == null ? "" : address.getAlternatePhone()));
        etPincode.setText(address.getPincode());
        etAddressLine1.setText(address.getAddressLine1());
        etAddressLine2.setText(address.getAddressLine2() == null ? "" : address.getAddressLine2());
        etCity.setText(address.getCity());
        etState.setText(address.getState());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);

        chipHome = findViewById(R.id.chipHome);
        chipWork = findViewById(R.id.chipWork);
        chipOther = findViewById(R.id.chipOther);

        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etAlternatePhone = findViewById(R.id.etAlternatePhone);
        etPincode = findViewById(R.id.etPincode);
        etAddressLine1 = findViewById(R.id.etAddressLine1);
        etAddressLine2 = findViewById(R.id.etAddressLine2);
        etCity = findViewById(R.id.etCity);
        etState = findViewById(R.id.etState);

        btnSaveAddress = findViewById(R.id.btnSaveAddress);
        layoutCurrentLocation = findViewById(R.id.layoutCurrentLocation);

        // Set default home chip as selected
        updateChipSelection(chipHome);
        if (addressId != null && !addressId.isEmpty()) getAddress(addressId);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Add Delivery Address");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupAddressTypeChips() {
        chipHome.setOnClickListener(v -> {
            updateChipSelection(chipHome);
            selectedAddressType = "Home";
        });

        chipWork.setOnClickListener(v -> {
            updateChipSelection(chipWork);
            selectedAddressType = "Work";
        });

        chipOther.setOnClickListener(v -> {
            updateChipSelection(chipOther);
            selectedAddressType = "Other";
        });
    }

    private void updateChipSelection(MaterialCardView selectedChip) {
        // Reset all chips
        resetChipStyle(chipHome);
        resetChipStyle(chipWork);
        resetChipStyle(chipOther);

        // Highlight selected chip
        selectedChip.setCardBackgroundColor(getResources().getColor(R.color.logo_orange));
        selectedChip.setStrokeWidth(2);
        selectedChip.setCardElevation(4);
    }

    private void resetChipStyle(MaterialCardView chip) {
        chip.setCardBackgroundColor(getResources().getColor(R.color.logo_gold));
        chip.setStrokeWidth(1);
        chip.setCardElevation(2);
    }

    private void setupPincodeValidation() {
        etPincode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 6) {
                    validatePincode(s.toString());
                } else {
                }
            }
        });
    }

    private void validatePincode(String pincode) {
        // Show loading
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setupCurrentLocation() {
        layoutCurrentLocation.setOnClickListener(v -> {
            Toast.makeText(this, "Fetching current location...", Toast.LENGTH_SHORT).show();
            fetchUserLocation();
        });
    }

    private void setupSaveButton() {
        btnSaveAddress.setOnClickListener(v -> {
            if (validateInputs()) {
                saveAddress();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate Full Name
        if (TextUtils.isEmpty(etFullName.getText())) {
            etFullName.setError("Full name is required");
            isValid = false;
        }

        // Validate Phone
        String phone = etPhone.getText() != null ? etPhone.getText().toString() : "";
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone number is required");
            isValid = false;
        } else if (phone.length() != 10) {
            etPhone.setError("Enter valid 10-digit phone number");
            isValid = false;
        }

        // Validate Pincode
        String pincode = etPincode.getText() != null ? etPincode.getText().toString() : "";
        if (TextUtils.isEmpty(pincode)) {
            etPincode.setError("Pincode is required");
            isValid = false;
        } else if (pincode.length() != 6) {
            etPincode.setError("Enter valid 6-digit pincode");
            isValid = false;
        }

        // Validate Address Line 1
        if (TextUtils.isEmpty(etAddressLine1.getText())) {
            etAddressLine1.setError("Address is required");
            isValid = false;
        }

        // Validate City
        if (TextUtils.isEmpty(etCity.getText())) {
            etCity.setError("City is required");
            isValid = false;
        }

        // Validate State
        if (TextUtils.isEmpty(etState.getText())) {
            etState.setError("State is required");
            isValid = false;
        }

        return isValid;
    }

    private void saveAddress() {
        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        btnSaveAddress.setEnabled(false);

        // Create address object
        AddressModel address = new AddressModel(selectedAddressType, etFullName.getText().toString(), etPhone.getText().toString(), etAlternatePhone.getText() != null ? etAlternatePhone.getText().toString() : "", etPincode.getText().toString(), etAddressLine1.getText().toString(), etAddressLine2.getText() != null ? etAddressLine2.getText().toString() : "", etCity.getText().toString(), etState.getText().toString());

        // Simulate saving to backend
        btnSaveAddress.postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            btnSaveAddress.setEnabled(true);
            database.getReference("Address").child(userId).push().setValue(address);
            Toast.makeText(this, "Address saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        }, 1500);
    }

    // for location
    public void takeUserLocation() {
        permissionManager = PermissionManager.getInstance(AddAddressActivity.this);
        locationManager = LocationManager.getInstance(AddAddressActivity.this);

        localBroadcastIntentFilter = new IntentFilter();
        localBroadcastIntentFilter.addAction("foreground_location");
    }

    public void fetchUserLocation() {
        if (!permissionManager.checkPermissions(foregroundLocationPermissions)) {
            permissionManager.askPermissions(AddAddressActivity.this, foregroundLocationPermissions, 100);
        } else {
            if (locationManager.isLocationEnabled()) {
                Location location = locationManager.getLastLocation();
                if (location != null) {
//                    Toast.makeText(UploadPage.this, "T: " + location.getLatitude() + " Long: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    String dateTime = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new java.util.Date());
                    getAddressFromLocation(AddAddressActivity.this, location.getLatitude(), location.getLongitude());
                }
            } else {
                locationManager.createLocationRequest();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        locationManager.startLocationUpdates();
        LocalBroadcastManager.getInstance(AddAddressActivity.this).registerReceiver(foregroundLocationBroadCastReceiver, localBroadcastIntentFilter);
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(AddAddressActivity.this).unregisterReceiver(foregroundLocationBroadCastReceiver);
        locationManager.stopLocationUpdates();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionManager.handlePermissionResult(AddAddressActivity.this, 100, permissions, grantResults)) {
            locationManager.createLocationRequest();
        } else {
            Toast.makeText(AddAddressActivity.this, "Permission denied.", Toast.LENGTH_SHORT).show();
        }
    }

    BroadcastReceiver foregroundLocationBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(UploadPage.this,
//                    intent.getStringExtra("location"), Toast.LENGTH_SHORT).show();
        }
    };

    private void getAddressFromLocation(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
//                return addresses.get(0).getAddressLine(0); // Full address
                etAddressLine1.setText(addresses.get(0).getSubLocality());
                etCity.setText(addresses.get(0).getLocality());
                etPincode.setText(addresses.get(0).getPostalCode());
                etState.setText(addresses.get(0).getAdminArea());
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}