const urlParams = new URLSearchParams(window.location.search);
const genre = urlParams.get('genre');

function populateMovieList(data) {
    const movieList = data.movies;
    const ul = jQuery('#movie-list');

    // Populate the movie list dynamically
    jQuery.each(movieList, function(index, movie) {
        ul.append('<li>' + movie + '</li>');
    });
}

jQuery.ajax({
    url: 'movieListGenre?genre=' + genre,
    method: 'GET',
    success: function(data) {
        console.log(data)
        populateMovieList(data);
    },
    error: function() {
        console.error('Failed to fetch movie list.');
    }
});