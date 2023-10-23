function searchMovies() {
    window.location.href = "search.html";
}

function browseMovies() {
    window.location.href = "browse.html";
}

jQuery("#searchButton").click(searchMovies);
jQuery("#browseButton").click(browseMovies);
