@echo off
echo Construindo WBC Guild System...

REM Verificando se o Maven está instalado
mvn -version >nul 2>&1
if errorlevel 1 (
    echo Erro: Maven não encontrado, por favor instale o Maven primeiro
    pause
    exit /b 1
)

REM Limpando e compilando o projeto
echo Limpando o projeto...
mvn clean

echo Compilando o projeto...
mvn compile

echo Empacotando o projeto...
mvn package

if errorlevel 1 (
    echo Falha na construção!
    pause
    exit /b 1
)

echo Construção concluída com sucesso!
echo Localização do arquivo do plugin: target/guild-plugin-1.2.1.jar
pause
