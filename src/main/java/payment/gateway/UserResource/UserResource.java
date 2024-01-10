package payment.gateway.UserResource;


//import com.razorpay.RazorpayClient;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
        import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.json.JSONObject;
import payment.gateway.OrderDto.OrderId;
import payment.gateway.ProductsDto.ProductsDto;
import payment.gateway.RazorPayCheckoutRequest.CheckoutRequest;
import payment.gateway.RazorPayCheckoutResponse.CheckoutResponse;
import payment.gateway.Repository.*;
import payment.gateway.UserEntity.*;
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

//    @RestClient
//    RazorpayClient rc;

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
//    @GET
//    @Path("/viewAllProducts")
//    public Response getProducts(){
//        return Response.ok(productsRepo.listAll()).build();
//    }
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
    //delete the product from the cart
    // /removetheproduct: from the cart-> cid, pid

    @DELETE
    @Path("/deleteproduct{pid}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteProduct(@QueryParam("cid") int cid, @PathParam("pid") int pid){
        if(!isValidCustomer(cid)){
            return Response.ok("Customer Not Found").build();
        }else {
            Customer cust = customerRepo.findById((long) cid);
            //check for the valid product
                if(!isValidProduct(pid)){
                    return Response.ok("Not A Valid Product").build();
                }else{
                    //in real time product will be deleted that is sent via @RequestBody Products prod -> remove(prod)
                    List<Cart> cart = cust.getCart();
                    if(cart.isEmpty())
                        return Response.ok("Your Cart Is EMPTY ").build();
                    else {
                        cart.remove(pid - 1);
                        return Response.ok("Product Removed From the Cart").build();
                    }
                }
        }
    }

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
            orderRequest.put("amount", cust.getCtotal()); // amount in the smallest currency unit (should be in paise)
            orderRequest.put("currency", "INR");

            Order order = razorpay.orders.create(orderRequest);
                orderId.setOrder_id  ((String)order.get("id"));
                orderId.setMessage("OrderId Generated");
                orderId.setStatus(true);
            return Response.ok(orderId).build();
            } catch (RazorpayException re){
                orderId.setOrder_id("");
                orderId.setMessage("OrderId Not Generated");
                orderId.setStatus(true);
                //exception page
                return Response.ok(orderId).build();
            }

        }else
            return Response.ok("Not Valid Customer").build();
    }
    //Checkout RazorPayResponse : razorpay_payment_id, razorpay_order_id, razorpay_signature and will be shown in alert

    //This will be triggered just after getting generating CheckoutData:
    @POST
    @Path("/charge")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    //@RequestBody will take care of initializing the values in the CheckoutRequest class
    public Response verifyPayment(@RequestBody CheckoutRequest cr) throws RazorpayException {
        CheckoutResponse cres = new CheckoutResponse();
        RazorpayClient razorpay = new RazorpayClient("rzp_test_SMuMe11eNRBTkt", "ldbW0oVHlGWZ3eEXiX5xhd5J");

        String secret = "ldbW0oVHlGWZ3eEXiX5xhd5J";
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", cr.getRazorpay_order_id());
            options.put("razorpay_payment_id", cr.getRazorpay_payment_id());
            options.put("razorpay_signature", cr.getRazorpay_signature());

            boolean isSame = Utils.verifyPaymentSignature(options, secret);
            if (isSame) {
                //Payment SuccessPage
                cres.setMessage("Payment Success");
                cres.setStatus(true);
                return Response.ok("Payment Successful").build();
            } else
                //Payment FailedPage
                cres.setMessage("Payment Failed");
            cres.setStatus(false);
            return Response.ok("Payment Failed").build();
        } catch (RazorpayException re) {
            //exception page
            return Response.ok(new SignatureException("Invalid Signature")).build();
        }
    }

}

