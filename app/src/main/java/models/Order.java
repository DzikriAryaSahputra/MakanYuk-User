package models; // Sesuaikan package kamu

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Order implements Serializable {

    private String orderId;
    private String userId;

    @PropertyName("shippingAddress")
    private Map<String, Object> shippingAddress;

    private List<CartItem> items;
    @PropertyName("rejectionReason")
    private String rejectionReason;
    @PropertyName("subtotal") private double subtotal;
    @PropertyName("deliveryFee") private double deliveryFee;
    @PropertyName("total") private double total;
    @PropertyName("paymentMethod") private String paymentMethod;
    @PropertyName("status") private String status;

    // --- TAMBAHAN WAJIB UNTUK FITUR RATING ---
    @PropertyName("rating") private double rating;
    @PropertyName("review") private String review;
    // -----------------------------------------

    @ServerTimestamp
    private Date orderTimestamp;

    public Order() {}

    // Constructor Lengkap (Sesuaikan dengan yang kamu punya, biarkan saja)
    public Order(String userId, Map<String, Object> shippingAddress, List<CartItem> items, double subtotal, double deliveryFee, double total, String paymentMethod, String status) {
        this.userId = userId;
        this.shippingAddress = shippingAddress;
        this.items = items;
        this.subtotal = subtotal;
        this.deliveryFee = deliveryFee;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.status = status;
    }

    // --- GETTER & SETTER RATING (WAJIB ADA) ---
    @PropertyName("rating")
    public double getRating() { return rating; }
    @PropertyName("rating")
    public void setRating(double rating) { this.rating = rating; }

    @PropertyName("review")
    public String getReview() { return review; }
    @PropertyName("review")
    public void setReview(String review) { this.review = review; }
    @PropertyName("rejectionReason")
    public String getRejectionReason() { return rejectionReason; }

    @PropertyName("rejectionReason")
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    // ------------------------------------------

    // ... Getter Setter Lainnya (Biarkan saja) ...
    @Exclude
    public String getShippingAddressString() {
        if (shippingAddress == null) return "-";
        String fullAddr = "-";
        if (shippingAddress.containsKey("fullAddress")) fullAddr = String.valueOf(shippingAddress.get("fullAddress"));
        return fullAddr;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    @PropertyName("shippingAddress")
    public Map<String, Object> getShippingAddress() { return shippingAddress; }
    @PropertyName("shippingAddress")
    public void setShippingAddress(Map<String, Object> shippingAddress) { this.shippingAddress = shippingAddress; }
    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
    @PropertyName("subtotal")
    public double getSubtotal() { return subtotal; }
    @PropertyName("subtotal")
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    @PropertyName("deliveryFee")
    public double getDeliveryFee() { return deliveryFee; }
    @PropertyName("deliveryFee")
    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }
    @PropertyName("total")
    public double getTotal() { return total; }
    @PropertyName("total")
    public void setTotal(double total) { this.total = total; }
    @PropertyName("paymentMethod")
    public String getPaymentMethod() { return paymentMethod; }
    @PropertyName("paymentMethod")
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    @PropertyName("status")
    public String getStatus() { return status; }
    @PropertyName("status")
    public void setStatus(String status) { this.status = status; }
    public Date getOrderTimestamp() { return orderTimestamp; }
    public void setOrderTimestamp(Date orderTimestamp) { this.orderTimestamp = orderTimestamp; }
}