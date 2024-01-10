package payment.gateway.exceptions;

public class OrderIdGenerationException extends Exception{
    public OrderIdGenerationException(String message) {
        super(message);
    }
}
