package com.example.deliveryfood;
import java.util.List;
public class Menu {
    // Sesuaikan nama field ini dengan nama field di Firestore Anda
    private String productId;
    private String name;
    private String description;
    private double price;
    private String imageUrl; // URL gambar dari Firebase Storage
    private double rating;
    private String estimation; // Estimasi waktu, misal "25 mins"
    private List<String> variants;

    // Constructor kosong ini WAJIB ada untuk Firestore
    public Menu() {
    }
    // --- Tambahkan getter & setter untuk productId ---
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    // Getters (juga WAJIB ada untuk Firestore)
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getRating() {
        return rating;
    }

    public String getEstimation() {
        return estimation;
    }
    public List<String> getVariants() {
        return variants;
    }
}