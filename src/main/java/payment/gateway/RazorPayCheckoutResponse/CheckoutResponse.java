package payment.gateway.RazorPayCheckoutResponse;

public class CheckoutResponse {
    private String message;
    private boolean status;

    private long amount;


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
