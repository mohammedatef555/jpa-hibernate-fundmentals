# JPA Fundamentals - Lesson 8 - @AssociationOverride and @ElementCollection


###### following [JPA / Hibernate Fundamentals from Laur Spilca](https://www.youtube.com/playlist?list=PLEocw3gLFc8USLd90a_TicWGiMThDtpOJ "JPA / Hibernate Fundamentals Laur Spilca")

## @AssociationOverride
As we discussed before if you have @Embeddable class and you @Embedded it in an @Entity class, if there is a field you want to override its name you can either:
- use @Column  in the @Embeddable class (but this will affect the @Embeddable class itself whenever you @Embedded in any @Entity class)
- use @AttributeOverride  over the @Embedded in the @Entity class (this way you can leave the @Embeddable as it is and override the names if desired in any @Entity class)

### @AssociationOverride @ManyToOne Example 
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
CREATE TABLE `employee` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  `department` int DEFAULT NULL,
  `contract_no` int DEFAULT NULL,
  PRIMARY KEY (`id`)
);
```

##### Entity Creation
###### Create DepartmentDetails Embeddable
```java
@Embeddable
public class DepartmentDetails {
    private String contractNo;

    @ManyToOne
    private Department department;
    
    // getters and setters
}
```
###### Create Department Entity
```java
@Entity
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    // getters and setters
}
```
###### Create Employee Entity
```java
@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Embedded
    private DepartmentDetails details;
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

        Department d = new Department();
        d.setName("DEP1");

        DepartmentDetails dd = new DepartmentDetails();
        dd.setContractNo(123);
        dd.setDepartment(d);

        Employee e = new Employee();
        e.setName("EMP1");
        e.setDetails(dd);

        try {
            em.getTransaction().begin();
            em.persist(d);
            em.persist(e);
            em.getTransaction().commit();
        } catch (Exception exception) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
```

###### Hibernate SQL Log
```
Hibernate: insert into Department (name) values (?)
Hibernate: insert into Employee (contractNo, department_id, name) values (?, ?, ?)
Nov 06, 2022 2:33:28 PM org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
WARN: SQL Error: 1054, SQLState: 42S22
Nov 06, 2022 2:33:28 PM org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
ERROR: Unknown column 'contractNo' in 'field list'
```
as we can see the database has column named contract_no not contractNo.
So As we mentioned in the beginning before if you have @Embeddable class and you @Embedded it in an @Entity class, if there is a field you want to override its name you can either:
- use @Column  in the @Embeddable class (but this will affect the @Embeddable class itself whenever you @Embedded in any @Entity class)
- use @AttributeOverride  over the @Embedded in the @Entity class (this way you can leave the @Embeddable as it is and override the names if desired in any @Entity class)
we will use the second approach:
```
// Employee.java
@Embedded
@AttributeOverride(name = "contractNo", column = @Column(name = "contract_no"))
private DepartmentDetails details;
```
now lets rerun the main and see the log:
```
Hibernate: insert into Department (name) values (?)
Hibernate: insert into Employee (contract_no, department_id, name) values (?, ?, ?)
Nov 06, 2022 2:39:04 PM org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
WARN: SQL Error: 1054, SQLState: 42S22
Nov 06, 2022 2:39:04 PM org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
ERROR: Unknown column 'department_id' in 'field list'
```
the interesting part is :
`Unknown column 'department_id' in 'field list'`
now the `Employee` has @Embedded `DepartmentDetails`, we need to override `department` in the `DepartmentDetails` but this is not a normal field,

If the @Embeddable class have a relationship, we can override the relationship name using @AssociationOverride
Note :
- Putting relationships in @Embeddable can create a lot of complexity in your code, so avoid it as much as possible, but if you must deal with it, you know you can use @AssociationOverride
- Instead of @AssociationOverride you can use @JoinColumn or @JoinTable (depending in the type of the relationship) in the @Embeddable itself but this works like @Column if you override the names in the @Embeddable you affect it and you might just need to override depending in where you @Embedded it
we will use `@AssociationOverride` 

```
    // Employee.java
    @Embedded
    @AttributeOverride(name = "contractNo", column = @Column(name = "contract_no"))
    @AssociationOverride(name = "department", joinColumns = @JoinColumn(name = "department"))
    private DepartmentDetails details;
