jQuery(document).ready(function() {
    const urlParams = new URLSearchParams(window.location.search);
    const totalPrice = urlParams.get("totalPrice");
    jQuery("#totalPrice").text("$" + parseFloat(totalPrice).toFixed(2));

    // Add an event listener to the form submission
    jQuery("#paymentForm").on("submit", function(event) {
        event.preventDefault(); // Prevent the form from submitting

        // Gather user-entered payment information
        const firstName = jQuery("#firstName").val();
        const lastName = jQuery("#lastName").val();
        const creditCard = jQuery("#creditCard").val();
        const expirationDate = jQuery("#expirationDate").val();

        // Create a data object to send via AJAX
        const paymentData = {
            firstName: firstName,
            lastName: lastName,
            creditCard: creditCard,
            expirationDate: expirationDate,
            totalPrice: totalPrice
        };

        // Send an AJAX POST request to the PlaceOrderServlet
        jQuery.ajax({
            url: "api/place-order", // Replace with your actual endpoint
            method: "POST",
            data: JSON.stringify(paymentData),
            contentType: "application/json",
            success: function(response) {
                // Handle the response from the servlet
                if (response.success) {
                    // Payment was successful, display a success message
                    jQuery("#paymentForm").html("<p>Payment Successful. Thank you!</p>");
                } else {
                    // Payment failed, display an error message
                    jQuery("#paymentForm").append("<p>Payment Error: " + response.message + "</p>");
                }
            },
            error: function(xhr, status, error) {
                console.error("Error during payment: " + error);
            }
        });
    });
});
