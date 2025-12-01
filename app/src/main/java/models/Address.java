package models;

public class Address {
    private String addressId;
    private String label;
    private String recipientName;
    private String phoneNumber;
    private String fullAddress;
    private boolean isDefault;

    // --- TAMBAHKAN DUA VARIABEL INI ---
    private String zipCode;
    private String notes;
    // ----------------------------------

    // Constructor kosong WAJIB
    public Address() {}

    // --- Getters ---
    public String getAddressId() { return addressId; }
    public String getLabel() { return label; }
    public String getRecipientName() { return recipientName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getFullAddress() { return fullAddress; }
    public boolean getIsDefault() { return this.isDefault; }

    // --- TAMBAHKAN DUA FUNGSI GETTER INI ---
    public String getZipCode() { return zipCode; }
    public String getNotes() { return notes; }
    // ---------------------------------------

    // --- Setters ---
    public void setAddressId(String addressId) { this.addressId = addressId; }
    public void setIsDefault(boolean isDefault) { this.isDefault = isDefault; }
}