# TaskBoard – a hands-on Docker + Azure DevOps learning project

A small to-do app (ASP.NET Core 10 + PostgreSQL) built to practice the things a
DevOps intern touches daily: **images, containers, networks, volumes, compose,
registries, and CI/CD pipelines**.

```
 Browser ──► localhost:8080 ──► [ lb: nginx ] ──► [ web x3 ]  ──(compose network)──►  [ db container ]
                                 load balancer     ASP.NET Core    host name "db"        PostgreSQL 17
                                                                              │
                                                                     named volume "pgdata"
```

## Project layout

| File | What it teaches |
|---|---|
| `src/TaskBoard/` | The app: minimal API (`/api/tasks`, `/api/info`, `/health`) + one HTML page |
| `Dockerfile` | Multi-stage build, layer caching, build args, non-root user |
| `.dockerignore` | Keeping the build context small |
| `docker-compose.yml` | Multi-container apps, service-name DNS, env vars, volumes, healthchecks, replicas |
| `nginx/nginx.conf` | Load balancing, failover, reverse proxy headers |
| `.env.example` | Keeping secrets out of source control |
| `azure-pipelines.yml` | CI (build image) → push to Azure Container Registry → deploy to App Service |

## Prerequisites

- **Docker Desktop** for Windows (uses WSL 2). That's all you need to run it — the
  .NET SDK is inside the build image.
- Optional: the [.NET 10 SDK](https://dotnet.microsoft.com/download) if you want to run the app outside Docker.

## Quick start

```powershell
cd "C:\Users\Matthew Martinez\Docker Project"
copy .env.example .env          # already done for you
docker compose up --build
```

Open **http://localhost:8080**. Add a few tasks. Stop with `Ctrl+C`.

---

## Learning path (do these in order)

### 1. Single container, by hand
```powershell
docker build -t taskboard:v1 .           # build an image from the Dockerfile
docker images                             # see it (note the size!)
docker history taskboard:v1               # see the layers
docker run --rm -p 8080:8080 taskboard:v1 # run it
```
The page loads, but tasks fail — there's no database. `/health` returns **503**.
That's the problem compose solves.

### 2. Multi-container with compose
```powershell
docker compose up --build -d   # -d = detached (background)
docker compose ps              # what's running + health status
docker compose logs -f web     # follow the app's logs
docker compose exec db psql -U taskboard -d taskboard -c "SELECT * FROM tasks;"
docker compose exec web bash   # get a shell INSIDE the app container
```
Inside the web container try `whoami` (→ `app`, not root) and `env | grep Conn`.

### 3. Volumes = persistence
```powershell
docker compose down        # containers gone...
docker compose up -d       # ...tasks still there (volume kept)
docker compose down -v     # -v deletes the volume -> tasks gone for good
docker volume ls
```

### 4. Networking
```powershell
docker network ls
docker network inspect dockerproject_default
```
The app reaches Postgres at `Host=db` — Docker's built-in DNS resolves service names.
Note the DB has **no published port**: it's unreachable from your PC, only from the web container.

### 5. Layer caching
Edit some text in `wwwroot/index.html`, then `docker compose up --build -d` and
watch the output: the `dotnet restore` step says **CACHED**. Now change the
`.csproj` — restore runs again. That's why the Dockerfile copies the `.csproj` first.

### 6. Load balancing & scaling
`docker compose up` now starts **3 web replicas** behind an **nginx load balancer** (`lb`).
Only nginx is published to your PC; the replicas are private, like the database.

```
browser -> localhost:8080 -> [ lb: nginx ] --round robin--> web-1 / web-2 / web-3 -> db
```

Watch requests rotate between replicas (the hostname changes each time):
```powershell
1..6 | ForEach-Object { (Invoke-RestMethod http://localhost:8080/api/info).hostname }
curl.exe -i http://localhost:8080/api/info     # see the X-Served-By header
```

**Failover:** stop one replica and keep refreshing — users never see an error.
```powershell
docker compose ps                        # find the replica names
docker stop dockerproject-web-2
1..6 | ForEach-Object { (Invoke-RestMethod http://localhost:8080/api/info).hostname }
docker start dockerproject-web-2
```

**Scale up/down:**
```powershell
docker compose up -d --scale web=5
docker compose restart lb                # nginx resolves "web" only at startup
```

**Experiment:** in `nginx/nginx.conf`, uncomment `ip_hash;`, run `docker compose restart lb`,
and repeat the loop above. Every request now hits the same replica (sticky sessions).

### 7. Break things on purpose (great for learning troubleshooting)
- `docker compose stop db` → refresh the page, hit `/health`, read `docker compose logs web`.
- Put a wrong password in `.env` and restart — find the error in the logs.
- Remove `USER $APP_UID` from the Dockerfile, rebuild, run `whoami` inside. Why is root bad?

---

## Azure CI/CD (`azure-pipelines.yml`)

**Flow:** push to `main` → Azure DevOps builds the code → builds the Docker image
tagged with the build number → pushes to **Azure Container Registry** → deploys that
exact image to **Azure App Service**. Pull requests only run the build (no push/deploy).

One-time Azure setup (Azure CLI):
```bash
az group create -n rg-taskboard -l centralus
az acr create -g rg-taskboard -n <yourregistry> --sku Basic
az appservice plan create -g rg-taskboard -n plan-taskboard --is-linux --sku B1
az webapp create -g rg-taskboard -p plan-taskboard -n <taskboard-web> \
  --deployment-container-image-name <yourregistry>.azurecr.io/taskboard:latest
az webapp config appsettings set -g rg-taskboard -n <taskboard-web> --settings \
  WEBSITES_PORT=8080 \
  "ConnectionStrings__Default=Host=<your-azure-postgres>.postgres.database.azure.com;Database=taskboard;Username=...;Password=...;SSL Mode=Require"
```
For the database in Azure, use **Azure Database for PostgreSQL – Flexible Server**.

In Azure DevOps: create two **service connections** (Docker Registry → your ACR, and
Azure Resource Manager → your subscription), fill in the `TODO` variables at the top of
`azure-pipelines.yml`, and create a pipeline from that file.

> Cost tip: ACR Basic + App Service B1 + Postgres cost money. Delete everything with
> `az group delete -n rg-taskboard` when you're done practicing.

## Interview-ready concepts this project covers
- Image vs. container; layers and caching
- Multi-stage builds and why they shrink images
- Why containers should run as non-root
- `EXPOSE` vs. `-p` (documenting vs. publishing a port)
- Bind mounts vs. named volumes
- Compose networks and service discovery
- Healthchecks and `depends_on: condition: service_healthy`
- Config via environment variables (12-factor app); secrets out of git
- Image tagging strategy (`latest` vs. immutable build-number tags)
- CI vs. CD, container registries, deployment environments with approvals
