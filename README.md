# Project Centollo
Compare implementing data access layer with Hibernate, JOOQ and SansOrm.

## Metrics
- amount and complexity of code
- amount of hand written SQL
- performance with large data sets

## Requirements / Test cases:
### 1. Products
-  Search for products by name or code
-  get product by identifier

### 2. Customers
-  Search for customers by name
-  get customer by identifier

### 3. Purchase Orders
-  create a new purchase order with order lines and orderer
-  get purchase order
-  update purchase order
-  aggrete purchase order queries
-  list all purchase orders

### 4. Non-functional test cases
- assert optimistic locking behaviour

## Other concerns
- RESTful api
- operations should be transactional
- database could be postgreSql in a docker container



