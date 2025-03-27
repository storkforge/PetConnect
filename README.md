


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









## Environment Setup

This project requires an OpenAI API key to run certain features.




    * **Important:** Never commit the `.env` file to version control. It is already included in `.gitignore`.

1.  **Create `.env` file:**

2.  **Add your API key:**
    * Open the `.env` file and add:
    
    
    OPENAI_API_KEY=sk-your-real-api-key

3.  **Run the application:**

## Running Test

Some tests require a valid OpenAI API key. Please follow the environment setup instructions above.

