package org.bp.paymentclient;

import org.bp.payment.model.PaymentRequest;
import org.bp.payment.model.PaymentCard;
import org.bp.payment.model.Amount;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.regex.Pattern;

@Service
public class PaymentValidationService {

    public void validatePaymentRequest(PaymentRequest paymentRequest) {
        if (paymentRequest == null) {
            throw new IllegalArgumentException("Payment request cannot be null.");
        }

        validatePaymentCard(paymentRequest.getPaymentCard());
        validateAmount(paymentRequest.getAmount());
    }

    private void validatePaymentCard(PaymentCard card) {
        if (card == null) {
            throw new IllegalArgumentException("Payment card cannot be null.");
        }

        // Validate card name
        if (card.getName() == null || !Pattern.matches("^[a-zA-Z ]+$", card.getName())) {
            throw new IllegalArgumentException("Card name must contain only letters and spaces.");
        }

        // Validate valid-to date (MM/YY and in the future)
        if (card.getValidTo() == null || !Pattern.matches("^(0[1-9]|1[0-2])/\\d{2}$", card.getValidTo())) {
            throw new IllegalArgumentException("Card valid-to date must be in the format MM/YY.");
        }

        YearMonth validToDate = YearMonth.parse(card.getValidTo(), DateTimeFormatter.ofPattern("MM/yy"));
        if (validToDate.isBefore(YearMonth.now())) {
            throw new IllegalArgumentException("Card has expired.");
        }

        // Validate card number (only digits)
        if (card.getNumber() == null || !card.getNumber().matches("^\\d+$")) {
            throw new IllegalArgumentException("Card number must contain only digits.");
        }
    }

    private void validateAmount(Amount amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null.");
        }

        // Validate value
        if (amount.getValue() == null || amount.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount value must be greater than 0.");
        }

        // Validate currency
        if (amount.getCurrency() == null) {
            throw new IllegalArgumentException("Currency cannot be null.");
        }

        try {
            Currency.getInstance(amount.getCurrency());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid currency code: " + amount.getCurrency());
        }
    }
}
