# JPA Fundamentals - Lesson 11 - The EntityManager

###### following [JPA / Hibernate Fundamentals from Laur Spilca](https://www.youtube.com/playlist?list=PLEocw3gLFc8USLd90a_TicWGiMThDtpOJ "JPA / Hibernate Fundamentals Laur Spilca")

## The Entity Manager
You will not deal with the Entity manager and its methods, when you use frameworks like spring for example, it became too transparent, you don’t even see it anymore you use it through the means of the framework, so its used but you don’t call these methods yourself.

The Entity Manager basic operations:
  - persist()
  - flush()
  - find()
  - getReference()
  - contains()
  - detach()
  - clear()
  - remove()
  - merge()
  - refresh()

These methods work with objects that are `@Entity` only, I mean if the method expects a paramter that is an object this object in most cases is @Entity instance.
### The Entity Manager basic operations
let's create a table to work with in all our examples
```sql
CREATE TABLE `jpa`.`product` (
  `id` INT NOT NULL,
  `name` VARCHAR(45) NULL,
  PRIMARY KEY (`id`));
```

```java

@Entity
public class Product {
    @Id
    private Long id;
    
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```
#### persist()
When you persist an @Entity object, this will not trigger the INSERT, but the commit of the transaction is the one who triggered the INSERT, but this is because we didn’t set the strategy of the id as identity, if we have set it as identity strategy, the persist() might done the INSERT, this because the insert has first to be done in order for the database to generate a new value of the id
```java
public class Main {
    public static void main(String[] args) {
        // Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // Entity Manager
        EntityManager em = emf.createEntityManager();



        em.getTransaction().begin();
        Product p = new Product();
        p.setId(1L);
        p.setName("Product");

        em.persist(p);
        System.out.println(":)");

        em.getTransaction().commit();
        em.close();

    }
}
```
###### Log
```
:)
Hibernate: insert into Product (name, id) values (?, ?)
```
so here the INSERT triggered by the commit of the transaction

#### flush()
As we said the changes happens to the database by the commit of the transaction, the flush() force the JPA implementation to match  the context to the persistence level()
```java
public class Main {
    public static void main(String[] args) {
        // Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // Entity Manager
        EntityManager em = emf.createEntityManager();



        em.getTransaction().begin();
        Product p = new Product();
        p.setId(2L);
        p.setName("Product");

        em.persist(p);
        em.flush();
        System.out.println(":)");

        em.getTransaction().commit();
        em.close();

    }
}
```
###### Log
```
Hibernate: insert into Product (name, id) values (?, ?)
:)
```
See the difference between the `:)` in the `persist()` example and in the `flush()` example.

#### find()
It takes the class of the entity and the id, it puts it in the context also 

If it finds an entity with that id it will return it, if not it will not throw an error it will just return null

```java
public class Main {
    public static void main(String[] args) {
        // Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // Entity Manager
        EntityManager em = emf.createEntityManager();



        em.getTransaction().begin();

        Product p = em.find(Product.class, 1L);

        em.getTransaction().commit();
        em.close();

    }
}
```
###### Log
```
Hibernate: select product0_.id as id1_0_0_, product0_.name as name2_0_0_ from Product product0_ where product0_.id=?
```

#### getReference()
It works as find() but its kind of lazy loading, the find() will make the select query weather you used the entity or not, the getReference() will not run the select query if you don’t use it, it gives you a proxy and if this proxy isn’t used it will not trigger a select to the database.

```java
public class Main {
    public static void main(String[] args) {
        // Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // Entity Manager
        EntityManager em = emf.createEntityManager();



        em.getTransaction().begin();

        System.out.println("Start Product with id 1");
        Product p1 = em.getReference(Product.class, 1L);
        System.out.println("Finish Product with id 1");

        System.out.println("Start Product with id 2");
        Product p2 = em.getReference(Product.class, 2L);
        System.out.println(p2);
        System.out.println("Finish Product with id 2");

        em.getTransaction().commit();
        em.close();

    }
}
```
###### Log
```
Start Product with id 1
Finish Product with id 1
Start Product with id 2
Hibernate: select product0_.id as id1_0_0_, product0_.name as name2_0_0_ from Product product0_ where product0_.id=?
org.example.entities.Product@5e9f73b
Finish Product with id 2
```


