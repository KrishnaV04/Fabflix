jQuery(document).ready(function() {
    const urlParams = new URLSearchParams(window.location.search);
    const totalPrice = urlParams.get("totalPrice");
    console.log(totalPrice);
    jQuery("#totalPrice").text("$" + parseFloat(totalPrice).toFixed(2));
    jQuery("#paymentForm").on("submit", function(event) {
        event.preventDefault();

        const firstName = jQuery("#firstName").val();
        const lastName = jQuery("#lastName").val();
        const creditCard = jQuery("#creditCard").val();
        const expirationDate = jQuery("#expirationDate").val();

        const paymentData = {
            firstName: firstName,
            lastName: lastName,
            creditCard: creditCard,
            expirationDate: expirationDate,
            totalPrice: totalPrice,
        };

        jQuery.ajax({
            url: "api/place-order",
            method: "POST",
            data: JSON.stringify(paymentData),
            contentType: "application/json",
            success: function(response) {
                if (response.success) {
                    window.location.href = "confirmation.html";
                } else {
                    jQuery("#paymentForm").append("<p>Payment Error: " + response.message + "</p>");
                }
            },
            error: function(xhr, status, error) {
                alert("Error during payment: PLEASE RE-ENTER PAYMENT INFORMATION");
            }
        });
    });
});
