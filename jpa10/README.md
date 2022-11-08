# JPA Fundamentals - Lesson 10 - @MappedSuperclass and Inheritance strategies for entities

###### following [JPA / Hibernate Fundamentals from Laur Spilca](https://www.youtube.com/playlist?list=PLEocw3gLFc8USLd90a_TicWGiMThDtpOJ "JPA / Hibernate Fundamentals Laur Spilca")

## Inheritance between entities.
We have 3 strategies that you can specify in the super class via @Inheritance.

which are
- Single Table (default) (all attributes in one class)
- Table Per class (very rarely used)
- Joined (popular)

When you use inheritance between the entities just define the id in the superclass, you can not define it in the both entities,The id should be declared once in all the inheritance chain

### Single Table strategy:
##### Entity Creation
###### Create Animal Entity
```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // this is the default you can omit it
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    // getters and setters
}

```
###### Create Cat Entity
```java
@Entity
public class Cat extends Animal{

    private String color;

    // getters and setters
}
```

##### Database Tables Creation
How should we represent a table in the database, we will have column for each attribute in both super and subclasses with dtype column which is discriminator to know what subclass this row belongs to
And as the name, we will have one table whenever the `strategy = InheritanceType.SINGLE_TABLE`
###### Create Animal table
```sql
CREATE TABLE `jpa`.`animal` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  `color` VARCHAR(45) NULL,
  `dtype` VARCHAR(45) NULL,
  PRIMARY KEY (`id`));
```

##### Persist and Results
###### Persist the entity
```java
public class Main {
    public static void main(String[] args) {
        // Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // Entity Manager
        EntityManager em = emf.createEntityManager();

        Animal a = new Animal();
        a.setName("Animal 1");

        Cat c = new Cat();
        c.setName("Cat1");
        c.setColor("grey");

        try {
            em.getTransaction().begin();
            em.persist(a);
            em.persist(c);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
```

###### Hibernate SQL Log
```
Hibernate: insert into Animal (name, DTYPE) values (?, 'Animal')
Hibernate: insert into Animal (name, color, DTYPE) values (?, ?, 'Cat')```
###### Querying the database to see the results
```sql
mysql> select * from animal;
+----+----------+-------+--------+
| id | name     | color | dtype  |
+----+----------+-------+--------+
|  1 | Animal 1 | NULL  | Animal |
|  2 | Cat1     | grey  | Cat    |
+----+----------+-------+--------+
2 rows in set (0.00 sec)
```

- Advantages:
    -  the search is easy (perform better than the Joined strategy, because you execute queries in one table, in joined it will be in multiple tables)
  
- DisAdvantages:
    - You will have nullable, because for example cat has color and animal does not have a color, what if we have a dog entity that has another column, this means this column value would be null in both Animal, and Cat rows and so on..
    - you always have to store the type in the discriminator column (dtype)

### Joined strategy:
In joined strategy we will have table for each entity and they refer through the id, what that means we will have a table for the base class, we will store the attributes in the base entity at the base table, and in the sub table we will store its attributes along with the id we stored in the Base class so it work as FK for the join operations later.
##### Entity Creation
###### Create Product Entity
```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // getters and setters
}
```
###### Create Chocolate Entity
```java
@Entity
public class Chocolate extends Product {
    
    private int kcal;
    
    // getters and setters
}

```

##### Database Tables Creation
In joined strategy we will have table for each entity.
###### Create Product table
```sql
CREATE TABLE `jpa`.`product` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  PRIMARY KEY (`id`));
```
###### Create Chocolate table
```sql
CREATE TABLE `jpa`.`chocolate` (
  `kcal` INT NULL,
  `id` INT NULL,
  INDEX `chocolate_product_idx` (`id` ASC) VISIBLE,
  CONSTRAINT `chocolate_product`
    FOREIGN KEY (`id`)
    REFERENCES `jpa`.`product` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

```
##### Persist and Results
###### Persist the entity
```java
public class Main {
    public static void main(String[] args) {
        // Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // Entity Manager
        EntityManager em = emf.createEntityManager();

        Product p = new Product();
        p.setName("Product");

        Chocolate c = new Chocolate();
        c.setName("chocolate");
        c.setKcal(100);

        try {
            em.getTransaction().begin();
            em.persist(p);
            em.persist(c);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
```
###### Hibernate SQL Log
```
Hibernate: insert into Product (name) values (?)
Hibernate: insert into Product (name) values (?)
Hibernate: insert into Chocolate (kcal, id) values (?, ?)
```
###### Querying the database to see the results
```sql
mysql> select * from product;
+----+-----------+
| id | name      |
+----+-----------+
|  1 | Product   |
|  2 | chocolate |
+----+-----------+
2 rows in set (0.00 sec)

mysql> select * from chocolate;
+------+------+
| kcal | id   |
+------+------+
|  100 |    2 |
+------+------+
1 row in set (0.00 sec)
```

- Advantages:
  - you no longer need a discriminator column
  - we are not forced to have nullable columns (in single table we are forced to have nullable columns)
- DisAdvantages:
  - Queries will be slower because we will have  join operations 

