# JPA Fundamentals - Lesson 3 - @Enumerated, @Temporal types and @Embeddable
###### following [JPA / Hibernate Fundamentals from Laur Spilca](https://www.youtube.com/playlist?list=PLEocw3gLFc8USLd90a_TicWGiMThDtpOJ "JPA / Hibernate Fundamentals Laur Spilca")

## @Enumerated

We can save Enum values in the database using @Enumerated annotation.

we can save it as:
- oridnal value
- string value

###### To save its ordinal value:
in MySQL we create the price table that has a currency column which will hold the ordinal value of the enum.
```sql
CREATE TABLE `price` (
  `id` int NOT NULL AUTO_INCREMENT,
  `amount` double DEFAULT NULL,
  `currency` int DEFAULT NULL,
  PRIMARY KEY (`id`)
);
```
note the currency column type is int since we will save it's oridnal value

Now we will define the Price Entity Class and the Currency ENUM
```java
package org.example.entities.enums;

public enum Currency {
    EUR, USD
}
```
```java
@Entity
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private double amount;

    @Enumerated(EnumType.ORDINAL)// this is the default
    private Currency currency;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}

```

Note how we used `@Enumerated()` here we specified we need to store the oridnal value using `@Enumerated(EnumType.ORDINAL)` this is the default so we can just annotate the field with `@Enumerated` and it will still work as expected

###### Persist one object:
As we did before our main will look like this:
```java
public class Main {
    public static void main(String[] args) {
        //Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        //Entity Manager
        EntityManager em = emf.createEntityManager();

        Price price = new Price();
        price.setAmount(1.0);
        price.setCurrency(Currency.EUR);

        try {
            em.getTransaction().begin();
            em.persist(price);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
             em.close();
        }
    }
}
```
###### Querying the database to see the results;
```sql
mysql> select * from price;
+----+--------+----------+
| id | amount | currency |
+----+--------+----------+
|  1 |      1 |        0 |
+----+--------+----------+
1 row in set (0.00 sec)
```
###### To save its String value:
@Enumerated by default will store the ordinal value ( 0, 1, etc..) which is the index of the value in the enum definition you can change this behavior to save the name instead of the ordinal value.
We will start by alerting the price table since we defined the column type as int before to store the ordinal value, changing the column type to ENUM() we can also set it to varchar
```sql
ALTER TABLE `jpa`.`price` 
CHANGE COLUMN `currency` `currency` ENUM('EUR', 'USD') NULL DEFAULT NULL ;

```
now we need to change the Price Entity class to use the string value for the enumerated type:
```java
@Entity
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private double amount;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}

```

Note how we used `@Enumerated()` we used `@Enumerated(EnumType.STRING)`
to tell the JPA/Hibernate to store the string value.

###### Persist one object:
As we did before our main will look like this:
```java
public class Main {
    public static void main(String[] args) {
        //Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        //Entity Manager
        EntityManager em = emf.createEntityManager();

        Price price = new Price();
        price.setAmount(1.0);
        price.setCurrency(Currency.EUR);

        try {
            em.getTransaction().begin();
            em.persist(price);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
             em.close();
        }
    }
}
```
###### Querying the database to see the results;
```sql
mysql> select * from price;
+----+--------+----------+
| id | amount | currency |
+----+--------+----------+
|  3 |      1 | EUR      |
+----+--------+----------+
1 row in set (0.00 sec)
```
as we can see we saved the string represntation of the enum value instead of its ordinal value.


##### Should you save the enumerated types as ordinal or string values?
As we know save it as a number can help us if we planning to index the column holding the enum as ordinal value as it’s just a number but what if you change the order in the java code this will lead to a problem
If you are not planning to index the column holding the enum values you can insert the values as its string values.

## Temporal types
Temporal types
Before jpa2.2 you couldn’t use anything except `Date` from `java.util` from jpa2.2 you can use also `java.time` package classes like `LocalDate`, `LocalDateTime`, `LocalTime`, `ZonedDate`, etc…

If you want to store the date
- in database the column type should be DATE
- the column type in entity class should be LocalDate
If you want to store the Timestamp
- in database the column type should be TIMESTAMP
- the column type in entity class should be LocalDateTime
If you want to store the Time
- in database the column type should be TIME
- the column type in entity class should be LocalTime

##### Using LocalDate
###### creating the product table
```sql
CREATE TABLE product (
  `id` INT NOT NULL AUTO_INCREMENT,
  `exp_date` DATE NULL,
  PRIMARY KEY (`id`));
```
###### creating the entity class
```java
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "exp_date")
    private LocalDate expDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getExpDate() {
        return expDate;
    }

    public void setExpDate(LocalDate expDate) {
        this.expDate = expDate;
    }
}

```

###### Persist one object:
```java
public class Main {
    public static void main(String[] args) {
        //Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        //Entity Manager
        EntityManager em = emf.createEntityManager();

//        Price price = new Price();
//        price.setAmount(1.0);
//        price.setCurrency(Currency.EUR);

        Product product = new Product();
        product.setExpDate(LocalDate.now());

        try {
            em.getTransaction().begin();
//            em.persist(price);
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

###### Querying the database to see the results;
```sql
mysql> select * from product;
+----+------------+
| id | exp_date   |
+----+------------+
|  1 | 2022-10-31 |
+----+------------+
1 row in set (0.00 sec)
```

##### Using ZonedDateTime
###### creating the event table
```sql
CREATE TABLE event (
  `id` INT NOT NULL AUTO_INCREMENT,
  `event_time` DATETIME NULL,
  PRIMARY KEY (`id`));
