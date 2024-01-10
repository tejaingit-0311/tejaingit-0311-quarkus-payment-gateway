package payment.gateway.exceptions;

public class SignatureMismatchException extends Exception{
    public SignatureMismatchException(String message){
        super(message);
    }
}
