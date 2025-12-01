package activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.deliveryfood.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import fragments.UserOrderFragment;

public class MyOrdersActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ImageView btnBack;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        // Inisialisasi View
        tabLayout = findViewById(R.id.tabLayoutMyOrders);
        viewPager = findViewById(R.id.viewPagerMyOrders);
        btnBack = findViewById(R.id.backButtonMyOrders);
        bottomNavigationView = findViewById(R.id.bottomNavigationMyOrders);

        // Logika Tombol Back (Header)
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        });

        setupTabs();
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        // PENTING: Set item "Pesanan" sebagai yang terpilih agar warnanya oranye
        bottomNavigationView.setSelectedItemId(R.id.nav_pesanan);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(getApplicationContext(), CartActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            else if (itemId == R.id.nav_pesanan) {
                // Sudah di sini, tidak perlu reload
                return true;
            }
            else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });
    }

    private void setupTabs() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Dalam Proses");
                    } else {
                        tab.setText("Riwayat");
                    }
                }).attach();
    }

    class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull AppCompatActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return UserOrderFragment.newInstance("active");
            } else {
                return UserOrderFragment.newInstance("history");
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}