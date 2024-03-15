# Performance Tests with k6
[k6](https://k6.io/) is a load testing tool. 👀 See [here](https://k6.io/docs/get-started/installation/) to install it.

## How to Run 🚀

To run k6 tests use the command:

``` shell
k6 run --env VARS=local.environment.json --env TEST_TYPE=./test-types/load.json --env API_SUBSCRIPTION_KEY=<your-secret> <script-name>.js
```

where 
-  _VARS_  is a environment file
-  _TEST_TYPE_  is a file in `/test-types` folder <load|stress|spike|...> 
-  _API_SUBSCRIPTION_KEY_  is your sub-key

`<script-name>.js` is the scenario to run with k6 (e.g. get_organization_receipts.js)

### PreCond
```sh
docker build -f DockerfilePre -t exec-node .

docker run --rm --name initToRunk6 \
-e COSMOS_CONN_STRING=${COSMOS_CONN_STRING} \
-e ENVIRONMENT_STRING="${ENVIRONMENT_STRING}" \
exec-node 
```
### TearDown
```sh
docker build -f DockerfilePost -t exec-node .

docker run --rm --name initToRunk6 \
-e COSMOS_RECEIPTS_CONN_STRING=${COSMOS_CONN_STRING} \
-e ENVIRONMENT_STRING="${ENVIRONMENT_STRING}" \
exec-node 
```