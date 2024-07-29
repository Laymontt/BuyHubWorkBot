package buyhub.prom.ua.buyhubworkbot.services;

import buyhub.prom.ua.buyhubworkbot.models.Employee;
import buyhub.prom.ua.buyhubworkbot.repositories.EmployeeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    // TODO
    public String addEmployee(String text) {
        String[] textArr = text.substring(13).split(", ");
        String username = textArr[0];
        String name = textArr[1];
//        String chatId = textArr[2];
        if (employeeRepository.findByUsername(username).isPresent())
            return "Данный сотрудник уже существует";
        Employee employee = new Employee();
        employee.setName(name);
        employee.setUsername(username);
//        employee.setChatId(chatId);
        employeeRepository.save(employee);
        return employee.getName();
    }

    public String removeEmployee(String text) {
        text = text.substring(16);
        String username = text;
        if (employeeRepository.findByUsername(username).isEmpty())
            return "Данного сотрудника не существует";
        Employee employee = employeeRepository.findByUsername(username).orElseThrow();
        employeeRepository.deleteById(employee.getId());
        return employee.getName();
    }

    public String getListEmployees() {
        List<Employee> employees = (List<Employee>) employeeRepository.findAll();
        List<String> employeeNames = new ArrayList<>();
        for (Employee e : employees) {
            employeeNames.add(e.getName());
        }
        String employeeNamesString = employeeNames.toString();
        return employeeNamesString;
    }

    public Employee getEmployeeInfo(String name) {
        Employee employee = employeeRepository.findByName(name).orElseThrow();
        return employee;
    }
}
