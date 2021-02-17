### A simple server that provides basic live-reloading 

 
1) From commandline run jar and pass directory to watch  
       `$java -jar LiveLoad-1.0.0.jar  aProject/dir/`
        
2) Request project html file from the server  
   From the browser request -- localhost:8080/someFile.html
    
3) Modify file in directory  
   Save, Create, Delete a file in your project
        
4) Browser forces reload  
   The change triggers websocket to close calling browser to reload



***
Notes:  
- Will not move up a directory for requests  
- Will not handle requests to outside the project
- Will only handle GET requests
- Currently, configured to ignore events from .swp files 
- I would set jar file up to run with a bash alias/function