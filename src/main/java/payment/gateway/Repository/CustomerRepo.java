package payment.gateway.Repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import payment.gateway.UserEntity.Customer;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CustomerRepo implements PanacheRepository<Customer> {

}
