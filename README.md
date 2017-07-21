# Proxy Example
Simple proxy example, with simple ORM

Interesting classes are:
* persistence.**PersistenceManager** 
  * allows simple [ORM](https://en.wikipedia.org/wiki/Object-relational_mapping) 
  * using simple Proxy for [Lazy loading](https://en.wikipedia.org/wiki/Lazy_initialization) of objects
* persistence.**ProxyHelper** 
  * implements [InvocationHandler](https://docs.oracle.com/javase/7/docs/api/java/lang/reflect/InvocationHandler.html)
  
  
