const urlParams = new URLSearchParams(window.location.search);

let order = "title";
let title_sort = "asc";
let rating_sort = "desc";
let page_results = 10;
let page_number = 0;
let url;

let howToQuery;
if (urlParams.get('browseTitle') !== null) {
    howToQuery = 'movieListTitle';
} else if (urlParams.get('browseGenre') !== null) {
    howToQuery = 'movieListGenre';
} else {
    howToQuery = 'movieSearch';
}

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
        row.append(jQuery('<td>').html(genres.map(genre => '<a href="movies_list.html?browseGenre=' + genre + '">' + genre + '</a>').join(', ')));

        // Split the stars and take the first three
        const stars = movie['movie_stars'].split(',');

        row.append(jQuery('<td>').html(stars.map(star => {
            const [starName, starId] = star.split(':');
            return '<a href="single-star.html?id=' + starId + '">' + starName + '</a>';
        }).join(', ')));

        row.append(jQuery('<th>').text(movie['movie_rating']));
        movieList.append(row);
    });
}

// TODO refactoring event handler when have time
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

function makeAjaxCall() {

    url = howToQuery + window.location.search +
        "&order=" + order +
        "&title_sort=" + title_sort +
        "&rating_sort=" + rating_sort +
        "&results_per_page=" + page_results +
        "&page_number=" + page_number;

    console.log(url)

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

makeAjaxCall();

