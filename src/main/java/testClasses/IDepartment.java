package testClasses;

/**
 * Created by Juraj on 7.3.2016.
 */
public interface IDepartment {
    IEmployee getBoss();

    String getName();

    void setName(String name);

    String getCode();

    void setCode(String code);

    void setBoss(Employee boss);
}
