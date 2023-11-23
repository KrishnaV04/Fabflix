jQuery('#search-button').click(function() {
    const searchTitle = jQuery('#search-title').val();
    const searchYear = jQuery('#search-year').val();
    const searchDirector = jQuery('#search-director').val();
    const searchStar = jQuery('#search-star').val();

    jQuery.ajax({
        url: 'movieSearch',
        method: 'GET',
        data: {
            title: searchTitle,
            year: searchYear,
            director: searchDirector,
            star: searchStar,
            order: 'title',
            title_sort: 'asc',
            rating_sort: 'desc',
            page_results: '10',
            page_number: '0'

        },
        success: function(data) {
            if (data) {
                // Redirect to movies_list.html with search parameters
                window.location.href = 'movies_list.html' +
                    '?title=' + searchTitle +
                    '&year=' + searchYear +
                    '&director=' + searchDirector +
                    '&star=' + searchStar +
                    '&order=title' +
                    '&title_sort=asc' +
                    '&rating_sort=desc' +
                    '&page_results=10' +
                    '&page_number=0';

            } else {
                console.error('Failed to perform the search.');
            }
        },
        error: function() {
            console.error('Failed to perform the search.');
        }
    });
});

jQuery('#full-text-search-button').click(function() {
    runFullTextSearch();
});

fullTextSearchInput = jQuery("#full-text-search")
fullTextSearchInput.keypress(function(event) {
    if (event.keyCode === 13) {
        event.preventDefault();
        runFullTextSearch();
    }
})

function runFullTextSearch() {
    const searchText = jQuery('#full-text-search').val(); // Retrieve full-text search query
    jQuery.ajax({
        url: 'movieSearch',
        method: 'GET',
        data: {
            search_text: searchText,
            order: 'title',
            title_sort: 'asc',
            rating_sort: 'desc',
            page_results: '10',
            page_number: '0'
        },
        success: function(data) {
            if (data) {
                window.location.href = 'movies_list.html' +
                    '?title=' + "" +
                    '&year=' + "" +
                    '&director=' + "" +
                    '&star=' + "" +
                    '&order=title' +
                    '&title_sort=asc' +
                    '&rating_sort=desc' +
                    '&page_results=10' +
                    '&page_number=0' +
                    '&search_text=' + searchText;
            } else {
                console.error('Failed to perform the search.');
            }
        },
        error: function() {
            console.error('Failed to perform the full-text search.');
        }
    });
}

jQuery("#full-text-search").autocomplete({
    serviceUrl: 'movie-autocomplete-servlet',
    paramName: 'query',
    minChars: 3,
    deferRequestBy: 300,

    onSelect: function (suggestion) {
        jQuery(this).val(suggestion.value);
        window.location.href = 'single-movie.html?id=' + suggestion.data.id;
    },

    onSearchComplete: function (query, suggestions) {
        let selectedIndex = -1;

        jQuery(document).on('keydown', '#full-text-search', function (e) {
            const suggestionList = jQuery('.autocomplete-suggestions');
            if (e.keyCode === 40) {
                // Down arrow key
                selectedIndex = (selectedIndex + 1) % suggestions.length;
            } else if (e.keyCode === 38) {
                // Up arrow key
                selectedIndex = (selectedIndex - 1 + suggestions.length) % suggestions.length;
            }

            suggestionList.find('.autocomplete-suggestion').removeClass('autocomplete-selected');
            suggestionList.find('.autocomplete-suggestion').eq(selectedIndex).addClass('autocomplete-selected');
        });
    },

    lookup: function (query, doneCallback) {
        console.log("Autocomplete search initiated");
        const cachedSuggestions = sessionStorage.getItem("auto-complete=" + query);
        if (cachedSuggestions) {
            console.log("Autocomplete search using cached results");
            const suggestionsArray = JSON.parse(cachedSuggestions);
            console.log(suggestionsArray);
            doneCallback({ suggestions: suggestionsArray });
        } else {
            console.log("Autocomplete search sending ajax request to the server");
            jQuery.ajax({
                url: 'movie-autocomplete-servlet',
                method: 'GET',
                data: { query: query },
                dataType: 'json',
                success: function (data) {
                    const transformedData = data.map(function (item) {
                        return { value: item.title, data: item };
                    });
                    console.log(transformedData);
                    sessionStorage.setItem("auto-complete=" + query, JSON.stringify(transformedData));
                    doneCallback({ suggestions: transformedData });
                },
                error: function () {
                    console.error('Failed to fetch suggestions from the server.');
                }
            });
        }
    },
});

function checkout() {
    window.location.href = 'shopping_cart.html';
}