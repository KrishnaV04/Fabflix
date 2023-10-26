// Define a function to populate the movie list based on search or browse results

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
        console.log(stars);
        row.append(jQuery('<td>').html(stars.map(star => {
            const [starName, starId] = star.split(':');
            console.log(starId);
            return '<a href="single-star.html?id=' + starId + '">' + starName + '</a>';
        }).join(', ')));

        row.append(jQuery('<th>').text(movie['movie_rating']));
        movieList.append(row);
    });
}

// AJAX request to fetch search or browse results
const urlParams = new URLSearchParams(window.location.search);
const browseTitle = urlParams.get('browseTitle');
const browseGenre = urlParams.get('browseGenre');

if (browseTitle !== null) {
    // Perform a search and retrieve the movie data
    const url = 'movieListTitle?browseTitle=' + browseTitle;
    jQuery.ajax({
        url: url,
        method: 'GET',
        success: function(data) {
            populateMovieList(data);
        },
        error: function() {
            console.error('Failed to fetch search results.');
        }
    });
} else if (browseGenre !== null) {
    // Browse by genre and retrieve the movie data
    const url = 'movieListGenre?browseGenres=' + browseGenre;
    jQuery.ajax({
        url: url,
        method: 'GET',
        success: function(data) {
            populateMovieList(data);
        },
        error: function() {
            console.error('Failed to fetch browse results.');
        }
    });
}

// Add event listeners for sorting by title and rating
// currently these listeners don't do anything TODO
// issue is if we want to make this functionality here on in backend
// backend might be better as we have large amounts of data
// jQuery(document).ready(function() {
//     // Event listener for sorting by title
//     jQuery("#title_sorting").on("click", function() {
//         // Toggle sorting direction (asc or desc)
//
//         const sortIcon = $(this);
//         if (sortIcon.data("sort") === "asc") {
//             sortIcon.removeClass("fa-sort-asc").addClass("fa-sort-desc");
//             sortIcon.data("sort", "desc");
//         } else {
//             sortIcon.removeClass("fa-sort-desc").addClass("fa-sort-asc");
//             sortIcon.data("sort", "asc");
//         }
//
//         console.log("Sorting by title event triggered");
//         console.log(sortIcon.data("sort"))
//     });
//
//
//     // Event listener for sorting by rating
//     jQuery("#rating_sorting").on("click", function() {
//         // Toggle sorting direction (asc or desc)
//         const sortIcon = $(this);
//         if (sortIcon.data("sort") === "asc") {
//             sortIcon.removeClass("fa-sort-asc").addClass("fa-sort-desc");
//             sortIcon.data("sort", "desc");
//         } else {
//             sortIcon.removeClass("fa-sort-desc").addClass("fa-sort-asc");
//             sortIcon.data("sort", "asc");
//         }
//
//         console.log("Sorting by rating event triggered");
//         console.log(sortIcon.data("sort"))
//     });
//
//     // event listener listening to the dropdown
//     jQuery("#sort-options").on("change", function() {
//         const selectedOption = jQuery(this).val();
//
//         if (selectedOption === "title-then-rating") {
//             // Sort by Title then Rating
//             columnNames = ["movie_title", "movie_rating"];
//
//         } else if (selectedOption === "rating-then-title") {
//             // Sort by Rating then Title
//             columnNames = ["movie_rating", "movie_title"];
//         }
//     });
// });