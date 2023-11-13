package parser_files;

public class CastMember {
    private String starName = null;
    private String starId = null;
    private String fid = null;
    private String movieId = null;

    public boolean isValid() {
        return this.starName != null && !this.starName.equals("sa") && this.fid != null;
    }

    public void setStarName(String starName) {
        this.starName = starName;
    }

    public String getStarName() {
        return this.starName;
    }

    public void setStarId(String starId) {
        this.starId = starId;
    }

    public String getStarId() {
        return this.starId;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getFid() {
        return this.fid;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getMovieId() {
        return this.movieId;
    }

    @Override
    public String toString() {
        return "parser_files.Star Name: " + starName  + " parser_files.Star Id: " + starId + " FID: " + fid + " parser_files.Movie Id: " + movieId;
    }
}
