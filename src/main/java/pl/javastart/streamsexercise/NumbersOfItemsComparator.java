package pl.javastart.streamsexercise;

import java.util.Comparator;

public class NumbersOfItemsComparator implements Comparator<Payment> {
    @Override
    public int compare(Payment payment1, Payment payment2) {
        return Integer.compare(payment1.getPaymentItems().size(), payment2.getPaymentItems().size());
    }
}
