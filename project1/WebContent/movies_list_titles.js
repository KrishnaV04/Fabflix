const urlParamsTitle = new URLSearchParams(window.location.search);
const titleChar = urlParamsTitle.get('title'); // Change 'genre' to 'titleChar' for the parameter

function populateMovieList(data) {
    const movieList = data.movies;
    const ul = jQuery('#movie-list');

    // Populate the movie list dynamically
    jQuery.each(movieList, function(index, movie) {
        ul.append('<li>' + movie + '</li>');
    });
}

jQuery.ajax({
    url: 'movieListTitle?title=' + titleChar,
    method: 'GET',
    success: function(data) {
        console.log(data);
        populateMovieList(data);
    },
    error: function() {
        console.error('Failed to fetch movie list.');
    }
});
