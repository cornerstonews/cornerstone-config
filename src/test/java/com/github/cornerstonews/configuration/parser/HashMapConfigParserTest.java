package com.github.cornerstonews.configuration.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.cornerstonews.configuration.ConfigException;

public class HashMapConfigParserTest {

    @Test
    public void marshalMapTest() throws ConfigException {
        Map<String, Object> employeeMap = new HashMap<>(Map.of("name", "John", "dept", "Engineering", "salary", 75000, "phone", "555-555-5555", "address",
                new HashMap<>(Map.of("street", "11 Wall Street", "city", "New York", "zipCode", "10118"))));

        Employee employee = new HashMapConfigParser<Employee>(Employee.class).build(employeeMap);
        System.out.println("Employee: " + employee);

        assertTrue(employee.getName().equals("John"));
        assertTrue(employee.getAddress().getCity().equals("New York"));
    }

    @Test
    public void createNewPojoFromExistingWithMapTest() throws ConfigException {
        Employee existingEmployee = new Employee("Bob", "Operations", 50000, "555-555-5556", new Address("11 Wall Street", "New York", "10118"));
        System.out.println("existingEmployee: " + existingEmployee);

        assertTrue(existingEmployee.getName().equals("Bob"));
        assertTrue(existingEmployee.getAddress().getCity().equals("New York"));

        Map<String, Object> newEmployeeMap = new HashMap<>(Map.of("name", "John", "dept", "Engineering", "salary", 75000, "phone", "555-555-555", "address",
                new HashMap<>(Map.of("street", "233 S Wacker Dr", "city", "Chicago", "zipCode", "60606"))));
        Employee newEmployee = new HashMapConfigParser<Employee>(Employee.class).build(newEmployeeMap, existingEmployee);
        System.out.println("newEmployee: " + newEmployee);

        assertTrue(newEmployee.getName().equals("John"));
        assertTrue(newEmployee.getAddress().getCity().equals("Chicago"));
    }


    @Test
    public void updateExistingPOJOTest() throws JsonMappingException, JsonProcessingException, ConfigException {
        Map<String, Object> newEmployeeMap = new HashMap<>(Map.of("name", "John", "dept", "Engineering", "phone", "555-555-555", "address",
                new HashMap<>(Map.of("street", "233 S Wacker Dr", "city", "Chicago", "zipCode", "60606"))));
        System.out.println("existingEmployeeMap: " + newEmployeeMap);

        Employee existingEmployee = new Employee("Bob", "Operations", 50000, null, new Address("11 Wall Street", "New York", "10118"));
        Employee newEmployee = new HashMapConfigParser<Employee>(Employee.class).merge(newEmployeeMap, existingEmployee);
        System.out.println("newEmployee: " + newEmployee);
        
        assertTrue(newEmployee.getName().equals("John"));
        assertTrue(newEmployee.getAddress().getCity().equals("Chicago"));
        assertTrue(newEmployee.getPhone().equals("555-555-555"));
        assertTrue(newEmployee.getSalary() == 50000);
    }
    
    
    @Test
    public void updateExistingMapTest() throws JsonMappingException, JsonProcessingException {
        Map<String, ?> existingEmployeeMap = new HashMap<>(Map.of("name", "Bob", "dept", "Operations", "salary", 50000, "phone", "555-555-556", "address",
                new HashMap<>(Map.of("street", "11 Wall Street", "city", "New York", "zipCode", "10118"))));
        System.out.println("existingEmployeeMap: " + existingEmployeeMap);

        String newEmployeeJson = "{ \"name\": \"John\", \"salary\": 75000, \"address\": { \"street\": \"233 S Wacker Dr\", \"city\": \"Chicago\" } }";
        System.out.println("newEmployeeJson: " + newEmployeeJson);

        ObjectMapper objectMapper = new ObjectMapper();
//      Map<?, ?> updatedEmployeeMap = objectMapper.updateValue(existingEmployeeMap, newEmployeeJson);
        ObjectReader objectReader = objectMapper.readerForUpdating(existingEmployeeMap);
        Map<?, ?> updatedEmployeeMap = objectReader.readValue(newEmployeeJson);
        System.out.println("newEmployee map: " + updatedEmployeeMap);

        assertTrue(updatedEmployeeMap.get("name").equals("John"));
        assertTrue(((Map<?, ?>) updatedEmployeeMap.get("address")).get("city").equals("Chicago"));
    }
    
    @Test
    public void marshalJsonToMapTest() throws JsonMappingException, JsonProcessingException {
        String employeeJson = "{ \"name\": \"John\", \"salary\": 75000, \"address\": { \"street\": \"233 S Wacker Dr\", \"city\": \"Chicago\" } }";
        System.out.println("employeeJson: " + employeeJson);
        
        ObjectMapper objectMapper = new ObjectMapper();
        Map<?, ?> employeeMap = objectMapper.readValue(employeeJson, Map.class);
        System.out.println("Employee map: " + employeeMap);
        
        assertTrue(employeeMap.get("name").equals("John"));
        assertTrue(((Map<?, ?>) employeeMap.get("address")).get("city").equals("Chicago"));
    }
}
