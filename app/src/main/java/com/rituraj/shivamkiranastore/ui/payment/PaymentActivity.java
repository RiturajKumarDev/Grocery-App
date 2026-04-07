package com.rituraj.shivamkiranastore.ui.payment;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.rituraj.shivamkiranastore.R;

public class PaymentActivity extends AppCompatActivity {
    ImageView imgQR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.logo_blue));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        String etAmount = getIntent().getStringExtra("Amount");
        if (etAmount == null) {
            finish();
            return;
        }
        imgQR = findViewById(R.id.imgQR);
        String amount = etAmount.trim();
        generateUPIQR(amount);
        fun(amount);
    }

    private void generateUPIQR(String amount) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        try {
            String upiId = "9507091174@apl";
            String payeeName = "Shivam Kirana Store";   // your store name
            String note = "User: " + user.getUid();

            String upiUrl = "upi://pay" + "?pa=" + upiId + "&pn=" + Uri.encode(payeeName) + "&tn=" + Uri.encode(note) + "&am=" + amount + "&cu=INR";

            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(upiUrl, BarcodeFormat.QR_CODE, 600, 600);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            imgQR.setImageBitmap(bitmap);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void fun(String amount) {
        TextView paymentPayAmount = findViewById(R.id.paymentPayAmount);
        paymentPayAmount.setText(String.valueOf("Rs. " + amount));
    }
}
