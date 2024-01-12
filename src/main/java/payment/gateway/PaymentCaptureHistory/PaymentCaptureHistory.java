package payment.gateway.PaymentCaptureHistory;

import com.razorpay.Payment;

public class PaymentCaptureHistory {
    private Payment payment;

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public PaymentCaptureHistory(Payment payment){
        this.payment = payment;
    }

}
