


### START DOCKER ###


# Run command

docker run --name petconnect-db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=petconnect -e POSTGRES_DB=petconnect -p 5432:5432 -d postgres

# wait for server to create & confirm its working with 

mvn spring-boot:run


# Server running, run local:

After server is running open a browser and input 

http://localhost:8080 

# user and password

to login on the website you need to put user as: user
and the password will be generated in the server console, it looks
like this:

Using generated security password: 38915130-c6d6-46d3-93f7-1f93ad77dc1d



Using generated security password: 38915130-c6d6-46d3-93f7-1f93ad77dc1d