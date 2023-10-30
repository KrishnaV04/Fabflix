const PRICE = 0.99;
let totalPrice = 0
function updateCartDisplay(cartData) {
    const cartBody = document.getElementById("cart_body");
    const totalPriceSpan = document.getElementById("totalPrice");

    cartBody.innerHTML = "";
    totalPrice = 0;

    cartData.forEach(item => {
        const row = document.createElement("tr");

        const titleCell = document.createElement("td");
        titleCell.textContent = item.title;

        const quantityCell = document.createElement("td");
        console.log("IN SHOPPING CART JS");
        console.log(item.id);
        quantityCell.innerHTML = `
            <button onclick="updateItemQuantityInCart('${item.id}', ${item.quantity - 1})">-</button>
            <span>${item.quantity}</span>
            <button onclick="updateItemQuantityInCart('${item.id}', ${item.quantity + 1})">+</button>
        `;

        const priceCell = document.createElement("td");
        priceCell.textContent = `$${PRICE.toFixed(2)}`;

        const totalCell = document.createElement("td");
        const itemTotalPrice = (item.quantity * PRICE).toFixed(2);
        totalCell.textContent = `$${itemTotalPrice}`;

        const actionCell = document.createElement("td");
        actionCell.innerHTML = `<button onclick="removeItemFromCart('${item.id}')">Delete</button>`;

        row.appendChild(titleCell);
        row.appendChild(quantityCell);
        row.appendChild(priceCell);
        row.appendChild(totalCell);
        row.appendChild(actionCell);

        cartBody.appendChild(row);

        totalPrice += parseFloat(itemTotalPrice);
    });

    if (cartData.length === 0) {
        proceedToPaymentButton.disabled = true;
    } else {
        proceedToPaymentButton.disabled = false;
    }

    totalPriceSpan.textContent = `$${totalPrice.toFixed(2)}`;
}

function proceedToPayment() {
    window.location.href = `payment.html?totalPrice=${totalPrice}`;
}

proceedToPaymentButton = document.getElementById("proceedToPayment")
proceedToPaymentButton.addEventListener("click", proceedToPayment);

function updateItemQuantityInCart(itemId, newQuantity) {
    if (newQuantity > 0) {
        // Send an AJAX request to update the quantity in the shopping cart
        jQuery.ajax({
            url: 'api/shopping-cart',
            method: 'POST',
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify({ id: itemId, quantity: newQuantity }),
            success: function (response) {
                console.log(response);
                if (response.success) {
                    updateCartDisplay(response.cart);
                } else {
                    alert('Failed to update item quantity in the shopping cart.');
                }
            },
            error: function () {
                alert('Error updating item quantity in the shopping cart.');
            }
        });
    } else {
        alert('You cannot have 0 of one item unless you want to delete it')
    }
}

function removeItemFromCart(itemId) {
    jQuery.ajax({
        url: 'api/shopping-cart',
        method: 'DELETE',
        contentType: 'application/json',
        dataType: 'json',
        data: JSON.stringify({ id: itemId }),
        success: function (response) {
            if (response.success) {
                updateCartDisplay(response.cart);
            } else {
                alert('Failed to remove the item from the shopping cart.');
            }
        },
        error: function () {
            alert('Error removing the item from the shopping cart.');
        }
    });
}

function fetchCartDataFromBackend() {
    jQuery.ajax({
        url: "api/shopping-cart",
        method: "GET",
        dataType: "json",
        success: function(data) {
            updateCartDisplay(data);
        },
        error: function(xhr, status, error) {
            console.error("Error fetching cart data: " + error);
        }
    });
}

fetchCartDataFromBackend();