```
So:
  - use @AttributeOverride if you override a simple field (over the @Embedded)
  - use @ AssociationOverride if you override a relationship (over the @Embedded)

rerun our main again and see the log:
```
Hibernate: insert into Department (name) values (?)
Hibernate: insert into Employee (contract_no, department, name) values (?, ?, ?)

Process finished with exit code 0
```
###### Querying the database to see the results
```sql
mysql> select * from department;
+----+------+
| id | name |
+----+------+
|  3 | DEP1 |
+----+------+
1 row in set (0.00 sec)

mysql> select * from employee;
+----+------+------------+-------------+
| id | name | department | contract_no |
+----+------+------------+-------------+
|  1 | EMP1 |          3 |         123 |
+----+------+------------+-------------+
1 row in set (0.00 sec)
```
everything works fine.

### @AssociationOverride @ManyToMany Example

##### Database Tables Creation
###### Create Professor table
```sql
CREATE TABLE `jpa`.`professor` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  PRIMARY KEY (`id`));

```
###### Create Student table
```sql
CREATE TABLE `student` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
);
```
now we will implement ManyToMany relationship, and we will make it unidirectional for the sake of the simplicity
and as we know we should have a third table
```sql
CREATE TABLE `student_prof` (
  `student` int NOT NULL,
  `professor` int DEFAULT NULL,
  PRIMARY KEY (`student`)
);
```
##### Entity Creation
###### Create Professor Entity
```java
@Entity
public class Professor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    //getters and setters
}
```
###### Create Student Entity
```java
@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Embedded
    private ProfessorDetails details;

    //getters and setters
}
```
###### Create ProfessorDetails Entity
```java
@Embeddable
public class ProfessorDetails {
    @ManyToMany
    private List<Professor> professors;

    public List<Professor> getProfessors() {
        return professors;
    }

    public void setProfessors(List<Professor> professors) {
        this.professors = professors;
    }
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

        Professor professor = new Professor();
        professor.setName("Prof");

        ProfessorDetails details = new ProfessorDetails();
        details.setProfessors(new ArrayList<>());
        details.getProfessors().add(professor);

        Student student = new Student();
        student.setName("Stud");
        student.setDetails(details);