#### contains()
Tells you weather  the entity is or is not in the context, so if for example you run a find() it will add the entity to the context so the contains() will return true for this entity, if for example you just created an instance of an @Entity object and did not persist if for example it means its not part of the context so the contains() will return false


```java
public class Main {
    public static void main(String[] args) {
        // Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // Entity Manager
        EntityManager em = emf.createEntityManager();



        em.getTransaction().begin();

        Product p = em.find(Product.class, 1L);
        System.out.println(em.contains(p));

        System.out.println("###################");

        Product p3 = new Product();
        p3.setId(3L);
        p3.setName("Product");
        System.out.println(em.contains(p3));

        em.getTransaction().commit();
        em.close();

    }
}
```
###### Log
```
Hibernate: select product0_.id as id1_0_0_, product0_.name as name2_0_0_ from Product product0_ where product0_.id=?
true
###################
false
```



#### remove()
Detach the instance from the context, and it also deletes it at the persistence level (the database level), so it works as detach() + DELETE

remove() only works on managed instances, so the entity must be in the context on order to use remove()

```java
public class Main {
    public static void main(String[] args) {
        // Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // Entity Manager
        EntityManager em = emf.createEntityManager();



        em.getTransaction().begin();

        Product p = em.find(Product.class, 1L);
        System.out.println(em.contains(p));

        em.remove(p);

        em.getTransaction().commit();
        em.close();

    }
}
```
###### Log
```
Hibernate: select product0_.id as id1_0_0_, product0_.name as name2_0_0_ from Product product0_ where product0_.id=?
true
Hibernate: delete from Product where id=?
```


#### merge()
As we said in order to remove an @Entity instance, it should be in the context first, one way to add an instance to the context if its in the database is by using find(), another way is to use merge(), so the merge() adds in the context the detached instance, so it works as persist also but it doesn’t trigger an insert at the end becuase it expects it to be a record on the database

```sql
mysql> select * from product;
+----+---------+
| id | name    |
+----+---------+
|  2 | Product |
+----+---------+
1 row in set (0.00 sec)
```
```java
public class Main {
    public static void main(String[] args) {
        // Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // Entity Manager
        EntityManager em = emf.createEntityManager();



        em.getTransaction().begin();

        Product p = new Product();
        p.setId(2L);
        p = em.merge(p);
        System.out.println(em.contains(p));

        em.getTransaction().commit();
        em.close();

    }
}
```
###### Log
```
Hibernate: select product0_.id as id1_0_0_, product0_.name as name2_0_0_ from Product product0_ where product0_.id=?
true
Hibernate: update Product set name=? where id=?
```


#### refresh()
If there is @Entity instance in the context, which means its managed by the EntityManager if we update the entity through set methods for example this will trigger an update when we reach the commit of the transaction, but lets say after updating through the setMethod we called refresh() before the getTransaction().commit(), it will update the instance with what we have in the DB

```java
public class Main {
    public static void main(String[] args) {
        // Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // Entity Manager
        EntityManager em = emf.createEntityManager();



        em.getTransaction().begin();

        Product p = em.find(Product.class, 2L);
        p.setName("Product"); // since this instance is on the context it will update it in the DB also
        em.refresh(p); // this will update the instance with what we have in the DB

        em.getTransaction().commit();
        em.close();

    }
}
```


#### detach()
It detaches an @Entity instance from the context.

#### clear()
It clears the context, detach all the instances from the context.



Note:if there is a relationship These method can be cascaded:
- persist()
- merge()
- remove()
- refresh()
- detach()

By default, they are not cascaded, but you can cascade it through relationships @Annotations.

**You will not deal with the Entity manager and its methods, when you use frameworks like spring for example, it became too transparent, you don’t even see it anymore you use it through the means of the framework, so its used but you don’t call these methods yourself.**


