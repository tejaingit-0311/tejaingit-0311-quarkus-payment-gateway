package payment.gateway.UserEntity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.dialect.function.ListaggGroupConcatEmulation;

import java.util.List;


@Entity
public class Products {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pid")
    private Integer pid;
    private String pname;
    private int pcost;
    @Column(name = "instock")
    private boolean inStock;
    private short units;

    private String pimages;
    @JsonManagedReference
    @OneToMany(mappedBy = "products")
    private List<Cart> carts;

    public Integer getId() {
        return pid;
    }

    @Override
    public String toString() {
        return "Products{" +
                "pname= " + pname +
                ", pcost= " + pcost+ "}";
    }

    public void setId(Integer id) {
        this.pid = id;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public int getPcost() {
        return pcost;
    }

    public void setPcost(int pcost) {
        this.pcost = pcost;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public List<Cart> getCarts() {
        return carts;
    }

    public void setCarts(List<Cart> carts) {
        this.carts = carts;
    }

    public short getUnits() {
        return units;
    }

    public void setUnits(short units) {
        this.units = units;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getPimages() {
        return pimages;
    }

    public void setPimages(String pimages) {
        this.pimages = pimages;
    }
}
