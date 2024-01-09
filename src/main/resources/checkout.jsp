<%@ page language="java" contentType="text/html; charset=US-ASCII"
    pageEncoding="US-ASCII"%>
<html>
    <head>
        <title>Checkout Page</title>
        <meta charset="UTF-8">
        <script src="https://checkout.razorpay.com/v1/checkout.js"></script>
        <script>
            <!-- Here trigger this api -> https://localhost:8080/code-payment/src/main/java/ -> store the order_id -> trigger the checkoutPage-->
        </script>
        <script>
        function OpenCheckoutPage(){
            var options = {
                "key": "rzp_test_SMuMe11eNRBTkt",
                "amount": "900000",
                "currency": "INR",
                "name": "Acme Corp",
                "description": "Test Transaction",
                "image": "https://example.com/your_logo",
                "order_id": "order_NK4xW7wPW2dj1c", // RazorpayorderId should come
                "handler": function (response){
                    alert(response.razorpay_payment_id);
                    alert(response.razorpay_order_id);
                    alert(response.razorpay_signature)
                },
                "prefill": {
                    "name": "Rama",
                    "email": "rama.kumar@gmail.com",
                    "contact": "7017838219"
                },
                "notes": {
                    "address": "Razorpay Corporate Office"
                },
                "theme": {
                    "color": "#3399cc"
                }
            };
            var rzp1 = new Razorpay(options);
            rzp1.on('payment.failed', function (response){
                    alert(response.error.code);
                    alert(response.error.description);
                    alert(response.error.source);
                    alert(response.error.step);
                    alert(response.error.reason);
                    alert(response.error.metadata.order_id);
                    alert(response.error.metadata.payment_id);
            });
            document.getElementById('rzp-button1').onclick = function(e){
                rzp1.open();
                e.preventDefault();
            }
        }
        </script>
    </head>
    <body>
        <p>This is a Demo Checkout</p>
        <button id="rzp-button1" onclick = "createOrderID()">Pay</button>
    </body>
</html>