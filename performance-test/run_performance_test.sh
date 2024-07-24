
# sh run_performance_test.sh <local|dev|uat|prod> <load|stress|spike|soak|...> <api-subkey> <tokenizer-api-subkey> <biz-cosmos-account-pk> <receipt-cosmos-account-pk> <storage-account-pk> <script-name>

ENVIRONMENT=$1
TYPE=$2
API_SUBSCRIPTION_KEY=$3
TOKENIZER_API_SUBSCRIPTION_KEY=$4
BIZ_COSMOS_ACCOUNT_PK=$5
RECEIPT_COSMOS_ACCOUNT_PK=$6
STORAGE_ACCOUNT_PK=$7
SCRIPT=$8


if [ -z "$ENVIRONMENT" ]
then
  echo "No env specified: sh run_performance_test.sh <local|dev|uat|prod> <load|stress|spike|soak|...> <api-subkey> <tokenizer-api-subkey> <biz-cosmos-account-pk> <receipt-cosmos-account-pk> <storage-account-pk> <script-name>"
  exit 1
fi

if [ -z "$TYPE" ]
then
  echo "No test type specified: sh run_performance_test.sh <local|dev|uat|prod> <load|stress|spike|soak|...> <api-subkey> <tokenizer-api-subkey> <biz-cosmos-account-pk> <receipt-cosmos-account-pk> <storage-account-pk> <script-name>"
  exit 1
fi
if [ -z "$SCRIPT" ]
then
  echo "No script name specified: sh run_performance_test.sh <local|dev|uat|prod> <load|stress|spike|soak|...> <api-subkey> <tokenizer-api-subkey> <biz-cosmos-account-pk> <receipt-cosmos-account-pk> <storage-account-pk> <script-name>"
  exit 1
fi

export env=${ENVIRONMENT}
export type=${TYPE}
export api_sub_key=${API_SUBSCRIPTION_KEY}
export tokenizer_api_sub_key=${TOKENIZER_API_SUBSCRIPTION_KEY}
export biz_cosmos_account_pk=${BIZ_COSMOS_ACCOUNT_PK}
export receipt_cosmos_account_pk=${RECEIPT_COSMOS_ACCOUNT_PK}
export storage_account_pk=${STORAGE_ACCOUNT_PK}
export script=${SCRIPT}

stack_name=$(cd .. && basename "$PWD")
docker compose -p "${stack_name}" up -d --remove-orphans --force-recreate --build
docker logs -f k6
docker stop nginx
