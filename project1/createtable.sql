DROP DATABASE IF EXISTS moviedb;
CREATE DATABASE IF NOT EXISTS moviedb;

USE moviedb;

CREATE TABLE IF NOT EXISTS employees (
    email varchar(50) primary key,
    password varchar(20) not null,
    fullname varchar(100)
);

INSERT INTO employees VALUES('classta@email.edu','classta','TA CS122B');

CREATE TABLE IF NOT EXISTS movies (
    id VARCHAR(10) PRIMARY KEY,
    title VARCHAR(100) NOT NULL DEFAULT '',
    year INTEGER NOT NULL,
    director VARCHAR(100) NOT NULL DEFAULT '',
    FULLTEXT idx (title)
);

CREATE TABLE IF NOT EXISTS stars (
    id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL DEFAULT '',
    birthYear INTEGER,
    numMovies INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS stars_in_movies (
    starId VARCHAR(10),
    movieId VARCHAR(10),
    PRIMARY KEY (starId, movieId),
    FOREIGN KEY (starId) REFERENCES stars(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE IF NOT EXISTS genres (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(32) NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS genres_in_movies (
    genreId INTEGER,
    movieId VARCHAR(10),
    PRIMARY KEY (genreId, movieId),
    FOREIGN KEY (genreId) REFERENCES genres(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE IF NOT EXISTS creditcards (
    id VARCHAR(20) PRIMARY KEY,
    firstName VARCHAR(50) NOT NULL DEFAULT '',
    lastName VARCHAR(50) NOT NULL DEFAULT '',
    expiration DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS customers (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    firstName VARCHAR(50) NOT NULL DEFAULT '',
    lastName VARCHAR(50) NOT NULL DEFAULT '',
    ccId VARCHAR(20) NOT NULL DEFAULT '',
    address VARCHAR(200) NOT NULL DEFAULT '',
    email VARCHAR(50) NOT NULL DEFAULT '',
    password VARCHAR(20) NOT NULL DEFAULT '',
    FOREIGN KEY (ccId) REFERENCES creditcards(id)
);

CREATE TABLE IF NOT EXISTS sales (
     id INTEGER PRIMARY KEY AUTO_INCREMENT,
     customerId INTEGER NOT NULL,
     movieId VARCHAR(10) NOT NULL DEFAULT '',
     saleDate DATE NOT NULL,
     FOREIGN KEY (customerId) REFERENCES customers(id),
     FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE IF NOT EXISTS ratings (
    movieId VARCHAR(10),
    rating FLOAT NOT NULL,
    numVotes INTEGER NOT NULL,
    PRIMARY KEY (movieId),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);


DELIMITER //

CREATE PROCEDURE UpdateNumMoviesCount(IN star_id VARCHAR(10))
BEGIN
    UPDATE stars
    SET numMovies = numMovies + 1
    WHERE id = star_id;
END//

DELIMITER ;
