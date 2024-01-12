package payment.gateway.UserResource;


//import com.razorpay.RazorpayClient;
import com.razorpay.*;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
        import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.json.JSONObject;
import payment.gateway.CardDetailsRequestDto.CardDetails;
import payment.gateway.OrderIdDto.OrderId;
import payment.gateway.PaymentCaptureHistory.PaymentCaptureHistory;
import payment.gateway.ProductsDto.ProductsDto;
import payment.gateway.RazorPayCheckoutRequest.CheckoutRequest;
import payment.gateway.RazorPayCheckoutResponse.CheckoutResponse;
import payment.gateway.Repository.*;
import payment.gateway.UserEntity.*;
import payment.gateway.UserEntity.Customer;
import payment.gateway.ViewCartDTO.ViewCartdto;
import payment.gateway.exceptions.CustomerNotFoundException;

import java.security.SignatureException;
import java.time.LocalDate;
import java.util.*;


@Path("/user")
public class UserResource {
    @Inject
    ProductsRepository productsRepo;
    @Inject
    CustomerRepo customerRepo;

    //Inject in the service class and call the method of this repository
    @Inject
    CartRepo cartRepo;

    @Inject
    PaymentRepo paymentRepo;

    //Verify which customer is buying the product
    private boolean isValidCustomer(int id){

        Customer cust = customerRepo.findById((long)id);
        if(cust != null) {
            if (cust.getCid() == id)
                return true;
            else
                return false;
        }else
            return false;
    }
    private boolean isValidProduct(int pid){
        Optional<Products> prod = productsRepo.findByIdOptional((long) pid);
        if(prod.isPresent()){
            Products product = prod.get();
            if(product.getId() == pid)
                return true;
            else
                return false;
        }else
            return false;
    }
    //APIs to create:
        //UserLogin, Register, viewAllProducts, addToCart, buyNow

    //Userlogin or Loginpage
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/login")
    public Response login(@QueryParam("uname")String username, @QueryParam("pwd") String password){
        Customer cust = customerRepo.find("cusername", username).firstResult();
        if(cust != null) {
            if (cust.getCusername().equals(username) && cust.getCpwd().equals(password))
                //redirect to Homepage
                return Response.ok("Welcome To Homepage...\n"+ productsRepo.listAll()).build();
            else
                return Response.ok("Invalid Credentials").build();
        }else
            return Response.ok("Please Register...").build();

    }
    //RegisterCustomer
    @Path("/register")
    @POST
    @Transactional
    @Produces(MediaType.TEXT_PLAIN)
    public Response registerCustomer(Customer customer){

        customerRepo.persist(customer);
        return Response.ok("Customer Saved").build();
    }

    //viewAllProducts
    @GET
    @Path("/getproducts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProducts(){
        List<Products> prod = productsRepo.listAll();
        return Response.ok(prod).build();
    }
//
//    //viewallcustomers
//    @GET
//    @Path("/viewAllCustomers")
//    public Response getCustomers(){
//        return Response.ok(customerRepo.listAll()).build();
//    }

    @POST
    @Path("/saveproducts")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response saveProducts(List<Products> prods){
        productsRepo.persist(prods);
        return Response.ok("Products saved").build();
    }