        try {
            em.getTransaction().begin();
            em.persist(professor);
            em.persist(student);
            em.getTransaction().commit();
        } catch (Exception exception) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
```
###### Hibernate SQL Log
```
Hibernate: insert into Professor (name) values (?)
Hibernate: insert into Student (name) values (?)
Hibernate: insert into Student_Professor (Student_id, professors_id) values (?, ?)
Nov 06, 2022 3:04:43 PM org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
WARN: SQL Error: 1146, SQLState: 42S02
Nov 06, 2022 3:04:43 PM org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
ERROR: Table 'jpa.student_professor' doesn't exist
Nov 06, 2022 3:04:43 PM org.hibernate.engine.jdbc.batch.internal.AbstractBatchImpl release
INFO: HHH000010: On release of batch it still contained JDBC statements
```
as we see, it tried to insert in a table called student_professor, but it didn't exist and as we can see `Student_id, professors_id` these are the column names, and as we know we can override these in ManyToMany field using @JoinTable, but as we have it as @Embeddable we cab also use @AssociationOverride
so let's override the defaults
```
    // Student.java
    @Embedded
    @AssociationOverride(
            name = "professors", // the name of  List<Professor> in ProfessorDetails
            joinTable = @JoinTable(
                    name = "student_prof", // overriding the default table name
                    joinColumns = @JoinColumn(name = "student"), // overriding the default column name
                    inverseJoinColumns = @JoinColumn(name = "professor")// overriding the default column name
            )
    )
    private ProfessorDetails details;
```
```
Hibernate: insert into Professor (name) values (?)
Hibernate: insert into Student (name) values (?)
Hibernate: insert into student_prof (student, professor) values (?, ?)

Process finished with exit code 0
```
###### Querying the database to see the results
```sql
mysql>
mysql> select * from professor;
+----+------+
| id | name |
+----+------+
|  6 | Prof |
+----+------+
1 row in set (0.00 sec)

mysql> select * from student;
+----+------+
| id | name |
+----+------+
|  3 | Stud |
+----+------+
1 row in set (0.00 sec)

mysql>
mysql> select * from student_prof;
+---------+-----------+
| student | professor |
+---------+-----------+
|       3 |         6 |
+---------+-----------+
1 row in set (0.00 sec)
```
everything works fine.

if the @Embeddable class have a relationship, we can override the relationship name using @AssociationOverride
Note :
- Putting relationships in @Embeddable can create a lot of complexity in your code, so avoid it as much as possible, but if you must deal with it, you know you can use @AssociationOverride
- Instead of @AssociationOverride you can use @JoinColumn or @JoinTable (depending on the type of the relationship) in the @Embeddable itself but this works like @Column if you override the names in the @Embeddable you affect it and you might just need to override depending in where you @Embedded it

## @ElementCollection
If you see @ElementCollection, It means that the collection is not a collection of entities, but a collection of simple types (Strings, etc.) or a collection of embeddable elements (class annotated with @Embeddable).

It also means that the elements are completely owned by the containing entities: they're modified when the entity is modified, deleted when the entity is deleted, etc. They can't have their own lifecycle.

Relationships annotation works only with @Entity classes, so both sides of the relationship must have @Entity annotation,
If you have a relationship and one of the tables, is not an Entity you can use @ElementCollection, and @ElementCollection works with @CollectionTable which works similar to @JoinTable but @JoinTable works only with relationships not @ElementCollection.
@CollectionTable(name=“table-name”) you can specify the table name here
@CollectionTable(joinColumns=@JoinColumn) you can specify the FK column name here
And for the column that holds the values it self we can use @Column

If the relationship consists of primitive type and the FK, you can just use List<Wrapper>

If it is an object (consists of more than one column) use @Embeddable that is how it works with @ELementCollection, here we can use @CollectionTable to override the table and the FK names, but because we have more than one column we can not use @Column as the previous example, we can use @AttributeOverride

### Collection of simple types (Strings, etc.)

##### Database Tables Creation
###### Create Person table
```sql
CREATE TABLE `jpa`.`person` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  PRIMARY KEY (`id`));

```
###### Create person_phones table
```sql
CREATE TABLE `jpa`.`person_phones` (
  `phone` VARCHAR(45) NULL,
  `person` INT NULL);
 
