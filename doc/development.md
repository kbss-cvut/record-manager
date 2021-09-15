Development Notes

Frontend of the application can be developed separately from the backend. 
The setup requires following steps:
1) configure the application according to [Setup Guide](setup.md)
2) configure `config.properties` to contain `security.sameSite=None`    
   This is important if you are running the application over http so 
   web browser would not block requests to the server due to CORS policy. 
3) build the backend `mvn clean package`
4) deploy created web application archive (`./target/record-manager-*.war`) to a web server 
5) run frontend `cd ./src/main/webapp; npm run dev`
6) frontend is by default accessible from `http://localhost:3000`