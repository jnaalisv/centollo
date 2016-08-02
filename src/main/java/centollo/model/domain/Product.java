package centollo.model.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Product {

    @Id
    private long id;

    private String name;

    public String getName() {
        return name;
    }
}
