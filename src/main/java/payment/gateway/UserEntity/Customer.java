package payment.gateway.UserEntity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cid;
    @Column(name = "cuname")
    private String cusername;
    private String cpwd;
    private String address;
    private long cphone;

    private long ctotal;
    @JsonManagedReference(value = "cust_cart")
    @OneToMany(mappedBy = "customer", fetch = FetchType.EAGER)
    private List<Cart> cart;

    //mappedBy - don't create extra column in Customer table instead you map Customer cust.id, in Banks bank

    public void setCid(Integer cid){
        this.cid = cid;
    }
    public Integer getCid(){
        return cid;
    }

    public String getCusername() {
        return cusername;
    }

    public void setCusername(String cname) {
        this.cusername = cname;
    }

    public String getCpwd() {
        return cpwd;
    }

    public void setCpwd(String cpwd) {
        this.cpwd = cpwd;
    }
    public Long getCphone() {
        return cphone;
    }

    public void setCphone(long cphone) {
        this.cphone = cphone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getCtotal() {
        return ctotal;
    }

    public void setCtotal(long ctotal) {
        this.ctotal = ctotal;
    }

    public List<Cart> getCart() {
        return cart;
    }

    public void setCart(List<Cart> cart) {
        this.cart = cart;
    }

}
