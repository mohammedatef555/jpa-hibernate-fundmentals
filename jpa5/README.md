# JPA Fundamentals - JPA Fundamentals - Lesson 5 - Relationships, @OneToOne and Secondary Tables
###### following [JPA / Hibernate Fundamentals from Laur Spilca](https://www.youtube.com/playlist?list=PLEocw3gLFc8USLd90a_TicWGiMThDtpOJ "JPA / Hibernate Fundamentals Laur Spilca")

## One To One Using @SecondaryTable
The following is not widely used, but its available if you need to use it.
The following is not a relationship in terms of JPA, in JPA to have a relationship you have to have @Entity s.

We will have two tables `company` and `address`
##### Database Tables Creation
###### Create Company table
```sql
CREATE TABLE `jpa`.`company` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  PRIMARY KEY (`id`));

```

###### Create address table
```sql
CREATE TABLE `jpa`.`address` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `street` VARCHAR(45) NULL,
  `number` VARCHAR(45) NULL,
  `company` INT NULL,
  PRIMARY KEY (`id`),
  INDEX `address_company_idx` (`company` ASC) VISIBLE,
  CONSTRAINT `address_company`
    FOREIGN KEY (`company`)
    REFERENCES `jpa`.`company` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

```
As we can see the `company column` in `address` table is FK to id column in the `company` table

##### Entity Creation
###### Create Company Entity
We can use @SecondaryTable to merge two database tables into one JPA Entity, this is not a relationship in terms of the JPA because we have only one entity, its only a relationship in terms of the database implementation

Now we have company column in address table which should work as fk, but the JPA does not know that so it will make sure to save the id of the primary table in the id of the secondary table
In a nutshell: `x` id in company table will have `x` id in address table this how it works be default. Since all your fields in one entity we need to tell the JPA about the fields that belongs to the secondary table using @Column(table=“secondary-table-name”)

note how we used `@SecondaryTable(name = "address")`
and `@Column(table = "address")`

