@echo off

@for /R %LIB_DIR% %%i in (*.jar) do call %BATCH_DIR%\core\addclasspath %%i

