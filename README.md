To enable AI-powered features, you’ll need to generate and securely store your OpenAI API key.

### 1. Get Your OpenAI API Key

1. Visit the official OpenAI platform: [https://platform.openai.com/account/api-keys](https://platform.openai.com/account/api-keys)
2. Sign in or sign up for an account.
3. Click **Create new secret key**
4. Copy the generated API key (starts with `sk-...`)

### 2. Create `OpenAI.env` File

Inside your project root (`PetConnect/`), create a new file called: OpenAI.env

### 3. Add Your API Key as:

OPENAI_API_KEY=sk-YourGeneratedKeyHere
save file
### THIS FILE SHOULD ALREADY BE IN .GITIGNORE





## Run Program, Server running, run local:

After server is running open a browser and input 

http://localhost:8080 

### user and password

to login on the website you need to put 

## user as: user

## password as: password









### Environment Setup ###

This project requires an OpenAI API key to run certain features.


    * **Important:** Never commit the `.env` file to version control. It is already included in `.gitignore`.

1.  **Create `.env` file in the project root directory:**

2.  **Add your API key:**
    * Open the `.env` file and add:
    
    
    OPENAI_API_KEY=your_actual_api_key_here

3.  **Run the application:**

### Running Test ###

Some tests require a valid OpenAI API key. Please follow the environment setup instructions above.

