# JPA & Hibernate
###### following [JPA / Hibernate Fundamentals from Laur Spilca](https://www.youtube.com/playlist?list=PLEocw3gLFc8USLd90a_TicWGiMThDtpOJ "JPA / Hibernate Fundamentals Laur Spilca")

## Getting started with JPA/Hibernate
First of all you will need to add some dependicies to the pom.xml
```xml
<dependencies>
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>5.4.4.Final</version>
        </dependency>

        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.16</version>
        </dependency>

        <dependency>
            <groupId>javax.xml.bind</groupId>
            <artifactId>jaxb-api</artifactId>
            <version>2.3.1</version>
        </dependency>
        <dependency>
            <groupId>com.sun.xml.bind</groupId>
            <artifactId>jaxb-impl</artifactId>
            <version>2.3.2</version>
        </dependency>
        <dependency>
            <groupId>com.sun.xml.bind</groupId>
            <artifactId>jaxb-core</artifactId>
            <version>2.3.0.1</version>
        </dependency>
    </dependencies>
```

the most important two are :
- hibernate-core
- mysql-connector-java

##### persistenc.xml
This file should be in the following path `src/main/resources/META-INF/persistence.xml` exactly like this.

Here is the place which you can define persistence units, the persistence unit is the configuration of a connection of specific database usually we will have one persistence unit

###### To define a persistence unit:
```xml
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
        </properties>
    </persistence-unit>
```

and the whole file should look like this:
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
        </properties>
    </persistence-unit>
</persistence>
```

## Define The Entity
### Define the entity in the database
The following code will create a table in the database called product
```sql
CREATE TABLE `product` (
  `id` int NOT NULL,
  `name` varchar(45) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `exp_date` date DEFAULT NULL,
  PRIMARY KEY (`id`)
);

```

now we need to model this table as Entity class with JPA/Hibernate
### Define the entity to be known by JPA/Hibernate
```java
@Entity
public class Product {
    @Id
    private int id;
    private String name;
    private double price;
    @Column(name = "exp_date")
    private LocalDate expDate;
}
```
as you can see the entity class must use `@Entity` annotation, The JPA specification requires the `@Entity` annotation. It identifies a class as an entity class.

here the entity class name matches the table name in the database but incase they don't match you can use the `@Table` annotation like the following
```java
@Table(name = "<the table name in the database>")
```

as `@Table` there is also `@Column` which can be used if the column name in the database table doesn't match with the  `@Entity` class attribuite so you can use the `@Column` to solve this problem:
```java
@Column(name = "exp_date")
private LocalDate expDate;
```

The `@Entity` class should look like this now:
```java
@Entity
public class Product {
    @Id
    private int id;
    private String name;
    private double price;
    @Column(name = "exp_date")
    private LocalDate expDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDate getExpDate() {
        return expDate;
    }

    public void setExpDate(LocalDate expDate) {
        this.expDate = expDate;
    }
}

```

## How to actually use JPA/Hibernate
inside the main class we can use JPA/Hibernate to insert one product in the database table

now we need to get an `EntityManager` which we can get from `EntityManagerFactory` which use `persistenceUnitName` which we defined in `persistence.xml` in `persistence-unit`:
```java
// Entity Manager Factory
EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
// Entity Manager
EntityManager em = emf.createEntityManager();
```

now we have an entity manager which has a context that can manage entites

you can create the entity as any POJO and fill the data through it's setters:
```java
Product product = new Product();
product.setId(1);
product.setName("water");
product.setPrice(1.0);
product.setExpDate(LocalDate.now());
```

now we need to insert it to the database table
note that any operation that represents a change should be inside a transaction:
```java
try {
	em.getTransaction().begin();
    em.persist(product);
    em.getTransaction().commit();
} catch (Exception e) {
	em.getTransaction().rollback();
} finally {
	em.close();
}
```

As you can see, we wrapped it inside `try catch finally` block, you can write it directly without it, but it should be written inside `try catch finally` block.

`Note!`: when we used `em.persist(product)` this did not persist the entity to the table this persist it to the context which `EntityManager` manages, the real database insertion happens here `em.getTransaction().commit()`, if any thing goes wrong we can rollback as you see in the catch block `em.getTransaction().rollback()`, and finally we close the  `EntityManager` using `em.close()`

now we can see the entity inserted correctly in the database:
```sql
SELECT * FROM jpa.product;
```
```sql
+----+-------+-------+------------+
| id | name  | price | exp_date   |
+----+-------+-------+------------+
|  1 | water |     1 | 2022-10-29 |
+----+-------+-------+------------+
1 row in set (0.01 sec)
```