package pl.javastart.streamsexercise;

import java.util.Comparator;

public class PaymentDateComparator implements Comparator<Payment> {
    @Override
    public int compare(Payment payment1, Payment payment2) {
        return payment1.getPaymentDate().compareTo(payment2.getPaymentDate());
    }
}
