// Function to parse URL parameters
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function handleResult(resultData) {

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let singleMovieInfoElement = jQuery("#single-movie_info");

    // append two html <p> created to the h3 body, which will refresh the page
    singleMovieInfoElement.append("<p>Movie Title: " + resultData[0]["movie_title"] + "</p>");

    console.log("handleResult: populating single-movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#single_movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        //rowHTML += "<th>" + resultData[i]["movie_genres"] + "</th>";

        let genres = resultData[i]["movie_genres"];

        rowHTML += "<th>";
        for (let j = 0; j < genres.length; j++) {
            const genre = genres[j];
            console.log(genre['movie_genres']);
            rowHTML += '<a href="movies_list.html?browseGenre=' + genre['genre_name'] + '&order=title&title_sort=asc&rating_sort=desc&page_results=10&page_number=0' + '">' + genre['genre_name']  + '</a>';
            if (j < genres.length - 1) {
                rowHTML += ", ";
            }
        }
        rowHTML += "</th>";

        // Create star hyperlinks for each star
        let stars = resultData[i]["movie_stars"];
        rowHTML += "<th>";
        for (let j = 0; j < stars.length; j++) {
            const star = stars[j];
            console.log(star['star_name']);
            rowHTML += '<a href="single-star.html?id=' + star['star_id'] + '">' + star['star_name'] + '</a>';
            if (j < stars.length - 1) {
                rowHTML += ", "; // Add a comma if not the last star
            }
        }
        rowHTML += "</th>";

        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}

// Get the movie ID from the URL
const movieId = getParameterByName("id");

// Make an AJAX request to fetch movie details
// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});
