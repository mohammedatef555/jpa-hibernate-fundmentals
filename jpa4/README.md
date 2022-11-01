# JPA Fundamentals - Lesson 4 - @AttributeOverride and Composed Primary Keys
###### following [JPA / Hibernate Fundamentals from Laur Spilca](https://www.youtube.com/playlist?list=PLEocw3gLFc8USLd90a_TicWGiMThDtpOJ "JPA / Hibernate Fundamentals Laur Spilca")

## @AttributeOverride

We discussed before that if you have a table in the database and some columns in that table can be treated as seperate class in the code, like the company table which have id, name, number, street, city.
now the `number, street, city` can be treated as Address so we can represent it as @Embeddable class and @Embedded it in our @Entity class we discussed this in the previous tutorial but what if we have different names for the same column in the database and the code we can use `@AttributeOverride`

###### Creating the company table
```sql
CREATE TABLE `jpa`.`company` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  `number` VARCHAR(45) NULL,
  `street` VARCHAR(45) NULL,
  `city` VARCHAR(45) NULL,
  PRIMARY KEY (`id`));
```
###### Creating the Entity and the Embeddable classes
The Embeddable class:
```java

@Embeddable
public class Address {
    private String number;
    private String street;
    private String city;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
```

The @Entity class:
```java
@Entity
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String name;
    
    @Embedded
    private Address address;

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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}

```

so as we did in the previuos lesson we just created an @Embeddable class and used @Embedded to embed it in our @Entity class

###### Persist one object
```java
public class Main {
    public static void main(String[] args) {
        // EntityManagerFactory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // EntityManager
        EntityManager em = emf.createEntityManager();

        Company company = new Company();
        company.setName("Comp1");
        company.setAddress(new Address());
        company.getAddress().setNumber("10");
        company.getAddress().setStreet("STR-10");
        company.getAddress().setCity("Cairo");


        try {
            em.getTransaction().begin();
            em.persist(company);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
```

###### Querying the database to see the results:
```sql
mysql> select * from company;
+----+-------+--------+--------+-------+
| id | name  | number | street | city  |
+----+-------+--------+--------+-------+
|  1 | Comp1 | 10     | STR-10 | Cairo |
+----+-------+--------+--------+-------+
1 row in set (0.00 sec)
```

now every thing works as expected because the fields names in the @Embeddable mathces the column names in the company table in the database, But what if we have different names:

Let's change the `Address @Embeddable` to have different names than the database column names:
```java
@Embeddable
public class Address {
    private String no;
    private String str;
    private String city;

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}

```

now if we run the same example we will get an error:
```
Hibernate: insert into Company (city, no, str, name) values (?, ?, ?, ?)
Nov 01, 2022 1:05:32 PM org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
WARN: SQL Error: 1054, SQLState: 42S22
Nov 01, 2022 1:05:32 PM org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
ERROR: Unknown column 'no' in 'field list'
```

How to solve that?
- Using @AttributeOverride
so in our @Entity class we can use
```java
@Embedded
@AttributeOverride(name = "no", column = @Column(name = "number"))
@AttributeOverride(name = "str", column = @Column(name = "street"))
private Address address;
```
now the @Entity class will look like this:
```java
@Entity
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Embedded
    @AttributeOverride(name = "no", column = @Column(name = "number"))
    @AttributeOverride(name = "str", column = @Column(name = "street"))
    private Address address;

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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}

```

###### Persist one object
```java
public class Main {
    public static void main(String[] args) {
        // EntityManagerFactory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // EntityManager
        EntityManager em = emf.createEntityManager();

        Company company = new Company();
        company.setName("Comp2");
        company.setAddress(new Address());
        company.getAddress().setNo("10");
        company.getAddress().setStr("STR-10");
        company.getAddress().setCity("Cairo");


        try {
            em.getTransaction().begin();
            em.persist(company);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
```

###### Querying the database to see the results:
```sql
mysql> select * from company;
+----+-------+--------+--------+-------+
| id | name  | number | street | city  |
+----+-------+--------+--------+-------+
|  1 | Comp1 | 10     | STR-10 | Cairo |
|  2 | Comp2 | 10     | STR-10 | Cairo |
+----+-------+--------+--------+-------+
2 rows in set (0.00 sec)
```

now everything works fine.
From JPA2.2 you can use multiple @AttributeOverride
So if you are using JPA thats below JPA2.2 you can use @AttributeOverrides() and it can receive array of @AttributeOverride
If you are using JPA2.2 stick to using multiple @AttributeOverride over @AttributeOverrides because the latter one has Moore boilerplate code.

