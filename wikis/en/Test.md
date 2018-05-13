## Test

In order to guarantee the stability of PageHelper, the project contains a large number of unit tests, and can be tested for supported databases.

### Multi database test

To make it easier to test different databases, 
add a different database mybatis configuration file to the `src/test/resources` directory, 
and modify the configuration in `test.properties` 
so that the tests can be tested with different configurations.

In order to make it easier to test different database, 
there are many different mybatis profile in the `src/test/resources` directory.
By modifying the `test.properties` allows you to test different databases.
`test.properties`：  

```properties
# Need to configure the corresponding database first

# Write the database name which you want to test
# Optional values as following
#hsqldb
#mysql
#mariadb - Note In the config files, database port is 3309
#oracle
#postgresql
#sqlserver
#db2
#h2
#derby
database = hsqldb
```