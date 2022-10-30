# JPA Fundamentals - Lesson 2 - @ Id and generating @ Id values
###### following [JPA / Hibernate Fundamentals from Laur Spilca](https://www.youtube.com/playlist?list=PLEocw3gLFc8USLd90a_TicWGiMThDtpOJ "JPA / Hibernate Fundamentals Laur Spilca")

## to see hibernate sql logging you can add this to persistence.xml
```xml
<property name = "hibernate.show_sql" value = "true" />
```
the persistence.xml should look like the following;
```xml
<persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="2.2"
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd">
    <!-- Define persistence unit -->
    <persistence-unit name="my-persistence-unit">
        <description>JpaForBeginners</description>
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
        <exclude-unlisted-classes>false</exclude-unlisted-classes>

        <properties>
            <!-- database connection -->
            <property name="javax.persistence.jdbc.driver" value="com.mysql.jdbc.Driver" />
            <property name="javax.persistence.jdbc.url" value="jdbc:mysql://localhost/jpa" />
            <property name="javax.persistence.jdbc.user" value="root" />
            <property name="javax.persistence.jdbc.password" value="" />
            <property name = "hibernate.show_sql" value = "true" />
        </properties>
    </persistence-unit>
</persistence>
```
this file will be in `src/main/resources/META-INF/persistence.xml` path
## Using Just @ Id
If you add just @ Id you are responsible of providing the values  for this attribute by yourself.

## Using  @ Id and @ GeneratedValue
By using @ GeneratedValue your are setting the strategy for generating the values
@ GeneratedValue strategy have 4 constants:
- AUTO: depends on your DBMS so you need to be aware of how the hibernate will generate the id for this specific DBMS, it will differ from mySQL to Oracle sql , etc..
- IDENTITY: When we specify the generation strategy as GenerationType.IDENTITY we are telling the persistence provider(hibernate) to let the database handle the auto incrementing of the id. The id will be provided by DBMS.
- table and sequence strategies, In MySQL we don’t have sequence but we can have table that will be responsible of generating the id.we should have a table and call it key_generator this table should have 2 columns
    - 1 column for the sequence name so if you have multiple entities each one will have it’s own sequence name in the key_generator table
    - The second column is for the value itself that is used


### Using GenerationType.IDENTITY

