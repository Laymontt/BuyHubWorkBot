package buyhub.prom.ua.buyhubworkbot.repositories;

import buyhub.prom.ua.buyhubworkbot.models.Employee;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EmployeeRepository extends CrudRepository<Employee, Long> {
    Optional<Employee> findByUsername(String username);

    Optional<Employee> findByName(String name);

    // TODO
//    Optional<Employee> findByChatId(String chatId);
}
