# Library of AWEsome Conductor example applications
Payment processing [SAGA](https://learn.microsoft.com/en-us/azure/architecture/reference-architectures/saga/saga) with Conductor and Go.

### Get Credentials

1. Go to https://developer.orkescloud.com/
2. On the menu to the left, Go to Access Control -> Applications https://developer.orkescloud.com/applicationManagement/applications
3. Create a New Application and enable necessary permissions.
4. Create a New access Key.
4. Copy the Key
5. Copy the Secret.

```shell
# Setup Conductor server connection
export CONDUCTOR_SERVER_URL=https://developer.orkescloud.com/api/
export CONDUCTOR_AUTH_KEY=<your api key from step 4 above >
export CONDUCTOR_AUTH_SECRET=<your api secret from step 5 above>
```
