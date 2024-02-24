docker-compose up -d
code_client=$(docker exec hydra hydra create client \
--endpoint http://127.0.0.1:4445 \
--grant-type authorization_code,refresh_token \
--response-type code,id_token \
--format json \
--token-endpoint-auth-method client_secret_post \
--scope openid --scope offline --scope profile \
--redirect-uri http://localhost:8080/callback)

code_client_id=$(echo $code_client | jq -r '.client_id')
code_client_secret=$(echo $code_client | jq -r '.client_secret')

echo "auth_client_id=$code_client_id" > ./auth-secrets.txt
echo "auth_client_secret=$code_client_secret" >> ./auth-secrets.txt
