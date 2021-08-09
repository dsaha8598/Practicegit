# Introduction

Masters Application This application is used to maintain Masters related service calls.

# Getting Started

Place application-local.yml file in src/main/resources file and override the required properties for development purpose.

Following property is required to be overriden in development systems in order to suppress kubernetes
related exception.

```
spring:
  cloud:
    kubernetes:
      reload:
        enabled: false
```

To run the application just right click on the application and either run or debug as Spring Boot Application

# Build and Test

TODO: Describe and show how to build your code and run the tests.

# Contribute

TODO: Explain how other users and developers can contribute to make your code better.
