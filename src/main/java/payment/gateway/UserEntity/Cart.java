package payment.gateway.UserEntity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Integer cart_id;

    @JsonBackReference(value = "cust_cart")
    @ManyToOne(fetch = FetchType.EAGER)
    private Customer customer;

    @JsonBackReference
    @ManyToOne(cascade = CascadeType.ALL)
    private Products products;

    private LocalDate date;

    private int quantity;

    public Integer getId() {
        return cart_id;
    }

    public void setId(Integer id) {
        this.cart_id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Products getProducts() {
        return products;
    }

    public void setProducts(Products products) {
        this.products = products;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
