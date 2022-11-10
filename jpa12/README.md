# JPA Fundamentals - Lesson 12 - JPQL and using queries


###### following [JPA / Hibernate Fundamentals from Laur Spilca](https://www.youtube.com/playlist?list=PLEocw3gLFc8USLd90a_TicWGiMThDtpOJ "JPA / Hibernate Fundamentals Laur Spilca")

## Native queries
Native queries is specific to a special DBMS like MySQL or Oracle DB.

Try to avoid native queries

## JPQL Queries
Querying over objects

JPQL query is more abstract, so it will be translated to the Native queries by the
JPA implementation.

### `Query`

`Query` interface and `TypeQuery<Type>`

`Query q = em.createQuery(jpql, Product.class);`
`TypedQuery<Product> q = em.createQuery(jpql, Product.class);`

`query.getResultList();`

### `createQuery()`
`“SELECT p FROM Product p”` this Product is the @Entity class not the product table

### `createQuery()` with parameters
“SELECT p FROM Product p where p.price = 1” this Product is the @Entity class and the price column is the same as the one defined in the @Entity class not the DB although in this example the column have the same name in the DB and the @Entity Definition but the one refer here must match the attribute name in the @Entity class

To add a parameter you can give it a name and write it like this
`:name-of-the-parameter`, make sure there is no space between the colon and the parameter name
“SELECT p FROM Product p where p.price > :price”, now we have a parameter named price, we can pass an argument now to this JPQL query

`q.setParameter(“name-of-the-parameter”, value)`

In our example since we have parameter named price we can use the following:

`q.setParameter(“price”, 10)`

### Named queries

You define them in the @Entity class, you can have multiple
To create a named query you use
@NamedQuery, and it takes two parameter the name of the query to refer to it later and the query it self, when you refer to  it you can use entityManager.createNamedQuery()

The named queries are checked and validated even before using them, so it we have a problem in a named query the application will fail to start, the normal query will fail only when you use it in a createQuery() for example.


## Small Example
```sql
CREATE TABLE `product` (
  `id` int NOT NULL,
  `name` varchar(45) DEFAULT NULL,
  `price` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ;

```

```sql
INSERT INTO `jpa`.`product` (`id`, `name`, `price`) VALUES ('1', 'p1', '10');
INSERT INTO `jpa`.`product` (`id`, `name`, `price`) VALUES ('2', 'p2', '15');
INSERT INTO `jpa`.`product` (`id`, `name`, `price`) VALUES ('3', 'p3', '5');
INSERT INTO `jpa`.`product` (`id`, `name`, `price`) VALUES ('4', 'p4', '20');
INSERT INTO `jpa`.`product` (`id`, `name`, `price`) VALUES ('5', 'p5', '15');

```

```java

public class Main {
    public static void main(String[] args) {

        var emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        var em = emf.createEntityManager();

        em.getTransaction().begin();
        // this code is from https://github.com/lspil/youtubechannel/blob/master/jpac12/src/main/java/main/Main.java 
//        String jpql = "SELECT p FROM Product p";
//        TypedQuery<Product> q = em.createQuery(jpql, Product.class);

//        String jpql = "SELECT p FROM Product p WHERE p.price > :price";
//        TypedQuery<Product> q = em.createQuery(jpql, Product.class);
//        q.setParameter("price", 10.0);

//        String jpql = "SELECT SUM(p.price) FROM Product p WHERE p.price > :price";
//        TypedQuery<Double> q = em.createQuery(jpql, Double.class);
//        q.setParameter("price", 10.0);
//
//        double sum = q.getSingleResult();
//        System.out.println(sum);
//        list.forEach(System.out::println);

//        TypedQuery<Product> query =
//                em.createNamedQuery("Product.all", Product.class);
//        query.getResultStream()
//                .filter(p -> p.getPrice() > 10)
//                .forEach(System.out::println);

//          String jpql = "SELECT p FROM Product p WHERE p.price > ?1";
//          TypedQuery<Product> q = em.createQuery(jpql, Product.class);
//          q.setParameter(1, 10.0);

//        String sql = "SELECT * FROM product p";
//        Query q = em.createNativeQuery(sql, Product.class);
//        List<Product> products = q.getResultList();
//        products.forEach(System.out::println);
      
        String jpql = "SELECT p FROM Product p";

        TypedQuery<Product> q = em.createQuery(jpql, Product.class);
        List<Product> products = q.getResultList();

        products.forEach(System.out::println);


        TypedQuery<Product> namedQuery =
                em.createNamedQuery("Product.all", Product.class);
        List<Product> products1 = namedQuery.getResultList();

        products1.forEach(System.out::println);

        em.getTransaction().commit();
        em.close();
    }
}
```
###### Log
```
Hibernate: select product0_.id as id1_0_, product0_.name as name2_0_, product0_.price as price3_0_ from Product product0_
Product{id=1, name='p1', price=10.0}
Product{id=2, name='p2', price=15.0}
Product{id=3, name='p3', price=5.0}
Product{id=4, name='p4', price=20.0}
Product{id=5, name='p5', price=15.0}
Hibernate: select product0_.id as id1_0_, product0_.name as name2_0_, product0_.price as price3_0_ from Product product0_
Product{id=1, name='p1', price=10.0}
Product{id=2, name='p2', price=15.0}
Product{id=3, name='p3', price=5.0}
Product{id=4, name='p4', price=20.0}
Product{id=5, name='p5', price=15.0}
```

## Notes:
- JPQL vs native query
    - when we discuss native query, it's about columns
    - when we discuss JPQL query, it's about attributes.
- parameters
    - named parameters -> :name-of-the-parameter
    - use numbers -> ?1

If you use `getResultStream()` don’t use filter(), you may get out of memory, if there is a filter criteria use the WHERE clause in the query itself

There is also criteria query, but it's not recommended to be used.


this is a very brief overview and very small notes

The Full [JPA12 video](https://youtu.be/ZqD-lNutsys "JPA12 video")