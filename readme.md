# Wrote Vote

## TODO
- create new user
- create register and login flows
  - Create DB user to match authed user
- Create Candidates
- Create Ballet
- Close Ballet
- view historical votes (per user, totals)
- reporting
- Do full user/group/role/permission permissions
- add keycloak auth?
- error handling
- cors for redirect to login


## Running Locally

```
./setup.sh
```

Other commands
```
docker exec -it hydra sh
docker exec -it vote_db psql -U president -d postgres

```


## Hydra Stuff

```
docker-compose -f quickstart.yml up -d --build

docker-compose -f quickstart.yml exec hydra sh

client=$(hydra create client \
  --endpoint http://127.0.0.1:4445 \
  --grant-type authorization_code,refresh_token,client_credentials \
  --response-type code,id_token \
  --format json \
  --token-endpoint-auth-method client_secret_post \
  --scope openid --scope offline --scope profile \
  --redirect-uri http://127.0.0.1:5555/callback)
    
hydra perform client-credentials \
--endpoint http://127.0.0.1:4444/ \
--client-id $client_id \
--client-secret $client_secret \
--scope openid,offline,profile

  
hydra list oauth2-clients -e http://127.0.0.1:4445 

hydra get oauth2-client 98914052-58dd-4c7e-8148-cc96bf01611c -e http://127.0.0.1:4445 

client_id=$(echo $client | jq -r '.client_id')
client_secret=$(echo $client | jq -r '.client_secret')

hydra perform authorization-code \
    --client-id $client_id \
    --client-secret $client_secret \
    --endpoint http://127.0.0.1:4444/ \
    --port 5556 \
    --scope openid,offline,profile

hydra perform authorization-code \
    --client-id $client_id \
    --client-secret $client_secret \
    --endpoint http://127.0.0.1:4444/ \    
    --scope openid,offline,profile

```