```

##### Entity Creation
Here we will have only one entity, although we have 2 tables in the database
###### Create Person Entity
```java
@Entity
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ElementCollection
    private List<String> phones;

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
        person.setName("Person1");
        person.setPhones(Arrays.asList("123","456"));

        try {
            em.getTransaction().begin();
            em.persist(person);
            em.getTransaction().commit();
        } catch (Exception exception) {
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
Hibernate: insert into Person_phones (Person_id, phones) values (?, ?)
Nov 06, 2022 3:24:42 PM org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
WARN: SQL Error: 1054, SQLState: 42S22
Nov 06, 2022 3:24:42 PM org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
ERROR: Unknown column 'Person_id' in 'field list'
Nov 06, 2022 3:24:42 PM org.hibernate.engine.jdbc.batch.internal.AbstractBatchImpl release
INFO: HHH000010: On release of batch it still contained JDBC statements 
```
as we can see, the jpa expects some default values so let's modify the Person entity to either meet these criteria or override the defaults
let's override the default values:
```
    // Person.java
    @ElementCollection
    @CollectionTable(
            name = "person_phones", // table name
            joinColumns = @JoinColumn(name = "person") // FK column name
    )
    /*because this is only simple type (String) we can override the 
    name using @Column it's an object we wil use AttributeOverride*/
    @Column (name = "phone") 
    private List<String> phones;
```
rerun our main:
```
Hibernate: insert into Person (name) values (?)
Hibernate: insert into person_phones (person, phone) values (?, ?)
Hibernate: insert into person_phones (person, phone) values (?, ?)

Process finished with exit code 0
```
###### Querying the database to see the results
```sql
mysql>
mysql> select * from person;
+----+---------+
| id | name    |
+----+---------+
|  2 | Person1 |
+----+---------+
1 row in set (0.00 sec)

mysql> select * from person_phones;
+-------+--------+
| phone | person |
+-------+--------+
| 123   |      2 |
| 456   |      2 |
+-------+--------+
2 rows in set (0.00 sec)
```
### Collection of embeddable elements
##### Database Tables Creation
we will use person table from the previous example
###### Create person_documents table
```sql
CREATE TABLE `person_documents` (
  `number` varchar(45) DEFAULT NULL,
  `reference` varchar(45) DEFAULT NULL,
  `person` varchar(45) DEFAULT NULL
);
```

##### Entity Creation
we will use person entity from the previous example
###### Create Document Class
```java
@Embeddable
public class Document {
    private String number;
    private String reference;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
```
###### Modifying Person Entity
```java
@Entity
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ElementCollection
    @CollectionTable(
            name = "person_phones", // table name
            joinColumns = @JoinColumn(name = "person") // FK column name
    )
    /*because this is only simple type (String) we can override the
    name using @Column it's an object we wil use AttributeOverride*/
    @Column(name = "phone")
    private List<String> phones;

    @ElementCollection
    private List<Document> documents; // Collection of embeddable elements
    
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

        Document document = new Document();
        document.setNumber("123");
        document.setReference("124");

        Person person = new Person();
        person.setName("Person1");
        //person.setPhones(Arrays.asList("123","456"));
        person.setDocuments(new ArrayList<>());
        person.getDocuments().add(document);

        try {
            em.getTransaction().begin();
            em.persist(person);
            em.getTransaction().commit();
        } catch (Exception exception) {
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
Hibernate: insert into Person_documents (Person_id, number, reference) values (?, ?, ?)
Nov 06, 2022 3:39:17 PM org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
WARN: SQL Error: 1054, SQLState: 42S22
Nov 06, 2022 3:39:17 PM org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
ERROR: Unknown column 'Person_id' in 'field list'
Nov 06, 2022 3:39:17 PM org.hibernate.engine.jdbc.batch.internal.AbstractBatchImpl release
INFO: HHH000010: On release of batch it still contained JDBC statements
```
here we can use @CollectionTable to override the table and the FK names, but because we have more than one column we can not use @Column as the previous example, we can use @AttributeOverride if the columns have different names than the document class
```
    // Person.java
    @ElementCollection
    @CollectionTable(
            name = "person_documents",// table name
            joinColumns = @JoinColumn(name = "person") // FK column name
    )
    private List<Document> documents;
```
let's rerun our main and see the log:
```
Hibernate: insert into Person (name) values (?)
Hibernate: insert into person_documents (person, number, reference) values (?, ?, ?)
```
###### Querying the database to see the results
```sql
mysql>
mysql> select * from person;
+----+---------+
| id | name    |
+----+---------+
|  2 | Person1 |
|  5 | Person1 |
+----+---------+
2 rows in set (0.01 sec)

mysql> select * from person_documents;
+--------+-----------+--------+
| number | reference | person |
+--------+-----------+--------+
| 123    | 124       | 5      |
+--------+-----------+--------+
1 row in set (0.01 sec)
```
everything works fine