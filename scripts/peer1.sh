cd ../src
javac code/*.java
javac channels/*.java
rmiregistry &
java code/Peer 1.0 1 peer1 225.0.0.0 8008 226.0.0.0 8004 227.0.0.0 8000

