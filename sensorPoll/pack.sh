mvn clean
rm src/main/webapp
ln -s `pwd`/webapp src/main
mvn package
