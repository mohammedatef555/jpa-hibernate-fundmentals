# JPA Fundamentals - Lesson 14 - Entity lifecycle events and caching

###### following [JPA / Hibernate Fundamentals from Laur Spilca](https://www.youtube.com/playlist?list=PLEocw3gLFc8USLd90a_TicWGiMThDtpOJ "JPA / Hibernate Fundamentals Laur Spilca")


## Entity lifecycle events

##### Database Tables Creation
###### Create Product table
```sql
CREATE TABLE `jpa`.`product` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  `date_created` TIMESTAMP NULL DEFAULT NULL,
  `last_modified` TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (`id`));
```

##### Entity Creation
###### Create MappedSuperclass
```java
@MappedSuperclass
public class GeneralEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(name = "date_created")
    protected LocalDateTime dateCreated;
    @Column(name = "last_modified")
    protected LocalDateTime lastModified;
    // getters and setters
}
```
###### Create Product Entity
```java
@Entity
public class Product extends GeneralEntity {
    private String name;

    // getters and setters and toString()

}
```

We have 7 lifecycle annotations:
- LOAD		— `@PostLoad()`
- UPDATE	— `@PreUpdate()` `@PostUpdate()`
- REMOVE	— `@PreRemove()` `@PostRemove()`
- PERSIST	— `@PrePersist()` `@PostPersist()`

These lifecycle events, operate in the JPA context, and its result will reflect in the DB at the end.

In the @Entity class, Just create a method annotate is with one of these lifecycle events and the JPA implementation will call it at the specific event.

see the following:
```java
@MappedSuperclass
public class GeneralEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(name = "date_created")
    protected LocalDateTime dateCreated;
    @Column(name = "last_modified")
    protected LocalDateTime lastModified;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    @PrePersist
    public void prePersist() {
        this.dateCreated = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.lastModified = LocalDateTime.now();
    }
}

```

```java
@Entity
public class Product extends GeneralEntity {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /*
    LOAD -- @PostLoad
    UPDATE -- @PreUpdate @PostUpdate
    REMOVE -- @PreRemove @PostRemove
    PERSIST -- @PrePersist @PostPersist
     */


    @PostLoad
    public void postLoad() {
        System.out.println("Entity " + this + " was loaded!");
    }

    @PreRemove
    public void preRemove() {
        System.out.println("Entity " + this + " will be removed.");
    }

    @PostRemove
    public void postRemove() {
        System.out.println("Entity " + this + " was removed.");
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", dateCreated=" + dateCreated +
                ", lastModified=" + lastModified +
                '}';
    }
}

```
The JPA implementation will automatically handle these lifecycle methods. 

### Persist and Results
##### Example1
###### Persist
```java
// Example1.java
public class Example1 {

    public static void main(String[] args) {
        var emf = Persistence.createEntityManagerFactory("my-persistence-unit");

        var em = emf.createEntityManager();

        em.getTransaction().begin();

        Product p = new Product();
        p.setName("Product 1");
        em.persist(p);

        em.getTransaction().commit();
        em.close();
    }
}
```
as you can see we didn't set the `dateCreated` nor the `lastModified`
but we used `@PrePersist` in `GeneralEntity class` to set them

###### Hibernate SQL Log

```
Hibernate: insert into Product (date_created, last_modified, name) values (?, ?, ?)
```
###### Querying the database to see the results
```sql
mysql> select * from product;
+----+-----------+---------------------+---------------------+
| id | name      | date_created        | last_modified       |
+----+-----------+---------------------+---------------------+
|  1 | Product 1 | 2022-11-12 15:29:33 | 2022-11-12 15:29:33 |
+----+-----------+---------------------+---------------------+
1 row in set (0.02 sec)
```

##### Example2
###### Persist
```java
// Example2.java
public class Example2 {

    public static void main(String[] args) {
        var emf = Persistence.createEntityManagerFactory("my-persistence-unit");

        var em = emf.createEntityManager();

        em.getTransaction().begin();

        Product p = em.find(Product.class, 1);
        p.setName("Product 2");

        em.getTransaction().commit(); // UPDATE
        em.close();
    }
}
```

###### Hibernate SQL Log

```
Hibernate: select product0_.id as id1_0_0_, product0_.date_created as date_cre2_0_0_, product0_.last_modified as last_mod3_0_0_, product0_.name as name4_0_0_ from Product product0_ where product0_.id=?
Entity Product{name='Product 1', id=1, dateCreated=2022-11-12T15:29:33, lastModified=2022-11-12T15:29:33} was loaded!
Hibernate: update Product set date_created=?, last_modified=?, name=? where id=?
```
###### Querying the database to see the results
```sql
mysql> select * from product;
+----+-----------+---------------------+---------------------+
| id | name      | date_created        | last_modified       |
+----+-----------+---------------------+---------------------+
|  1 | Product 1 | 2022-11-12 15:29:33 | 2022-11-12 15:29:33 |
+----+-----------+---------------------+---------------------+
1 row in set (0.02 sec)

mysql> select * from product;
+----+-----------+---------------------+---------------------+
| id | name      | date_created        | last_modified       |
+----+-----------+---------------------+---------------------+
|  1 | Product 2 | 2022-11-12 15:29:33 | 2022-11-12 15:51:06 |
+----+-----------+---------------------+---------------------+
1 row in set (0.00 sec)
```

