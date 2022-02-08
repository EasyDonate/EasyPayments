package ru.easydonate.easypayments.easydonate4j.longpoll.data.model.object;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.data.model.PrettyPrintable;
import ru.easydonate.easypayments.exception.StructureValidationException;

import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class PurchasedProduct implements PrettyPrintable {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("commands")
    private List<String> commands;

    @SerializedName("cost")
    private double cost;

    @SerializedName("count")
    private int count;

    public void validate() throws StructureValidationException {
        if(id <= 0)
            throw new StructureValidationException(this, "'id' must be > 0, but it's %d", id);

        if(name == null || name.isEmpty())
            throw new StructureValidationException(this, "'name' is required");

        if(commands == null || commands.isEmpty())
            throw new StructureValidationException(this, "no commands present");

        if(cost < 0D)
            throw new StructureValidationException(this, "'cost' must be >= 0, but it's %s", cost);

        if(count <= 0)
            throw new StructureValidationException(this, "'count' must be > 0, but it's %s", count);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PurchasedProduct product = (PurchasedProduct) o;
        return id == product.id &&
                Double.compare(product.cost, cost) == 0 &&
                count == product.count &&
                Objects.equals(name, product.name) &&
                Objects.equals(commands, product.commands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, commands, cost, count);
    }

    @Override
    public @NotNull String toString() {
        return "PurchasedProduct{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", commands=" + commands +
                ", cost=" + cost +
                ", count=" + count +
                '}';
    }

}