### TABLE_PER_CLASS strategy ⚠️:
Very poor implementation, and it's not recommended to use, so do not use it, and the JPA specification  states that the implementation does not have to support this, we just learn about it for the sake of knowledge.
You can not use identity generation for the id column 😥, so you need to set the id explicitly
##### Entity Creation
###### Create Employee Entity
```java
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS )
public class Employee {
    @Id
    private Long id;

    private String name;

    // getters and setters
}
```
###### Create Manager Entity
```java
@Entity
public class Manager extends Employee{

    private String responsibility;

    // getters and setters
}
```
##### Database Tables Creation
In TABLE_PER_CLASS strategy we will have table for each entity.
###### Create Employee table
```sql
CREATE TABLE `jpa`.`employee` (
`id` INT NOT NULL AUTO_INCREMENT,
`name` VARCHAR(45) NULL,
PRIMARY KEY (`id`));
```
###### Create Manager table
```sql
 CREATE TABLE `jpa`.`manager` (
  `id` INT NOT NULL,
  `name` VARCHAR(45) NULL,
  `responsibility` VARCHAR(45) NULL,
  PRIMARY KEY (`id`));

```
##### Persist and Results
###### Persist the entity
```java
public class Main {
    public static void main(String[] args) {
        // Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // Entity Manager
        EntityManager em = emf.createEntityManager();

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setName("Emp1");

        Manager manager = new Manager();
        manager.setId(2L);
        manager.setName("Man1");
        manager.setResponsibility("Manage");


        try {
            em.getTransaction().begin();
            em.persist(employee);
            em.persist(manager);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
```
###### Hibernate SQL Log
```
Hibernate: insert into Employee (name, id) values (?, ?)
Hibernate: insert into Manager (name, responsibility, id) values (?, ?, ?)
```
###### Querying the database to see the results
```sql
mysql> select * from employee;
+----+------+
| id | name |
+----+------+
|  1 | Emp1 |
+----+------+
1 row in set (0.00 sec)

mysql> select * from manager;
+----+------+----------------+
| id | name | responsibility |
+----+------+----------------+
|  2 | Man1 | Manage         |
+----+------+----------------+
1 row in set (0.00 sec)
```
In this strategy we will have table for each entity, and every table will have all the columns for it and the bass class

## `@MappedSuperclas`:
A class whose mapping information is applied to the entities that inherit from it.
It is not persistent itself, but has subclasses that are persistent, we can think of @MappedSuperclass as this annotation tell the JPA implementation that this class will contain other annotations that have to be managed by the implementation so it has to be known by the implementation but its not an entity.
You can’t use @Inheritance with @MappedSuperclass

##### Entity Creation
###### Create Vehicle @MappedSuperClass
**Its not a good idea to have the id at the @MappedSuperclass, what if you use Table strategy or sequence, its not doable anymore, if its identity strategy it's okay to have it at @MappedSuperclass level.**
```java
@MappedSuperclass
public abstract class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // getters and setters
}
```
###### Create Bicycle Entity
```java
@Entity
public class Bicycle extends Vehicle{
    private String model;
    // getters and setters
}
```
###### Create Car Entity
```java
@Entity
public class Car extends Vehicle{
    private String gas;
    // getters and setters
}
```

##### Database Tables Creation
In @MappedSuperclass the entities that extends it, each one will have its own table
###### Create vehicle table
This `Vehicle` is not an entity its even an abstract class, so we will not have table for it
###### Create Bicycle table
```sql
CREATE TABLE `jpa`.`bicycle` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  `model` VARCHAR(45) NULL,
  PRIMARY KEY (`id`));
```
###### Create Car table
```sql
CREATE TABLE `jpa`.`car` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  `gas` VARCHAR(45) NULL,
  PRIMARY KEY (`id`));
```

##### Persist and Results
###### Persist the entity
```java
public class Main {
    public static void main(String[] args) {
        // Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // Entity Manager
        EntityManager em = emf.createEntityManager();

        Bicycle bicycle = new Bicycle();
        bicycle.setName("Bicycle");
        bicycle.setModel("Bicycle2022");

        Car car = new Car();
        car.setName("Car");
        car.setGas("Petrol");

        try {
            em.getTransaction().begin();
            em.persist(bicycle);
            em.persist(car);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
```
###### Hibernate SQL Log
```
Hibernate: insert into Bicycle (name, model) values (?, ?)
Hibernate: insert into Car (name, gas) values (?, ?)
```
###### Querying the database to see the results
```sql
mysql> select * from bicycle;
+----+---------+-------------+
| id | name    | model       |
+----+---------+-------------+
|  1 | Bicycle | Bicycle2022 |
+----+---------+-------------+
1 row in set (0.00 sec)

mysql> select * from car;
+----+------+--------+
| id | name | gas    |
+----+------+--------+
|  1 | Car  | Petrol |
+----+------+--------+
1 row in set (0.00 sec)
```

## @MappedSuperClass VS Entity inheritance @Inheritance  
**MappedSuperClass**
- must be used to inherit properties, associations, and methods.
- tells the JPA provider to include the base class persistent properties as if they were declared by the child class extending the superclass annotated with

**Entity inheritance**
- must be used when you have an entity, and several sub-entities.

#### **Note: The id should be declared once in all the inheritance chain**