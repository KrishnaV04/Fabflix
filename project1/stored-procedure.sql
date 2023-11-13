USE moviedb;

-- Procedure for Adding Stars
-- CALL AddStar('test 1', NULL); -- tried this case and it works

DELIMITER //
DROP PROCEDURE IF EXISTS AddStar //
CREATE PROCEDURE AddStar(IN p_starName VARCHAR(100), IN p_birthYear INT)
BEGIN
    DECLARE newId VARCHAR(10);

    SELECT CONCAT('nm',CAST(SUBSTRING(MAX(stars.id), 3) AS UNSIGNED) + 1) INTO newId FROM stars;

    IF p_birthYear IS NOT NULL THEN
        INSERT INTO stars (id, name, birthYear) VALUES (newId, p_starName, p_birthYear);
    ELSE
        INSERT INTO stars (id, name) VALUES (newId, p_starName);
    END IF;

END //
DELIMITER ;



-- Procedure for Adding Movies
--  CALL AddMovie('testtitle',2000,'testdirector','test 1', 'testgenre'); -- tried this case and works

DELIMITER //
DROP PROCEDURE IF EXISTS AddMovie//
CREATE PROCEDURE AddMovie(IN p_title VARCHAR(100), IN p_year INT, IN p_director VARCHAR(100), IN p_star VARCHAR(100), IN p_genre VARCHAR(32))
BEGIN

    DECLARE p_movieId VARCHAR(10);
    DECLARE countStars INT;
    DECLARE countGenre INT;
    DECLARE p_genreId INT;
    DECLARE p_starId VARCHAR(10);

    SELECT CONCAT('tt',CAST(SUBSTRING(MAX(movies.id), 3) AS UNSIGNED) + 1) INTO p_movieId FROM movies;
    INSERT INTO movies (id, title, year, director) VALUES (p_movieId, p_title, p_year, p_director);

    SELECT COUNT(*) INTO countStars FROM stars WHERE name = p_star;
    IF countStars = 0 THEN
        CALL AddStar(p_star, NULL);
    END IF;

    SELECT COUNT(*) INTO countGenre FROM genres WHERE name = p_genre;
    IF countGenre = 0 THEN
        INSERT INTO genres (name) VALUES (p_genre);
    END IF;

    SELECT id INTO p_genreId FROM genres WHERE name=p_genre LIMIT 1;
    SELECT id INTO p_starId FROM stars WHERE name=p_star LIMIT 1;

    INSERT INTO genres_in_movies(genreId, movieId) VALUES (p_genreId, p_movieId);
    INSERT INTO stars_in_movies(starId, movieId) VALUES (p_starId, p_movieId);

END //
DELIMITER ;
