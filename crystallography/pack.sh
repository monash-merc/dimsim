mvn clean
rm src/main/webapp
ln -s $1/webapp src/main
mvn package