    @Path("/addToCart")
    @POST
    @Transactional
    @Produces(MediaType.TEXT_PLAIN)
    //customer token, quantity
    public Response addToCart(@QueryParam("cid") int cid, @QueryParam("pid") int pid, @QueryParam("quantity") int quantity){
        if(isValidCustomer(cid)){
            Optional<Products> optionalProduct = productsRepo.findByIdOptional((long) pid);
            if(optionalProduct.isPresent()) {
                Products prod = optionalProduct.get();
                //Check whether item is in the stock
                if(!prod.isInStock()){
                    return Response.ok("Current Item is not in the stock").build();
                }else {
                    Customer cust = customerRepo.findById((long) cid);
                    //how many pieces we have that much quantity customer can select
                    //internally it calls -> /getUnits
                    //int units = prod.getUnits();
                    //display the units
                    //user selects within the range of displayed units
                    //update the (Products) table according to selected units by the customer

                    //if customer exceeds the asking limits:remainingUnits = -1
                    short remainingUnits = (short) (prod.getUnits() - quantity);
                    if(remainingUnits < 0){
                        return Response.ok("Choose the quantity, within this range: " + prod.getUnits()).build();
                    }else{
                        long old_total = cust.getCtotal(), curr_total = 0;
                        prod.setUnits(remainingUnits);
                        //if in stock last prod sold out then:
                        if(prod.getUnits() == 0)
                            prod.setInStock(false);

                        productsRepo.persist(prod);
                        Cart cart = new Cart();
                        LocalDate createdDate = LocalDate.now();
                        cart.setDate(createdDate);
                        cart.setProducts(prod);
                        cart.setCustomer(cust);
                        cart.setQuantity(quantity);
                        cust.setCtotal(old_total + (prod.getPcost() * cart.getQuantity()));
                        customerRepo.persist(cust);
                        cartRepo.persist(cart);

                        //show the total of products, present in the cart
//                        List<Cart> custCart = cust.getCart();
//                        for(Cart custcart : custCart){
//                            Products product = custcart.getProducts();
//                           curr_total = curr_total + (product.getPcost() * cart.getQuantity());
//                        }
//                        old_total = curr_total;
//                        cust.setCtotal(old_total);
//                        customerRepo.persist(cust);
                        return Response.ok("Product added in the Cart").build();
                    }

                }
            }else{
                return Response.ok("Product doesn't exist").build();
            }
        }
        return Response.ok("Customer doesn't exist").build();
    }


    //delete the product from the cart
    // /removetheproduct: from the cart-> cid, pid



    @GET
    @Path("/viewcart{cid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response viewCart(@PathParam("cid") int cid) {
        ViewCartdto vcdto = new ViewCartdto();
        if (isValidCustomer(cid)) {
            Customer cust = customerRepo.findById((long) cid);//1
            List<Cart> custCart = cust.getCart();
            //if the customer didn't added anything in the cart
            if(custCart.isEmpty()){
                vcdto.setProducts(List.of());
                vcdto.setTotal(0);
                vcdto.setStatus(false);
                vcdto.setMessage("FAILED");
                return Response.ok(vcdto).build();
            }else {
                //We only want pname and pcost: created ProductsDto
                ArrayList<Products> prodlist = new ArrayList<>();
                //which product is bought by which customer:
                //boolean success = true;
                for (Cart cart : custCart) {
                    prodlist.add(cart.getProducts());
                }
                customerRepo.persist(cust);
                vcdto.setProducts(prodlist);
                vcdto.setTotal(cust.getCtotal());
                vcdto.setMessage("SUCCESS");
                vcdto.setStatus(true);
                return Response.ok(vcdto).build();
            }
        } else {
            return Response.ok(new CustomerNotFoundException("Customer Not Found")).build();
        }
    }
    @DELETE
    @Path("/deleteproduct{pid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response deleteProduct(@QueryParam("cid") int cid ,@RequestBody ProductsDto pdto){
        if(!isValidCustomer(cid)){
            String s = "Customer Not Found";
            return Response.ok(s).build();
        }else {
            Customer cust = customerRepo.findById((long) cid);
            //check for the valid product:
            //in real time in cart, added products only will be there while adding in the cart
            //In real time product will be deleted that is sent via @RequestBody Products prod -> remove(prod)
                List<Cart> cart = cust.getCart();
                if(cart.isEmpty()) {
                    String s = "Your Cart Is Empty";
                    return Response.ok(s).build();
                }
                else {
                       //cart.removeIf(cart1 -> cart1.getProducts().getId().equals(pdto));
                    for (Cart c:cart) {
                      if (c.getProducts().getId().equals(pdto.getPid())){
                          cart.remove(c);
                          cust.setCart(cart);
                          customerRepo.persist(cust);
                          return Response.ok("Product Removed from the Cart").build();
                      }
                    }
                    return Response.ok("Product not exist").build();
                }
        }
    }
    //OrderCreation
    @Path("/placeorder")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response placeAnOrder(@QueryParam("cid") int cid) throws RazorpayException{
        if(isValidCustomer(cid)){
            OrderId orderId = new OrderId();
            Customer cust  = customerRepo.findById((long) cid);
            try{
            RazorpayClient razorpay = new RazorpayClient("rzp_test_SMuMe11eNRBTkt", "ldbW0oVHlGWZ3eEXiX5xhd5J");
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", cust.getCtotal() * 100); // amount in the smallest currency unit (should be in paise)
            orderRequest.put("currency", "INR");

            Order order = razorpay.orders.create(orderRequest);


                orderId.setOrder_id  ((String)order.get("id"));
                orderId.setMessage("OrderId Generated");
                orderId.setStatus(true);
            return Response.ok(orderId).build();
            } catch (RazorpayException re){
                orderId.setOrder_id("");
                orderId.setMessage("OrderId Is Not Generated");
                orderId.setStatus(true);
                //exception page
                return Response.ok(orderId).build();
            }

        }
           // CustomerNotFound cnf = new CustomerNotFound();
            String cnf = "Customer Not Found";
            return Response.ok(cnf).build();
    }



