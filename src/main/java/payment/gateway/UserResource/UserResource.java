package payment.gateway.UserResource;


import com.fasterxml.jackson.annotation.JsonManagedReference;
//import com.razorpay.RazorpayClient;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Payload;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.annotations.Pos;
import org.jboss.resteasy.annotations.Query;
import org.json.JSONObject;
import payment.gateway.RazorPayCheckoutResponse.CheckoutResponse;
import payment.gateway.Repository.*;
import payment.gateway.UserEntity.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


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
        if (isValidCustomer(cid)) {
            Customer cust = customerRepo.findById((long) cid);
            List<Cart> custCart = cust.getCart();
            if(custCart.isEmpty()){
                return Response.ok("Your Cart is Empty").build();
            }else {
                ArrayList<Products> prodlist = new ArrayList<>();
                //which product is bought by which customer:
                for (Cart cart : custCart) {
                    if (cid == cart.getCustomer().getCid())
                        prodlist.add(cart.getProducts());
                }
               // int total = 0;
                //Find the total cost of all products added int the cart
                customerRepo.persist(cust);
                return Response.ok("Items You Bought: " + "\n" + prodlist + "\n" + "Total Cost: " + cust.getCtotal()).build();
            }
        } else {
            return Response.ok("Customer Not Found").build();
        }
    }

    //OrderCreation
    @Path("/placeorder")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response placeAnOrder(@QueryParam("cid") int cid) throws RazorpayException{
        if(isValidCustomer(cid)){
            Customer cust  = customerRepo.findById((long) cid);

            RazorpayClient razorpay = new RazorpayClient("rzp_test_SMuMe11eNRBTkt", "ldbW0oVHlGWZ3eEXiX5xhd5J");
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", cust.getCtotal()); // amount in the smallest currency unit (should be in paise)
            orderRequest.put("currency", "INR");

            Order order = razorpay.orders.create(orderRequest);
            return Response.ok((String)order.get("id")).build();
        }
        return Response.ok("Not Valid Customer").build();

    }
    //Checkout RazorPayResponse : razorpay_payment_id, razorpay_order_id, razorpay_signature and will be shown in alert

//    @POST
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//    public Response verifyPayment(@RequestBody CheckoutResponse cr) throws RazorpayException {
//        RazorpayClient razorpay = new RazorpayClient("rzp_test_SMuMe11eNRBTkt", "ldbW0oVHlGWZ3eEXiX5xhd5J");
//        //CheckoutResponse checkoutResponse = new CheckoutResponse();
//
//        String secret = "ldbW0oVHlGWZ3eEXiX5xhd5J";
//        try {
//            JSONObject options = new JSONObject();
//            options.put("razorpay_order_id", cr.getRazorpay_order_id());
//            options.put("razorpay_payment_id", cr.getRazorpay_payment_id());
//            options.put("razorpay_signature", cr.getRazorpay_signature());
//
//            boolean isSame = Utils.verifyPaymentSignature(options, secret);
//            if (isSame) {
//                //Payment SuccessPage
//                return Response.ok("Payment Successful").build();
//            } else
//                //Payment FailedPage
//                return Response.ok("Payment Failed").build();
//        } catch (RazorpayException re) {
//            //
//        }
//
//    return
//    }


}

