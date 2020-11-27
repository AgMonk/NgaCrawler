chcp 65001

SET war=target\ngacrawler-0.0.1-SNAPSHOT.war

if exist %war% (java  -Dfile.encoding=UTF-8 -jar %war%)else echo %war% 不存在

pause