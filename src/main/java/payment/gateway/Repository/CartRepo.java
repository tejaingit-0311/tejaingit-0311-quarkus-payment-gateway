package payment.gateway.Repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import payment.gateway.UserEntity.Cart;
import javax.swing.text.html.parser.Entity;

@ApplicationScoped
public class CartRepo implements PanacheRepository<Cart>{

}
