### Running Migrations
By default, Flyway runs migrations automatically when Spring Boot starts.\
Can be changed in the application.properties ```spring.flyway.enabled=****```
****
### Migration Naming Convention
*Prefix versions with V (uppercase)*
- Use double underscore __ between version and description
- Version numbers should be sequential (1, 2, 3... or with timestamps)
- Descriptions should be short and descriptive

*Examples:*
- V1__Initial_schema.sql
- V2__Add_user_roles.sql
- V20230501__Update_product_table.sql

****
***Please remember to migrate when making changes to the database or entity models***