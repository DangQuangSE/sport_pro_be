# Run the backend locally with Docker

The Compose stack contains:

- `api`: Spring Boot API on `http://localhost:8080`
- `db`: PostgreSQL on `localhost:5434`, persisted in the `postgres_data` volume
- `import-neon`: an opt-in tool that replaces the local database with a snapshot from Neon

Docker Desktop displays these under one Compose group named `sport-pro`, with the main containers named `sport_pro_be_api` and `sport_pro_be_db`.

## Start API and local database

Create the application environment file once, then replace the placeholder secrets needed by the app:

```powershell
Copy-Item .env.example .env
docker compose up --build
```

Database connection variables from `.env` are intentionally overridden inside Compose so the API always connects to the `db` container. Customize local ports or credentials by adding these optional variables to `.env`:

```dotenv
API_PORT=8080
LOCAL_DB_PORT=5434
LOCAL_DB_NAME=sport_pro
LOCAL_DB_USERNAME=sport_pro
LOCAL_DB_PASSWORD=sport_pro_local
```

For a blank database, local Compose defaults to `ddl-auto=update` and disables Flyway because this repository currently has migrations V2–V4 but no V1 migration that creates the base schema. To explicitly override these local-only settings, use `LOCAL_SPRING_JPA_DDL_AUTO` and `LOCAL_SPRING_FLYWAY_ENABLED`.

## Copy deployed Neon data into local PostgreSQL

Use the direct (non-pooled) Neon connection string when possible. In PowerShell, keep it only in the current shell instead of saving it to the repository:

```powershell
$env:NEON_DATABASE_URL='postgresql://USER:PASSWORD@HOST/DBNAME?sslmode=require'
docker compose stop api
docker compose --profile tools run --rm import-neon
Remove-Item Env:NEON_DATABASE_URL
docker compose up --build api
```

The import command is destructive only to the local `db` container database. It does not write to Neon. The API is stopped first so it cannot reconnect while the local database is being recreated.

The temporary import container receives the Neon URL through its environment. `--rm` removes it after a normal run. If the command is interrupted, remove the stopped tool container and clear the shell variable:

```powershell
docker compose rm --force import-neon
Remove-Item Env:NEON_DATABASE_URL -ErrorAction SilentlyContinue
```

To discard all local database data and start clean:

```powershell
docker compose down --volumes
```

Do not commit `.env`, a Neon connection string, or database dumps. Production data may contain personal information; sanitize it before sharing or using it outside an approved development machine.
