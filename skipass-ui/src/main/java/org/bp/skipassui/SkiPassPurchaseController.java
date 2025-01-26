package org.bp.skipassui;

import org.bp.BuySkiPassRequest;
import org.bp.skipass.SkiPassFaultMsg;
import org.bp.skipass.SkiPassPurchase;
import org.bp.skipass.SkiPassPurchaseEndpointService;
import org.bp.types.BookingInfo;
import org.bp.types.Person;
import org.bp.types.SkiPass;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.net.URL;

@Controller
public class SkiPassPurchaseController {
    private static final QName SERVICE_NAME = new QName("http://skipass.bp.org/", "SkiPassPurchaseEndpointService");

    @GetMapping("/buySkiPass")
    public String buySkiPassForm(Model model) {
        BuySkiPassRequest request = new BuySkiPassRequest();
        request.setPerson(new Person());
        request.setSkiPass(new SkiPass());
        model.addAttribute("buySkiPassRequest", request);
        return "buySkiPass";
    }

    @PostMapping("/buySkiPass")
    public String buySkiPass(@ModelAttribute BuySkiPassRequest bspr, Model model) {
        System.out.println("Invoking buySkiPass...");
        System.out.println("BuySkiPassRequest details: " + bspr);

        System.out.println("Person First Name: " + bspr.getPerson().getFirstName());
        System.out.println("Person Last Name: " + bspr.getPerson().getLastName());
        System.out.println("SkiPass Type: " + bspr.getSkiPass().getType());
        System.out.println("SkiPass Duration: " + bspr.getSkiPass().getDuration());

        URL wsdlURL = SkiPassPurchaseEndpointService.WSDL_LOCATION;
        SkiPassPurchaseEndpointService endpointService = new SkiPassPurchaseEndpointService(wsdlURL, SERVICE_NAME);
        SkiPassPurchase port = endpointService.getSkiPassPurchaseEndpointPort();
        try {
            BookingInfo _buySkiPass__return = port.buySkiPass(bspr);
            System.out.println("buySkiPass.result=" + _buySkiPass__return);
            model.addAttribute("bookingInfo", _buySkiPass__return);
            return "result";
        } catch (SkiPassFaultMsg e) {
            System.out.println("Expected exception: SkiPassFaultMsg has occured.");
            String faultCode = String.valueOf(e.getFaultInfo().getCode());
            String faultMessage = e.getFaultInfo().getText();
            model.addAttribute("faultCode", faultCode);
            model.addAttribute("faultMessage", faultMessage);
            model.addAttribute("skiPassFaultMsg", e);
            System.out.println("Fault details: " + e.getMessage());
            System.out.println("Fault Code: " + e.getFaultInfo().getCode());
            System.out.println("Fault Message: " + e.getFaultInfo().getText());
            return "fault";
        }
    }
}
