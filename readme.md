# Wrote Vote

## TODO
- read info from hydra principal
- move hydra docker compose into project compose
- make git ignored props file for auth secrets and hydra secrets
- test DB persistance across docker down
- test db setup idempotence


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

code_client_id=$(echo $code_client | jq -r '.client_id')
code_client_secret=$(echo $code_client | jq -r '.client_secret')
```


hydra perform client-credentials \
--endpoint http://127.0.0.1:4444/ \
--client-id "03329e29-4685-4048-9e88-ffe629b8b955" \
--client-secret "pcns.hN9u5ISlT3lGe6s9jPsI_"