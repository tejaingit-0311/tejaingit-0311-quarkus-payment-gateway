package payment.gateway.Repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import payment.gateway.PaymentCaptureHistory.PaymentCaptureHistory;
import payment.gateway.UserEntity.CheckoutPaymentResponse;

@ApplicationScoped
public class PaymentRepo implements PanacheRepository<CheckoutPaymentResponse> {
}
