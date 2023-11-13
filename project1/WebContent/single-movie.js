function getParameterByName(target) {
    let url = window.location.href;
    target = target.replace(/[\[\]]/g, "\\$&");

    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function handleResult(resultData) {
    let singleMovieInfoElement = jQuery("#single-movie_info");
    singleMovieInfoElement.append("<p>Movie Title: " + resultData[0]["movie_title"] + "</p>");
    console.log("handleResult: populating single-movie table from resultData");

    let movieTableBodyElement = jQuery("#single_movie_table_body");
    console.log(resultData);

    for (let i = 0; i < resultData.length; i++) {
        const movie = resultData[i];

        const row = jQuery('<tr>');
        row.append(jQuery('<td>').text(movie['movie_year']));
        row.append(jQuery('<td>').text(movie['movie_director']));

        const genres = movie['movie_genres']
        row.append(jQuery('<td>').html(genres.map(genre => '<a href="movies_list.html?browseGenre=' + genre["genre_name"] +  '&order=title&title_sort=asc&rating_sort=desc&page_results=10&page_number=0' + '">' + genre["genre_name"] + '</a>').join(', ')));

        const stars = movie['movie_stars']
        row.append(jQuery('<td>').html(stars.map(star => {
            const starName = star['star_name'];
            const starId = star['star_id'];
            return '<a href="single-star.html?id=' + starId + '">' + starName + '</a>';
        }).join(', ')));

        row.append(jQuery('<th>').text(movie['movie_rating']));

        const addToCartButton = jQuery('<button>').text('Add to Shopping Cart');
        addToCartButton.on('click', function () {
            addToShoppingCart(movie['movie_id'], movie['movie_title']);
        });
        row.append(jQuery('<td>').append(addToCartButton));

        movieTableBodyElement.append(row);
    }
}


// Get the movie ID from the URL
const movieId = getParameterByName("id");

// Make an AJAX request to fetch movie details
// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});

function checkout() {
    window.location.href = 'shopping_cart.html';
}

function addToShoppingCart(movieId, movieTitle) {
    // Send an AJAX request to add the movie to the shopping cart
    jQuery.ajax({
        url: 'api/shopping-cart',
        method: 'POST',
        contentType: 'application/json',
        dataType: 'json',
        data: JSON.stringify({ id: movieId, title: movieTitle }),
        success: function (response) {
            if (response.success) {
                alert('Movie added to shopping cart successfully!');
            } else {
                alert('Failed to add the movie to the shopping cart.');
            }
        },
        error: function () {
            alert('Error adding the movie to the shopping cart.');
        }
    });
}