package xyz.tbvns.ao3m.AO3;

public class Category {
    private final String name;
    private final String url;

    public Category(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "Category{name='" + name + "', url='" + url + "'}";
    }
}