so in the above example we could replace:
```java
@Embedded
@AttributeOverride(name = "no", column = @Column(name = "number"))
@AttributeOverride(name = "str", column = @Column(name = "street"))
private Address address;
```
by this:
```java
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "no", column = @Column(name = "number")),
            @AttributeOverride(name = "str", column = @Column(name = "street"))
    })
    private Address address;
```

## Composed Primary Keys
We will discuss two ways for creating Composed Primary Keys
- Using @IdClass
- Using @Embeddable and @EmbeddedId

#### Using @IdClass
###### Creating the company table
```sql
CREATE TABLE `department` (
  `code` varchar(45) NOT NULL,
  `number` varchar(45) NOT NULL,
  `name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`code`,`number`)
);
```
note both our PK here is composed and cotaines both `code`,`number`.
###### Creating the @IdClass and @Entity class
###### @IdClass:
```java
public class DepartmentPk implements Serializable {
    private String code;
    private String number;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}

```
###### @Entity class
```java
@Entity
@IdClass(DepartmentPk.class)
public class Department {
    @Id
    private String code;
    @Id
    private String number;

    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

```
so you add @IdClass in the @Entity class and pass the pkclass.Class to it,
note we have defined the primary key parts in both `DepartmentPk` and `Department` and how we annotated the pk fields in the @Entity class with `@Id`
so in a nuthshell:
Use @Idclass(pkClass.class) over the entity
And it will have the same attributes as the attributes annotated with @Id in the entity class and pkClass must be serializable

###### Persist one object
```java
public class Main {
    public static void main(String[] args) {
        // EntityManagerFactory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // EntityManager
        EntityManager em = emf.createEntityManager();

        Department department = new Department();
        department.setCode("ABC");
        department.setNumber("10");
        department.setName("Department 1");

        try {
            em.getTransaction().begin();
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

###### Querying the database to see the results:
```sql
mysql> select * from department;
+------+--------+--------------+
| code | number | name         |
+------+--------+--------------+
| ABC  | 10     | Department 1 |
+------+--------+--------------+
1 row in set (0.00 sec)
```

If you are using @IdClass and one of the id columns that is part of composed primary key have a different name in the database than the name in the entity and @IdClass class you can use @Column() over the attribute in the @Entity class, like this:
```java
    @Id
    @Column(name = "code")
    private String code;
```

#### Using @Embeddable and @EmbeddedId
Using @Embeddable and @EmbeddedId, again the Pk class which is annotated with  @Embeddable should be serializable
###### Creating the company table
```sql
CREATE TABLE `jpa`.`building` (
  `code` VARCHAR(45) NOT NULL,
  `number` VARCHAR(45) NOT NULL,
  `name` VARCHAR(45) NULL,
  PRIMARY KEY (`code`, `number`));

```
note both our PK here is composed and cotaines both `code`,`number`.
###### Creating the  @Embeddable and @Entity class
###### @Embeddable:
```java
@Embeddable
public class BuildingPk implements Serializable {
    private String code;
    private String number;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
```
###### @Entity class
```java
@Entity
public class Building {
    @EmbeddedId
    private BuildingPk id;

    private String name;

    public BuildingPk getId() {
        return id;
    }

    public void setId(BuildingPk id) {
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

###### Persist one object
```java
public class Main {
    public static void main(String[] args) {
        // EntityManagerFactory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        // EntityManager
        EntityManager em = emf.createEntityManager();

        Building building = new Building();
        building.setId(new BuildingPk());
        building.getId().setCode("ABC");
        building.getId().setNumber("10");
        building.setName("BUILDING");

        try {
            em.getTransaction().begin();
            em.persist(building);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
```

###### Querying the database to see the results:
```sql
mysql> select * from building;
+------+--------+----------+
| code | number | name     |
+------+--------+----------+
| ABC  | 10     | BUILDING |
+------+--------+----------+
1 row in set (0.00 sec)
```

If you are using @Embeddable and @EmbeddedId  and one of the id columns that is part of composed primary key have a different name in the database than the name in the entity you can use @AttributeOverride and @AttributeOverrides, like the following:
```java
    @EmbeddedId
    @AttributeOverride(name = "code", column = @Column(name = "code"))
    private BuildingPk id;
```

## Field or Property Access?
This topic is not so practical you will not often see it in production.
By default the JPA/Hibernate access is on field, If you place the annotation over the field it’s also access on field, but if you place the annotations over the getters the access will be on the property and JPA/Hibernate will use the getters and setters in order to manipulate your entities, or you can use @Access annotation over the @Entity Class to choose which one.
