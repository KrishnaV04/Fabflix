const urlParamsTitle = new URLSearchParams(window.location.search);
const titleChar = urlParamsTitle.get('browseTitle');

function populateMovieList(data) {
    const movieList = data.movies;
    const ul = jQuery('#movie-list');

    ul.empty()

    // Populate the movie list dynamically
    jQuery.each(movieList, function(index, movie) {
        ul.append('<li>' + movie + '</li>');
    });
}

if (titleChar !== null) {
    jQuery.ajax({
        url: 'movieListTitle?browseTitle=' + titleChar,
        method: 'GET',
        success: function(data) {
            populateMovieList(data);
        },
        error: function() {
            console.error('Failed to fetch movie list.');
        }
    });
}
