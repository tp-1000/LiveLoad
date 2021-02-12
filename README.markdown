#### A simple server that provides basic live-reloading 
 
1) From commandline run and pass directory to watch  
       `$java -jar LiveLoad-1.0.0.jar  aProject/dir/`
        
2) Request project html file from the server  
   From the browser request -- localhost:8080/someFile.html
    
3) Modify file in directory  
   Save, Create, Delete a file in your project
        
4) Browser forces reload  
   The change triggers websocket to close calling browser to reload


***
###### Note -  Will not move up a directory for requests 
###### Will not handle requests to outside the project