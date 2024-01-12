package payment.gateway.UserEntity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cart cart = (Cart) o;
        return Objects.equals(products.getPid(), cart.products.getPid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(products);
    }

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
