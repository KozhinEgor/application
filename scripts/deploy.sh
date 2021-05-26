
#! usr/bin/env bash
mvn clean package
echo 'Copy files...'
scp -i "C:\\Users\\egkozhin\\.ssh\\id_rsa" \
      "C:\\Users\\egkozhin\\IdeaProjects\\application\\target\\tender-0.0.1-SNAPSHOT.jar" \
      root@77.222.55.82:./tender.jar
echo 'Restart server...'
ssh -i "C:\Users\egkozhin\.ssh\id_rsa"  root@77.222.55.82 <<EOF
pgrep java | xargs kill -9
nohup java -jar tender.jar > log.txt &
EOF
echo 'Bye..'
#pgrep java | xargs kill -9
#nohup java -jar tender.jar > log.txt &