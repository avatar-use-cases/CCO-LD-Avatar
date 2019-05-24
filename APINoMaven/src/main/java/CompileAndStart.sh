javac -d classes -cp classes QueryTemplateFactory.java
javac -d classes -cp classes QueryParser.java
javac -d classes -cp classes:dependencies/apache-jena-classes/jena-arq-3.8.0.jar:dependencies/apache-jena-classes/jena-core-3.8.0.jar CustomQuery.java
javac -cp classes:dependencies/apache-jena-classes/*:dependencies/jetty-all-uber.jar:dependencies/json-simple-1.1.1.jar -d classes queryHandlerForAPI.java
javac -cp classes:dependencies/apache-jena-classes/*:dependencies/jetty-all-uber.jar:dependencies/json-simple-1.1.1.jar -d classes QueryParseHandler.java
# javac -cp classes:dependencies/jetty-all-uber.jar -d classes AddQueryHandlerForAPI.java
javac -cp classes:dependencies/jetty-all-uber.jar:/dependencies/slf4j-1.8.0-beta2/slf4j-api-1.8.0-beta2.jar:/dependencies/shiro-root-1.3.2/core/src/main/java/:/dependencies/commons-beanutils-1.9.3/commons-beanutils-1.9.3.jar:dependencies/apache-jena-classes/* -d classes avatarServerV1.java
java -cp classes:dependencies/jetty-all-uber.jar:dependencies/json-simple-1.1.1.jar:dependencies/shiro-root-1.3.2/core/src/main/java/:dependencies/commons-beanutils-1.9.3/commons-beanutils-1.9.3.jar:dependencies/slf4j-1.8.0-beta2/slf4j-api-1.8.0-beta2.jar:dependencies/slf4j-1.8.0-beta2/slf4j-simple-1.8.0-beta2.jar:dependencies/apache-jena-classes/* avatarServerV1 localhost 8180
