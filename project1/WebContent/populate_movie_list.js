let urlParams = new URLSearchParams(window.location.search);

let order = urlParams.get("order");
let title_sort = urlParams.get("title_sort");
let rating_sort = urlParams.get("rating_sort");
let page_results = parseInt(urlParams.get("page_results"),10);
let page_number = parseInt(urlParams.get("page_number"),10);
let url;

let howToQuery;

function populateMovieList(data) {
    let movieList = jQuery('#movie_list_body');
    movieList.empty();

    data.movies.forEach(function (movie) {
        const row = jQuery('<tr>');
        row.append(jQuery('<td>').html('<a href="single-movie.html?id=' + movie['movie_id'] + '">' + movie['movie_title'] + '</a>'));
        row.append(jQuery('<td>').text(movie['movie_year']));
        row.append(jQuery('<td>').text(movie['movie_director']));

        // Split the genres and take the first three
        const genres = movie['movie_genres'].split(',');
        row.append(jQuery('<td>').html(genres.map(genre => '<a href="movies_list.html?browseGenre=' + genre + '&order=title&title_sort=asc&rating_sort=desc&page_results=10&page_number=0' + '">' + genre + '</a>').join(', ')));

        // Split the stars and take the first three
        const stars = movie['movie_stars'].split(',');

        row.append(jQuery('<td>').html(stars.map(star => {
            const [starName, starId] = star.split(':');
            return '<a href="single-star.html?id=' + starId + '">' + starName + '</a>';
        }).join(', ')));

        row.append(jQuery('<th>').text(movie['movie_rating']));

        // Add an "Add to Shopping Cart" button with a click event handler
        const addToCartButton = jQuery('<button>').text('Add to Shopping Cart');
        addToCartButton.on('click', function () {
            addToShoppingCart(movie['movie_id'], movie['movie_title']);
        });
        // console.log("in populate_movies_list")
        // console.log(movie['movie_id']);
        row.append(jQuery('<td>').append(addToCartButton));

        movieList.append(row);
    });
}

jQuery(document).ready(function () {
    // Event listener for sorting by title
    jQuery("#title_sorting").on("click", function () {
        // Toggle sorting direction (asc or desc)

        const sortIcon = $(this);
        if (sortIcon.data("sort") === "asc") {
            sortIcon.removeClass("fa-sort-asc").addClass("fa-sort-desc");
            sortIcon.data("sort", "desc");
        } else {
            sortIcon.removeClass("fa-sort-desc").addClass("fa-sort-asc");
            sortIcon.data("sort", "asc");
        }

        title_sort = sortIcon.data("sort");
        makeAjaxCall();
    });

    // Event listener for sorting by rating
    jQuery("#rating_sorting").on("click", function () {
        // Toggle sorting direction (asc or desc)
        const sortIcon = $(this);
        if (sortIcon.data("sort") === "asc") {
            sortIcon.removeClass("fa-sort-asc").addClass("fa-sort-desc");
            sortIcon.data("sort", "desc");
        } else {
            sortIcon.removeClass("fa-sort-desc").addClass("fa-sort-asc");
            sortIcon.data("sort", "asc");
        }

        rating_sort = sortIcon.data("sort");
        makeAjaxCall();

    });

    // event listener listening to the dropdown
    jQuery("#sort-options").on("change", function () {
        const selectedOption = jQuery(this).val();

        if (selectedOption === "title-then-rating") {
            order = "title";

        } else if (selectedOption === "rating-then-title") {
            order = "rating";
        }
        makeAjaxCall();
    });

    //results per page event listener
    jQuery("#results-per-page").on("change", function () {
        page_results = jQuery(this).val();
        makeAjaxCall();
    });

    jQuery("#prev-button").on("click", function () {
        if (page_number != null && page_number !== 0) {
            page_number -= 1;
            makeAjaxCall();
        }
    });

    jQuery("#next-button").on("click", function () {
        if (page_number != null) {
            page_number += 1;
            makeAjaxCall();
        }
    });

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

function makeAjaxCall() {

    urlParams = new URLSearchParams(window.location.search);
    console.log(window.location.search);

    if (howToQuery === 'previousQuery') {
        console.log("populating with previous numbers")
        order = urlParams.get("order");
        title_sort = urlParams.get("title_sort");
        rating_sort = urlParams.get("rating_sort");
        page_results = parseInt(urlParams.get("page_results"), 10);
        page_number = parseInt(urlParams.get("page_number"), 10);
    }

    if (urlParams.get('browseTitle') !== null) {
        howToQuery = 'movieListTitle';
    } else if (urlParams.get('browseGenre') !== null) {
        howToQuery = 'movieListGenre';
    } else if (urlParams.get('title') !== null || urlParams.get('director') !== null || urlParams.get('year') !== null || urlParams.get('star') !== null){
        howToQuery = 'movieSearch';
    } else {
        howToQuery = 'previousQuery';
        url = sessionStorage.getItem('last_session');
        console.log("usingSession");
    }

    urlParams.set("order", order);
    urlParams.set("title_sort", title_sort);
    urlParams.set("rating_sort", rating_sort);
    urlParams.set("page_results", page_results);
    urlParams.set("page_number", page_number);

    if (howToQuery !== 'previousQuery') {
        url = howToQuery + '?' + urlParams;
    }
    sessionStorage.setItem('last_session', url);

    console.log(url);
    window.history.pushState(null, null, url);


    let full_text_search = urlParams.get('search_text') !== null;
    if (full_text_search) {
        jQuery.ajax({
            url: url,
            method: 'GET',
            success: function (data) {
                console.log("IN FULL TEXT SEARCH populate_movie_list.js")
                console.log(data);
                console.log("END FULL TEXT SEARCH populate_movie_list.js data")
                populateMovieList(data);
            },
            error: function () {
                console.error('Failed to retrieve movie data.');
            }
        });
    } else {
        jQuery.ajax({
            url: url,
            method: 'GET',
            success: function (data) {
                populateMovieList(data);
            },
            error: function () {
                console.error('Failed to retrieve movie data.');
            }
        });
    }
}

makeAjaxCall();