package org.bp.paymentclient;

import org.bp.payment.model.PaymentRequest;
import org.bp.payment.model.PaymentResponse;
import org.bp.paymentclient.PaymentValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Controller
public class PaymentController {
    private final RestTemplate restTemplate;
    private final PaymentValidationService validationService;

    public PaymentController(RestTemplate restTemplate, PaymentValidationService validationService) {
        this.restTemplate = restTemplate;
        this.validationService = validationService;
    }

    @GetMapping("/payment")
    public String payForm(Model model) {
        model.addAttribute("paymentRequest", new PaymentRequest());
        return "payment";
    }


    @PostMapping("/payment")
    public String pay(@ModelAttribute PaymentRequest paymentRequest, Model model) {
        try {
            validationService.validatePaymentRequest(paymentRequest);

            ResponseEntity<PaymentResponse> re = restTemplate.postForEntity(
                    "http://localhost:8083/payment", paymentRequest,
                    PaymentResponse.class);
            model.addAttribute("paymentResponse", re.getBody());
            return "result";
        } catch (IllegalArgumentException e) {
            // Catch validation errors and send the message to the fault page
            model.addAttribute("faultMsg", e.getMessage());
            return "fault";
        } catch (HttpClientErrorException e) { //catch 4xx response codes
            model.addAttribute("faultMsg", e.getMessage());
            return "fault";
        }
    }

}
