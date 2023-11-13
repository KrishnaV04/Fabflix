Demo Video Link: [https://drive.google.com/file/d/15Bmy4TqWi5CSxmd84By22arNbfs09wUE/view?usp=sharing](https://drive.google.com/file/d/15Bmy4TqWi5CSxmd84By22arNbfs09wUE/view?usp=sharing)
<br />
Contributions:
- Raymond: Recaptcha, HTTPS, Queries, XML Parsing
- Radhakrishna: Queries, Encrypted Passwords, Dashboard

- Substring Matching: '%AN%': All strings that contain the pattern 'AN' anywhere. E.g. 'LOS ANGELES' and 'SAN FRANCISCO'.

List filenames with Prepared Statements:
- addMovie, addStas, EmployeeLoginServlet, LoginServlet, MovieListGenreServlet, MovieListTitleServlet, MovieSearchServlet, PlaceOrderServlet, SAXParserActors, SAXParserCasts, SAXParserMovies, SingleMovieServlet, SingleStarServlet

Two parsing time optimization strategies compared with the naive approach:
- Batch execution, instead of inserting one-by-one
- creating own ID, instead of MySQL autogenereating it's own
- Overall time went from ~20+ mins to a few mins at most

Inconsistent data reports from parsing. It also can be referred to from another separate report file generated by your code:
- Please refer to actors_inconsistencies.txt, casts_inconsistencies.txt, and movies_inconsistencies.txt
