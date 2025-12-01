package models;

import java.io.Serializable;
import java.util.List;

public class Menu implements Serializable {

    // Sesuaikan nama field ini dengan nama field di Firestore Anda
    private String productId;
    private String name;
    private String description;
    private double price;
    private String imageUrl; // URL gambar dari Firebase Storage
    private double rating;
    private String estimation;
    private List<String> variants;

    // --- TAMBAHAN BARU UNTUK FITUR RATING ---
    // Jumlah orang yang sudah menilai (Penting untuk rumus rata-rata)
    private int ratingCount;

    // Constructor kosong ini WAJIB ada untuk Firestore
    public Menu() {
    }

    // --- GETTER & SETTER ---
    // (Pastikan Setter ada agar Firestore bisa mengisi data)

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getEstimation() { return estimation; }
    public void setEstimation(String estimation) { this.estimation = estimation; }

    public List<String> getVariants() { return variants; }
    public void setVariants(List<String> variants) { this.variants = variants; }

    // --- GETTER & SETTER KHUSUS RATING COUNT ---
    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }
}