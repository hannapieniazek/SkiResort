package org.bp.skitripui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
public class BookingController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "http://localhost:8090/api/skitrip/booking";

    @GetMapping("/")
    public String bookingForm(Model model) {
        model.addAttribute("bookTripRequest", new BookTripRequest());
        return "booking-form";
    }

    @PostMapping("/book")
    public String bookTrip(@ModelAttribute BookTripRequest bookTripRequest, Model model) {
        try {
            BookingInfo bookingInfo = restTemplate.postForObject(apiUrl, bookTripRequest, BookingInfo.class);
            model.addAttribute("bookingInfo", bookingInfo);
            return "booking-result";
        } catch (Exception e) {
            model.addAttribute("error", "Error booking trip: " + e.getMessage());
            return "error";
        }
    }
    @GetMapping("/error")
    public String handleError(@RequestParam(name = "message", required = false) String message, Model model) {
        model.addAttribute("errorMessage", message != null ? message : "An unexpected error occurred.");
        return "error";
    }

}