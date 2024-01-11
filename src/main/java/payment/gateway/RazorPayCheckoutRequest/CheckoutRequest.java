package payment.gateway.RazorPayCheckoutRequest;

public class CheckoutRequest {
    private String razorpay_payment_id;
    private String razorpay_order_id;
    private String razorpay_signature;

    private long amount;

    public String getRazorpay_payment_id() {
        return razorpay_payment_id;
    }

    public void setRazorpay_payment_id(String razorpay_payment_id) {
        this.razorpay_payment_id = razorpay_payment_id;
    }

    public String getRazorpay_order_id() {
        return razorpay_order_id;
    }

    public void setRazorpay_order_id(String razorpay_order_id) {
        this.razorpay_order_id = razorpay_order_id;
    }

    public String getRazorpay_signature() {
        return razorpay_signature;
    }

    public void setRazorpay_signature(String razorpay_signature) {
        this.razorpay_signature = razorpay_signature;
    }

    public void setAmount(long amount){
        this.amount = amount;
    }
    public long getAmount() {
        return amount;
    }
}
