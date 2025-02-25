package xyz.tbvns.ao3m.AO3;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FandomCategoryObject {
    private String name;
    private String link;

    @Override
    public String toString() {
        return "Category{name='" + name + "', link='" + link + "'}";
    }
}