    //Checkout RazorPayResponse : razorpay_payment_id, razorpay_order_id, razorpay_signature and will be shown in alert

    //This will be triggered just after Payment is received by our website.
    //This is used to verify after the payment captured by our website internally calls this endpoint.
    //And that payment received is from the authentic source or not
    @POST
    @Path("/savedetails")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    //@RequestBody will take care of initializing the values in the CheckoutRequest class
    //send the AMOUNT from the UI to store it in the DB
    public Response saveDetails(@RequestBody CheckoutRequest cr) throws RazorpayException {
        CheckoutResponse cres = new CheckoutResponse();
        //store the details in the DB: in order to capture the Payment in our website with the help of payment_id
        if(!cr.isSuccess()){
            cres.setMessage("Failed");
            cres.setStatus(false);
          return Response.ok(cres).build();
        }
        CheckoutPaymentResponse ph = new CheckoutPaymentResponse();
        ph.setRazorpay_payment_id(cr.getRazorpay_payment_id());
        ph.setRazorpay_order_id(cr.getRazorpay_order_id());
        ph.setRazorpay_signature(cr.getRazorpay_signature());
        paymentRepo.persist(ph);
        cres.setMessage("Success");
        cres.setStatus(true);
        return Response.ok(cres).build();


    }

     /*@POST
    @Path("/pay")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response capture(@RequestBody CapturePayment capturePayment) throws RazorpayException{
        //After authentication capture the amount into my Website
        RazorpayClient razorpay = new RazorpayClient("[YOUR_KEY_ID]", "[YOUR_KEY_SECRET]");

        String paymentId = capturePayment.getPaymentid();

        JSONObject paymentRequest = new JSONObject();
        paymentRequest.put("amount", capturePayment.getAmount());
        paymentRequest.put("currency", "INR");
    //Payment Success or Failed
        Payment payment = razorpay.payments.capture(paymentId, paymentRequest);
        //store the response of the Payment
        PaymentHistory ph = new PaymentHistory(payment);
        return Response.ok(ph).build();
    }*/
    /* @POST
    @Path("/pay")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response pay(@RequestBody CardDetails cardDetails) throws RazorpayException{
        RazorpayClient razorpay = new RazorpayClient("rzp_test_SMuMe11eNRBTkt", "ldbW0oVHlGWZ3eEXiX5xhd5J");

        //pass the carddetailsobj in the method
        //fetch the requestCardRef.. Check if that card details is matched then we have valid card with that customer...press ok
        //then deduct that amount from the customer and update it in customer's table..
        //  public Card requestCardReference(JSONObject request) throws RazorpayException {
        //        return (Card)this.post("v1", "cards/fingerprints", request);
        //    }
        JSONObject carddetails= new JSONObject();
        carddetails.put("CardNetwork" , cardDetails.getCardNetwork());
        carddetails.put("CardNumber", cardDetails.getCardNumber());
        carddetails.put("CVV", cardDetails.getCvv());
        carddetails.put("ExpirayDate", cardDetails.getExpdate());
        //retrived Card
            Card card = razorpay.cards.requestCardReference(carddetails);
        if(cardDetails.equals(card)){
            return Response.ok("Payment Succesful").build();
        }
        //re-enter the details of the card and make the payment
        return Response.ok("Re-enter the Card Details").build();
        //return the Card Details: if success then redirect to the(UI)home page, else redirect to the payment failed page

    }*/

}

