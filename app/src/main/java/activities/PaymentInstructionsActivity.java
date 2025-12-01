package activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.OnBackPressedCallback; // Import

import com.example.deliveryfood.R;

import java.text.NumberFormat;
import java.util.Locale;

public class PaymentInstructionsActivity extends AppCompatActivity {

    private TextView totalPaymentText;
    private Button buttonDonePayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_instructions);

        Toolbar toolbar = findViewById(R.id.toolbarPayment);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        totalPaymentText = findViewById(R.id.totalPaymentText);
        buttonDonePayment = findViewById(R.id.buttonDonePayment);

        // Ambil data dari Intent
        double totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT", 0);
        final boolean isBuyNow = getIntent().getBooleanExtra("IS_BUY_NOW", false);

        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        totalPaymentText.setText(formatRupiah.format(totalAmount));

        buttonDonePayment.setOnClickListener(v -> {
            goToMainActivity();
        });

        // Blokir tombol kembali agar alur jelas
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Saat tombol kembali ditekan, paksa ke MainActivity
                goToMainActivity();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void goToMainActivity() {
        Intent intent = new Intent(PaymentInstructionsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}