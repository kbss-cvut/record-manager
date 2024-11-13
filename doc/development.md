# Development Notes

Frontend of the application is developed separately.

The setup of this backend requires the following steps:
1) configure the application according to [Setup Guide](setup.md)
2) configure `application.properties` to contain `security.sameSite=None`    
   This is important if you are running the application over HTTP so 
   web browser would not block requests to the server due to CORS policy.
3) build the backend `mvn clean package`
4) Run the created application archive (`./target/record-manager.jar`)
5) Checkout and run frontend

Alternatively, to step 2, a browser plugin can be used to disable CORS policy.

## Running with Dockerized Services

It is possible to run all related services, including the frontend, as described in the [frontend development guide](https://github.com/kbss-cvut/record-manager-ui/blob/main/doc/development.md#additional-configuration-parameters). The guide also describes how to use local backend with the dockerized services using variable `INTERNAL_RECORD_MANAGER_SERVER_SERVICE`.

## Health check

To check that the backend is running, use path `/actuator/health` (e.g. `http://localhost:8080/record-manager/actuator/health`).
