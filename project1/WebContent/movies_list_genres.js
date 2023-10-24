const urlParams = new URLSearchParams(window.location.search);
const genre = urlParams.get('browseGenre');

function populateMovieList(data) {
    const movieList = data.movies;
    const ul = jQuery('#movie-list');

    ul.empty()

    jQuery.each(movieList, function(index, movie) {
        ul.append('<li>' + movie + '</li>');
    });
}
if (genre != null) {
    jQuery.ajax({
        url: 'movieListGenre?browseGenres=' + genre,
        method: 'GET',
        success: function(data) {
            console.log(data)
            populateMovieList(data);
        },
        error: function() {
            console.error('Failed to fetch movie list.');
        }
    });
}
