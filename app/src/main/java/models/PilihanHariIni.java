
package models;

public class PilihanHariIni {
    private String name;
    private String imageUrl;
    private String productId; // <-- TAMBAHKAN INI

    // Constructor kosong WAJIB untuk Firestore
    public PilihanHariIni() {}

    // --- TAMBAHKAN GETTER & SETTER INI ---
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    // -------------------------------------

    // Getters lainnya (tetap sama)
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
}