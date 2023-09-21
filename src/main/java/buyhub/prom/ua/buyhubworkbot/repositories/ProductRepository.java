package buyhub.prom.ua.buyhubworkbot.repositories;


import buyhub.prom.ua.buyhubworkbot.models.Product;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProductRepository extends CrudRepository<Product, Long> {
    Optional<Product> findByName(String name);
}
