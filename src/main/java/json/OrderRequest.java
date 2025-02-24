package json;

import java.util.List;

public class OrderRequest {
    private List<String> ingredients;

    // Constructor
    public OrderRequest(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    // Getters and setters
    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }
}
