package persistence;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Juraj on 14.3.2016.
 */
class DBHelper {
    private Connection connection;
    private PersistenceManager pm;
    private static DBHelper helper;
    private Set<String> tableNames;

    private DBHelper(Connection connection, PersistenceManager pm) {
        this.connection = connection;
        this.pm = pm;
        setListOfTables();
    }

    public static DBHelper getDBHelper(Connection connection, PersistenceManager pm) {
        if (helper == null)
            helper = new DBHelper(connection, pm);
        return helper;
    }

    public void makeTable(Class clas, List<String> listOfTablesWithFK) {
        List<String> tablesFK = new ArrayList<String>();
        String query;
        if (tableNames.contains(clas.getSimpleName().toLowerCase())) {
            System.out.println("Table " + clas.getSimpleName() + " almost exists.");
            return;
        } else {
            query = "Create table " + clas.getSimpleName() + " ( ";
            if (!tableNames.contains(clas.getSimpleName().toLowerCase() + "_seq")) {
                executeQuery("Create SEQUENCE " + clas.getSimpleName() + "_seq START 1");
                tableNames.add(clas.getSimpleName().toLowerCase() + "_seq");
            }
        }

        boolean isFirstField = true;
        for (Field field : clas.getDeclaredFields()) {
            if (isFirstField) {
                isFirstField = false;
            } else {
                query = query + ", ";
            }
            if (field.getType().isPrimitive()) {
                if (field.getName().equals("id")) {
                    query = query + " id integer PRIMARY KEY DEFAULT nextval('" + clas.getSimpleName() + "_seq')";
                } else {
                    query = query + field.getName() + " " + getSqlType(field.getType());
                }
            } else if (field.getType().equals(String.class)) {
                query = query + field.getName() + " varchar";
            } else if (field.getType().isInterface()) {
                String classNameByInterface = pm.getClassByInterface(field.getType()).getSimpleName();
                tablesFK.add("ALTER TABLE " + clas.getSimpleName() + " ADD FOREIGN KEY (" +
                        field.getName() + //classNameByInterface +
                        "_id) REFERENCES " + classNameByInterface + " (id)");
                query = query + field.getName() + "_id integer";
            } else {
                tablesFK.add("ALTER TABLE " + clas.getSimpleName() + " ADD FOREIGN KEY (" +
                        field.getName() + //field.getType().getSimpleName() +
                        "_id) REFERENCES " + field.getType().getSimpleName() + " (id)");
                query = query + field.getName() + "_id integer";
            }
        }
        query = query + " );";
        listOfTablesWithFK.addAll(tablesFK);
        System.out.println(query);
        executeQuery(query);
        tableNames.add(clas.getSimpleName().toLowerCase());
    }

    private String getSqlType(Type type) {
        if (type.equals(Integer.TYPE))
            return "integer";
        if (type.equals(Boolean.TYPE))
            return "boolean";
        if (type.equals(Long.TYPE))
            return "long";
        if (type.equals(Short.TYPE))
            return "short";
        if (type.equals(Float.TYPE))
            return "float";
        return null;
    }

