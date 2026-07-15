# Compress-My-Cloud-Drive-Media

The goal of this software is to manage the size of any cloud drive.

But initially, the goal was to compress my video files from Google Drive to reduce storage costs, 
and as things get working, images and PDF files also was included.

The application architecture is based on a mix of chain of responsibility [(1)](https://sourcemaking.com/design_patterns/chain_of_responsibility) [(2)](https://refactoring.guru/design-patterns/chain-of-responsibility) 
and Observer [(3)](https://sourcemaking.com/design_patterns/observer) [(4)](https://refactoring.guru/design-patterns/observer). It's a mix because initially this could be made just with 
chain of responsibility handlers, but with handlers also been observers enables paginated query 
of cloud client API. Otherwise, it would be necessary to run all files first, and then compress 
them, diminishing the user experience.

This architecture enables the implementation of any compressor interacting with any cloud drive, 
by just implementing interfaces and registering observer-handlers in the factory [(5)](https://refactoring.guru/design-patterns/factory-method) [(6)](https://sourcemaking.com/design_patterns/abstract_factory).

Using the [C4 Model](https://c4model.com/) diagram notation, you can see the System Context Diagram 
and the Component Diagram below. The Container Diagram is unnecessary due to the current simplicity of
the application.

## System Context Diagram

![compress-my-cloud-drive-media-System Context.drawio.png](diagram/compress-my-cloud-drive-media-System%20Context.drawio.png)

## Component Diagram

![compress-my-cloud-drive-media-Component.drawio.png](diagram/compress-my-cloud-drive-media-Component.drawio.png)

## Google Drive Setup

Before running the application with `--cloud-drive=google`, you need to create your own OAuth
client credentials in Google Cloud and make them available to the application. If this step is
skipped or misconfigured, the application fails at startup with `Fail to initialize Google Drive
Client`.

1. Go to the [Google Cloud Console](https://console.cloud.google.com/) and create a new project
   (or select an existing one).
2. Enable the **Google Drive API** for that project (APIs & Services > Library > Google Drive API
   > Enable).
3. Configure the **OAuth consent screen** (APIs & Services > OAuth consent screen). For personal
   use, "External" with your own Google account added as a test user is enough.
4. Create an **OAuth client ID** (APIs & Services > Credentials > Create Credentials > OAuth
   client ID), choosing **Desktop app** as the application type.
5. Download the generated JSON file and save it as `client_secret.json` in the following path:

   ```text
   ~/credentials/client_secret.json
   ```

   This path is fixed and not currently configurable.
6. Run any command with `--cloud-drive=google`. The application will open a browser window (it
   starts a local server on port `8888` for the OAuth redirect) so you can sign in and grant
   access. After you approve it, the resulting token is cached under a `tokens/` folder created
   next to where you run the jar, so future executions won't ask you to sign in again.
   * If you ever change the requested permissions/scopes, delete the `tokens/` folder to force a
     new authorization flow.

## How to Use

### List Files

After downloading a release you can just list all your media files in the console like this:

```bash
java -jar cloud-drive-compressor-1.0.0-runner.jar --list --cloud-drive=google
```

This command will launch the OAuth flow for your Google account.
Once authorized, the program will list all your files, not just media files, 
because since the goal is to reduce the storage of your drive, you need to 
understand what your cloud drive storage is composed of, then, after being sure
that the problem is your media files, you could compress it.

### Export to CSV

Another command that yoy can run is the following:

```bash
java -jar cloud-drive-compressor-1.0.0-runner.jar --csv --cloud-drive=google
```

This command will create a CSV file containing some information about all your files that you
can group by type or folder, in order to understand where is the biggest files.

### Compressing Media Files

Finally, to compress your media files you can compress just Videos, just Photos or just PDFs 
or all of them, like the following command:

```bash
java -jar cloud-drive-compressor-1.0.0-runner.jar --download --download-folder=~/Downloads --video --image --pdf --cloud-drive=google
```
