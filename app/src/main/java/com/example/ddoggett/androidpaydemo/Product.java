package com.example.ddoggett.androidpaydemo;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable  {
    int price;
    String name;
    String description;
    int imageResource;
    int shipping;
    int tax;

    public Product() {
    }

    public Product(int price, String name) {
        this.price = price;
        this.name = name;
    }


    public void setImageResource(int resource) {
        imageResource = resource;
    }

    @Override
    public String toString() {
        return "Product{" +
                "price=" + price +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", shipping=" + shipping +
                ", tax=" + tax +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.name);
        parcel.writeInt(this.price);
        parcel.writeInt(this.imageResource);
        parcel.writeInt(this.shipping);
        parcel.writeInt(this.tax);
    }
}
