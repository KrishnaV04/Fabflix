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
    const searchText = jQuery('#full-text-search').val(); // Retrieve full-text search query

    jQuery.ajax({
        url: 'movieSearch',
        method: 'GET',
        data: {
            search_text: searchText, // Pass full-text search parameter
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
});

function checkout() {
    window.location.href = 'shopping_cart.html';
}