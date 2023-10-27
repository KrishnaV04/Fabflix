function getQueryParameter(name) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(name);
}

function populateMovieList(data) {
    let movieList = jQuery('#movie_list_body');

    data.movies.forEach(function(movie) {
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
            console.log(starId);
            return '<a href="single-star.html?id=' + starId + '">' + starName + '</a>';
        }).join(', ')));

        row.append(jQuery('<th>').text(movie['movie_rating']));
        movieList.append(row);
    });
}

const searchTitle = getQueryParameter('title');
const searchYear = getQueryParameter('year');
const searchDirector = getQueryParameter('director');
const searchStar = getQueryParameter('star');

let order = "title";
let title_sort = "asc";
let rating_sort = "desc";

url = 'movieSearch' +
    '?title=' + searchTitle +
    '&year=' + searchYear +
    '&director=' + searchDirector +
    '&star=' + searchStar +
    "&order=" + order +
    "&title_sort=" + title_sort +
    "&rating_sort=" + rating_sort;

function makeAjaxCall() {

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

jQuery(document).ready(function() {
    // Event listener for sorting by title
    jQuery("#title_sorting").on("click", function() {
        // Toggle sorting direction (asc or desc)

        const sortIcon = $(this);
        title_sort = sortIcon.data("sort");
        makeAjaxCall();
    });

    // Event listener for sorting by rating
    jQuery("#rating_sorting").on("click", function() {
        // Toggle sorting direction (asc or desc)
        const sortIcon = $(this);
        rating_sort = sortIcon.data("sort");
        makeAjaxCall();

    });

    // event listener listening to the dropdown
    jQuery("#sort-options").on("change", function() {
        const selectedOption = jQuery(this).val();

        if (selectedOption === "title-then-rating") {
            order = "title"

        } else if (selectedOption === "rating-then-title") {
            order = "rating"
        }
        makeAjaxCall();
    });

});
