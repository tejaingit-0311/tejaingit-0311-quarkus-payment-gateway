package payment.gateway.Repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import payment.gateway.UserEntity.PaymentHistory;

@ApplicationScoped
public class PaymentRepo implements PanacheRepository<PaymentHistory> {
}
