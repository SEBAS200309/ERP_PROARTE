# Determinar rutas relativas al script
$scriptDir = $PSScriptRoot
if (-not $scriptDir) { $scriptDir = (Get-Location).Path }
$rootDir = (Resolve-Path "$scriptDir\..").Path
$backendDir = Join-Path $rootDir "backend"
$frontendDir = Join-Path $rootDir "frontend"
$docsDir = Join-Path $rootDir "docs"

# Inicio de docker compose
$contenedor = "proarte-postgres"

# Verifica si el contenedor existe y esta corriendo
$containerStatus = docker ps -a --filter "name=$contenedor" --format "{{.Status}}"

if ($containerStatus -like "*Up*") {
    Write-Host "El contenedor '$contenedor' ya se encuentra corriendo." -ForegroundColor Green
}
else {
    Write-Host "Iniciando el contenedor '$contenedor'..." -ForegroundColor Cyan
    docker compose -f "$rootDir\docker-compose.yml" up -d
}

# Ejecución y compilación del backend
Set-Location $backendDir
Write-Host "Compilando backend..." -ForegroundColor Cyan
mvn clean compile -DskipTests -f pom.xml

# Si compila ejecutar el comando de arranque del backend
if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilación del backend exitosa" -ForegroundColor Green
    $PASSWORD = Read-Host -Prompt "Ingrese la contraseña de la base de datos" -AsSecureString
    $PlainPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto([System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($PASSWORD))
    $env:DB_PASSWORD = $PlainPassword

    $frontendIniciado = $false

    Write-Host "`nIniciando Spring Boot..." -ForegroundColor Cyan

    # Ejecuta Spring Boot transmitiendo la salida en tiempo real y detectando cuando arranca
    & mvn spring-boot:run "-Dspring-boot.run.profiles=dev" "-DskipTests" 2>&1 | ForEach-Object {
        $line = $_.ToString()
        Write-Host $line

        # Detectar cuando Spring Boot ha iniciado correctamente
        if (-not $frontendIniciado -and ($line -match "Started\s+\w+Application" -or $line -match "Started\s+.*in\s+[\d\.]+\s+seconds")) {
            $frontendIniciado = $true
            Write-Host "`n=======================================================" -ForegroundColor Green
            Write-Host " Backend iniciado correctamente!" -ForegroundColor Green

            # Verificar si el Frontend ya está ejecutándose en el puerto 4200
            $frontendCorriendo = Get-NetTCPConnection -LocalPort 4200 -State Listen -ErrorAction SilentlyContinue
            if ($frontendCorriendo) {
                Write-Host " El Frontend ya se encuentra en ejecución (puerto 4200). No se abrirá otra terminal." -ForegroundColor Yellow
            }
            else {
                Write-Host " Abriendo nueva terminal para el Frontend..." -ForegroundColor Green
                # Iniciar el servidor frontend de Angular
                Start-Process powershell.exe -ArgumentList "-NoExit", "-Command", "Set-Location '$frontendDir'; Write-Host 'Iniciando servidor de desarrollo Frontend...' -ForegroundColor Cyan; npx ng serve --open"
            }
            Write-Host "=======================================================`n" -ForegroundColor Green
        }
    }
}
else {
    Write-Host "Error al compilar el backend" -ForegroundColor Red
    Write-Host "Presiona ENTER para cerrar terminal"
    Read-Host
    Set-Location $docsDir
    exit $LASTEXITCODE
}