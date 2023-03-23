package pl.javastart.streamsexercise;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DateTimeProvider dateTimeProvider;

    PaymentService(PaymentRepository paymentRepository, DateTimeProvider dateTimeProvider) {
        this.paymentRepository = paymentRepository;
        this.dateTimeProvider = dateTimeProvider;
    }

    /*
    Znajdź i zwróć płatności posortowane po dacie rosnąco
     */
    List<Payment> findPaymentsSortedByDateAsc() {
        return paymentRepository.findAll().stream()
                .sorted(new PaymentDateComparator())
                .collect(Collectors.toList());
    }

    /*
    Znajdź i zwróć płatności posortowane po dacie malejąco
     */
    List<Payment> findPaymentsSortedByDateDesc() {
        List<Payment> sortedPayments = findPaymentsSortedByDateAsc();
        Collections.reverse(sortedPayments);
        return sortedPayments;
    }

    /*
    Znajdź i zwróć płatności posortowane po liczbie elementów rosnąco
     */
    List<Payment> findPaymentsSortedByItemCountAsc() {
        return paymentRepository.findAll().stream()
                .sorted(new NumbersOfItemsComparator())
                .collect(Collectors.toList());
    }

    /*
    Znajdź i zwróć płatności posortowane po liczbie elementów malejąco
     */
    List<Payment> findPaymentsSortedByItemCountDesc() {
        List<Payment> sortedPayments = findPaymentsSortedByItemCountAsc();
        Collections.reverse(sortedPayments);
        return sortedPayments;
    }

    /*
    Znajdź i zwróć płatności dla wskazanego miesiąca
     */
    List<Payment> findPaymentsForGivenMonth(YearMonth yearMonth) {
        return paymentRepository.findAll().stream()
                .filter(p -> YearMonth.from(p.getPaymentDate()).equals(yearMonth))
                .collect(Collectors.toList());

    }

    /*
    Znajdź i zwróć płatności dla aktualnego miesiąca
     */
    List<Payment> findPaymentsForCurrentMonth() {
        return paymentRepository.findAll().stream()
                .filter(p -> YearMonth.from(p.getPaymentDate()).equals(dateTimeProvider.yearMonthNow()))
                .collect(Collectors.toList());
    }

    /*
    Znajdź i zwróć płatności dla ostatnich X dni
     */
    List<Payment> findPaymentsForGivenLastDays(int days) {
        ZonedDateTime currentDateTimeMinus5days = dateTimeProvider.zonedDateTimeNow().minusDays(days);
        return paymentRepository.findAll().stream()
                .filter(p -> p.getPaymentDate().isAfter(currentDateTimeMinus5days))
                .collect(Collectors.toList());
    }

    /*
    Znajdź i zwróć płatności z jednym elementem
     */
    Set<Payment> findPaymentsWithOnePaymentItem() {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getPaymentItems().size() == 1)
                .collect(Collectors.toSet());
    }

    /*
    Znajdź i zwróć nazwy produktów sprzedanych w aktualnym miesiącu
     */
    Set<String> findProductsSoldInCurrentMonth() {
        List<Payment> paymentsForCurrentMonth = findPaymentsForCurrentMonth();
        return paymentsForCurrentMonth.stream()
                .map(Payment::getPaymentItems)
                .flatMap(List::stream)
                .map(PaymentItem::getName)
                .collect(Collectors.toSet());
    }

    /*
    Policz i zwróć sumę sprzedaży dla wskazanego miesiąca
     */
    BigDecimal sumTotalForGivenMonth(YearMonth yearMonth) {
        List<PaymentItem> paymentItemListForGivenMonth = createPaymentItemListForGivenMonth(yearMonth);
        return paymentItemListForGivenMonth.stream()
                .map(PaymentItem::getFinalPrice)
                .reduce(BigDecimal.valueOf(0), BigDecimal::add);
    }

    private List<PaymentItem> createPaymentItemListForGivenMonth(YearMonth yearMonth) {
        return paymentRepository.findAll().stream()
                .filter(p -> YearMonth.from(p.getPaymentDate()).equals(yearMonth))
                .map(Payment::getPaymentItems)
                .flatMap(List::stream)
                .toList();
    }

    /*
    Policz i zwróć sumę przyznanych rabatów dla wskazanego miesiąca
     */
    BigDecimal sumDiscountForGivenMonth(YearMonth yearMonth) {
        List<PaymentItem> paymentItemListForGivenMonth = createPaymentItemListForGivenMonth(yearMonth);
        return paymentItemListForGivenMonth.stream()
                .map(p -> p.getRegularPrice().subtract(p.getFinalPrice()))
                .reduce(BigDecimal.valueOf(0), BigDecimal::add);
    }

    /*
    Znajdź i zwróć płatności dla użytkownika z podanym mailem
     */
    List<PaymentItem> getPaymentsForUserWithEmail(String userEmail) {
        Optional<User> user = getUser(userEmail);
        if (user.isPresent()) {
            User user1 = user.get();
            return paymentRepository.findAll().stream()
                    .filter(p -> p.getUser().equals(user1))
                    .map(Payment::getPaymentItems)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private Optional<User> getUser(String userEmail) {
        return paymentRepository.findAll().stream()
                .map(Payment::getUser)
                .filter(u -> u.getEmail().equals(userEmail))
                .findAny();
    }

    /*
    Znajdź i zwróć płatności, których wartość przekracza wskazaną granicę
     */
    Set<Payment> findPaymentsWithValueOver(int value) {
        return paymentRepository.findAll().stream()
                .filter(p -> p.sumAllItemsPrices().compareTo(BigDecimal.valueOf(value)) > 0)
                .collect(Collectors.toSet());
    }
}
