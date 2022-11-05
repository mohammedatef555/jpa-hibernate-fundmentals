# JPA Fundamentals - Lesson 7 - @ManyToMany

###### following [JPA / Hibernate Fundamentals from Laur Spilca](https://www.youtube.com/playlist?list=PLEocw3gLFc8USLd90a_TicWGiMThDtpOJ "JPA / Hibernate Fundamentals Laur Spilca")

**Many To Many: Each Part Of The Relationship Can Refer To Multiple Records On The Other Side, In The Database You Will Have One Table For Each Entity And One Tablet Store The Relation Between The Records Of The Two Tables**

## Unidirectional @ManyToMany
##### Database Tables Creation
###### Create Professor table
```sql
CREATE TABLE `professor` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ;

```
###### Create Student table
```sql
CREATE TABLE `student` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ;

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
	@ManyToMany
    private List<Student> students;
    // getters and setters
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

    // getters and setters
}
```

##### Persist and Results
###### Persist the entities
```java
public class Main {
    public static void main(String[] args) {
        //Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        //Entity Manager
        EntityManager em = emf.createEntityManager();

        Professor professor = new Professor();
        professor.setName("Prof1");

        Student student = new Student();
        student.setName("Stud1");

        professor.setStudents(new ArrayList<>());
        professor.getStudents().add(student);

        try {
            em.getTransaction().begin();
            em.persist(professor);
            em.persist(student);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
}
```
###### Error
####### Hibernate SQL Log:
```sql
Hibernate: insert into Professor (name) values (?)
Hibernate: insert into Student (name) values (?)
Hibernate: insert into Professor_Student (Professor_id, students_id) values (?, ?)
Nov 05, 2022 1:19:59 PM org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
WARN: SQL Error: 1146, SQLState: 42S02
Nov 05, 2022 1:19:59 PM org.hibernate.engine.jdbc.spi.SqlExceptionHelper logExceptions
ERROR: Table 'jpa.professor_student' doesn't exist
```
as we see, because Now we have only 2 tables, JPA expects to have three tables, the third one for the relation ship and it should be name firstTableName_secondTableName
And it will have two columns:
- First Column name: first entity with _id suffix
- Second Column name: second entity plural name with _id suffix
###### Create professor_student table
```sql
CREATE TABLE `jpa`.`professor_student` (
  `professor_id` INT NOT NULL,
  `students_id` INT NOT NULL,
  PRIMARY KEY (`professor_id`, `students_id`));

```

now let is rerun the main again:
```java
public class Main {
    public static void main(String[] args) {
        //Entity Manager Factory
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");
        //Entity Manager
        EntityManager em = emf.createEntityManager();

        Professor professor = new Professor();
        professor.setName("Prof1");

        Student student = new Student();
        student.setName("Stud1");

        professor.setStudents(new ArrayList<>());
        professor.getStudents().add(student);

        try {
            em.getTransaction().begin();
            em.persist(professor);
            em.persist(student);
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
Hibernate: insert into Professor (name) values (?)
Hibernate: insert into Student (name) values (?)
Hibernate: insert into Professor_Student (Professor_id, students_id) values (?, ?)
```
###### Querying the database to see the results
```sql
mysql> select * from professor;
+----+-------+
| id | name  |
+----+-------+
|  2 | Prof1 |
+----+-------+
1 row in set (0.00 sec)

mysql> select * from student;
+----+-------+
| id | name  |
+----+-------+
|  2 | Stud1 |
+----+-------+
1 row in set (0.00 sec)

mysql> select * from professor_student;
+--------------+-------------+
| professor_id | students_id |
+--------------+-------------+
|            2 |           2 |
+--------------+-------------+
1 row in set (0.00 sec)
```
everything works fine.

#####Override the default names of the third table:
To override the names of the third table or the column of the third table we can use @JoinTable, we can not use @JoinColumn because it is not just a column it is an entire table that defines the relationship
Using @JoinTable we have
- name : the table name
- join columns : the first entity column
- inverse join column the other entity column
###### Alert the third table:
```sql
ALTER TABLE `jpa`.`professor_student` 
CHANGE COLUMN `professor_id` `professor` INT NOT NULL ,
CHANGE COLUMN `students_id` `student` INT NOT NULL ;
```
###### Change the default names:
```java
    @ManyToMany
    @JoinTable(
            name = "professor_student",
            joinColumns = @JoinColumn(name = "professor"),
            inverseJoinColumns = @JoinColumn(name = "student")
    )
    private List<Student> students;

```
everything will work fine with the new names:
```
Hibernate: insert into Professor (name) values (?)
Hibernate: insert into Student (name) values (?)
Hibernate: insert into professor_student (professor, student) values (?, ?)
```
```sql
mysql> select * from professor;
+----+-------+
| id | name  |
+----+-------+
|  2 | Prof1 |
|  3 | Prof1 |
+----+-------+
2 rows in set (0.00 sec)

mysql> select * from student;
+----+-------+
| id | name  |
+----+-------+
|  2 | Stud1 |
|  3 | Stud1 |
+----+-------+
2 rows in set (0.00 sec)

mysql> select * from professor_student;
+-----------+---------+
| professor | student |
+-----------+---------+
|         2 |       2 |
|         3 |       3 |
+-----------+---------+
2 rows in set (0.01 sec)
```


## Bidirectional  @ManyToMany
How we determine the owner of the relationship in ManyToMany relationship? the one who use @JoinTable we can consider it as the owner and now we can use mappedBy in @ManyToMany in the other entity


##### Database Tables Creation
we will use the same tables and entites of the previous example

##### Entity Creation
we will use the same tables and entites of the previous example,
in the previous example, only the professot knows about the students, so let is add this to the student entity
```java
@ManyToMany(mappedBy = "students")
private List<Professor> professors;
```
###### Student Entity:
```java
@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "students")
    private List<Professor> professors;
	
	// getters and setters
}
```
###### Professor Entity:
```java
@Entity
public class Professor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @ManyToMany
    @JoinTable(
            name = "professor_student",
            joinColumns = @JoinColumn(name = "professor"),
            inverseJoinColumns = @JoinColumn(name = "student")
    )
    private List<Student> students;
	// getters and setters
}
```
now they both now about each other
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
        professor.setName("Prof100");

        Student student = new Student();
        student.setName("Stud100");

        professor.setStudents(new ArrayList<>());
        professor.getStudents().add(student);

        try {
            em.getTransaction().begin();
            em.persist(professor);
            em.persist(student);
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
```java
Hibernate: insert into Professor (name) values (?)
Hibernate: insert into Student (name) values (?)
Hibernate: insert into professor_student (professor, student) values (?, ?)
```
###### Querying the database to see the results
```sql
mysql> select * from professor;
+----+---------+
| id | name    |
+----+---------+
|  2 | Prof1   |
|  3 | Prof1   |
|  4 | Prof100 |
+----+---------+
3 rows in set (0.00 sec)

mysql> select * from student;
+----+---------+
| id | name    |
+----+---------+
|  2 | Stud1   |
|  3 | Stud1   |
|  4 | Stud100 |
+----+---------+
3 rows in set (0.00 sec)

mysql> select * from professor_student;
+-----------+---------+
| professor | student |
+-----------+---------+
|         2 |       2 |
|         3 |       3 |
|         4 |       4 |
+-----------+---------+
3 rows in set (0.00 sec)
```
### Notes about Relationship
Notes:
- whenever you have a bidirectional relationship you will have mappedBy
- fetch
- if above object, the default is Eager
- if above collection, the default is Lazy

Remember you will never use mappedBy with @JoinColumn or @JoinTable in the same entity, they always on the opposite sides.
