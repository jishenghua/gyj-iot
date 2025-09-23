@echo off
echo.
echo [启动后端服务]
echo.

cd %~dp0
cd ../gyjiot-admin/target

set JAVA_OPTS=-Xms256m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m

java -jar %JAVA_OPTS% gyjiot-admin.jar

cd bin
pause