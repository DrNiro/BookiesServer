# Bookies (Server)
REST-API Layered architecture Backend application.

# Backend REST API Layered Architecture

This repository contains the backend codebase for a REST API that serves as the server-side component of a related client application. The architecture is designed to provide a scalable, modular, and maintainable backend system for supporting the client's functionality.

## Architecture Overview

The backend REST API follows a layered architecture approach, which helps to separate concerns and promote code organization. The architecture consists of the following layers:

1. **Presentation Layer**: This layer is responsible for handling client requests and generating appropriate responses. It includes the REST API endpoints that define the available resources and operations. The endpoints communicate with the underlying layers to process the requests and return responses.

2. **Business Logic Layer**: The business logic layer contains the application's core logic and rules. It coordinates the processing of requests, performs validations, and applies business rules and transformations. It interacts with the data access layer for retrieving or modifying data as necessary.

3. **Data Access Layer**: The data access layer is responsible for interacting with the underlying data storage, such as databases or external APIs. It provides methods for querying, updating, and persisting data. This layer abstracts the data storage specifics from the upper layers.

4. **Utility/Helper Layer**: The utility/helper layer consists of reusable components, functions, or modules that support the other layers. It may include authentication/authorization modules, data validation helpers, error handling utilities, logging mechanisms, and other common functionalities.

## Technologies Used

The backend REST API is developed using the following technologies:

- **Programming Language**: Java
- **Framework**: Spring Boot
- **Database**: MySQL/AWS-RDB
- **Testing Framework**: JUnit
