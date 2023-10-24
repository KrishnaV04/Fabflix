function getQueryParameter(name) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(name);
}

const searchTitle = getQueryParameter('title');
const searchYear = getQueryParameter('year');
const searchDirector = getQueryParameter('director');
const searchStar = getQueryParameter('star');

const url = 'movieSearch' +
    '?title=' + searchTitle +
    '&year=' + searchYear +
    '&director=' + searchDirector +
    '&star=' + searchStar;

jQuery.ajax({
    url: url,
    method: 'GET',
    success: function(data) {
        const ul = jQuery('#movie-list');

        // Clear the previous content
        ul.empty();

        jQuery.each(data.movies, function(index, movie) {
            let liHTML = "";
            liHTML += "<li>"
            liHTML += movie
            liHTML += "</li>"
            ul.append(liHTML);
        });
    },
    error: function() {
        console.error('Failed to retrieve movie data.');
    }
});

