@echo off
echo Creating local android/libs directory...
if not exist "android\libs" mkdir "android\libs"

echo Copying all libraries and native dependencies from BMHBILLING...
xcopy /E /I /Y "D:\Ajmal\BMHBILLING\app\libs" "android\libs"

echo Done! All library files have been copied locally.
pause
