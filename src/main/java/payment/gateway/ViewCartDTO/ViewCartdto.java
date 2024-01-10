package payment.gateway.ViewCartDTO;

import payment.gateway.ProductsDto.ProductsDto;
import payment.gateway.UserEntity.Products;

import java.util.List;

public class ViewCartdto {
    private List<Products> products;
    private long total;
    private String message;
    private boolean status;

    public List<Products> getProducts() {
        return products;
    }

    public void setProducts(List<Products> products) {
        this.products = products;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