```java
@Entity
@SecondaryTable(name = "address")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    private String name;
    
    @Column(table = "address")
    private String street;
    @Column(table = "address")
    private String number;
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

        Company company = new Company();
        company.setName("ABC");
        company.setNumber("10");
        company.setStreet("STR");

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
###### Hibernate SQL Log
```
Hibernate: insert into Company (name) values (?)
Hibernate: insert into address (number, street, id) values (?, ?, ?)
```
###### Querying the database to see the results
```sql
mysql> select * from company;
+----+------+
| id | name |
+----+------+
|  1 | ABC  |
+----+------+
1 row in set (0.00 sec)
```
```sql
mysql> select * from address;
+----+--------+--------+---------+
| id | street | number | company |
+----+--------+--------+---------+
|  1 | STR    | 10     |    NULL |
+----+--------+--------+---------+
1 row in set (0.01 sec)
```

So if we used this way to represents the relationship, as we discussed it will save to the primary table and use the id of the primary table as the id also for the secondary table and the one we defined to work as FK (company column in address table) will be null here.
But this is not desired we need to save the PK of the primary entity in the FK of the secondary table, to achieve that we can use
@SecondaryTable(name=“secondary-table-name”, pkJoinColumns=@PrimaryKeyJoinColumn(name=“fk-column-name”))
This will save the pk into the fk column in the secondary table, so lets say this in action
```java
@Entity
@SecondaryTable(name = "address", pkJoinColumns = @PrimaryKeyJoinColumn(name = "company"))
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    private String name;

    @Column(table = "address")
    private String street;
    @Column(table = "address")
    private String number;
	
	// getters and setters
}
```

###### Persist the entity
```java
public class Main {
    public static void main(String[] args) {
        //Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        //Entity Manager
        EntityManager em = emf.createEntityManager();

        Company company = new Company();
        company.setName("QWE");
        company.setNumber("10");
        company.setStreet("STR");

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
###### Hibernate SQL Log
```
Hibernate: insert into Company (name) values (?)
Hibernate: insert into address (number, street, company) values (?, ?, ?)
```
###### Querying the database to see the results
```sql
mysql> select * from company;
+----+------+
| id | name |
+----+------+
|  1 | ABC  |
|  2 | QWE  |
+----+------+
2 rows in set (0.00 sec)
```
```sql
mysql> select * from address;
+----+--------+--------+---------+
| id | street | number | company |
+----+--------+--------+---------+
|  1 | STR    | 10     |    NULL |
|  2 | STR    | 10     |       2 |
+----+--------+--------+---------+
2 rows in set (0.01 sec)
```

You can use multiple @SecondaryTable

If you are using Java 8 or above and JPA2.2 and above you can use multiple @SecondaryTable or  @SecondaryTables(), if not you can only use @SecondaryTables() which receive array of @SecondaryTable

To summarize, this is not a relationship in terms of JPA, in JPA you have to have `Entities` in order to have relationships.

## One To One Using @OneToOne (Unidirectiona Way)
Unidirectional way(one-way)(only one entity knows about the other)
In Unidirectional one-to-one we store the relationship in the class that contains the FK using @OneToOne
##### Database Tables Creation
###### Create Product Table
```sql
CREATE TABLE `jpa`.`product` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  `price` DOUBLE NULL,
  PRIMARY KEY (`id`));

```

###### Create Detail Table
```sql
CREATE TABLE `jpa`.`detail` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `kcal` INT NULL,
  `product_id` INT NULL,
  PRIMARY KEY (`id`),
  INDEX `detail_product_idx` (`product_id` ASC) VISIBLE,
  CONSTRAINT `detail_product`
    FOREIGN KEY (`product_id`)
    REFERENCES `jpa`.`product` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

```

As we can see the `product_id column` in `detail` table is FK to id column in the `product` table

##### Entity Creation
###### Create Product Entity

```java
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String name;
    private double price;
    
    // getters and setters
}

```

###### Create Detail Entity (the owner entity)
The onwer entity is the entity that has the FK and since this is unidirectional one to one relationship, and only the onwer entity knows about the other entities
```java
@Entity
public class Detail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private int kcal;

    @OneToOne
    private Product product;

    // getters and setters
}
```
###### Persist the entities
```java
public class Main {
    public static void main(String[] args) {
        //Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        //Entity Manager
        EntityManager em = emf.createEntityManager();

        Product product = new Product();
        product.setName("ABC");
        product.setPrice(10.0);
        
        Detail detail = new Detail();
        detail.setKcal(10);
        detail.setProduct(product);
        
        try {
            em.getTransaction().begin();
            em.persist(product);
            em.persist(detail);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
```
When persisting the entities, we should persist both entities, if you for example persisted only the one who has the FK, JPA will throw an Exception saying that it doesn’t know about the other entity, so we need to persist both to add to the context that the entity manager manages .

###### Hibernate SQL Log
```
Hibernate: insert into Product (name, price) values (?, ?)
Hibernate: insert into Detail (kcal, product_id) values (?, ?)
```
###### Querying the database to see the results
```sql
mysql> select * from product;
+----+------+-------+
| id | name | price |
+----+------+-------+
|  1 | ABC  |    10 |
+----+------+-------+
1 row in set (0.01 sec)
```
```sql
mysql> select * from detail;
+----+------+------------+
| id | kcal | product_id |
+----+------+------------+
|  1 |   10 |          1 |
+----+------+------------+
1 row in set (0.00 sec)
```

If you need to persist only one entity you can add cascade to the @OneToOne annotation like cascade = CascadeType.PERSIST
This way you can only persist the detail entity and it will work fine since it will persist the product to the context without you explicitly persist it.

```java
@Entity
public class Detail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private int kcal;

    @OneToOne(cascade = CascadeType.PERSIST)
    private Product product;
	// getters and setters
}
```
###### Persist the entities
```java
public class Main {
    public static void main(String[] args) {
        //Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        //Entity Manager
        EntityManager em = emf.createEntityManager();

        Product product = new Product();
        product.setName("QWE");
        product.setPrice(20.0);

        Detail detail = new Detail();
        detail.setKcal(20);
        detail.setProduct(product);

        try {
            em.getTransaction().begin();
//            em.persist(product);
            em.persist(detail);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
```
As you can see, we persisted only the detail entity
###### Hibernate SQL Log
```
Hibernate: insert into Product (name, price) values (?, ?)
Hibernate: insert into Detail (kcal, product_id) values (?, ?)
```
###### Querying the database to see the results
```sql
mysql> select * from product;
+----+------+-------+
| id | name | price |
+----+------+-------+
|  1 | ABC  |    10 |
|  2 | QWE  |    20 |
+----+------+-------+
2 rows in set (0.00 sec)
```
```sql
mysql> select * from detail;
+----+------+------------+
| id | kcal | product_id |
+----+------+------------+
|  1 |   10 |          1 |
|  2 |   20 |          2 |
+----+------+------------+
2 rows in set (0.00 sec)
```

##### Override the default FK column name
Now the JPA expects the FK column to be named like the other table name followed by _id in our example detail has FK for product so JPA expects to have the FK column named product_id
If you need to override the name of the FK column name you can use @JoinColumn(name=“”) over the FK column in the entity
###### change the detail table schema
```sql
ALTER TABLE `jpa`.`detail` 
DROP FOREIGN KEY `detail_product`;
ALTER TABLE `jpa`.`detail` 
CHANGE COLUMN `product_id` `product` INT NULL DEFAULT NULL ;
ALTER TABLE `jpa`.`detail` 
ADD CONSTRAINT `detail_product`
  FOREIGN KEY (`product`)
  REFERENCES `jpa`.`product` (`id`);

```
###### use @JoinColumn
```java
    @JoinColumn(name = "product")
    @OneToOne(cascade = CascadeType.PERSIST)
    private Product product;

```
now everything will work fine
```java
public class Main {
    public static void main(String[] args) {
        //Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        //Entity Manager
        EntityManager em = emf.createEntityManager();

        Product product = new Product();
        product.setName("XYZ");
        product.setPrice(30.0);

        Detail detail = new Detail();
        detail.setKcal(30);
        detail.setProduct(product);

        try {
            em.getTransaction().begin();
//            em.persist(product);
            em.persist(detail);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
```
```
Hibernate: insert into Product (name, price) values (?, ?)
Hibernate: insert into Detail (kcal, product) values (?, ?)
```
```sql
mysql> select * from product;
+----+------+-------+
| id | name | price |
+----+------+-------+
|  1 | ABC  |    10 |
|  2 | QWE  |    20 |
|  3 | XYZ  |    30 |
+----+------+-------+
3 rows in set (0.00 sec)

mysql> select * from detail;
+----+------+---------+
| id | kcal | product |
+----+------+---------+
|  1 |   10 |       1 |
|  2 |   20 |       2 |
|  3 |   30 |       3 |
+----+------+---------+
3 rows in set (0.00 sec)
```
Note we call the entity who has the FK column THE OWNER OF THE RELATIONSHIP

## One To One Using @OneToOne (Bidirectional Way)
Bidirectional way, that means both entities know about each other
In the other side of the owner relationship (the entity who does not own the FK) you have to use @OneToOne(mappedBy=“the attribute name in the owner of the relationship”) THIS IS MUST

When dealing with bidirectional one to one, you must add the relationship between the entities using the setters for example.
In @OneToOne you can set the optional to false to tell JPA/Hibernate to restrict and throw an exception if you forget to add the relationship
##### Database Tables Creation
We will not make any thing new in the database and will work with product and detail tables
###### Make the relationships bidirectional (two way)
Before only `detail` (the owner entity knew about the other entity `product`), now we will make `product` aware about `detail` also

add this to the `product` entity
```java
@OneToOne(mappedBy = "product")
private Detail detail;
```

so the product entity will look like the following:
```java
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private double price;

    @OneToOne(mappedBy = "product")
    private Detail detail;
	// getters and setters
}
```
now `product` know about the `detail` also.

###### Persist the entities
```java
public class Main {
    public static void main(String[] args) {
        //Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        //Entity Manager
        EntityManager em = emf.createEntityManager();

        Product product = new Product();
        product.setName("Water");
        product.setPrice(1.0);

        Detail detail = new Detail();
        detail.setKcal(0);
        detail.setProduct(product);
        product.setDetail(detail);

        try {
            em.getTransaction().begin();
//            em.persist(product);
            em.persist(detail);
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
Hibernate: insert into Product (name, price) values (?, ?)
Hibernate: insert into Detail (kcal, product) values (?, ?)
```
###### Querying the database to see the results
```sql
mysql> select * from product;
+----+-------+-------+
| id | name  | price |
+----+-------+-------+
|  1 | ABC   |    10 |
|  2 | QWE   |    20 |
|  3 | XYZ   |    30 |
|  4 | Water |     1 |
+----+-------+-------+
4 rows in set (0.00 sec)

mysql> select * from detail;
+----+------+---------+
| id | kcal | product |
+----+------+---------+
|  1 |   10 |       1 |
|  2 |   20 |       2 |
|  3 |   30 |       3 |
|  4 |    0 |       4 |
+----+------+---------+
4 rows in set (0.00 sec)
```

## EAGER AND LAZY fetch
In @OneToOne you can choose the fetch to be either EAGER (default) or LAZY this means if you call find method for one entity it will bring only what is inside the table for this entity and will bring the relationship If you call get method of that entity.

you can use Lazy fetch as following:
```java
    @OneToOne(mappedBy = "product", fetch = FetchType.LAZY)
    private Detail detail;
```

```java
public class Main {
    public static void main(String[] args) {
        //Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        //Entity Manager
        EntityManager em = emf.createEntityManager();

        Product product = em.find(Product.class, 4);
	}
}
```