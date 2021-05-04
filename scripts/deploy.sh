#! usr/bin/env bash
mvn clean package
echo 'Copy files...'
scp -i "C:\\Users\\egkozhin\\.ssh\\id_rsa" \
      "C:\\Users\\egkozhin\\IdeaProjects\\application\\target\\tender-0.0.1-SNAPSHOT.jarmvn" \
      rtf2267gma@77.222.40.7
echo 'Restart server...'
ssh -i "C:\Users\egkozhin\.ssh\id_rsa"  rtf2267gma@77.222.40.7 <<EOF
pgrep java | xargs kill -9
nohup java -jar sweater -1.0-SNAPSHOT.jar > log.txt &
EOF
echo 'Bye..'