    private void setListOfTables() {
        tableNames = new HashSet<String>();
        DatabaseMetaData md = null;
        try {
            md = connection.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            while (rs.next()) {
                String table = rs.getString(3);
                tableNames.add(table);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List getResultsList(String query, Class type) {
        List list = new ArrayList();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                list.add(setAttributesOfObject(rs, type));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Object setAttributesOfObject(ResultSet rs, Class clas) {
        Object obj = null;
        try {
            obj = clas.newInstance();
//            if (rs.next()) {
            for (Field field : obj.getClass().getDeclaredFields()) {
                Class type = field.getType();
                field.setAccessible(true);
                if (type.equals(String.class)) {
                    field.set(obj, rs.getString(field.getName()));
                } else if (type.equals(Integer.TYPE)) {
                    field.set(obj, rs.getInt(field.getName()));
                } else if (type.equals(Boolean.TYPE)) {
                    field.set(obj, rs.getBoolean(field.getName()));
                } else if (type.equals(Long.TYPE)) {
                    field.set(obj, rs.getLong(field.getName()));
                } else if (type.equals(Short.TYPE)) {
                    field.set(obj, rs.getShort(field.getName()));
                } else if (type.equals(Float.TYPE)) {
                    field.set(obj, rs.getFloat(field.getName()));
                } else if (type.isInterface()) {
                    Integer idField = rs.getInt(field.getName() + "_id");//.getType().getSimpleName().substring(1)
                    if (idField > 0)
                        field.set(obj, ProxyHelper.createProxy(pm.getClassByInterface(field.getType()), idField, pm, field, obj));
                } else {
                    Integer idField = rs.getInt(field.getName() + "_id");//.getType().getSimpleName().substring(1)
                    if (idField > 0)
                        field.set(obj, pm.get(field.getType(), idField));
                }
            }
//            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return obj;
    }


    void executeQuery(String query) {
        System.out.println(query);
        try {
            Statement stmt = null;
            stmt = connection.createStatement();
            stmt.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void updateObject(Object obj) {
        Class clas = obj.getClass();
        String values = "";
        int id = 0;
        try {
            Field idF = clas.getDeclaredField("id");
            idF.setAccessible(true);
            id = (Integer) idF.get(obj);
            for (Field f : clas.getDeclaredFields()) {
                f.setAccessible(true);
                if (f.getType().isPrimitive() || f.getType().equals(String.class)) {
                    if (!f.getName().equals("id")) {
                        try {
                            values = values + f.getName() + "='" + f.get(obj).toString() + "', ";
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Object innerObject = f.get(obj);
                    try {
                        if (innerObject == null) {
                            values = values + pm.getClassByInterface(f.getType()).getSimpleName() + "_id=null, ";
                        } else if (!innerObject.getClass().getSimpleName().startsWith("$Proxy")) {
//                            if (innerObject.getClass().isInterface()) {
                            innerObject = pm.getClassByInterface(innerObject.getClass()).cast(innerObject);
//                            }
                            Field idOf = innerObject.getClass().getDeclaredField("id");
                            idOf.setAccessible(true);
                            pm.save(innerObject);
                            values = values + f.getName() + "_id=";//innerObject.getClass().getSimpleName()
                            int fieltId = (Integer) idOf.get(innerObject);
                            if (fieltId > 0)
                                values = values + fieltId + ", ";
                            else {
                                values = values + "null, ";
                            }
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IllegalAccessException e) {
            System.out.println("*****" + id + "--- " + clas);
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        values = values.substring(0, values.length() - 2);
        String query = "UPDATE " + clas.getSimpleName() + " SET " + values + " WHERE id=" + id;
        executeQuery(query);
    }

    int insertObject(Object obj) {
        Class clas = obj.getClass();
        String fields = "", values = "";
        for (Field f : clas.getDeclaredFields()) {
            f.setAccessible(true);
            if (f.getType().isPrimitive() || f.getType().equals(String.class)) {
                if (!f.getName().equals("id")) {
                    fields = fields + f.getName() + ", ";
                    try {
                        values = values + "'" + f.get(obj).toString() + "'" + ", ";
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    Object innerObject = f.get(obj);
                    if (innerObject != null) {
//                        if (innerObject.getClass().isInterface()) {
                        innerObject = pm.getClassByInterface(innerObject.getClass()).cast(innerObject);
//                        }
                        fields = fields + f.getName() + "_id" + ", ";//innerObject.getClass().getSimpleName()
                        if (pm.objectExist(innerObject)) {
                            updateObject(innerObject);
                            values = values + pm.getAttrOfObject(innerObject, "id") + ", ";//+ "'"
                        } else {
                            values = values + insertObject(innerObject) + ", ";
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        fields = fields.substring(0, fields.length() - 2);
        values = values.substring(0, values.length() - 2);
        String query = "INSERT into " + clas.getSimpleName() + " ( " +
                fields + " ) VALUES( " +
                values + " ) RETURNING id;";
        int objectId = -1;
        try {
            Statement stmt = null;
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next())
                objectId = rs.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (objectId != -1) {
            try {
                Field f = clas.getDeclaredField("id");
                f.setAccessible(true);
                f.set(obj, objectId);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return objectId;
    }

}
