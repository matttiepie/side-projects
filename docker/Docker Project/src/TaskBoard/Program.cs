using Npgsql;

var startedAt = DateTime.UtcNow;
var builder = WebApplication.CreateBuilder(args);

// ---------------------------------------------------------------------------
// Configuration comes from ENVIRONMENT VARIABLES when running in Docker.
// ASP.NET Core maps "ConnectionStrings__Default" (double underscore) to
// the config key "ConnectionStrings:Default". docker-compose.yml sets it.
// ---------------------------------------------------------------------------
var connString = builder.Configuration.GetConnectionString("Default")
    ?? "Host=localhost;Port=5432;Database=taskboard;Username=postgres;Password=postgres";

builder.Services.AddSingleton(NpgsqlDataSource.Create(connString));

var app = builder.Build();

app.UseDefaultFiles();   // serves wwwroot/index.html at "/"
app.UseStaticFiles();

// Create the table on startup. Retry because the database container may
// still be booting even after Docker has started it.
await EnsureDatabaseAsync(app.Services.GetRequiredService<NpgsqlDataSource>(), app.Logger);

// --- Info: shows WHICH container answered (try `docker compose up --scale web=3`)
app.MapGet("/api/info", () => Results.Ok(new
{
    hostname    = Environment.MachineName,                               // = container ID in Docker
    environment = app.Environment.EnvironmentName,                       // ASPNETCORE_ENVIRONMENT
    version     = Environment.GetEnvironmentVariable("APP_VERSION") ?? "dev", // baked in by CI
    dotnet      = Environment.Version.ToString(),
    startedAt
}));

// --- Health: used by Docker / Azure to decide if the app is alive
app.MapGet("/health", async (NpgsqlDataSource db) =>
{
    try
    {
        await using var cmd = db.CreateCommand("SELECT 1");
        await cmd.ExecuteScalarAsync();
        return Results.Ok(new { status = "healthy", database = "up" });
    }
    catch (Exception ex)
    {
        return Results.Json(new { status = "unhealthy", database = ex.Message }, statusCode: 503);
    }
});

// --- Tasks CRUD (stored in Postgres, which lives in a separate container)
app.MapGet("/api/tasks", async (NpgsqlDataSource db) =>
{
    var tasks = new List<TaskItem>();
    await using var cmd = db.CreateCommand("SELECT id, title, done, created_at FROM tasks ORDER BY id DESC");
    await using var reader = await cmd.ExecuteReaderAsync();
    while (await reader.ReadAsync())
        tasks.Add(new TaskItem(reader.GetInt32(0), reader.GetString(1), reader.GetBoolean(2), reader.GetDateTime(3)));
    return Results.Ok(tasks);
});

app.MapPost("/api/tasks", async (NewTask input, NpgsqlDataSource db) =>
{
    if (string.IsNullOrWhiteSpace(input.Title))
        return Results.BadRequest(new { error = "Title is required" });

    await using var cmd = db.CreateCommand(
        "INSERT INTO tasks (title) VALUES ($1) RETURNING id, title, done, created_at");
    cmd.Parameters.AddWithValue(input.Title.Trim());
    await using var reader = await cmd.ExecuteReaderAsync();
    await reader.ReadAsync();
    var task = new TaskItem(reader.GetInt32(0), reader.GetString(1), reader.GetBoolean(2), reader.GetDateTime(3));
    return Results.Created($"/api/tasks/{task.Id}", task);
});

app.MapPut("/api/tasks/{id:int}/toggle", async (int id, NpgsqlDataSource db) =>
{
    await using var cmd = db.CreateCommand("UPDATE tasks SET done = NOT done WHERE id = $1");
    cmd.Parameters.AddWithValue(id);
    return await cmd.ExecuteNonQueryAsync() == 0 ? Results.NotFound() : Results.NoContent();
});

app.MapDelete("/api/tasks/{id:int}", async (int id, NpgsqlDataSource db) =>
{
    await using var cmd = db.CreateCommand("DELETE FROM tasks WHERE id = $1");
    cmd.Parameters.AddWithValue(id);
    return await cmd.ExecuteNonQueryAsync() == 0 ? Results.NotFound() : Results.NoContent();
});

app.Run();


static async Task EnsureDatabaseAsync(NpgsqlDataSource db, ILogger logger)
{
    const string sql = """
        CREATE TABLE IF NOT EXISTS tasks (
            id         SERIAL PRIMARY KEY,
            title      TEXT NOT NULL,
            done       BOOLEAN NOT NULL DEFAULT FALSE,
            created_at TIMESTAMP NOT NULL DEFAULT NOW()
        );
        """;

    for (var attempt = 1; attempt <= 10; attempt++)
    {
        try
        {
            await using var cmd = db.CreateCommand(sql);
            await cmd.ExecuteNonQueryAsync();
            logger.LogInformation("Database ready.");
            return;
        }
        catch (Exception ex) when (attempt < 10)
        {
            logger.LogWarning("Database not ready (attempt {Attempt}/10): {Message}", attempt, ex.Message);
            await Task.Delay(TimeSpan.FromSeconds(3));
        }
    }
}

record TaskItem(int Id, string Title, bool Done, DateTime CreatedAt);
record NewTask(string Title);
