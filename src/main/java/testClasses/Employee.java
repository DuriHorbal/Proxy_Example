package testClasses;

/**
 * Created by Juraj on 23.2.2016.
 */
public class Employee implements IEmployee{

    private int id;
    private String name;
    private int age;
    private float salary;
    private IDepartment department;

    public Employee() {
    }

    // This is the constructor of the class Employee
    public Employee(String name){
        this.name = name;
    }


    // Assign the age of the Employee  to the variable age.
    public void empAge(int empAge){
        age =  empAge;
    }

    public IDepartment getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    /* Assign the salary to the variable	salary.*/
    public void empSalary(float empSalary){
        salary = empSalary;
    }

    public String toString(){
        return  "\n****************" +
                "\n*Name: "+ name +
                "\n*Age: " + age +
                "\n*Salary: " + salary +
                "\n****************";
    }
}