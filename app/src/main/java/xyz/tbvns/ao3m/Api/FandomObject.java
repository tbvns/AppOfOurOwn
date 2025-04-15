package xyz.tbvns.ao3m.Api;

import lombok.Data;

@Data
public class FandomObject {
    private final String name;
    private final String url;

    public FandomObject(String name, String url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public String toString() {
        return "Category{name='" + name + "', url='" + url + "'}";
    }
}
