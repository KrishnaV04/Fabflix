function populateGenreList(genres) {
    const genreList = jQuery('#genre-list');
    genres.forEach(function(genre) {
        const link = jQuery('<a>').attr('href', 'movies_list.html?browseGenre=' + genre + '&order=title&title_sort=asc&rating_sort=desc&page_results=10&page_number=0').text(genre);
        const li = jQuery('<li>').append(link);
        genreList.append(li);
    });
}

function populateTitleCharList(titleChars) {
    const titleCharList = jQuery('#title-char-list');
    titleChars.forEach(function(titleChar) {
        const link = jQuery('<a>').attr('href', 'movies_list.html?browseTitle=' + titleChar + '&order=title&title_sort=asc&rating_sort=desc&page_results=10&page_number=0').text(titleChar);
        const li = jQuery('<li>').append(link);
        titleCharList.append(li);
    });
}

$.ajax({
    dataType: "json",
    url: 'getGenres',
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
    url: 'getTitleCharacters',
    method: 'GET',
    success: function(data) {
        populateTitleCharList(data);
    },
    error: function() {
        console.error('Failed to fetch title characters.');
    }
});