```
###### creating the entity class
```java
@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "event_time")
    private ZonedDateTime eventTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ZonedDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(ZonedDateTime eventTime) {
        this.eventTime = eventTime;
    }
}
```

###### Persist one object:
```java
public class Main {
    public static void main(String[] args) {
        //Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        //Entity Manager
        EntityManager em = emf.createEntityManager();

//        Price price = new Price();
//        price.setAmount(1.0);
//        price.setCurrency(Currency.EUR);

//        Product product = new Product();
//        product.setExpDate(LocalDate.now());

        Event event = new Event();
        event.setEventTime(ZonedDateTime.now(ZoneId.of("Africa/Cairo")));

        try {
            em.getTransaction().begin();
//            em.persist(price);
//            em.persist(product);
            em.persist(event);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
             em.close();
        }
    }
}
```

###### Querying the database to see the results;
```sql
mysql> select * from event;
+----+---------------------+
| id | event_time          |
+----+---------------------+
|  1 | 2022-10-31 21:37:17 |
+----+---------------------+
1 row in set (0.00 sec)
```
Now JPA does not save ZonedDateTime as you might expect so if you specify the zone it will still save the dateTime according to local time zone even if you explicitly define the zone, that’s happen because JPA does not store the zone id, actually it converts the ZonedDateTime to LocalDateTime and save it

Now if you work in a project use JPA below 2.2, you can’t use java.time Classes like LocalDate, LocalDateTime, LocalTime, ZonedDate, etc…,
You can use Date from java.util, when we were using the java.time classes we didn’t add any @Annotation specific for storing the date because we are confident about what we are saving,
Do we need to store Date?
- Use LocalDate
Do we need to store timestamp ?
- Use LocalDateTime
Do we need to store Time?
- Use LocalTime
But this is not the case when using Date class from `java.util` you can store Date, TimeStamp and Time so we need to use specific @Annotation to tell JPA what we are planning to save the annotation is `@Temporal(<here you specify the TemporalType>)`

##### Using `Date` from `java.util`
###### creating the employee table
```sql
CREATE TABLE `employee` (
  `id` int NOT NULL AUTO_INCREMENT,
  `emp_date` date DEFAULT NULL,
  PRIMARY KEY (`id`)
);
```
###### creating the entity class
```java
@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "emp_date")
    @Temporal(TemporalType.DATE)
    private Date empDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getEmpDate() {
        return empDate;
    }

    public void setEmpDate(Date empDate) {
        this.empDate = empDate;
    }
}
```
when we created the employee table we specified the emp_date as DATE so when you are using `Date` class from `java.util` we need to tell `JPA` what is the exact temporal type so we used `@Temporal(TemporalType.DATE)` here.

###### Persist one object:
```java
public class Main {
    public static void main(String[] args) {
        //Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        //Entity Manager
        EntityManager em = emf.createEntityManager();

//        Price price = new Price();
//        price.setAmount(1.0);
//        price.setCurrency(Currency.EUR);

//        Product product = new Product();
//        product.setExpDate(LocalDate.now());

//        Event event = new Event();
//        event.setEventTime(ZonedDateTime.now(ZoneId.of("Africa/Cairo")));

        Employee employee = new Employee();
        employee.setEmpDate(new Date());

        try {
            em.getTransaction().begin();
//            em.persist(price);
//            em.persist(product);
//            em.persist(event);
            em.persist(employee);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
             em.close();
        }
    }
}
```

###### Querying the database to see the results;
```sql
mysql> select * from employee;
+----+------------+
| id | emp_date   |
+----+------------+
|  1 | 2022-10-31 |
+----+------------+
1 row in set (0.01 sec)
```

## @Embeddable
What if you have a table in the database like company table which has
Id, name, number, street, city
Now number, street, city is actually representing an address so you can basically define it as different class in the code and embed it in the company entity class

###### Create the company table
```sql
CREATE TABLE `jpa`.`company` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  `number` VARCHAR(45) NULL,
  `street` VARCHAR(45) NULL,
  `city` VARCHAR(45) NULL,
  PRIMARY KEY (`id`));
```

###### Create the @Embeddable class
```sql
package org.example.entities.embeddables;

import javax.persistence.Embeddable;

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
###### Create the @Entity class which will embed the @Embeddable class using @Embedded annotation
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

###### Persist one object
```java
public class Main {
    public static void main(String[] args) {
        //Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        //Entity Manager
        EntityManager em = emf.createEntityManager();

//        Price price = new Price();
//        price.setAmount(1.0);
//        price.setCurrency(Currency.EUR);

//        Product product = new Product();
//        product.setExpDate(LocalDate.now());

//        Event event = new Event();
//        event.setEventTime(ZonedDateTime.now(ZoneId.of("Africa/Cairo")));

//        Employee employee = new Employee();
//        employee.setEmpDate(new Date());

        Company company = new Company();
        company.setName("ABC");
        company.setAddress(new Address());
        company.getAddress().setNumber("1");
        company.getAddress().setStreet("street 1");
        company.getAddress().setCity("Cairo");

        try {
            em.getTransaction().begin();
//            em.persist(price);
//            em.persist(product);
//            em.persist(event);
//            em.persist(employee);
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

###### Querying the database to see the results;
```sql
mysql> select * from company;
+----+------+--------+----------+-------+
| id | name | number | street   | city  |
+----+------+--------+----------+-------+
|  1 | ABC  | 1      | street 1 | Cairo |
+----+------+--------+----------+-------+
1 row in set (0.00 sec)
```

@Embeddable annotation means that, this object is actually a part of an entity and can be @Embedded in the main prototype of an entity
Now you can embed this @Embeddable class in another entities

We can use @Embeddable is a way to store a composed primary keys.