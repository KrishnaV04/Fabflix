public class Star {
    private String id = null;
    private String name = null;
    private int birthYear = -1;

    public boolean isValid() {
        return this.id != null && this.name != null;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setBirthYear(int year) {
        this.birthYear = year;
    }

    public int getBirthYear() {
        return this.birthYear;
    }

    @Override
    public String toString() {
        return "Actor ID: " + id  + " Name: " + name + " Birth Year: " + birthYear;
    }
}
