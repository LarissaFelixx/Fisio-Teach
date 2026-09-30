# Sobe a API em modo dev (H2 em memoria + usuario admin de seed).
# Uso: .\dev.ps1 [porta]
param(
    [string]$Port = "8080"
)

Set-Location $PSScriptRoot
& .\mvnw.cmd spring-boot:run `
    "-Dspring-boot.run.profiles=dev" `
    "-Dspring-boot.run.arguments=--server.port=$Port"
