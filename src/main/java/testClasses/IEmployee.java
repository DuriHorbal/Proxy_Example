package testClasses;

/**
 * Created by Juraj on 7.3.2016.
 */
public interface IEmployee {
    IDepartment getDepartment();

    void empAge(int empAge);

    void setDepartment(Department department);

    void empSalary(float empSalary);

}
