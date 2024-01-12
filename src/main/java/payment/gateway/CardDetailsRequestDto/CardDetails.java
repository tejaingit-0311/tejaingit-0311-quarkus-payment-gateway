package payment.gateway.CardDetailsRequestDto;

import java.util.Objects;

public class CardDetails {
    private String cardNetwork;
    private long cardNumber;
    private short  cvv;
    private String expdate;

    public String getCardNetwork() {
        return cardNetwork;
    }

    public void setCardNetwork(String cardNetwork) {
        this.cardNetwork = cardNetwork;
    }

    public long getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(long cardNumber) {
        this.cardNumber = cardNumber;
    }

    public short getCvv() {
        return cvv;
    }

    public void setCvv(short cvv) {
        this.cvv = cvv;
    }

    public String getExpdate() {
        return expdate;
    }

    public void setExpdate(String expdate) {
        this.expdate = expdate;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardDetails that = (CardDetails) o;
        return cardNumber == that.cardNumber && cvv == that.cvv && Objects.equals(cardNetwork, that.cardNetwork) && Objects.equals(expdate, that.expdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardNetwork, cardNumber, cvv, expdate);
    }
}