###### if you choosed the IDENTITY strategy, the DBMS will be responsible of generating the id, so in MySQL we can use Auto Increment feature
```sql
CREATE TABLE `product` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
);
```
###### Now the @Entity class will look like the following
```java
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
###### Now we can test:
```java
public class Main {
    public static void main(String[] args) {
        // Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // Entity Manager
        EntityManager em = emf.createEntityManager();

        Product product = new Product();
        product.setName("water");

        try {
            em.getTransaction().begin();
            em.persist(product);
            em.getTransaction().commit();
        } catch (Exception e) {
             em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
```
This should insert one row in the product table in the database note we did not set the id by ourselves

###### query the product table
```sql
mysql> select * from product;
+----+-------+
| id | name  |
+----+-------+
|  1 | water |
+----+-------+
1 row in set (0.00 sec)
```

as we can see the row inserted successfully and the id value was generated from MySQL we did not set the id in our code.


### Using GenerationType.TABLE or  GenerationType.SEQUENCE

###### if you choosed the TABLE or SEQUENCE strategy, this Indicates that the persistence provider must assign primary keys for the entity using an underlying database table or database sequence to ensure uniqueness.
The following sql statment create the table for the item entity itself
```sql
CREATE TABLE `Item` (
  `id` int NOT NULL,
  `name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
);
```
The following sql statment create the key generator table
```sql
CREATE TABLE `key_generator` (
  `sequence_name` varchar(100) DEFAULT NULL,
  `next_val` int DEFAULT NULL
);
```

now `sequence_name` and `next_val` are not arbitrary chosen they are specifed by JPA but we can customize it but for now let's keep it as specified in JPA specification


###### Now the @Entity class will look like the following
notice the generator here matches the table in the database
```java
@Entity
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "key_generator")
    private Integer id;
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
###### Now we can test:
```java
public class Main {
    public static void main(String[] args) {
        // Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // Entity Manager
        EntityManager em = emf.createEntityManager();

//        Product product = new Product();
//        product.setName("water");
        Item item = new Item();
        item.setName("Item #1");

        try {
            em.getTransaction().begin();
            em.persist(item);
            em.getTransaction().commit();
        } catch (Exception e) {
             em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
```
This should insert one row in the item table in the database note we did not set the id by ourselves, but it will be generated by using the key generator


###### query the item table
```sql
mysql> select * from item;
+----+---------+
| id | name    |
+----+---------+
|  1 | Item #1 |
+----+---------+
1 row in set (0.01 sec)
```
###### query the key_generator table
```sql
mysql> select * from key_generator;
+---------------+----------+
| sequence_name | next_val |
+---------------+----------+
| Item          |      100 |
+---------------+----------+
1 row in set (0.00 sec)
```
###### if we insert again and query both tables
```sql
mysql> select * from item;
+----+---------+
| id | name    |
+----+---------+
|  1 | Item #1 |
| 52 | Item #1 |
+----+---------+
2 rows in set (0.00 sec)
```
```sql
mysql> select * from key_generator;
+---------------+----------+
| sequence_name | next_val |
+---------------+----------+
| Item          |      150 |
+---------------+----------+
1 row in set (0.00 sec)
```
in the key_generator table you can have another sequence_name and next_val for another table as well.

#### Customize sequence_name and next_val when using TABLE Strategy
now let is change the key_generator table to use different column names
```sql
ALTER TABLE `jpa`.`key_generator` 
CHANGE COLUMN `sequence_name` `key_name` VARCHAR(100) NULL DEFAULT NULL ,
CHANGE COLUMN `next_val` `key_value` INT NULL DEFAULT NULL ;

```
so now this is different from specification and JPA/Hibernate should be aware of this so we can use
```java
@TableGenerator
```

so the entity class will look like this
```java
@Entity
public class Item {
    @Id
    @TableGenerator(
            name = "key_generator",
            table = "key_generator",
            pkColumnName = "key_name",
            pkColumnValue = "item_sequence",
            valueColumnName = "key_value"
    )
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "key_generator")
    private Integer id;
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
As you can see we can customize as we want

```java
public class Main {
    public static void main(String[] args) {
        // Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // Entity Manager
        EntityManager em = emf.createEntityManager();

//        Product product = new Product();
//        product.setName("water");
        Item item = new Item();
        item.setName("Item #3");

        try {
            em.getTransaction().begin();
            em.persist(item);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
```
to see the results:
```sql
mysql> select * from item;
+-----+---------+
| id  | name    |
+-----+---------+
|   1 | Item #1 |
|  52 | Item #1 |
| 102 | Item #3 |
+-----+---------+
3 rows in set (0.01 sec)
```

```sql
mysql> select * from key_generator;
+---------------+-----------+
| key_name      | key_value |
+---------------+-----------+
| Item          |       150 |
| item_sequence |       200 |
+---------------+-----------+
2 rows in set (0.00 sec)
```

I should have dropped the tables, but I just executed the main manytimes so it generates another id than the ones who already was in the tables

### Using Other Generators:

You can use other generators or creating your own generator like using UUIDGenrator from hibernate
```java
@Entity
public class MyEntity {
    @Id
    @GeneratedValue( generator="uuid" )
    @GenericGenerator(
            name="uuid",
            strategy="org.hibernate.id.UUIDGenerator",
            parameters = {
                    @Parameter(
                            name="uuid_gen_strategy_class",
                            value="org.hibernate.id.uuid.CustomVersionOneStrategy"
                    )
            }
    )
    public UUID id;
	...
}
```