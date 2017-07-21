package testClasses;

/**
 * Created by Juraj on 23.2.2016.
 */
public class Department implements IDepartment {
    private int id;
    private String name;
    private String code;
    private IEmployee boss;

    public Department() {
    }

    public Department(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String toString() {
        return "\n****************" +
                "*Department: " + name +
                "\n*Code: " + code +
                "\n*id: " + id +
                "\n****************";
    }

    public void setBoss(Employee boss) {
        this.boss = boss;
    }

    public IEmployee getBoss() {
        return boss;
    }
}
