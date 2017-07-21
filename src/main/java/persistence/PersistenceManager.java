package persistence;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Allows automatize operations with DB (simple ORM)
 * Created by Juraj on 23.2.2016.
 */
public class PersistenceManager {

    private List<Class> clazzes;
    private DBHelper dbHelper;
    private HashMap<Class, Class> mapOfInterfaces = new HashMap<Class, Class>();

    public PersistenceManager(Connection con, List<Class> clazzes) {
        setMapOfInterfaces(clazzes);
        controlClasses(clazzes);
        this.clazzes = clazzes;
        dbHelper = DBHelper.getDBHelper(con, this);
    }

    private void controlClasses(List<Class> clazzes) {
        for (Class clas : clazzes) {
            try {
                clas.getDeclaredField("id");
            } catch (Exception e) {
                throw new RuntimeException("Class " + clas.getName() + " not contains field int id!\n");//+e.toString()
            }
            for (Field field : clas.getDeclaredFields()) {
                if (!field.getType().equals(String.class) &&
                        !field.getType().isPrimitive() &&
                        !clazzes.contains(field.getType()) &&
                        !clazzes.contains(getClassByInterface(field.getType()))) {
                    throw new RuntimeException("Class " + clas.getSimpleName() + " contains unsupported class " + field.getType());
                }
            }
        }
    }

    private void setMapOfInterfaces(List<Class> clazzes) {
        for (Class clas : clazzes) {
            for (Class inter : clas.getInterfaces()) {
                mapOfInterfaces.put(inter, clas);
            }
        }
    }

    public void initializeDatabase() {
        List<String> tablesFK = new ArrayList<String>();
        for (Class clas : clazzes) {
            dbHelper.makeTable(clas, tablesFK);
        }
        for (String query : tablesFK) {
            System.out.println(query);
            dbHelper.executeQuery(query);
        }
    }


    //load all object of type class
    public List getAll(Class type) {
        String query = "SELECT * FROM " + type.getSimpleName();
        return dbHelper.getResultsList(query, type);
    }

    //load object of type class by id
    public Object get(Class type, int id) {
        String query = "SELECT * FROM " + type.getSimpleName() +
                " WHERE id=" + id + " ";
        return dbHelper.getResultsList(query, type).get(0);
    }


    //load object of type class by field with value
    public List getBy(Class type, String field, Object value) {
        String val;
        if (value instanceof Integer || value instanceof Float || value instanceof Short) {
            val = value.toString();
        } else {
            val = "'" + value.toString() + "'";
        }
        String query = "SELECT * FROM " + type.getSimpleName() +
                " WHERE " +
                field + "=" + val;
        return dbHelper.getResultsList(query, type);
    }

    //Save current version of object.
    public void save(Object obj) {
        if (objectExist(obj)) {
            dbHelper.updateObject(obj);
        } else {
            dbHelper.insertObject(obj);
        }
    }

    boolean objectExist(Object obj) {
        Object id = getAttrOfObject(obj, "id");
        if (id == null || ((Integer) id) == 0)
            return false;
        return true;
    }

    Object getAttrOfObject(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    Class getClassByInterface(Class cInterface) {
        if (mapOfInterfaces.containsKey(cInterface))
            return mapOfInterfaces.get(cInterface);
        else return cInterface;
    }
}
