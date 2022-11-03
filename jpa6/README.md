# JPA Fundamentals - Lesson 6 - @OneToMany and @ManyToOne

###### following [JPA / Hibernate Fundamentals from Laur Spilca](https://www.youtube.com/playlist?list=PLEocw3gLFc8USLd90a_TicWGiMThDtpOJ "JPA / Hibernate Fundamentals Laur Spilca")

In the previous lesson we discussed about one-to-one, Technically speaking we can have the FK in any side, this logically might not make sense but technically it can be done, however this is not the case, the FK must be in the many side.


## Unidirectional @OneToMany
### Bad Example, Do not try this at home  ⚠️

What if you have both tables that represents the relationship (one to many) but you don’t add FK column, the JPA will try to insert to a table named OneEntity_ManyEntity in our example (department_employee) and will store the id for both for every insertion, this is okay if we have  ManyToMany relationship, but in OneToMany this is a bad practice and not desired do not do it, this example for demonstration only

##### Database Tables Creation
###### Create department table
```sql
CREATE TABLE `jpa`.`department` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  PRIMARY KEY (`id`));

```

###### Create employee table
```sql
CREATE TABLE `jpa`.`employee` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  PRIMARY KEY (`id`));

```

as we discussed we did not have a FK in the many side, just to demonstrate the JPA behavior in Unidirectional @OneToMany.

##### Entity Creation
###### Create Department Entity
```java
@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String name;
    
    // getters and setters
}

```
###### Create Company Entity
```java
@Entity
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @OneToMany
    private Collection<Employee> employees;

    // getters and setters

}
```

##### Persist and Results
###### Persist the entity
```java
public class Main {
    public static void main(String[] args) {
        //Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        //Entity Manager
        EntityManager em = emf.createEntityManager();

        Employee employee1 = new Employee();
        employee1.setName("Emp1");
        Employee employee2 = new Employee();
        employee1.setName("Emp2");

        Department department = new Department();
        department.setName("ABC");
        department.setEmployees(new ArrayList<>());

        department.getEmployees().add(employee1);
        department.getEmployees().add(employee2);


        try {
            em.getTransaction().begin();
            em.persist(employee1);
            em.persist(employee1);
            em.persist(department);
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
Hibernate: insert into Employee (name) values (?)
Hibernate: insert into Department (name) values (?)
Hibernate: insert into Department_Employee (Department_id, employees_id) values (?, ?)
Nov 03, 2022 7:06:31 PM org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
WARN: SQL Error: 1146, SQLState: 42S02
Nov 03, 2022 7:06:31 PM org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
ERROR: Table 'jpa.department_employee' doesn't exist
```
by default, that is how the unidirectional @OneToMany works, it will try to insert to a third table called **department_employee**, and this will throw an Exception since we did not create this table, so let is create and see the results.

