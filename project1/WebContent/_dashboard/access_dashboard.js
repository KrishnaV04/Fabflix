$(document).ready(function () {
    // Add Star
    $("#add-star-button").click(function () {
        var starName = $("#star-name").val();
        var birthYear = $("#star-year").val();

        // const starData = {
        //     name: starName,
        //     year: birthYear,
        // };


        $.ajax({
            method: "POST",
            url: "api/add-star", // Replace with your server endpoint
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify({ starName: starName, birthYear: birthYear }),
            success: function (response) {
                if (response.success) {
                    $("#star_add_msg").text("Star added successfully");
                } else {
                    $("#star_add_msg").text("Star adding unsuccessful");
                }
            },
            error: function () {
                $("#star_add_msg").text("Failed to add star!");
            }
        });
    });

    // Add Movie
    $("#add-movie-button").click(function () {
        var title = $("#movie-title").val();
        var year = $("#movie-year").val();
        var director = $("#movie-director").val();
        var starName = $("#movie-star").val();
        var genre = $("#movie-genre").val();


        // const postData = {
        //     title: title,
        //     year: year,
        //     director: director,
        //     starName: starName,
        //     genre: genre,
        // };


        $.ajax({
            type: "POST",
            url: "api/add-movie", // Replace with your server endpoint
            contentType: 'application/json',
            data: JSON.stringify({
                title: title,
                year: year,
                director: director,
                starName: starName,
                genre: genre,
            }),
            contentType: "application/json",
            success: function (response) {
                if (response.success) {
                    $("#movie_add_msg").text("Movie added successfully");
                } else {
                    $("#movie_add_msg").text("Movie adding unsuccessful");
                }
            },
            error: function () {
                $("#movie_add_msg").text("Failed to add Movie!");
            }
        });

    });
});