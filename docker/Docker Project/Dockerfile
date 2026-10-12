# =============================================================================
# MULTI-STAGE BUILD
# Stage 1 uses the big .NET SDK image (compilers, ~800 MB) to build the app.
# Stage 2 copies ONLY the compiled output into the small runtime image.
# Result: a much smaller, safer final image with no source code or SDK.
# =============================================================================

# ---------- Stage 1: build ----------
FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src

# Copy ONLY the project file first and restore NuGet packages.
# Docker caches each layer: if the .csproj hasn't changed, this slow step
# is skipped on the next build even if you edited Program.cs.
COPY src/TaskBoard/TaskBoard.csproj TaskBoard/
RUN dotnet restore TaskBoard/TaskBoard.csproj

# Now copy the rest of the source and publish a Release build.
COPY src/TaskBoard/ TaskBoard/
RUN dotnet publish TaskBoard/TaskBoard.csproj -c Release -o /app/publish --no-restore

# ---------- Stage 2: runtime ----------
FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS final
WORKDIR /app

# Build-time value (passed with --build-arg) turned into a runtime env var.
# The Azure pipeline passes the build number here so you can see which
# build is running at /api/info.
ARG APP_VERSION=local
ENV APP_VERSION=$APP_VERSION

COPY --from=build /app/publish .

# .NET 8+ images listen on port 8080 and ship a non-root "app" user.
# Running as non-root is a security best practice (and an interview question!).
USER $APP_UID
EXPOSE 8080

ENTRYPOINT ["dotnet", "TaskBoard.dll"]
