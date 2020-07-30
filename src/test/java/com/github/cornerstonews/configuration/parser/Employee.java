package com.github.cornerstonews.configuration.parser;

public class Employee {
    private String name;
    private String dept;
    private int salary;
    private String phone;
    private Address address;

    public Employee() {
    }

    public Employee(String name, String dept, int salary, String phone, Address address) {
        this.name = name;
        this.dept = dept;
        this.salary = salary;
        this.phone = phone;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "{ \"name\": \"" + name + "\", \"dept\": \"" + dept + "\", \"salary\": " + salary + ", \"phone\": \"" + phone + "\", \"address\": " + address + " }";
    }
}