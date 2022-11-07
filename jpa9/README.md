# JPA Fundamentals - Lesson 9 - Using maps for relationships

###### following [JPA / Hibernate Fundamentals from Laur Spilca](https://www.youtube.com/playlist?list=PLEocw3gLFc8USLd90a_TicWGiMThDtpOJ "JPA / Hibernate Fundamentals Laur Spilca")

This is not very practical, you will not see it much in projects, and it adds a complexity in the project.

In Maps :
- keys are unique
- the element important to which the relationship refers in JPA is the value not the key

If you have a map and the value is:
- An Entity, you can use @OneToMany and @ManyToMany
- Not an entity you can use @ElementCollection

We have map key set of annotations, we will use @MapKeyColumn to override the default name that’s expected by the JPA
For the value you use the one specific for the value type like, @Enumerated or @Temporal, @Column, etc..

When using Map:
-   MapKey summary:

    - if it’s simple, field -> @MapKeyColumn
    - if its Enum -> @MapKeyEnumerated
    - if its temporal -> @MapKeyTemporal and so on..

-   Value summary:
    - if it’s simple, field -> @Column
    - if its Enum -> @Enumerated
    - if its temporal -> @Temporal and so on..


[JPA9 video](https://youtu.be/KbmWN0gQ5ag "JPA9 video")