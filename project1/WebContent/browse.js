function populateGenreList(genres) {
    const genreList = jQuery('#genre-list');
    genres.forEach(function(genre) {
        const link = jQuery('<a>').attr('href', 'movies_list.html?genre=' + genre).text(genre);
        const li = jQuery('<li>').append(link);
        genreList.append(li);
    });
}

function populateTitleCharList(titleChars) {
    const titleCharList = jQuery('#title-char-list');
    titleChars.forEach(function(titleChar) {
        const link = jQuery('<a>').attr('href', 'movies_list.html?title=' + titleChar).text(titleChar);
        const li = jQuery('<li>').append(link);
        titleCharList.append(li);
    });
}

$.ajax({
    dataType: "json",
    url: 'getGenres', // Create a new servlet to handle this request
    method: 'GET',
    success: function(data) {
        populateGenreList(data);
    },
    error: function() {
        console.error('Failed to fetch genres.');
    }
});

$.ajax({
    dataType: "json",
    url: 'getTitleCharacters', // Create a new servlet to handle this request
    method: 'GET',
    success: function(data) {
        populateTitleCharList(data);
    },
    error: function() {
        console.error('Failed to fetch title characters.');
    }
});