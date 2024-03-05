#Generate Salts
cp ./compose/hydra-base.yml ./compose/hydra.yml
system_pass=$(openssl rand -hex 20)
salt_pass=$(openssl rand -hex 20)
sed -i -e "s/- youReallyNeedToChangeThis/- $system_pass/g" ./compose/hydra.yml
sed -i -e "s/salt: youReallyNeedToChangeThis/salt: $salt_pass/g" ./compose/hydra.yml

#Generate Client Secrets
cp ./compose/realm-base.json ./compose/realm.json
keycloak_client_secret=$(openssl rand -hex 20)
sed -i -e "s/\*\**/$keycloak_client_secret/g" ./compose/realm.json

#.env secrets
if [ ! -f .env ]; then
  postgres_pass=$(openssl rand -hex 20)
  echo "POSTGRES_USER=president" > .env
  echo "POSTGRES_PASSWORD=$postgres_pass" >> .env

  keycloak_postgres_pass=$(openssl rand -hex 20)
  keycloak_admin_pass=$(openssl rand -hex 20)
  echo "KEYCLOAK_ADMIN=admin" >> .env
  echo "KEYCLOAK_ADMIN_PASSWORD=$keycloak_admin_pass" >> .env
  echo "KEYCLOAK_POSTGRESQL_USER=keycloak" >> .env
  echo "KEYCLOAK_POSTGRESQL_DB=keycloak" >> .env
  echo "KEYCLOAK_POSTGRESQL_PASS=$keycloak_postgres_pass" >> .env
fi

docker-compose up -d

echo Waiting for Hydra
until [[ "`docker exec hydra hydra list oauth2-clients -e http://127.0.0.1:4445 2>&1`" = \CLIENT* ]]; do
    sleep 0.5;
done;

echo Post Docker Setup

code_client=$(docker exec hydra hydra create client \
--endpoint http://127.0.0.1:4445 \
--grant-type client_credentials,authorization_code,refresh_token \
--response-type code,id_token \
--format json \
--token-endpoint-auth-method client_secret_post \
--scope openid --scope offline --scope profile \
--redirect-uri http://localhost:8080/callback-oauth)

code_client_id=$(echo $code_client | jq -r '.client_id')
code_client_secret=$(echo $code_client | jq -r '.client_secret')

#Auth Secrets
echo "auth_client_id=$code_client_id" > ./auth-secrets.txt
echo "auth_client_secret=$code_client_secret" >> ./auth-secrets.txt
echo "keycloak_auth_client_secret=$keycloak_client_secret" >> ./auth-secrets.txt

echo Setup Complete