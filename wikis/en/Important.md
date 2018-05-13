## Important Note 

### `PageHelper.startPage` method important tips

Only the first Mybatis query (select) method immediately after the `PageHelper.startPage` method will be paged.

### Please do not configure more than one PageHelper

When using Spring, you can config PageHelper by `mybatis-config.xml` or `Spring<bean>`.
Select one of them, do not configure PageHelper in two ways at the same time.

### PageHelper does not support paging with `for update` statement

### PageHelper does not support Nested Results Mapping

Since the nested result mode causes the resultSet to be folded, 
the total number of results for the paged query will decrease after folding.
So It cannot guarantee the number of paged results correctly.
