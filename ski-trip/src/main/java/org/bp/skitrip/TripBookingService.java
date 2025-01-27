package org.bp.skitrip;
import static org.apache.camel.model.rest.RestParamType.body;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.model.rest.RestBindingMode;
import org.bp.skitrip.model.TripBookingRequest;
import org.bp.skitrip.model.Utils;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class TripBookingService extends RouteBuilder{
    @Autowired
    BookingIdentifierService bookingIdentifierService;

    @Override
    public void configure() throws Exception {
        restConfiguration()
                .component("servlet")
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true")
                .enableCORS(true)
                .contextPath("/api")
                // turn on swagger api-doc
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "Ski trip booking API")
                .apiProperty("api.version", "1.0.0");
        rest("/skitrip").description("Ski trip booking REST service")
                .consumes("application/json")
                .produces("application/json")
                .post("/booking").description("Book a ski trip").type(TripBookingRequest.class).outType(org.bp.payment.PaymentResponse.class)
                                .param().name("body").type(body).description("The trip to book").endParam()
                                .responseMessage().code(200).message("Ski trip successfully booked").endResponseMessage()
                                .to("direct:bookTrip");
        final JaxbDataFormat jaxbSkiEquipment = new
                JaxbDataFormat(org.bp.skiequipment.RentSkiEquipmentResponse.class.getPackage().getName());
        from("direct:rentSkiEquipment").routeId("rentSkiEquipment")
                .log("rentSkiEquipment fired")
                .saga()
                    .propagation(SagaPropagation.MANDATORY)
                    .compensation("direct:cancelSkiEquipment").option("tripBookingId",
                            simple("${exchangeProperty.tripBookingId}"))
                .process((exchange) ->
                {exchange.getMessage().setBody(
                        Utils.prepareSkiEquipmentRentalRequest(exchange.getMessage().getBody(TripBookingRequest.class)));
                } )
                .marshal(jaxbSkiEquipment)
                .to("spring-ws:http://localhost:8081/soap-api/service/skiequipment")
                .to("stream:out")
                .unmarshal(jaxbSkiEquipment)
                .process((exchange) -> {
                    org.bp.skiequipment.RentSkiEquipmentResponse rentSkiEquipmentResponse =
                            exchange.getMessage().getBody(org.bp.skiequipment.RentSkiEquipmentResponse.class);
                    String tripBookingId = exchange.getProperty("tripBookingId", String.class);
                    bookingIdentifierService.assignSkiEquipmentRentalId(tripBookingId,
                            rentSkiEquipmentResponse.getReturn().getId());
                })
        ;
        from("direct:cancelSkiEquipment").routeId("cancelSkiEquipment")
                .log("cancelSkiEquipment fired")
                .process((exchange) -> {
                    String tripBookingId=exchange.getMessage().getHeader("tripBookingId", String.class);
                    int skiEquipmentRentalId = bookingIdentifierService.getSkiEquipmentRentalId(tripBookingId);
                    org.bp.skiequipment.CancelBooking cancelSkiEquipmentRentalRequest = new org.bp.skiequipment.CancelBooking();
                    cancelSkiEquipmentRentalRequest.setArg0(skiEquipmentRentalId);
                    exchange.getMessage().setBody(cancelSkiEquipmentRentalRequest);
                } )
                .marshal(jaxbSkiEquipment)
                .to("spring-ws:http://localhost:8081/soap-api/service/skiequipment")
                .to("stream:out")
                .unmarshal(jaxbSkiEquipment);

        final JaxbDataFormat jaxbSkiPass = new JaxbDataFormat(org.bp.BookingInfo.class.getPackage().getName());
        from("direct:buySkiPass").routeId("buySkiPass")
                .log("buySkiPass fired")
                .saga()
                    .propagation(SagaPropagation.MANDATORY)
                    .compensation("direct:cancelSkiPass").option("tripBookingId",
                            simple("${exchangeProperty.tripBookingId}"))
                .process((exchange) ->
                {exchange.getMessage().setBody(
                        Utils.prepareSkiPassPurchaseRequest(exchange.getMessage().getBody(TripBookingRequest.class)));
                } )
                .marshal(jaxbSkiPass)
                .to("spring-ws:http://localhost:8080/soap-api/service/skipass")
                .to("stream:out")
                .unmarshal(jaxbSkiPass)
                .process((exchange) -> {
                    org.bp.BookingInfo buySkiPassResponse = exchange.getMessage().getBody(org.bp.BookingInfo.class);
                    String tripBookingId = exchange.getProperty("tripBookingId", String.class);
                    bookingIdentifierService.assignSkiPassPurchaseId(tripBookingId, buySkiPassResponse.getId());
                })
        ;
        from("direct:payment").routeId("payment")
                .streamCaching()
                .log("payment fired")
                .marshal().json()
                .removeHeaders("CamelHttp*")
                .to("rest:post:payment?host=localhost:8083")
                .to("stream:out")
                .unmarshal().json()
                .end();
        ;
        from("direct:cancelSkiPass").routeId("cancelSkiPass")
                .log("cancelSkiPass fired")
                .process((exchange) -> {
                    String tripBookingId = exchange.getMessage().getHeader("tripBookingId", String.class);
                    int skiPassPurchaseId = bookingIdentifierService.getSkiPassPurchaseId(tripBookingId);
                    org.bp.CancelPurchaseRequest cancelSkiTripRequest = new org.bp.CancelPurchaseRequest();
                    cancelSkiTripRequest.setBookingId(skiPassPurchaseId);
                    exchange.getMessage().setBody(cancelSkiTripRequest);
                } )
                .marshal(jaxbSkiPass)
                .to("spring-ws:http://localhost:8080/soap-api/service/skipass")
                .to("stream:out")
                .unmarshal(jaxbSkiPass)
        ;
        from("direct:bookTrip").routeId("bookTrip")
                .log("bookTrip fired")
                .process(
                        (exchange) -> {
                            exchange.setProperty("paymentRequest",
                                    Utils.preparePaymentRequest(exchange.getMessage().getBody(TripBookingRequest.class)));
                            exchange.setProperty("tripBookingId", bookingIdentifierService.generateBookingId());
                        }
                )
                .saga()
                .multicast()
                .parallelProcessing()
                .aggregationStrategy(
                        (prevEx, currentEx) -> {
                            if (currentEx.getException()!=null){
                                log.error("Error in current exchange", currentEx.getException());
                                return currentEx;
                            }
                            if (prevEx!=null && prevEx.getException()!=null)
                                return prevEx;
                            org.bp.payment.PaymentRequest paymentRequest;
                            if (prevEx==null)
                                paymentRequest=currentEx.getProperty("paymentRequest",
                                        org.bp.payment.PaymentRequest.class);
                            else
                                paymentRequest=prevEx.getMessage().getBody(org.bp.payment.PaymentRequest.class);
                            Object body = currentEx.getMessage().getBody();
                            BigDecimal cost;
                            if (body instanceof org.bp.BookingInfo)
                                cost=((org.bp.BookingInfo)body).getCost();
                            else if (body instanceof org.bp.skiequipment.RentSkiEquipmentResponse)
                                cost=((org.bp.skiequipment.RentSkiEquipmentResponse)body).getReturn().getCost();
                            else
                                return prevEx;
                            paymentRequest.getAmount().setValue(
                                    paymentRequest.getAmount().getValue().add(cost));
                            currentEx.getMessage().setBody(paymentRequest);
                            return currentEx;
                        }
                )
                .to("direct:rentSkiEquipment")
                .to("direct:buySkiPass")
                .end()
                .process(
                        (currentEx) -> {
                            currentEx.getMessage().setBody(currentEx.getProperty("paymentRequest",
                                    org.bp.payment.PaymentRequest.class));
                        }
                )
                .to("direct:payment")
                .removeHeaders("Camel*")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
        ;



    }
}
