package models; // Sesuaikan package

import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;

public class CartItem implements Serializable {

    @PropertyName("name")
    private String foodName; // Kita samakan variabelnya dengan Admin agar konsisten

    private String productId;
    private double price;
    private String imageUrl;
    private int quantity;
    private String variant;

    public CartItem() {
    }

    public CartItem(String productId, String foodName, double price, String imageUrl, int quantity, String variant) {
        this.productId = productId;
        this.foodName = foodName;
        this.price = price;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.variant = variant;
    }

    // --- GETTER SETTER DENGAN ANOTASI ---

    @PropertyName("name")
    public String getFoodName() { return foodName; }

    @PropertyName("name")
    public void setFoodName(String foodName) { this.foodName = foodName; }

    // Jika kode lama kamu memanggil getName(), hapus atau arahkan ke getFoodName()
    // Agar aman, kita hapus getName() lama dan pakai getFoodName() saja.

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getVariant() { return variant; }
    public void setVariant(String variant) { this.variant = variant; }
}