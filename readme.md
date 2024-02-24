# Wrote Vote

## TODO
- read info from hydra principal
- create new user
- move hydra docker compose into project compose
- make git ignored props file for auth secrets and hydra secrets
- test DB persistance across docker down
- test db setup idempotence
- creat register and login flows
- dynamically generate hydra salt/secret

## Running Locally

Create `./.env`, and fill it with values
```
export POSTGRES_USER=voter
export POSTGRES_PASSWORD=voting
export HYDRA_SECRET=flagsinthewind
export HYDRA_SALT=rainovertheocean
```

```
docker exec -it hydra sh


```


## Hydra Client Setup

```
docker-compose -f quickstart.yml up -d --build

docker-compose -f quickstart.yml exec hydra sh

    hydra create client \
    --endpoint http://127.0.0.1:4445 \
    --grant-type authorization_code,refresh_token \
    --response-type code,id_token \
    --format json \
    --token-endpoint-auth-method client_secret_post \
    --scope openid --scope offline --scope profile \
    --redirect-uri http://localhost:8080/callback
    
    hydra perform client-credentials \
  --endpoint http://127.0.0.1:4444/ \
  --client-id "1b607c87-f02e-4238-81b9-bb3b779b9824" \
  --client-secret "XQVxO6S7PaoayYtK7cG0~3T4Nj"
  

hydra list oauth2-clients -e http://127.0.0.1:4445 


code_client_id=$(echo $code_client | jq -r '.client_id')
code_client_secret=$(echo $code_client | jq -r '.client_secret')
```