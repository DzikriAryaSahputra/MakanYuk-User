package com.example.deliveryfood;

public class CartItem {
    private String productId; // ID unik produk dari koleksi 'products'
    private String name;
    private double price;
    private String imageUrl;
    private int quantity; // Jumlah item ini di keranjang

    // Constructor kosong WAJIB untuk Firestore
    public CartItem() {}

    // Constructor lain (opsional, untuk kemudahan)
    public CartItem(String productId, String name, double price, String imageUrl, int quantity) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
    }

    // Getters (WAJIB)
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public int getQuantity() { return quantity; }

    // Setter (WAJIB agar Firestore bisa update quantity)
    public void setQuantity(int quantity) { this.quantity = quantity; }
}