package persistence;

/**
 * Created by Juraj on 7.3.2016.
 */

import java.lang.reflect.*;

/**
 * This class allows Lazy loading from DB
 */
class ProxyHelper implements InvocationHandler {

    /**
     * @param clas     - class for which it is current proxy
     * @param idObject - id of specific Object from DB
     * @param pm       - persistence manager for access to DB
     * @param field    - name of field in parent Object (field is type of clas)
     * @param obj      - parent object, that contains current proxy object
     * @return Proxy object
     */
    static Object createProxy(Class clas, int idObject, PersistenceManager pm, Field field, Object obj) {
        return Proxy.newProxyInstance(clas.getClassLoader(),
                clas.getInterfaces(),
                new ProxyHelper(idObject, pm, clas, field, obj));
    }

    private int id;
    private Object target;
    private PersistenceManager pm;
    private Class objClass;
    private Field parentsField;
    private Object parent;

    /**
     * Private class constructor.
     */
    private ProxyHelper(int idObject, PersistenceManager persistenceManager, Class objClass, Field field, Object obj) {
        this.target = null;
        this.pm = persistenceManager;
        this.id = idObject;
        this.objClass = objClass;
        this.parent = obj;
        this.parentsField = field;
    }


    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        System.out.println("Invoking method: " + method.getName() + " from " + objClass.getSimpleName() + ".class");
        try {
            if (target == null) {
                target = pm.get(objClass, id);
                parentsField.setAccessible(true);
                parentsField.set(parent, target);
            }
            result = method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
        return result;
    }
}

