jQuery('#search-button').click(function() {
    const searchTitle = jQuery('#search-title').val();
    const searchYear = jQuery('#search-year').val();
    const searchDirector = jQuery('#search-director').val();
    const searchStar = jQuery('#search-star').val();

    // Clear previous search results
    jQuery('#search-results').empty();

    jQuery.ajax({
        url: 'movieSearch',
        method: 'GET',
        data: {
            title: searchTitle,
            year: searchYear,
            director: searchDirector,
            star: searchStar
        },
        success: function(data) {
            if (data) {
                // Redirect to movies_list.html with search parameters
                window.location.href = 'movies_list.html' +
                    '?title=' + searchTitle +
                    '&year=' + searchYear +
                    '&director=' + searchDirector +
                    '&star=' + searchStar;

            } else {
                console.error('Failed to perform the search.');
            }
        },
        error: function() {
            console.error('Failed to perform the search.');
        }
    });
});