package payment.gateway.Repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import payment.gateway.UserEntity.Customer;
import payment.gateway.UserEntity.Products;

import java.util.List;

@ApplicationScoped
public class ProductsRepository implements PanacheRepository <Products>{

//    public boolean deleteById(Long aLong);
}
