# Wrote Vote

## TODO
- read info from hydra principal
- create new user
- move hydra docker compose into project compose
- make git ignored props file for auth secrets and hydra secrets
- test DB persistance across docker down
- test db setup idempotence
- creat register and login flows

## Hydra Client Setup

```
docker-compose -f quickstart.yml up -d --build

docker-compose -f quickstart.yml exec hydra sh

    hydra create client \
    --endpoint http://127.0.0.1:4445 \
    --grant-type authorization_code,refresh_token \
    --response-type code,id_token \
    --format json \
    --scope openid --scope offline \
    --redirect-uri http://localhost:8080/callback
    
    hydra perform client-credentials \
  --endpoint http://127.0.0.1:4444/ \
  --client-id "715f5a53-7aa7-44c3-b827-2dfa4c01e776" \
  --client-secret "6umRg4u_2JB80D.y.7qJ_pQLTW"
  
    hydra perform client-credentials \
  --endpoint http://127.0.0.1:4444/ \
  --client-id "5e50d357-46cc-4a1d-a282-e308503098df" \
  --client-secret "kTITYnDF.N~.L5i2a0OGDwzPOI"

hydra list oauth2-clients -e http://127.0.0.1:4445 


code_client_id=$(echo $code_client | jq -r '.client_id')
code_client_secret=$(echo $code_client | jq -r '.client_secret')
```