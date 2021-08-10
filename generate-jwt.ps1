
mvn exec:java -Dexec.mainClass="com.rtds.util.JwtTokenGenerator" -Dexec.classpathScope="test" -Dexec.args="/JwtClaims.json 86400"
