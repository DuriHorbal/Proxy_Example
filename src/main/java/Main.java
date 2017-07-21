import persistence.PersistenceManager;
import testClasses.Department;
import testClasses.Employee;
import testClasses.IDepartment;
import testClasses.IEmployee;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Juraj on 23.2.2016.
 */
public class Main {

    public static void main(String[] args) throws URISyntaxException {
        List<Class> listOfClasses= new ArrayList<Class>();
        listOfClasses.add(Department.class);
        listOfClasses.add(Employee.class);
        Connection c= makeConnection();
        PersistenceManager pm = new PersistenceManager(c, listOfClasses);
        pm.initializeDatabase();

        //first employee
        Employee emp0 = new Employee("Adam");
        emp0.empAge(30);
        emp0.empSalary(1000);
        pm.save(emp0);

        //second employee
        Employee bosss = new Employee("Boss");
        bosss.empAge(32);
        bosss.empSalary(1200);

        //first department (with boss)
        Department d1 = new Department("DepA", "PA");
        d1.setBoss(bosss);

        //third employee (with department)
        Employee emp1 = new Employee("John");
        emp1.empAge(125);
        emp1.empSalary(385);
        emp1.setDepartment(d1);
        //store third employee with other objects (first department)
        pm.save(emp1);

        // load third employee
        Employee worker = (Employee) pm.getBy(Employee.class, "name", "John").get(0);
        // check his department
        System.out.println(worker.getDepartment().toString());
        IDepartment department = worker.getDepartment();
        // check boss of department
        System.out.println(department.getBoss().toString());
        IEmployee boss = department.getBoss();
        // change second employee
        boss.empSalary(3000);

        // save third employee
        pm.save(worker);
        System.out.println();

        // check all of employees
        for(Object o : pm.getAll(Employee.class)){
            System.out.println(o.toString());
        }
        try {
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Connection makeConnection() {
        Connection connection = null;
        try {

            connection = DriverManager.getConnection(
                    "jdbc:postgresql://127.0.0.1:5432/postgres", "postgres",
                    "0000");

        } catch (SQLException e) {

            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
        }

        if (connection != null) {
            System.out.println("You made it, take control your database now!");
        } else {
            System.out.println("Failed to make connection!");
        }
        return connection;
    }
}
