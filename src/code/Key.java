package code;

public class Key {
    private final String file;
    private final Integer chunk;

    public Key(String file, Integer chunk) {
        this.file = file;
        this.chunk = chunk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Key)) return false;
        Key key = (Key) o;
        return file.equals(key.file) && chunk == key.chunk;
    }

    @Override
    public int hashCode() {
        int result = file.hashCode();
        result = 10 * result + chunk;
        return result;
    }

    @Override
    public String toString(){
        return file + " " + chunk;
    }
}