```sql
CREATE TABLE `jpa`.`department_employee` (
  `Department_id` INT NOT NULL,
  `employees_id` INT NOT NULL,
  PRIMARY KEY (`Department_id`, `employees_id`));

```
we made a table to satisfy the JPA according to what we have seen in the hibernate log:
```
Hibernate: insert into Department_Employee (Department_id, employees_id) values (?, ?)

```
now let is rerun our example, and see the results:
```sql
Hibernate: insert into Employee (name) values (?)
Hibernate: insert into Employee (name) values (?)
Hibernate: insert into Department (name) values (?)
Hibernate: insert into Department_Employee (Department_id, employees_id) values (?, ?)
Hibernate: insert into Department_Employee (Department_id, employees_id) values (?, ?)
```
everything works fine now.
###### Querying the database to see the results
```sql
mysql> select * from department;
+----+------+
| id | name |
+----+------+
|  1 | ABC  |
+----+------+
1 row in set (0.00 sec)

mysql> select * from employee;
+----+------+
| id | name |
+----+------+
|  1 | Emp1 |
|  2 | Emp2 |
+----+------+
2 rows in set (0.00 sec)

mysql> select * from department_employee;
+---------------+--------------+
| Department_id | employees_id |
+---------------+--------------+
|             1 |            1 |
|             1 |            2 |
+---------------+--------------+
2 rows in set (0.01 sec)
```
> For a DBA, this looks more like a many-to-many database association than a one-to-many relationship, and it’s not very efficient either. Instead of two tables, we now have three tables, so we are using more storage than necessary. Instead of only one Foreign Key, we now have two of them. However, since we are most likely going to index these Foreign Keys, we are going to require twice as much memory to cache the index for this association. Not nice! - [The best way to map a @OneToMany relationship with JPA and Hibernate](https://vladmihalcea.com/the-best-way-to-map-a-onetomany-association-with-jpa-and-hibernate/ "The best way to map a @OneToMany relationship with JPA and Hibernate")

## Unidirectional @ManyToOne
This means the **Many** only will know about the **One** side
##### Database Tables Creation
###### Create Person table
```sql
CREATE TABLE `jpa`.`person` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  PRIMARY KEY (`id`));

```
###### Create Document table
```sql
CREATE TABLE `jpa`.`document` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  `person_id` INT NULL,
  PRIMARY KEY (`id`),
  INDEX `document_person_idx` (`person_id` ASC) VISIBLE,
  CONSTRAINT `document_person`
    FOREIGN KEY (`person_id`)
    REFERENCES `jpa`.`person` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

```

##### Entity Creation
###### Create Person Entity
```java
@Entity
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String name;
    
    // getters and setters
}
```
###### Create Document Entity
```java
@Entity
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String name;
    
    @ManyToOne
    private Person person;
    
    //getters and setters
}
```

##### Persist and Results
###### Persist the entity
```java
public class Main {
    public static void main(String[] args) {
        //Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        //Entity Manager
        EntityManager em = emf.createEntityManager();

        Person person = new Person();
        person.setName("Mohammed");

        Document document = new Document();
        document.setName("ABC");
        document.setPerson(person);


        try {
            em.getTransaction().begin();
            em.persist(person);
            em.persist(document);
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
Hibernate: insert into Person (name) values (?)
Hibernate: insert into Document (name, person_id) values (?, ?)
```
###### Querying the database to see the results
```sql
mysql> select * from person;
+----+----------+
| id | name     |
+----+----------+
|  1 | Mohammed |
+----+----------+
1 row in set (0.00 sec)

mysql> select * from document;
+----+------+-----------+
| id | name | person_id |
+----+------+-----------+
|  1 | ABC  |         1 |
+----+------+-----------+
1 row in set (0.00 sec)
```

As expected everything works fine.
By default JPA expects the FK to be named like **entity_id** in our example **person_id**, as we did before with @OneToOne we can use @JoinColumn to override this behavior
```sql
ALTER TABLE `jpa`.`document` 
DROP FOREIGN KEY `document_person`;
ALTER TABLE `jpa`.`document` 
CHANGE COLUMN `person_id` `person` INT NULL DEFAULT NULL ;
ALTER TABLE `jpa`.`document` 
ADD CONSTRAINT `document_person`
  FOREIGN KEY (`person`)
  REFERENCES `jpa`.`person` (`id`);

```

```java
    // Document.java
	@ManyToOne
    @JoinColumn(name = "person")
    private Person person;
```

## Bidirectional @OneToMany and @ManyToOne

##### Database Tables Creation & Entity Creation
We will not create something new we will just edit the exixtence person and document entities, actually only the person entity.
```java
@Entity
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @OneToMany(mappedBy = "person")
    private Collection<Document> documents;
	// getters and setters
}
```


##### Persist and Results
###### Persist the entity
```java
public class Main {
    public static void main(String[] args) {
        //Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        //Entity Manager
        EntityManager em = emf.createEntityManager();

        Person person = new Person();
        person.setName("Mohamed");
        person.setDocuments(new ArrayList<>());
        
        Document document = new Document();
        document.setName("ABC");
        document.setPerson(person);
        
        person.getDocuments().add(document);
        
        try {
            em.getTransaction().begin();
            em.persist(person);
            em.persist(document);
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
Hibernate: insert into Person (name) values (?)
Hibernate: insert into Document (name, person) values (?, ?)
```
###### Querying the database to see the results
```sql
mysql> select * from person;
+----+---------+
| id | name    |
+----+---------+
|  1 | Mohamed |
+----+---------+
1 row in set (0.00 sec)

mysql> select * from document;
+----+------+--------+
| id | name | person |
+----+------+--------+
|  1 | ABC  |      1 |
+----+------+--------+
1 row in set (0.00 sec)
```
everything works fine.
