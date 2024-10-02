# Development Notes

Frontend of the application is developed separately.

The setup requires the following steps:
1) configure the application according to [Setup Guide](setup.md)
2) configure `config.properties` to contain `security.sameSite=None`    
   This is important if you are running the application over HTTP so 
   web browser would not block requests to the server due to CORS policy.
3) build the backend `mvn clean package`
4) Run the created application archive (`./target/record-manager.jar`)
5) Checkout and run frontend

Alternatively to step 2, a browser plugin can be used to disable CORS policy.

## Health check

To check that the backend is running, use path `/actuator/health` (e.g. `http://localhost:8080/record-manager/actuator/health`).
