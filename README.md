


### START DOCKER ###


# Run command

docker run --name petconnect-db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=petconnect -e POSTGRES_DB=petconnect -p 5432:5432 -d postgres

# wait for server to create & confirm its working with 

mvn spring-boot:run