notice the difference between the first result and the second one, and how the lifecycle events changed the `last_modified`

##### Example3
###### Persist
```java
// Example3.java
public class Example3 {

    public static void main(String[] args) {
        var emf = Persistence.createEntityManagerFactory("my-persistence-unit");

        var em = emf.createEntityManager();

        em.getTransaction().begin();

        Product p = em.find(Product.class, 1); // post load
        em.remove(p); // pre remove and post remove

        em.getTransaction().commit(); // DELETE
        em.close();
    }
}
```

###### Hibernate SQL Log

```
Hibernate: select product0_.id as id1_0_0_, product0_.date_created as date_cre2_0_0_, product0_.last_modified as last_mod3_0_0_, product0_.name as name4_0_0_ from Product product0_ where product0_.id=?
Entity Product{name='Product 2', id=1, dateCreated=2022-11-12T15:29:33, lastModified=2022-11-12T15:51:06} was loaded!
Entity Product{name='Product 2', id=1, dateCreated=2022-11-12T15:29:33, lastModified=2022-11-12T15:51:06} will be removed.
Hibernate: delete from Product where id=?
Entity Product{name='Product 2', id=1, dateCreated=2022-11-12T15:29:33, lastModified=2022-11-12T15:51:06} was removed.```
###### Querying the database to see the results
```
###### Querying the database to see the results
```sql
mysql> select * from product;
Empty set (0.00 sec)
```



##  Caching

We will discuss Caching (level 2 caching ) we consider the context as Level 1 Caching

Some JPA implementations offers caching out of the box caching to use   , but in hibernate you need to import some dependencies

### pom.xml
along with `hibernate core` and `the DB driver`, we need to add the `hibernate-ehcache`
```xml
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-ehcache</artifactId>
    <version>5.4.6.Final</version>
</dependency>
```

### persistence.xml
and we also need to Add the cache manager to `persistence.xml` 
```xml
<persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="2.2"
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd">
    <!-- Define persistence unit -->
    <persistence-unit name="my-persistence-unit">
        <description>JpaForBeginners</description>
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
        <exclude-unlisted-classes>false</exclude-unlisted-classes>
        <shared-cache-mode>ENABLE_SELECTIVE</shared-cache-mode>

        <properties>
            <!-- database connection -->
            <property name="javax.persistence.jdbc.driver" value="com.mysql.jdbc.Driver" />
            <property name="javax.persistence.jdbc.url" value="jdbc:mysql://localhost/jpa" />
            <property name="javax.persistence.jdbc.user" value="root" />
            <property name="javax.persistence.jdbc.password" value="" />
            <property name = "hibernate.show_sql" value = "true" />
            <property name="hibernate.cache.use_second_level_cache" value="true"/>
            <property name="hibernate.cache.region.factory_class"
                      value="org.hibernate.cache.ehcache.EhCacheRegionFactory"/>
        </properties>
    </persistence-unit>

</persistence>
```

### Configuring the caching
in persistence.xml we can use:
```xml
<shared-cache-mode>Enum SharedCacheMode </shared-cache-mode>
```

now the Enum SharedCacheMode can have different values that will affect what entities to be cached or not

    - ALL: All entities and entity-related state and data are cached.
    - DISABLE_SELECTIVE : Caching is enabled for all entities except those for which Cacheable(false) is specified.
    - ENABLE_SELECTIVE : Caching is enabled for all entities for Cacheable(true) is specified.
    - NONE : Caching is disabled for the persistence unit.
    - UNSPECIFIED : Caching behavior is undefined: provider-specific defaults may apply.


`ENABLE_SELECTIVE` used more than `DISABLE_SELECTIVE`

and as we said, if we used `ENABLE_SELECTIVE`, we need to add `@Cacheable(true)` for the entity to be cached:

```java

@Entity
@Cacheable(value = true) // or just @Cacheable
public class Product extends GeneralEntity {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /*
    LOAD -- @PostLoad
    UPDATE -- @PreUpdate @PostUpdate
    REMOVE -- @PreRemove @PostRemove
    PERSIST -- @PrePersist @PostPersist
     */

    @PostLoad
    public void postLoad() {
        System.out.println("Entity " + this + " was loaded!");
    }

    @PreRemove
    public void preRemove() {
        System.out.println("Entity " + this + " will be removed.");
    }

    @PostRemove
    public void postRemove() {
        System.out.println("Entity " + this + " was removed.");
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", dateCreated=" + dateCreated +
                ", lastModified=" + lastModified +
                '}';
    }
}

```

```java
public class Example4 {

    public static void main(String[] args) {
        var emf = Persistence.createEntityManagerFactory("my-persistence-unit");

        var em = emf.createEntityManager();

        em.getTransaction().begin();

        Product p = em.find(Product.class, 2); // goes to L2 cache

        // CACHE
        var cache = emf.getCache();
        System.out.println(cache.contains(Product.class, 2)); // true

        em.getTransaction().commit();
        em.close();
    }
}
```

```
Hibernate: select product0_.id as id1_0_0_, product0_.date_created as date_cre2_0_0_, product0_.last_modified as last_mod3_0_0_, product0_.name as name4_0_0_ from Product product0_ where product0_.id=?
Entity Product{name='Product 1', id=2, dateCreated=2022-11-12T16:10:06, lastModified=2022-11-12T16:10:06} was loaded!
true
```

