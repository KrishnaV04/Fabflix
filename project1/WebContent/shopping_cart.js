let shoppingCart = [];

function calculateTotalPrice() {
    let totalPrice = 0;
    shoppingCart.forEach(item => {
        totalPrice += item.quantity * item.price;
    });
    return totalPrice;
}

function addItemToCart(movie) {
    const existingItem = shoppingCart.find(item => item.id === movie.id);

    if (existingItem) {
        existingItem.quantity++;
    } else {
        movie.quantity = 1;
        shoppingCart.push(movie);
    }

    updateCartDisplay();
}

function removeItemFromCart(movieId) {
    shoppingCart = shoppingCart.filter(item => item.id !== movieId);
    updateCartDisplay();
}

function updateItemQuantity(movieId, quantity) {
    const item = shoppingCart.find(item => item.id === movieId);

    if (item) {
        item.quantity = quantity;
        if (item.quantity <= 0) {
            removeItemFromCart(movieId);
        }
        updateCartDisplay();
    }
}

function updateCartDisplay() {
    const cartTable = document.getElementById("cartTable");
    const totalPriceSpan = document.getElementById("totalPrice");

    cartTable.innerHTML = "";

    let totalPrice = 0;

    shoppingCart.forEach(item => {
        const row = document.createElement("tr");

        const titleCell = document.createElement("td");
        titleCell.textContent = item.title;

        const quantityCell = document.createElement("td");
        quantityCell.innerHTML = `
            <button onclick="updateItemQuantity(${item.id}, ${item.quantity - 1})">-</button>
            <span>${item.quantity}</span>
            <button onclick="updateItemQuantity(${item.id}, ${item.quantity + 1})">+</button>
        `;

        const priceCell = document.createElement("td");
        priceCell.textContent = `$${item.price.toFixed(2)}`;

        const totalCell = document.createElement("td");
        totalCell.textContent = `$${(item.quantity * item.price).toFixed(2)}`;

        const actionCell = document.createElement("td");
        actionCell.innerHTML = `<button onclick="removeItemFromCart(${item.id})">Delete</button>`;

        row.appendChild(titleCell);
        row.appendChild(quantityCell);
        row.appendChild(priceCell);
        row.appendChild(totalCell);
        row.appendChild(actionCell);

        cartTable.appendChild(row);

        totalPrice += item.quantity * item.price;
    });

    totalPriceSpan.textContent = `$${totalPrice.toFixed(2)}`;
}

function proceedToPayment() {
    if (shoppingCart.length > 0) {
        const totalPrice = calculateTotalPrice();
        window.location.href = `payment.html?totalPrice=${totalPrice}`;
    }
}

// Disable the "Proceed to Payment" button initially
document.getElementById("proceedToPayment").disabled = true;

// Attach a click event handler to the button
document.getElementById("proceedToPayment").addEventListener("click", proceedToPayment);

function fetchMovieData() {
    jQuery.ajax({
        url: "api/shopping-cart",
        method: "GET",
        dataType: "json",
        success: function(data) {
            data.forEach(cartItem => {
                addItemToCart(cartItem);
            });

            // Enable the button if the cart is not empty
            if (shoppingCart.length > 0) {
                document.getElementById("proceedToPayment").disabled = false;
            }
        },
        error: function(xhr, status, error) {
            console.error("Error fetching movie data: " + error);
        }
    });
}

fetchMovieData();

