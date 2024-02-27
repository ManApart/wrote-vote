#Generate Salts
cp ./compose/hydra-base.yml ./compose/hydra.yml
system_pass=$(openssl rand -hex 20)
salt_pass=$(openssl rand -hex 20)
sed -i -e "s/- youReallyNeedToChangeThis/- $system_pass/g" ./compose/hydra.yml
sed -i -e "s/salt: youReallyNeedToChangeThis/salt: $salt_pass/g" ./compose/hydra.yml

#.env secrets
if [ ! -f .env ]; then
  postgres_pass=$(openssl rand -hex 20)
  echo "POSTGRES_USER=president" > .env
  echo "POSTGRES_PASSWORD=$postgres_pass" >> .env
fi

docker-compose up -d

#Replace with health check
sleep 2

code_client=$(docker exec hydra hydra create client \
--endpoint http://127.0.0.1:4445 \
--grant-type client_credentials,authorization_code,refresh_token \
--response-type code,id_token \
--format json \
--token-endpoint-auth-method client_secret_post \
--scope openid --scope offline --scope profile \
--redirect-uri http://localhost:8080/callback)

code_client_id=$(echo $code_client | jq -r '.client_id')
code_client_secret=$(echo $code_client | jq -r '.client_secret')

#Auth Secrets
echo "auth_client_id=$code_client_id" > ./auth-secrets.txt
echo "auth_client_secret=$code_client_secret" >> ./auth-secrets.txt

echo Setup Complete